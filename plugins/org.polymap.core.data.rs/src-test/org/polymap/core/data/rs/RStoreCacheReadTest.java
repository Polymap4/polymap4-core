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
package org.polymap.core.data.rs;

import org.geotools.data.shapefile.ShapefileDataStore;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import org.polymap.core.data.PipelineDataStore;
import org.polymap.core.data.feature.DataSourceProcessor;
import org.polymap.core.data.feature.storecache.FullDataStoreSyncStrategy;
import org.polymap.core.data.feature.storecache.StoreCacheProcessor;
import org.polymap.core.data.pipeline.PipelineReadTest;
import org.polymap.core.data.pipeline.Shapefile;
import org.polymap.core.data.pipeline.SimplePipelineBuilder;
import org.polymap.core.data.rs.lucene.LuceneQueryDialect;

import org.polymap.recordstore.lucene.LuceneRecordStore;

/**
 * {@link PipelineReadTest} with {@link StoreCacheProcessor} with
 * {@link RDataStore} and {@link ShapefileDataStore} backend.
 *
 * @author Falko Bräutigam
 */
@RunWith(JUnit4.class)
public class RStoreCacheReadTest
        extends PipelineReadTest {
    
    /** Cached {@link ShapefileDataStore} for all tests. */
    private static ShapefileDataStore   _ds;

    private static Expected             _expected;
    
    private static RDataStore           _cacheDs;
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        _expected = Shapefile.testfileExpectations();
        _ds = Shapefile.openTestfile();
        //SimpleFeatureType schema = _cacheDs.getSchema();
        
        LuceneRecordStore rs = new LuceneRecordStore( /*RAM*/ );
        _cacheDs = new RDataStore( rs, new LuceneQueryDialect() );
        StoreCacheProcessor.init( () -> _cacheDs );
    }
    
    @AfterClass
    public static void tearDownClass() {
        _ds.dispose();
        //_cacheDs.dispose();
    }
    
    
    @Before
    public void setUp() throws Exception {
        origDs = _ds;
        origFs = origDs.getFeatureSource( _ds.getSchema().getName() );
        
        SimplePipelineBuilder builder = new SimplePipelineBuilder();
        StoreCacheProcessor.SYNC_TYPE.set( builder, FullDataStoreSyncStrategy.class.getSimpleName() );
        pipeline = builder.newFeaturePipeline( origFs, 
                StoreCacheProcessor.class,
                DataSourceProcessor.class );
        pipeDs = new PipelineDataStore( pipeline );
        expected = _expected;
    }

}
