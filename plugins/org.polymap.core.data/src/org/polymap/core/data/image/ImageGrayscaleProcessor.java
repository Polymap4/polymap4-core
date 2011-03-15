/* 
 * polymap.org
 * Copyright 2009, Polymap GmbH, and individual contributors as indicated
 * by the @authors tag.
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
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 * $Id$
 */
package org.polymap.core.data.image;

import java.util.Properties;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.ByteArrayOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.data.pipeline.PipelineProcessor;
import org.polymap.core.data.pipeline.ProcessorRequest;
import org.polymap.core.data.pipeline.ProcessorResponse;
import org.polymap.core.data.pipeline.ProcessorSignature;
import org.polymap.core.data.pipeline.PipelineExecutor.ProcessorContext;
import org.polymap.core.project.LayerUseCase;

/**
 * Process the image of a {@link ImageResponse} into an gray scale image.
 * 
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class ImageGrayscaleProcessor
        implements PipelineProcessor {

    private static final Log log = LogFactory.getLog( ImageGrayscaleProcessor.class );

    private static final ProcessorSignature signature = new ProcessorSignature(
            new Class[] {GetMapRequest.class, GetLegendGraphicRequest.class, GetLayerTypesRequest.class},
            new Class[] {GetMapRequest.class, GetLegendGraphicRequest.class, GetLayerTypesRequest.class},
            new Class[] {EncodedImageResponse.class, ImageResponse.class, GetLayerTypesResponse.class},
            new Class[] {EncodedImageResponse.class, ImageResponse.class, GetLayerTypesResponse.class}
            );

    public static ProcessorSignature signature( LayerUseCase usecase ) {
        return signature;
    }

    
    // instance *******************************************
    
    public void init( Properties props ) {
    }

    
    public void processRequest( ProcessorRequest r, ProcessorContext context )
    throws Exception {
        // pass through any request
        context.sendRequest( r );
    }

    
    public void processResponse( ProcessorResponse r, ProcessorContext context )
    throws Exception {
        // ImageResponse
        if (r instanceof ImageResponse) {
            ImageResponse response = (ImageResponse)r;
            Image image = response.getImage();
            doImageResponse( image, context );
        }
        // EncodedImageResponse
        else if (r instanceof EncodedImageResponse) {
            doEncodedImageResponse( r, context );
        }
        else if (r == ProcessorResponse.EOP && context.get( "buf" ) != null) {
            doEncodedImageResponse( r, context );
        }
        // pass any other response
        else {
            context.sendResponse( r );
        }
    }

    
    private void doEncodedImageResponse( ProcessorResponse r, ProcessorContext context )
    throws Exception {
        if (r != ProcessorResponse.EOP) {
            ByteArrayOutputStream buf = (ByteArrayOutputStream)context.get( "buf" ); 
            if (buf == null) {
                buf = new ByteArrayOutputStream();
                context.put( "buf", buf );
            }
            EncodedImageResponse response = (EncodedImageResponse)r;
            buf.write( response.getChunk(), 0, response.getChunkSize() );
        }
        else {
            ByteArrayOutputStream buf = (ByteArrayOutputStream)context.get( "buf" ); 
            Image image = Toolkit.getDefaultToolkit().createImage( buf.toByteArray() );
            
            Image gray = grayscale( image );
            
            ImageEncodeProcessor encodeProcessor = new ImageEncodeProcessor();
            encodeProcessor.doImageResponse( gray, context );
        }
    }


    protected void doImageResponse( Image image, final ProcessorContext context )
    throws Exception {
        context.sendResponse( new ImageResponse( grayscale( image ) ) );
        context.sendResponse( ProcessorResponse.EOP );
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
