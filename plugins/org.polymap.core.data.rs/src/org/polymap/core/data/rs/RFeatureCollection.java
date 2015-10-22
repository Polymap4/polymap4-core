/* 
 * polymap.org
 * Copyright (C) 2012-2014, Falko Bräutigam. All rights reserved.
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
package org.polymap.core.data.rs;

import java.util.Collection;
import java.util.Iterator;

import java.io.IOException;

import org.geotools.data.Query;
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
import com.google.common.collect.Iterators;

import org.polymap.core.data.rs.QueryDialect.PostProcessResultSet;

import org.polymap.recordstore.IRecordState;

/**
 * A {@link FeatureCollection} returned from {@link RFeatureStore} as a result
 * of a query.
 * <p/>
 * Does not support manipulation methods.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
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

    
    public RFeatureCollection( RFeatureStore fs, FeatureType schema, Query query, 
            QueryDialect queryDialect ) {
        this.id = String.valueOf( hashCode() );
        this.fs = fs;
        this.query = query;
        this.queryDialect = queryDialect;
        this.schema = schema;
    }

    
    @Override
    public String getID() {
        return id;
    }

    
    @Override
    public FeatureType getSchema() {
        return fs.getSchema();
    }


    @Override
    public ReferencedEnvelope getBounds() {
        try {
            return fs.getBounds( query );
        }
        catch (IOException e) {
            throw new RuntimeException( e );
        }
    }


    @Override
    public int size() {
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

    
    @Override
    public boolean isEmpty() {
        return size() == 0;
    }


    @Override
    public void accepts( FeatureVisitor visitor, ProgressListener progress ) throws IOException {
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
        }
    }

    
    @Override
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

    
    @Override
    public FeatureIterator features() {
        return new DelegateFeatureIterator( iterator() );
    }


    @Override
    public boolean contains( Object o ) {
        throw new UnsupportedOperationException( "not yet implemented." );
    }

    @Override
    public boolean containsAll( Collection o ) {
        throw new UnsupportedOperationException( "not yet implemented." );
    }

    @Override
    public FeatureCollection sort( SortBy order ) {
        throw new UnsupportedOperationException( "not yet implemented." );
    }

    @Override
    public FeatureCollection subCollection( Filter filter ) {
        throw new UnsupportedOperationException( "not yet implemented." );
    }

    @Override
    public Object[] toArray() {
        throw new UnsupportedOperationException( "not yet implemented." );
    }

    @Override
    public Object[] toArray( Object[] a ) {
        throw new UnsupportedOperationException( "not yet implemented." );
    }

}
