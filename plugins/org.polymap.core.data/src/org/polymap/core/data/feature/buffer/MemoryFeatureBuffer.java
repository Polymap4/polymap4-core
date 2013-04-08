/* 
 * polymap.org
 * Copyright 2012-2013, Polymap GmbH. All rights reserved.
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
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.geotools.data.Query;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.filter.Filter;
import org.opengis.filter.identity.FeatureId;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.data.FeatureChangeEvent.Type;
import org.polymap.core.data.feature.FidSet;

/**
 * Provides a simple in-memory feature buffer backed by a {@link ConcurrentHashMap}.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
class MemoryFeatureBuffer
        implements IFeatureBuffer {

    private static Log log = LogFactory.getLog( MemoryFeatureBuffer.class );
    
    public static final int                 INITIAL_CAPACITY = 1024;
    
    private ConcurrentMap<String,FeatureBufferState>  buffer = new ConcurrentHashMap( INITIAL_CAPACITY, 0.75f, 4 );
    
    private IFeatureBufferSite              site;
    
    
    MemoryFeatureBuffer() {
        super();
    }

    
    public void init( IFeatureBufferSite _site ) {
        this.site = _site;    
    }


    public void dispose()
    throws Exception {
        clear();
    }


    public void clear()
    throws Exception {
        buffer.clear();
        //site.fireFeatureChangeEvent( Type.FLUSHED, null );
    }


    public boolean isEmpty() { 
        return buffer.isEmpty();
    }

    public int size() { 
        return buffer.size();
    }


    public Collection<FeatureBufferState> content() {
        return new ArrayList( buffer.values() );
    }


    public FeatureBufferState contains( FeatureId identifier ) {
        return buffer.get( identifier.getID() );
    }


    public void registerFeatures( Collection<Feature> features ) {
        for (Feature original : features) {
            String fid = original.getIdentifier().getID();
            buffer.putIfAbsent( fid, new FeatureBufferState( original) );
        }
    }

    
    public void unregisterFeatures( Collection<Feature> features ) {
        for (Feature feature : features) {
            String fid = feature.getIdentifier().getID();
            buffer.remove( fid );
        }
    }


    public boolean supports( Filter filter ) {
        return true;
    }


    @Override
    public Set<FeatureId> markAdded( Collection<Feature> features )
    throws Exception {
        FidSet result = new FidSet( features.size() * 2 );

        for (Feature feature : features) {
            FeatureId identifier = feature.getIdentifier();
            FeatureBufferState buffered = buffer.get( identifier.getID() );
            if (buffered == null) {
                throw new IllegalStateException( "Feature is not registered with this buffer: " + identifier.getID() );
            }

            buffered.evolveState( FeatureBufferState.State.ADDED );
            result.add( identifier );
        }

        site.fireFeatureChangeEvent( Type.ADDED, features );
        return result;
    }


    @Override
    public Set<FeatureId> markModified( Filter filter, AttributeDescriptor[] type, Object[] value )
    throws Exception {
        List<Feature> features = new ArrayList( buffer.size() );
        FidSet fids = new FidSet( buffer.size() );

        for (FeatureBufferState buffered : buffer.values()) {

            if (filter.evaluate( buffered.feature() )) {
                buffered.evolveState( FeatureBufferState.State.MODIFIED );
                modifyFeature( buffered.feature(), type, value );

                features.add( buffered.feature() );
                fids.add( buffered.feature().getIdentifier() );
            }
        }

        site.fireFeatureChangeEvent( Type.MODIFIED, features );
        return fids;
    }


    @Override
    public Set<FeatureId> markRemoved( Filter filter )
    throws Exception {
        List<Feature> features = new ArrayList( buffer.size() );
        FidSet fids = new FidSet( buffer.size() );

        for (FeatureBufferState buffered : buffer.values()) {

            if (filter.evaluate( buffered.feature() )) {
                buffered.evolveState( FeatureBufferState.State.REMOVED );

                features.add( buffered.feature() );
                fids.add( buffered.feature().getIdentifier() );
            }
        }

        site.fireFeatureChangeEvent( Type.REMOVED, features );
        return fids;
    }


    @Override
    public List<Feature> blendFeatures( Query query, List<Feature> features )
    throws Exception {
        if (buffer.isEmpty()) {
            return features;
        }
        else {
            List<Feature> result = new ArrayList( features.size() );
            // just skip all modified features; added/modified features are
            // sent already, removed features are skipped altogether
            for (Feature feature : features) {
                String fid = feature.getIdentifier().getID();
                if (!buffer.containsKey( fid )) {
                    result.add( feature );
                }
            }
            return result;
        }
    }
    
    
    @Override
    public List<Feature> modifiedFeatures( Filter filter ) throws Exception {
        List<Feature> result = new ArrayList( buffer.size() );

        for (FeatureBufferState buffered : buffer.values()) {
            if (buffered.isAdded() || buffered.isModified()) {
                if (filter.evaluate( buffered.feature() )) {
                    result.add( buffered.feature() );
                }
            }
        }
        return result;
    }
    
    
    public int featureSizeDifference( Query query ) throws Exception {
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


    protected void modifyFeature( Feature feature, AttributeDescriptor[] type, Object[] value ) {
        for (int i=0; i<type.length; i++ ) {
            if (feature instanceof SimpleFeature) {
                ((SimpleFeature)feature).setAttribute( type[i].getName(), value[i] );
            }
            else {
                // XXX complex features
                throw new RuntimeException( "Complex features are not yet supported." );
            }
        }
    }


}
