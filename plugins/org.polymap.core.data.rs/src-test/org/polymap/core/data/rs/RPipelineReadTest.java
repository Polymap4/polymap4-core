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
import org.junit.BeforeClass;

import org.polymap.core.data.PipelineDataStore;
import org.polymap.core.data.feature.DataSourceProcessor;
import org.polymap.core.data.pipeline.PipelineReadTest;
import org.polymap.core.data.pipeline.Shapefile;
import org.polymap.core.data.pipeline.SimplePipelineBuilder;
import org.polymap.core.data.rs.lucene.LuceneQueryDialect;

import org.polymap.recordstore.lucene.LuceneRecordStore;

/**
 * Test against {@link RDataStore}.
 *
 * @author Falko Bräutigam
 */
public class RPipelineReadTest
        extends PipelineReadTest {

    /** Cached {@link ShapefileDataStore} for all tests. */
    private static RDataStore       _ds;
    
    private static RFeatureStore    _fs;
    
    private static Expected         _expected;

    @BeforeClass
    public static void setUpClass() throws Exception {
        LuceneRecordStore rs = new LuceneRecordStore( /*RAM*/ );
        _ds = new RDataStore( rs, new LuceneQueryDialect() );
        
        // creating schema / add features
        ShapefileDataStore shapeDs = Shapefile.openTestfile();
        _ds.createSchema( shapeDs.getSchema() );
        _fs = (RFeatureStore)_ds.getFeatureSource( shapeDs.getSchema().getName() );        
        _fs.addFeatures( shapeDs.getFeatureSource().getFeatures() );
        
        _expected = Shapefile.testfileExpectations();
    }
    
    @AfterClass
    public static void tearDownClass() {
        _ds.dispose();
    }

    public void setUp() throws Exception {
        origDs = _ds;
        origFs = _fs;
        pipeline = new SimplePipelineBuilder().newFeaturePipeline( origFs, DataSourceProcessor.class );
        pipeDs = new PipelineDataStore( pipeline );
        expected = _expected;
    }

}
