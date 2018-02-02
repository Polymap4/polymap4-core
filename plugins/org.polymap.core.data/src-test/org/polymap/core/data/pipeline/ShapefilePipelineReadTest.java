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

import org.geotools.data.shapefile.ShapefileDataStore;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import org.polymap.core.data.PipelineDataStore;
import org.polymap.core.data.feature.DataSourceProcessor;

/**
 * {@link PipelineReadTest} against {@link ShapefileDataStore}.
 *
 * @author Falko Bräutigam
 */
@RunWith(JUnit4.class)
public class ShapefilePipelineReadTest
        extends PipelineReadTest {
    
    /** Cached {@link ShapefileDataStore} for all tests. */
    private static ShapefileDataStore   _ds;
    
    private static Expected             _expected;

    @BeforeClass
    public static void setUpClass() throws Exception {
        _ds = Shapefile.openTestfile();
        _expected = Shapefile.testfileExpectations();
    }
    
    @AfterClass
    public static void tearDownClass() {
        _ds.dispose();
    }
    
    @Before
    public void setUp() throws Exception {
        origDs = _ds;
        origFs = _ds.getFeatureSource();
        pipeline = new SimplePipelineBuilder().newFeaturePipeline( origFs, DataSourceProcessor.class );
        pipeDs = new PipelineDataStore( pipeline );
        expected = _expected;
    }

}
