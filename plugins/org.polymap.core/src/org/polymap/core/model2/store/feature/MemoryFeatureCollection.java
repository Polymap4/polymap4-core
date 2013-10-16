/* 
 * polymap.org
 * Copyright (C) 2013, Falko Bräutigam. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.core.model2.store.feature;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import java.io.IOException;

import org.geotools.feature.CollectionListener;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.collection.DelegateFeatureIterator;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.util.NullProgressListener;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureVisitor;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.sort.SortBy;
import org.opengis.geometry.BoundingBox;
import org.opengis.util.ProgressListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Supplier;

import org.polymap.core.runtime.LazyInit;
import org.polymap.core.runtime.LockedLazyInit;

/**
 * In-memory {@link FeatureCollection} that supports all kind of {@link FeatureType}
 * (not just {@link SimpleFeatureType}. This implementation uses a {@link HashMap} as
 * backing store.
 * <p/>
 * XXX This will be moved to org.polymap.core.data once the Model2 package gets its own
 * plugin.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class MemoryFeatureCollection<T extends FeatureType, F extends Feature>
        implements FeatureCollection<T,F> {

    private static Log log = LogFactory.getLog( MemoryFeatureCollection.class );

    private Map<String,F>                   data = new HashMap();
    
    private LazyInit<ReferencedEnvelope>    bounds = new LockedLazyInit();
    
    private T                               schema;
    
    private String                          id;
    
    
    public MemoryFeatureCollection( T schema, String id ) {
        this.schema = schema;
        this.id = id;
    }

    @Override
    public T getSchema() {
        return schema;
    }

    @Override
    public String getID() {
        return id;
    }

    @Override
    public ReferencedEnvelope getBounds() {
        return bounds.get( new Supplier<ReferencedEnvelope>() {
            public ReferencedEnvelope get() {
                ReferencedEnvelope result = new ReferencedEnvelope();
                for (Feature feature : data.values()) {
                    BoundingBox featureBounds = feature.getBounds();
                    if (!featureBounds.isEmpty() ) {
                        result.include( featureBounds );
                    }
                }
                return result;
            }
        });
    }

    @Override
    public int size() {
        return data.size();
    }

    @Override
    public FeatureIterator features() {
        return new DelegateFeatureIterator<F>( this, data.values().iterator() );
    }

    @Override
    public Iterator iterator() {
        return data.values().iterator();
    }

    @Override
    public void close( FeatureIterator close ) {
    }

    @Override
    public void close( Iterator close ) {
    }

    @Override
    public void accepts( FeatureVisitor visitor, ProgressListener progress ) throws IOException {
        progress = progress != null ? progress : new NullProgressListener();
        try {
            float size = size();
            float position = 0;            
            progress.started();
            Iterator<F> it = iterator();
            while (!progress.isCanceled() && it.hasNext()) {
                progress.progress( position++/size );
                try {
                    visitor.visit( it.next() );
                }
                catch( Exception e ){
                    progress.exceptionOccurred( e );
                }
            }            
        }
        finally {
            progress.complete();            
        }   
    }

    @Override
    public boolean add( F feature ) {
        if (feature == null) {
            return false;
        }
        String featureId = feature.getIdentifier().getID();
        if (featureId == null) {
            log.warn( "No featureId found on feature: " + feature );
            return false;
        }
        if (data.containsKey( featureId )) {
            return false;
        }

        if (this.schema == null) {
            this.schema = (T)feature.getType(); 
        }
        else {
            // XXX quick check if featureTypes are equal
            schema.getName().equals( feature.getType().getName() );
        }
        data.put( featureId, feature );
        return true;
    }

    @Override
    public boolean addAll( Collection collection ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public boolean addAll( FeatureCollection resource ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public void addListener( CollectionListener listener ) throws NullPointerException {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public void removeListener( CollectionListener listener ) throws NullPointerException {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public FeatureCollection subCollection( Filter filter ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public FeatureCollection sort( SortBy order ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public void purge() {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public void clear() {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public boolean contains( Object o ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public boolean containsAll( Collection o ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public boolean isEmpty() {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public boolean remove( Object o ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public boolean removeAll( Collection c ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public boolean retainAll( Collection c ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public Object[] toArray() {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public Object[] toArray( Object[] a ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }
    
}
