/* 
 * polymap.org
 * Copyright 2009-2018, Polymap GmbH. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.core.data.image.grayscale;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.ByteArrayOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.data.feature.GetBoundsRequest;
import org.polymap.core.data.feature.GetBoundsResponse;
import org.polymap.core.data.image.EncodedImageResponse;
import org.polymap.core.data.image.GetLegendGraphicRequest;
import org.polymap.core.data.image.GetMapRequest;
import org.polymap.core.data.image.ImageEncodeProcessor;
import org.polymap.core.data.image.ImageResponse;
import org.polymap.core.data.pipeline.Consumes;
import org.polymap.core.data.pipeline.EndOfProcessing;
import org.polymap.core.data.pipeline.PipelineExecutor.ProcessorContext;
import org.polymap.core.data.pipeline.PipelineProcessor;
import org.polymap.core.data.pipeline.PipelineProcessorSite;
import org.polymap.core.data.pipeline.ProcessorRequest;
import org.polymap.core.data.pipeline.ProcessorResponse;
import org.polymap.core.data.pipeline.Produces;

/**
 * Processes {@link Image} response into gray scale image. Just to showcase an image
 * processor.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ImageGrayscaleProcessor
        implements PipelineProcessor {

    private static final Log log = LogFactory.getLog( ImageGrayscaleProcessor.class );

    @Override
    public void init( PipelineProcessorSite site ) {
    }

    
    @Consumes({GetMapRequest.class, GetLegendGraphicRequest.class, GetBoundsRequest.class})
    @Produces({GetMapRequest.class, GetLegendGraphicRequest.class, GetBoundsRequest.class})
    public void forwardRequest( ProcessorRequest r, ProcessorContext context ) throws Exception {
        context.sendRequest( r );
    }

    
    @Consumes({GetBoundsResponse.class})
    @Produces({GetBoundsResponse.class})
    public void forwardResponse( ProcessorResponse r, ProcessorContext context ) throws Exception {
        context.sendResponse( r );
    }

    
//    @Consumes(ImageResponse.class)
//    @Produces(ImageResponse.class)
//    public void handleImage( ImageResponse r, ProcessorContext context ) throws Exception {
//        context.sendResponse( new ImageResponse( grayscale( r.getImage() ) ) );
//    }

    
    @Consumes(EncodedImageResponse.class)
    @Produces(EncodedImageResponse.class)
    public void handleEncodedImage( EncodedImageResponse r, ProcessorContext context ) throws Exception {
        ByteArrayOutputStream buf = (ByteArrayOutputStream)context.get( "buf" ); 
        if (buf == null) {
            context.put( "buf", buf = new ByteArrayOutputStream() );
        }
        buf.write( r.getChunk(), 0, r.getChunkSize() );
    }


    @Consumes({EndOfProcessing.class})
    @Produces({EndOfProcessing.class})
    public void eop( ProcessorResponse r, ProcessorContext context ) throws Exception {
        ByteArrayOutputStream buf = (ByteArrayOutputStream)context.get( "buf" ); 
        if (buf != null) {
            Image image = Toolkit.getDefaultToolkit().createImage( buf.toByteArray() );
            Image gray = grayscale( image );
            ImageEncodeProcessor encodeProcessor = new ImageEncodeProcessor();
            encodeProcessor.encodeImageResponse( new ImageResponse( gray ), context );
        }
        else {
            context.sendResponse( r );
        }
    }

    
    protected Image grayscale( Image image ) {
        long start = System.currentTimeMillis();

        // load image data
        new javax.swing.ImageIcon( image ).getImage();

        if (!(image instanceof BufferedImage)) { 
            BufferedImage bimage = new BufferedImage( 
                    image.getHeight( null ), image.getWidth( null ), BufferedImage.TYPE_4BYTE_ABGR );
            Graphics g = bimage.getGraphics();
            g.drawImage( image, 0, 0, null );
            g.dispose();
            
            image = bimage;
        }
        
        // grayscale
        ColorConvertOp filter = new ColorConvertOp( ColorSpace.getInstance( ColorSpace.CS_GRAY), null );
        
        BufferedImage grayImage = new BufferedImage( 
                image.getHeight( null ), image.getWidth( null ), BufferedImage.TYPE_4BYTE_ABGR );

        Graphics g = grayImage.getGraphics();
        filter.filter( (BufferedImage)image, grayImage);
        g.dispose();

        log.info( "Gray scaling took: " + (System.currentTimeMillis()-start) + "ms" );
        return grayImage;
    }
    
}
