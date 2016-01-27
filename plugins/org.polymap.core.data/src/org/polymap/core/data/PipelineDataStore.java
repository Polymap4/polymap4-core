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

import java.io.IOException;

import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;

import org.geotools.data.AbstractDataStore;
import org.geotools.data.DataStore;
import org.geotools.data.FeatureListenerManager;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureWriter;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.data.collection.DelegateFeatureReader;
import org.geotools.geometry.jts.ReferencedEnvelope;

import com.vividsolutions.jts.geom.Envelope;

/**
 * The <code>DataStore</code> of a {@link PipelineFeatureSource}. 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
@SuppressWarnings("deprecation")
public class PipelineDataStore
        extends AbstractDataStore
        implements DataStore {

    protected FeatureListenerManager  listeners = new FeatureListenerManager();
    
    protected PipelineFeatureSource   fs;
    
    
    public PipelineDataStore( PipelineFeatureSource fs ) {
        this.fs = fs;
    }

    
//    @Override
//    public PipelineFeatureSource getFeatureSource() {
//        return fs;
//    }
    
    
    @Override
    protected FeatureReader<SimpleFeatureType,SimpleFeature> getFeatureReader( String typeName ) throws IOException {
        return new DelegateFeatureReader( fs.getSchema(), fs.getFeatures().features() );
    }

    
    @Override
    public FeatureReader<SimpleFeatureType,SimpleFeature> getFeatureReader( Query query, Transaction transaction ) throws IOException {
        return new DelegateFeatureReader( fs.getSchema(), fs.getFeatures( query ).features() );
    }

    
    @Override
    protected FeatureWriter<SimpleFeatureType,SimpleFeature> createFeatureWriter( String typeName, Transaction transaction)
            throws IOException {
        return new FeatureWriter<SimpleFeatureType,SimpleFeature>() {
            @Override
            public SimpleFeatureType getFeatureType() {
                // XXX Auto-generated method stub
                throw new RuntimeException( "not yet implemented." );
            }
            @Override
            public SimpleFeature next() throws IOException {
                // XXX Auto-generated method stub
                throw new RuntimeException( "not yet implemented." );
            }
            @Override
            public void remove() throws IOException {
                // XXX Auto-generated method stub
                throw new RuntimeException( "not yet implemented." );
            }
            @Override
            public void write() throws IOException {
                // XXX Auto-generated method stub
                throw new RuntimeException( "not yet implemented." );
            }
            @Override
            public boolean hasNext() throws IOException {
                // XXX Auto-generated method stub
                throw new RuntimeException( "not yet implemented." );
            }
            @Override
            public void close() throws IOException {
                // XXX Auto-generated method stub
                throw new RuntimeException( "not yet implemented." );
            }
        };
    }

    
    @Override
    public SimpleFeatureType getSchema( String _typeName ) throws IOException {
        assert _typeName != null : "typeName must not be null.";
//        if (fs.getSchema().getTtypeName.equals( _typeName ) ) {
            return fs.getSchema();
//        }
//        else {
//            throw new IOException( "TypeName cannot be found: " + _typeName );
//        }
    }

    
    @Override
    public String[] getTypeNames() throws IOException {
        return new String[] {fs.getSchema().getTypeName()};
    }

    
    @Override
    protected ReferencedEnvelope getBounds( Query query ) throws IOException {
        if (query.getFilter() == Filter.EXCLUDE) {
            return new ReferencedEnvelope( new Envelope(), 
                    getSchema( query.getTypeName() ).getCoordinateReferenceSystem());
        }
        else {
            return fs.getBounds( query );
        }
    }


    @Override
    protected int getCount( Query query ) throws IOException {
        return fs.getCount( query );
    }
    
}
