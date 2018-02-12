/* 
 * polymap.org
 * Copyright (C) 2015-2018, Falko Bräutigam. All rights reserved.
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

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.data.feature.FeatureRenderProcessor2;
import org.polymap.core.data.feature.GetBoundsRequest;
import org.polymap.core.data.feature.GetBoundsResponse;
import org.polymap.core.data.image.EncodedImageProcessor;
import org.polymap.core.data.image.EncodedImageResponse;
import org.polymap.core.data.image.GetLegendGraphicRequest;
import org.polymap.core.data.image.GetMapRequest;
import org.polymap.core.data.image.ImageEncodeProcessor;
import org.polymap.core.data.pipeline.PipelineExecutor.ProcessorContext;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
@RunWith(JUnit4.class)
public class ProcessorSignatureTest {

    private static final Log log = LogFactory.getLog( ProcessorSignatureTest.class );
    
    
    @Test
    public void testEncodedImageProducer() throws Exception {
        EncodedImageProcessor proc = new EncodedImageProcessor() {
            @Override
            public void init( PipelineProcessorSite site ) { }
        };
        ProcessorSignature signature = new ProcessorSignature( proc.getClass() );
        assertTrue( equals( signature.requestIn, GetMapRequest.class, GetLegendGraphicRequest.class, GetBoundsRequest.class ) );
        assertTrue( equals( signature.requestOut, GetMapRequest.class, GetLegendGraphicRequest.class, GetBoundsRequest.class ) );
        assertTrue( equals( signature.responseIn, EncodedImageResponse.class, EndOfProcessing.class, GetBoundsResponse.class ) );
        assertTrue( equals( signature.responseOut, EncodedImageResponse.class, EndOfProcessing.class, GetBoundsResponse.class ) );

        signature.invoke( proc, new GetMapRequest( null, null, null, null, null, 0, 0, 0 ), new TestProcessorContext() );
    }


    @Test
    public void testImageEncodeCompatibelFeatureRender() throws Exception {
        ImageEncodeProcessor proc1 = new ImageEncodeProcessor();
        FeatureRenderProcessor2 proc2 = new FeatureRenderProcessor2();
        
        ProcessorSignature sig1 = new ProcessorSignature( proc1.getClass() );
        ProcessorSignature sig2 = new ProcessorSignature( proc2.getClass() );
//        log.info( "ImageEncode:\n" + sig1 );
//        log.info( "FeatureRender:\n" + sig2 );
        assertTrue( sig1.isCompatible( sig2 ) );
    }

    
    class TestProcessorContext
            implements ProcessorContext {
        @Override
        public void sendRequest( ProcessorRequest request ) throws Exception {
            assertTrue( request instanceof GetMapRequest );
        }
        @Override
        public void sendResponse( ProcessorResponse response ) throws Exception {
            throw new RuntimeException( "not yet implemented." );
        }
        @Override
        public Object get( String key ) {
            throw new RuntimeException( "not yet implemented." );
        }
        @Override
        public Object put( String key, Object data ) {
            throw new RuntimeException( "not yet implemented." );
        }
    }

    
    protected boolean equals( Set s, Object... elms ) {
        return s.containsAll( Arrays.asList( elms ) ) && s.size() == elms.length;
    }
    
}
