/* 
 * polymap.org
 * Copyright (C) 2009-2015, Polymap GmbH. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.core.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.geotools.data.Query;
import org.geotools.feature.FeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.data.PipelineFeatureSource.FeatureResponseHandler;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
class SyncPipelineFeatureCollection
        extends AbstractPipelineFeatureCollection
        implements FeatureCollection<SimpleFeatureType, SimpleFeature> {

    private static final Log log = LogFactory.getLog( SyncPipelineFeatureCollection.class );

    protected PipelineFeatureSource     fs;

    protected Query                     query;
    
    private int                         size = -1;


    protected SyncPipelineFeatureCollection( PipelineFeatureSource fs, Query query ) {
        super( fs.getSchema() );
        this.fs = fs;
        this.query = query;
        fs.addFeatureListener( this );
    }

    @Override
    protected Iterator openIterator() {
        try {
            log.debug( "..." );
            return new SyncPipelineIterator();
        }
        catch (RuntimeException e) {
            throw e;
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }
    }

    protected void closeIterator( Iterator close ) {
        log.debug( "close= " + close );
    }

    @Override
    public int size() {
        if (size < 0) {
            size = fs.getFeaturesSize( query );
        }
        return size;
    }


    @Override
    public ReferencedEnvelope getBounds() {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    /**
     * 
     */
    class SyncPipelineIterator
            implements Iterator {

        Iterator                it;
        
//        int                     startIndex, maxFeatures, count;
        
        protected SyncPipelineIterator() throws Exception {
//            startIndex = query.getStartIndex() != null ? query.getStartIndex() : 0;
//            maxFeatures = query.getMaxFeatures();
//            count = 0;
            
            final List<Feature> buffer = new ArrayList();
            fs.fetchFeatures( query, new FeatureResponseHandler() {
                public void handle( List<Feature> features )
                throws Exception {
                    buffer.addAll( features );
                }
                public void endOfResponse()
                throws Exception {
                }
            });
            it = buffer.iterator();
        }
        
        @Override
        public boolean hasNext() {
//            if (it == null || !it.hasNext()) {
//                if ((startIndex + count) >= maxFeatures) {
//                    return false;
//                }
//                int size = Math.min( maxFeatures, 1000 );
//                
//                DefaultQuery q = new DefaultQuery( query );
//                q.setStartIndex( startIndex + count );
//                q.setMaxFeatures( size );
//                it = fs.fetchFeatures( q );
//                count += size;
//            }
            return it.hasNext();
        }

        @Override
        public Object next() {
            return it.next();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
        
    }

}
