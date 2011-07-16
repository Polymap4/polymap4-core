/* 
 * polymap.org
 * Copyright 2011, Polymap GmbH. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.core.data.feature.buffer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.geotools.data.Query;
import org.opengis.feature.Feature;
import org.opengis.filter.Filter;
import org.opengis.filter.identity.FeatureId;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Provides a simple in-memory feature buffer backed by {@link HashMap} and
 * synchronized with a {@link ReentrantReadWriteLock}.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class MemoryFeatureBuffer
        implements IFeatureBuffer {

    private static Log log = LogFactory.getLog( MemoryFeatureBuffer.class );
    
    public static final int                 INITIAL_CAPACITY = 128;
    
    private Map<String,FeatureBufferState>  buffer = new HashMap( INITIAL_CAPACITY );
    
    private ReentrantReadWriteLock          lock = new ReentrantReadWriteLock();


    public void dispose()
    throws Exception {
        clear();
    }


    public void clear()
    throws Exception {
        try {
            lock.writeLock().lock();
            buffer.clear();
        }
        finally {
            lock.writeLock().unlock();
        }
    }


    public boolean supports( Filter filter ) {
        return true;
    }


    public boolean isEmpty()
    throws Exception {
        return buffer.isEmpty();
    }


    public List<FeatureId> addFeatures( Collection<Feature> features )
    throws Exception {
        try {
            lock.writeLock().lock();
            List<FeatureId> result = new ArrayList();
            
            for (Feature feature : features) {
                FeatureId identifier = feature.getIdentifier();
                assert !buffer.containsKey( identifier.getID() );
                
                buffer.put( identifier.getID(), new FeatureBufferState( feature ).markAdded() );
                result.add( identifier );
            }
            return result;
        }
        finally {
            lock.writeLock().unlock();
        }
    }


    public void modifyFeatures( Collection<Feature> features )
    throws Exception {
        try {
            lock.writeLock().lock();
            for (Feature feature : features) {
                String fid = feature.getIdentifier().getID();
                
                FeatureBufferState buffered = buffer.get( fid );
                if (buffered == null) {
                    buffer.put( fid, new FeatureBufferState( feature ).markModified() );
                }
                else if (buffered.isRemoved()) {
                    throw new IllegalStateException( "Feature is already removed: " + fid );
                }
                // added or modified
                else {
                    buffered.updateFeature( feature );
                }
            }
        }
        finally {
            lock.writeLock().unlock();
        }
    }


    public void removeFeatures( Collection<Feature> features )
    throws Exception {
        throw new RuntimeException( "not yet implemented." );
    }


    public List<Feature> modifiedFeatures( Query query, Iterable<Feature> features )
    throws Exception {
        try {
            lock.readLock().lock();
            List<Feature> result = new ArrayList( buffer.size() );

            // tweak upstream features
            for (Feature feature : features) {
                String fid = feature.getIdentifier().getID();
                FeatureBufferState buffered = buffer.get( fid );

                if (buffered == null) {
                    result.add( feature );
                }
                else if (buffered.isRemoved()) {
                    // skip removed feature
                }
                else if (buffered.isModified()) {
                    result.add( buffered.feature() );
                }
                else {
                    throw new IllegalStateException( "Wrong buffered feature state: " + buffered );
                }
            }
            return result;
        }
        finally {
            lock.readLock().unlock();
        }
    }
    
    
    public List<Feature> addedFeatures( Filter filter )
    throws Exception {
        try {
            lock.readLock().lock();
            List<Feature> result = new ArrayList( buffer.size() );

            for (FeatureBufferState buffered : buffer.values()) {
                if (buffered.isAdded() && filter.evaluate( buffered.feature() )) {
                    result.add( buffered.feature() );
                }
            }
            return result;
        }
        finally {
            lock.readLock().unlock();
        }
    }
    
    
    public FeatureBufferState contains( FeatureId identifier ) {
        try {
            lock.readLock().lock();
            return buffer.get( identifier.getID() );
        }
        finally {
            lock.readLock().unlock();
        }
    }


    public int featureSizeDifference( Query query )
    throws Exception {
        try {
            lock.readLock().lock();
            int result = 0;
            for (FeatureBufferState buffered : buffer.values()) {
                if (query.getFilter().evaluate( buffered.feature() )) {
                    if (buffered.isAdded()) {
                        result ++;
                    }
                    else if (buffered.isRemoved()) {
                        result --;
                    }
                }
            }
            return result;
        }
        finally {
            lock.readLock().unlock();
        }
    }


    public Collection<FeatureBufferState> content() {
        try {
            lock.readLock().lock();
            return new ArrayList( buffer.values() );
        }
        finally {
            lock.readLock().unlock();
        }
    }

}
