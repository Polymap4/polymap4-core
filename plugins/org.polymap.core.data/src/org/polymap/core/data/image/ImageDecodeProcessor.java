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
package org.polymap.core.data.image;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.imageio.ImageIO;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.data.feature.GetBoundsRequest;
import org.polymap.core.data.feature.GetBoundsResponse;
import org.polymap.core.data.pipeline.Consumes;
import org.polymap.core.data.pipeline.EndOfProcessing;
import org.polymap.core.data.pipeline.PipelineExecutor.ProcessorContext;
import org.polymap.core.data.pipeline.PipelineProcessorSite;
import org.polymap.core.data.pipeline.ProcessorResponse;
import org.polymap.core.data.pipeline.Produces;

/**
 * Decode the image data responses to {@link ImageResponse} using the
 * {@link java.awt.Toolkit}.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ImageDecodeProcessor
        implements ImageProducer {

    private static final Log log = LogFactory.getLog( ImageDecodeProcessor.class );


    @Override
    public void init( PipelineProcessorSite site ) throws Exception {
    }

    @Override
    @Produces( GetMapRequest.class )
    public void getMapRequest( GetMapRequest request, ProcessorContext context ) throws Exception {
        context.sendRequest( request );        
    }

    @Override
    @Produces( GetLegendGraphicRequest.class )
    public void getLegendGraphicRequest( GetLegendGraphicRequest request, ProcessorContext context ) throws Exception {
        context.sendRequest( request );
    }

    @Override
    @Produces( GetBoundsRequest.class )
    public void getBoundsRequest( GetBoundsRequest request, ProcessorContext context ) throws Exception {
        context.sendRequest( request );
    }

    @Consumes( GetBoundsResponse.class )
    public void handleBoundsResponse( GetBoundsResponse response, ProcessorContext context ) throws Exception {
        context.sendResponse( response );
    }


    /**
     * Collect enoded image chunks. 
     */
    @Produces( ImageResponse.class )
    @Consumes( EncodedImageResponse.class )
    public void decodeImageResponse( EncodedImageResponse response, ProcessorContext context ) throws Exception {
        ByteArrayOutputStream data = (ByteArrayOutputStream)context.get( "data" );

        if (data == null) {
            data = new ByteArrayOutputStream( 64*1024 );
            context.put( "data", data );
        }
        data.write( response.getChunk(), 0, response.getChunkSize() );
    }
    

    /**
     * Decode image on EOP. 
     */
    @Produces( EndOfProcessing.class )
    @Consumes( EndOfProcessing.class )
    public void endOfProcessing( EndOfProcessing eop, ProcessorContext context ) throws Exception {
        long start = System.currentTimeMillis();

        ByteArrayOutputStream data = (ByteArrayOutputStream)context.get( "data" );

        //Image image = Toolkit.getDefaultToolkit().createImage( data.toByteArray() );
        BufferedImage image = ImageIO.read( new ByteArrayInputStream( data.toByteArray() ) );

        // load image data
        //new javax.swing.ImageIcon( image ).getImage();
        
        context.sendResponse( new ImageResponse( image ) );
        context.put( "data", null );
        log.info( "Decode: ready. (" + (System.currentTimeMillis()-start) + "ms)" );
        context.sendResponse( ProcessorResponse.EOP );
    }
   
}
