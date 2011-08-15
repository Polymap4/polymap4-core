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

import java.awt.Image;
import java.awt.image.ColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.RenderedImage;
import java.io.IOException;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.geotools.image.ImageWorker;

import org.polymap.core.data.pipeline.PipelineProcessor;
import org.polymap.core.data.pipeline.ProcessorRequest;
import org.polymap.core.data.pipeline.ProcessorResponse;
import org.polymap.core.data.pipeline.ProcessorSignature;
import org.polymap.core.data.pipeline.PipelineExecutor.ProcessorContext;
import org.polymap.core.data.util.ChunkedResponseOutputStream;
import org.polymap.core.project.LayerUseCase;

/**
 * Encode the image of a {@link ImageResponse} into PNG byte chunks of an
 * {@link EncodedImageResponse}.
 * 
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class ImageEncodeProcessor
        implements PipelineProcessor {

    private static final Log log = LogFactory.getLog( ImageEncodeProcessor.class );

    private static final ProcessorSignature signature = new ProcessorSignature(
            new Class[] {GetMapRequest.class, GetLegendGraphicRequest.class, GetLayerTypesRequest.class},
            new Class[] {GetMapRequest.class, GetLegendGraphicRequest.class, GetLayerTypesRequest.class},
            new Class[] {ImageResponse.class, GetLayerTypesResponse.class},
            new Class[] {EncodedImageResponse.class, GetLayerTypesResponse.class}
            );

    public static ProcessorSignature signature( LayerUseCase usecase ) {
        return signature;
    }

    public static final String[]    FORMATS = { "image/png", "image/jpeg" };

    
    // instance *******************************************
        
    public void init( Properties props ) {
    }

    
    public void processRequest( ProcessorRequest r, ProcessorContext context )
    throws Exception {
        // GetMapRequest
        if (r instanceof GetMapRequest) {
            String requestFormat = ((GetMapRequest)r).getFormat();
            if (!ArrayUtils.contains( FORMATS, requestFormat)) {
                throw new IllegalArgumentException( "This processor supports PNG and JPEG encoding. Requested format: " + requestFormat );
            }
            context.put( "format", requestFormat );
            context.sendRequest( r );
        }
        // GetLegendGraphicRequest
        else if (r instanceof GetLegendGraphicRequest) {
            context.sendRequest( r );
        }
        // GetLayerTypes
        else if (r instanceof GetLayerTypesRequest) {
            context.sendRequest( r );
        }
        else {
            throw new IllegalArgumentException( "Unhandled request type: " + r );
        }
    }

    
    public void processResponse( ProcessorResponse r, ProcessorContext context )
    throws Exception {
        // ImageResponse
        if (r instanceof ImageResponse) {
            ImageResponse response = (ImageResponse)r;
            Image image = response.getImage();
            doImageResponse( image, context );
        }
        // GetLayerTypes
        else if (r instanceof GetLayerTypesResponse
                || r == ProcessorResponse.EOP) {
            context.sendResponse( r );
        }
        else {
            throw new IllegalStateException( "Unhandled response type: " + r );
        }
    }

    
    protected void doImageResponse( Image image, final ProcessorContext context )
            throws Exception {
        long start = System.currentTimeMillis();

        // chunked reponse output stream
        ChunkedResponseOutputStream out = new ChunkedResponseOutputStream( context ) {
            protected ProcessorResponse createResponse( byte[] buf, int buflen ) {
                //log.debug( "sending: buflen= " + buflen );
                return new EncodedImageResponse( buf, buflen );
            }
        };

        // load image data
        //new javax.swing.ImageIcon( image ).getImage();
        
        String format = (String)context.get( "format" );
        if ("image/jpeg".equals( format )) {
            imageioEncodeJPEG( image, out );
        }
        else {
            gtEncodePNG( image, out );
        }
        log.debug( "encode: ready. (" + (System.currentTimeMillis()-start) + "ms)" );
        
        out.flush();
        context.sendResponse( ProcessorResponse.EOP );
        log.debug( "...all data sent. (" + out.getTotalSent() + " bytes " + format + ")" );
    }
    
    
    private void gtEncodePNG( Image image, ChunkedResponseOutputStream out )
    throws IOException {
        // using ImageWorker allows for native accelaration
        boolean nativeAcceleration = true;
        RenderedImage rimage = (RenderedImage)image;
        boolean paletted = false;  //rimage.getColorModel() instanceof IndexColorModel;
        new ImageWorker( rimage ).writePNG( out, "FILTERED", 0.9f, 
                nativeAcceleration, paletted );
    }
    

//    private void opEncodePNG( Image image, ChunkedResponseOutputStream out )
//    throws IOException {
//        PngEncoder encoder = new PngEncoder( PngEncoder.COLOR_TRUECOLOR_ALPHA );
//        encoder.encode( image, out );
//    }
    

    /**
     *
     * @param formatName "png" or "jpeg"
     * @throws IOException
     */
    private void imageioEncode( Image image, ChunkedResponseOutputStream out, String formatName )
    throws IOException {
        ImageIO.write( (RenderedImage)image, formatName, out );
    }

    
    private void imageioEncodeJPEG( Image image, ChunkedResponseOutputStream out )
    throws IOException {
        // this code is from http://forums.sun.com/thread.jspa?threadID=5197061
        ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
        ImageWriteParam param = writer.getDefaultWriteParam();
        param.setSourceBands(new int[] {0,1,2});
        ColorModel cm = new DirectColorModel( 24,
                                      0x00ff0000,   // Red
                                      0x0000ff00,   // Green
                                      0x000000ff,   // Blue
                                      0x0 );        // Alpha
        param.setDestinationType(
                new ImageTypeSpecifier(
                    cm,
                    cm.createCompatibleSampleModel( 1, 1 ) ) );
         
        ImageOutputStream imageOut =
                ImageIO.createImageOutputStream( out );
        writer.setOutput( imageOut );
        writer.write( null, new IIOImage( (RenderedImage)image, null, null), param );
        writer.dispose();
        imageOut.close();        
    }
    
    
//    private void jaiEncodePNG( Image image, ChunkedResponseOutputStream out )
//    throws IOException {
//        JAI.create( "encode", image, out, "PNG", null );
//    }


//    private void imageioEncodePNG( Image image, ChunkedResponseOutputStream out )
//    throws IOException {
//        // encode PNG
//        PngEncoder pngEncoder = new PngEncoder( image, true, null, 9 );
//        pngEncoder.encode( out );
//        out.flush();
//        log.debug( "encode: ready." );
//        context.sendResponse( ProcessorResponse.EOP );
//        log.debug( "...all data sent. (" + out.getTotalSent() + " bytes)" );
//    }

}
