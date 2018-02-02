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

import org.geotools.data.DataStore;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.store.ContentFeatureSource;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.data.feature.DataSourceProcessor;
import org.polymap.core.data.feature.FeaturesProducer;
import org.polymap.core.data.feature.storecache.NoSyncStrategy;
import org.polymap.core.data.feature.storecache.StoreCacheProcessor;
import org.polymap.core.data.pipeline.PipelineProcessorSite.Params;

/**
 * 
 * @author Falko Bräutigam
 */
@RunWith(JUnit4.class)
public class PipelineBuilderTest {

    private static final Log log = LogFactory.getLog( PipelineBuilderTest.class );
    
    private static DataStore            ds;
    
    private static ContentFeatureSource fs;

    private static DataSourceDescriptor dsd;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        ds = Shapefile.openTestfile();
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
        
        Params storeCacheProcessorParams = new Params();
        StoreCacheProcessor.SYNC_TYPE.set( storeCacheProcessorParams, NoSyncStrategy.class );
        Pipeline pipeline = builder.newPipeline( 
                FeaturesProducer.class, dsd, 
                new ProcessorDescriptor( StoreCacheProcessor.class, storeCacheProcessorParams ) );
        assertEquals( 2, pipeline.length() );
        assertEquals( StoreCacheProcessor.class, pipeline.get( 0 ).processorType() );
        assertEquals( DataSourceProcessor.class, pipeline.getLast().processorType() );
    }
    
}
