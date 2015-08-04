/* 
 * polymap.org
 * Copyright (C) 2015, Falko Bräutigam. All rights reserved.
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

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.data.image.EncodedImageProcessor;
import org.polymap.core.data.image.EncodedImageResponse;
import org.polymap.core.data.image.GetLayerTypesRequest;
import org.polymap.core.data.image.GetLayerTypesResponse;
import org.polymap.core.data.image.GetLegendGraphicRequest;
import org.polymap.core.data.image.GetMapRequest;
import org.polymap.core.data.pipeline.PipelineExecutor.ProcessorContext;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ProcessorSignatureTest
        extends TestCase {

    private static Log log = LogFactory.getLog( ProcessorSignatureTest.class );
    
    
    public void testEncodedImageProducer() throws Exception {
        EncodedImageProcessor proc = new EncodedImageProcessor() {
            @Override
            public void init( Properties props ) {
                // XXX Auto-generated method stub
                throw new RuntimeException( "not yet implemented." );
            }
        };
        ProcessorSignature signature = new ProcessorSignature( proc );
        log.info( "Signature" + signature );
        assertTrue( equals( signature.requestIn, GetMapRequest.class, GetLegendGraphicRequest.class, GetLayerTypesRequest.class ) );
        assertTrue( equals( signature.requestOut, GetMapRequest.class, GetLegendGraphicRequest.class, GetLayerTypesRequest.class ) );
        assertTrue( equals( signature.responseIn, EncodedImageResponse.class, GetLayerTypesResponse.class ) );
        assertTrue( equals( signature.responseOut, EncodedImageResponse.class, GetLayerTypesResponse.class ) );

        signature.invoke( new GetMapRequest( null, null, null, null, 0, 0, 0 ), new TestProcessorContext() );
    }

    
    class TestProcessorContext
            implements ProcessorContext {
        @Override
        public void sendRequest( ProcessorRequest request ) throws Exception {
            throw new RuntimeException( "not yet implemented." );
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

    
    protected boolean equals( List l, Object... elms ) {
        return l.containsAll( Arrays.asList( elms ) ) && l.size() == elms.length;
    }
    
}
