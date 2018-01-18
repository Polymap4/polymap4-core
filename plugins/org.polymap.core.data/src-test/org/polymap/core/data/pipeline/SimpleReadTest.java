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

import org.geotools.data.DataStore;
import org.geotools.data.FeatureSource;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * 
 * @author Falko Bräutigam
 */
public abstract class SimpleReadTest {

    private static final Log log = LogFactory.getLog( SimpleReadTest.class );

    /** The backend  {@link DataStore}. */
    protected ShapefileDataStore        ds;
            
    /** The backend  {@link FeatureSource}. */
    protected SimpleFeatureSource       fs;

    
    @Before
    public abstract void setUp() throws Exception;

    @After
    public abstract void tearDown() throws Exception;


    @Test
    public void testSchema() {
        
         new PipelineDataStore( )
    }
    
}
