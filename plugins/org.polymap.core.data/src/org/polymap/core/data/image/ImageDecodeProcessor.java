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

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.imageio.ImageIO;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.data.pipeline.PipelineProcessor;
import org.polymap.core.data.pipeline.ProcessorRequest;
import org.polymap.core.data.pipeline.ProcessorResponse;
import org.polymap.core.data.pipeline.ProcessorSignature;
import org.polymap.core.data.pipeline.PipelineExecutor.ProcessorContext;
import org.polymap.core.project.LayerUseCase;

/**
 * Decode the image data responses to {@link ImageResponse} using the
 * {@link java.awt.Toolkit}.
 * 
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class ImageDecodeProcessor
        implements PipelineProcessor {

    private static final Log log = LogFactory.getLog( ImageDecodeProcessor.class );

    private static final ProcessorSignature signature = new ProcessorSignature(
            new Class[] {GetMapRequest.class, GetLegendGraphicRequest.class, GetLayerTypesRequest.class},
            new Class[] {GetMapRequest.class, GetLegendGraphicRequest.class, GetLayerTypesRequest.class},
            new Class[] {EncodedImageResponse.class, GetLayerTypesResponse.class},
            new Class[] {ImageResponse.class, GetLayerTypesResponse.class}
            );

    public static ProcessorSignature signature( LayerUseCase usecase ) {
        return signature;
    }

    
    // instance *******************************************
    
    public void init( Properties props ) {
    }

    
    public void processRequest( ProcessorRequest r, ProcessorContext context )
    throws Exception {
        // GetMapRequest
        if (r instanceof GetMapRequest) {
            context.sendRequest( r );
        }
        else {
            context.sendRequest( r );
        }
    }

    
    public void processResponse( ProcessorResponse r, ProcessorContext context )
    throws Exception {
        ByteArrayOutputStream data = (ByteArrayOutputStream)context.get( "data" );

        // EncodedImageResponse
        if (r instanceof EncodedImageResponse) {
            EncodedImageResponse response = (EncodedImageResponse)r;
            if (data == null) {
                data = new ByteArrayOutputStream( 64*1024 );
                context.put( "data", data );
            }
            data.write( response.getChunk(), 0, response.getChunkSize() );
        }
        // decode on EOP
        else if (r == ProcessorResponse.EOP && data != null) {
            long start = System.currentTimeMillis();

            //Image image = Toolkit.getDefaultToolkit().createImage( data.toByteArray() );
            BufferedImage image = ImageIO.read( new ByteArrayInputStream( data.toByteArray() ) );

            // load image data
            //new javax.swing.ImageIcon( image ).getImage();
            
            context.sendResponse( new ImageResponse( image ) );
            context.sendResponse( ProcessorResponse.EOP );
            context.put( "data", null );
            log.debug( "Decode: ready. (" + (System.currentTimeMillis()-start) + "ms)" );
        }
        else {
            context.sendResponse( r );
        }
    }
    
}
