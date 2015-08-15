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

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.geotools.image.ImageWorker;

import org.polymap.core.data.pipeline.EndOfProcessing;
import org.polymap.core.data.pipeline.PipelineProcessorSite;
import org.polymap.core.data.pipeline.ProcessorResponse;
import org.polymap.core.data.pipeline.PipelineExecutor.ProcessorContext;
import org.polymap.core.data.pipeline.Produces;
import org.polymap.core.data.util.ChunkedResponseOutputStream;
import org.polymap.core.runtime.Timer;

import com.objectplanet.image.PngEncoder;

/**
 * Encode the image of a {@link ImageResponse} into PNG byte chunks of an
 * {@link EncodedImageResponse}.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ImageEncodeProcessor
        implements EncodedImageProducer {

    private static final Log log = LogFactory.getLog( ImageEncodeProcessor.class );

    public static final String[]    FORMATS = { "image/png", "image/jpeg" };

    
    @Override
    public void init( PipelineProcessorSite site ) throws Exception {
    }


    @Override
    @Produces(GetMapRequest.class)
    public void getMapRequest( GetMapRequest request, ProcessorContext context ) throws Exception {
        String requestFormat = request.getFormat();
        if (!ArrayUtils.contains( FORMATS, requestFormat)) {
            throw new IllegalArgumentException( "This processor supports PNG and JPEG encoding. Requested format: " + requestFormat );
        }
        context.put( "format", requestFormat );
        context.sendRequest( request );
    }

    
    @Override
    @Produces(GetLegendGraphicRequest.class)
    public void getLegendGraphicRequest( GetLegendGraphicRequest request, ProcessorContext context ) throws Exception {
        context.sendRequest( request );
    }
    
    
    @Override
    @Produces(GetLayerTypesRequest.class)
    public void getLayerTypesRequest( GetLayerTypesRequest request, ProcessorContext context ) throws Exception {
        context.sendRequest( request );
    }

    
    @Produces(GetLayerTypesResponse.class)
    public void getLayerTypesResponse( GetLayerTypesResponse response, ProcessorContext context ) throws Exception {
        context.sendResponse( response );
    }

    
    @Produces({EncodedImageResponse.class, EndOfProcessing.class})
    public void encodeImageResponse( ImageResponse response, ProcessorContext context ) throws Exception {
        Timer timer = new Timer();

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
            imageioEncodeJPEG( response.getImage(), out );
        }
        else {
            opEncodePNG( response.getImage(), out );
        }
        log.debug( "encode: ready. (" + timer.elapsedTime() + "ms)" );
        
        out.flush();
        context.sendResponse( ProcessorResponse.EOP );
        log.debug( "...all data sent. (" + out.getTotalSent() + " bytes " + format + ")" );
    }
    

    private void gtEncodePNG( Image image, ChunkedResponseOutputStream out ) throws IOException {
        // using ImageWorker allows for native accelaration
        boolean nativeAcceleration = true;
        RenderedImage rimage = (RenderedImage)image;
        boolean paletted = false;  //rimage.getColorModel() instanceof IndexColorModel;
        new ImageWorker( rimage ).writePNG( out, "FILTERED", 0.9f, nativeAcceleration, paletted );
    }
    

    private void opEncodePNG( Image image, ChunkedResponseOutputStream out ) throws IOException {
        PngEncoder encoder = new PngEncoder( PngEncoder.COLOR_TRUECOLOR_ALPHA /*, PngEncoder.BEST_COMPRESSION*/ );
        encoder.encode( image, out );
    }
    

    /**
     *
     * @param formatName "png" or "jpeg"
     * @throws IOException
     */
    private void imageioEncode( Image image, ChunkedResponseOutputStream out, String formatName ) throws IOException {
        ImageIO.write( (RenderedImage)image, formatName, out );
    }

    
    private void imageioEncodeJPEG( Image image, ChunkedResponseOutputStream out ) throws IOException {
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
