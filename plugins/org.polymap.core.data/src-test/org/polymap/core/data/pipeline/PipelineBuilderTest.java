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

import static java.util.Collections.EMPTY_MAP;
import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import java.io.File;
import java.io.Serializable;

import org.geotools.data.DataStore;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.store.ContentFeatureSource;
import org.junit.BeforeClass;
import org.junit.Test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.data.feature.DataSourceProcessor;
import org.polymap.core.data.feature.FeaturesProducer;
import org.polymap.core.data.feature.storecache.StoreCacheProcessor;


public class PipelineBuilderTest {

    private static final Log log = LogFactory.getLog( PipelineBuilderTest.class );
    
    private static DataStore            ds;
    
    private static ContentFeatureSource fs;

    private static DataSourceDescriptor dsd;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        // FIXME add to resources
        File f = new File( "/home/falko/Data/mittelsachen-alkis/GemeindenMittelsachsen.shp" );
        ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();

        Map<String, Serializable> params = new HashMap<String, Serializable>();
        params.put( "url", f.toURI().toURL() );
        params.put( "create spatial index", Boolean.TRUE );

        ds = dataStoreFactory.createNewDataStore( params );
        fs = ((ShapefileDataStore)ds).getFeatureSource();
        dsd = new DataSourceDescriptor( ds, fs.getName().getLocalPart() );
    }


    @Test
    public void testSimpleDataSourceAutoWire() throws PipelineBuilderException {
        AutoWirePipelineBuilder builder = new AutoWirePipelineBuilder( DataSourceProcessor.class );
        Pipeline pipeline = builder.newPipeline( FeaturesProducer.class, dsd );
        assertEquals( 1, pipeline.length() );
        assertEquals( DataSourceProcessor.class, pipeline.getLast().processorType() );
    }
    

    @Test
    public void testBufferDataSourceAutoWire() throws PipelineBuilderException {
        AutoWirePipelineBuilder builder = new AutoWirePipelineBuilder( DataSourceProcessor.class );
        Pipeline pipeline = builder.newPipeline( 
                FeaturesProducer.class, dsd, 
                new ProcessorDescriptor( StoreCacheProcessor.class, EMPTY_MAP ) );
        assertEquals( 2, pipeline.length() );
        assertEquals( StoreCacheProcessor.class, pipeline.get( 0 ).processorType() );
        assertEquals( DataSourceProcessor.class, pipeline.getLast().processorType() );
    }
    
}
