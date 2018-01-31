/* 
 * polymap.org
 * Copyright (C) 2018, the @authors. All rights reserved.
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
package org.polymap.core.data.pipeline;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import java.io.IOException;

import org.geotools.data.DataAccess;
import org.geotools.data.DataStore;
import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.data.store.ContentFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.util.NullProgressListener;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import org.polymap.core.data.PipelineDataStore;
import org.polymap.core.data.PipelineFeatureSource;

/**
 * 
 * 
 * @author Falko Bräutigam
 */
public abstract class SimpleReadTest {

    /**
     * Expected results of {@link SimpleReadTest}.
     */
    public static class Expected {
        public String               SCHEMA_NAME;    
        public int                  COUNT;
        public CoordinateReferenceSystem   CRS;
        public ReferencedEnvelope   BOUNDS;
    }
    
    /** The backend  {@link DataStore}. */
    protected DataAccess            origDs;
            
    /** The backend  {@link FeatureSource}. */
    protected FeatureSource         origFs;

    protected Pipeline              pipeline;
    
    protected PipelineDataStore     pipeDs;

    protected Expected              expected;
    
    
    @Before
    public abstract void setUp() throws Exception;

    @After
    public void tearDown() throws Exception {
        if (pipeDs != null) {
            pipeDs.dispose();
        }
    }


    @Test
    public void testSchema() throws IOException {
        SimpleFeatureType schema = pipeDs.getFeatureSource().getSchema();
        //System.out.println( "" +  schema );
        assertEquals( expected.SCHEMA_NAME, schema.getName().getLocalPart() );
        assertEquals( expected.SCHEMA_NAME, schema.getTypeName() );
    }
    

    @Test
    public void testCount() throws IOException {
        PipelineFeatureSource fs = pipeDs.getFeatureSource();
        Query query = Query.ALL;
        assertEquals( expected.COUNT, fs.getCount( query ) );

        query = new Query( null, Filter.INCLUDE );
        assertEquals( expected.COUNT, fs.getCount( query ) );
        
        query = new Query( null, Filter.EXCLUDE );
        assertEquals( 0 /*origFs.getCount( query )*/, fs.getCount( query ) );
    }
    

    @Test
    public void testBounds() throws IOException {
        PipelineFeatureSource fs = pipeDs.getFeatureSource();
        Query query = Query.ALL;
        assertEquals( expected.BOUNDS, fs.getBounds( query ) );

        query = new Query( null, Filter.INCLUDE );
        assertEquals( expected.BOUNDS, fs.getBounds( query ) );
        
        query = new Query( null, Filter.EXCLUDE );
        assertEquals( null, fs.getBounds( query ) );
    }
    

    protected void checkAll( FeatureCollection features, Consumer<Feature> check ) throws IOException {
        features.accepts( feature -> check.accept( feature ), new NullProgressListener() );
        
        try (FeatureIterator it = features.features()) {
            while (it.hasNext()) {
                check.accept( it.next() );
            }
        }
    }
    
    
    @Test
    public void testVisitAll() throws IOException {
        AtomicInteger count = new AtomicInteger();
        checkAll( pipeDs.getFeatureSource().getFeatures( Query.ALL), feature -> {
            count.incrementAndGet();
            assertNotNull( feature.getIdentifier() );
            assertNotNull( feature.getDefaultGeometryProperty().getValue() );
        });
        assertEquals( expected.COUNT * 2, count.get() );
    }
    
    
    @Test
    public void testFilter() throws IOException {
        throw new RuntimeException( "not yet implemented" );
    }
    
    @Test
    public void testProjection() throws IOException {
        Query query = new Query( null, Filter.INCLUDE, new String[] {"the_geom"} );
        ContentFeatureCollection features = pipeDs.getFeatureSource().getFeatures( query );
        SimpleFeatureType schema = features.getSchema();
        assertEquals( 1, schema.getAttributeCount() );
        assertEquals( 1, schema.getDescriptors().size() );
        assertEquals( "the_geom", schema.getAttributeDescriptors().get( 0 ).getLocalName() );
        checkAll( features, feature -> {
            assertEquals( 1, feature.getProperties().size() );
            assertEquals( "the_geom", feature.getDefaultGeometryProperty().getName().getLocalPart() );
        });
    }
    
    @Test
    public void testSort() throws IOException {
        throw new RuntimeException( "not yet implemented" );        
    }
    
    @Test
    public void testTransaction() throws IOException {
        throw new RuntimeException( "not yet implemented" );        
    }
    
    @Test
    public void testCollection() throws IOException {
        throw new RuntimeException( "not yet implemented" );        
    }
    
}
