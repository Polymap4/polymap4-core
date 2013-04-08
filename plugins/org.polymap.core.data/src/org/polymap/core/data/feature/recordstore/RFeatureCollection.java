/* 
 * polymap.org
 * Copyright 2012, Falko Br�utigam. All rights reserved.
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
package org.polymap.core.data.feature.recordstore;

import java.util.Collection;
import java.util.Iterator;

import java.io.IOException;

import org.geotools.data.Query;
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
import org.opengis.util.ProgressListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Predicate;
import com.google.common.base.Supplier;
import com.google.common.collect.Iterators;

import org.polymap.core.data.feature.recordstore.QueryDialect.PostProcessResultSet;
import org.polymap.core.runtime.LazyInit;
import org.polymap.core.runtime.LockedLazyInit;
import org.polymap.core.runtime.recordstore.IRecordState;

/**
 * A {@link FeatureCollection} returned from {@link RFeatureStore} as a result
 * of a query.
 * <p/>
 * Does not support manipulation methods.
 *
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
class RFeatureCollection
        implements FeatureCollection, Iterable<RFeature> {

    private static Log log = LogFactory.getLog( RFeatureCollection.class );

    private String              id;

    private RFeatureStore       fs;
    
    private Query               query;
    
    private QueryDialect        queryDialect;
    
    private FeatureType         schema;

    /** Set of open resource iterators. */
//    private Set                 open = new ConcurrentSkipListSet();

    /** Lazily initialized cache of {@link #size()}. */
    private LazyInit<Integer>   size = new LockedLazyInit();

    
    public RFeatureCollection( RFeatureStore fs, FeatureType schema, Query query, 
            QueryDialect queryDialect ) {
        this.id = String.valueOf( hashCode() );
        this.fs = fs;
        this.query = query;
        this.queryDialect = queryDialect;
        this.schema = schema;
    }

    
    public String getID() {
        return id;
    }

    
    public FeatureType getSchema() {
        return fs.getSchema();
    }


    public ReferencedEnvelope getBounds() {
        try {
            return fs.getBounds( query );
        }
        catch (IOException e) {
            throw new RuntimeException( e );
        }
    }


    public int size() {
        return size.get( new Supplier<Integer>() {
            public Integer get() {
                try {
                    PostProcessResultSet results = queryDialect.getFeatureStates( fs, query );
                    return results.hasPostProcessing()
                            ? Iterators.size( results.iterator() )
                            : results.size();
                }
                catch (Exception e) {
                    throw new RuntimeException( e );
                }
            }
        });
    }

    
    public boolean isEmpty() {
        return size() == 0;
    }


    public void accepts( FeatureVisitor visitor, ProgressListener progress )
    throws IOException {
        // prevent call of size() if no progress is given
        float _size = progress != null ? size() : 0;
        progress = progress != null ? progress : new NullProgressListener();
            
        Iterator it = null;
        try {
            float position = 0;            
            progress.started();
            for (it = iterator(); !progress.isCanceled() && it.hasNext(); ) {
                if (_size > 0) {
                    progress.progress( position++/_size );
                }
                try {
                    visitor.visit( (Feature)it.next() );
                }
                catch (Exception e) {
                    log.warn( "Error while visiting features.", e );
                    progress.exceptionOccurred( e );
                }
            }            
        }
        finally {
            progress.complete();            
            close( it );
        }
    }

    
    public Iterator iterator() {
        try {
            final PostProcessResultSet results = queryDialect.getFeatureStates( fs, query );
            
            // build features
            Iterator<Feature> result = new Iterator<Feature>() {
                private Iterator<IRecordState>  delegate = results.iterator();
                @Override
                public boolean hasNext() {
                    return delegate.hasNext(); 
                }
                @Override                
                public Feature next() {
                    return schema instanceof SimpleFeatureType
                            ? new RSimpleFeature( delegate.next(), schema )
                            : new RFeature( delegate.next(), schema );
                }
                @Override
                public void remove() {
                    delegate.remove();
                }
            };
            // post-process
            if (results.hasPostProcessing()) {
                result = Iterators.filter( result, new Predicate<Feature>() {
                    public boolean apply( Feature input ) {
                        return results.postProcess( input );
                    }
                });
            }
            return result;
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }
    }

    
    public FeatureIterator features() {
        return new DelegateFeatureIterator( this, iterator() );
    }


    public void close( FeatureIterator close ) {
//        open.remove( close );
    }

    public void close( Iterator close ) {
//        if (close != null) {
//            open.remove( close );
//        }
    }

    
    public void addListener( CollectionListener listener )
    throws NullPointerException {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    public void removeListener( CollectionListener listener )
    throws NullPointerException {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    public void purge() {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    public boolean add( Feature obj ) {
        throw new UnsupportedOperationException( "not yet implemented." );
    }


    public boolean addAll( Collection collection ) {
        throw new UnsupportedOperationException( "not yet implemented." );
    }


    public boolean addAll( FeatureCollection resource ) {
        throw new UnsupportedOperationException( "not yet implemented." );
    }


    public void clear() {
        throw new UnsupportedOperationException( "not yet implemented." );
    }


    public boolean contains( Object o ) {
        throw new UnsupportedOperationException( "not yet implemented." );
    }

    public boolean containsAll( Collection o ) {
        throw new UnsupportedOperationException( "not yet implemented." );
    }

    public boolean remove( Object o ) {
        throw new UnsupportedOperationException( "not yet implemented." );
    }

    public boolean removeAll( Collection c ) {
        throw new UnsupportedOperationException( "not yet implemented." );
    }

    public boolean retainAll( Collection c ) {
        throw new UnsupportedOperationException( "not yet implemented." );
    }

    public FeatureCollection sort( SortBy order ) {
        throw new UnsupportedOperationException( "not yet implemented." );
    }

    public FeatureCollection subCollection( Filter filter ) {
        throw new UnsupportedOperationException( "not yet implemented." );
    }

    public Object[] toArray() {
        throw new UnsupportedOperationException( "not yet implemented." );
    }

    public Object[] toArray( Object[] a ) {
        throw new UnsupportedOperationException( "not yet implemented." );
    }

}
