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


/**
 * Process the image of a {@link ImageResponse} into one color to transparent
 * pixel image.
 * 
 * @deprecated Not yet ported to Polymap4.
 * @author <a href="http://www.polymap.de">Falko Brutigam</a>
 */
public class ImageTransparencyProcessor {
//        implements PipelineProcessor {
//
//    private static final Log log = LogFactory.getLog( ImageTransparencyProcessor.class );
//
//    public static final String      PROP_MARKER_COLOR = "markerColor";
//    
//    private static final ProcessorSignature signature = new ProcessorSignature(
//            new Class[] {GetMapRequest.class, GetLegendGraphicRequest.class, GetLayerTypesRequest.class},
//            new Class[] {GetMapRequest.class, GetLegendGraphicRequest.class, GetLayerTypesRequest.class},
//            new Class[] {EncodedImageResponse.class, ImageResponse.class, GetLayerTypesResponse.class},
//            new Class[] {EncodedImageResponse.class, ImageResponse.class, GetLayerTypesResponse.class}
//            );
//
//    public static ProcessorSignature signature( LayerUseCase usecase ) {
//        return signature;
//    }
//
//    
//    // instance *******************************************
//    
//    private Color       markerColor;
//    
//    public void init( Properties props ) {
//        String color = props.getProperty( PROP_MARKER_COLOR, "ffffff" );
//        markerColor = new Color( Integer.parseInt( color, 16 ) );
//        log.info( "Color: " + markerColor.toString() );
//    }
//
//    
//    public void processRequest( ProcessorRequest r, ProcessorContext context )
//    throws Exception {
//        // pass through any request
//        context.sendRequest( r );
//    }
//
//    
//    public void processResponse( ProcessorResponse r, ProcessorContext context )
//    throws Exception {
//        // ImageResponse
//        if (r instanceof ImageResponse) {
//            ImageResponse response = (ImageResponse)r;
//            Image image = response.getImage();
//            doImageResponse( image, context );
//        }
//        // EncodedImageResponse
//        else if (r instanceof EncodedImageResponse) {
//            doEncodedImageResponse( r, context );
//        }
//        else if (r == ProcessorResponse.EOP && context.get( "buf" ) != null) {
//            doEncodedImageResponse( r, context );
//        }
//        // pass any other response
//        else {
//            context.sendResponse( r );
//        }
//    }
//
//    
//    private void doEncodedImageResponse( ProcessorResponse r, ProcessorContext context )
//    throws Exception {
//        if (r != ProcessorResponse.EOP) {
//            ByteArrayOutputStream buf = (ByteArrayOutputStream)context.get( "buf" ); 
//            if (buf == null) {
//                buf = new ByteArrayOutputStream();
//                context.put( "buf", buf );
//            }
//            EncodedImageResponse response = (EncodedImageResponse)r;
//            buf.write( response.getChunk(), 0, response.getChunkSize() );
//        }
//        else {
//            ByteArrayOutputStream buf = (ByteArrayOutputStream)context.get( "buf" ); 
//            //Image image = Toolkit.getDefaultToolkit().createImage( buf.toByteArray() );
//            BufferedImage image = ImageIO.read( new ByteArrayInputStream( buf.toByteArray() ) );
//            
//            Image result = transparency( image, markerColor );
//            
//            ImageEncodeProcessor encodeProcessor = new ImageEncodeProcessor();
//            context.put( "format", "image/png" );
//            encodeProcessor.doImageResponse( result, context );
//        }
//    }
//
//
//    protected void doImageResponse( Image image, final ProcessorContext context )
//    throws Exception {
//        context.sendResponse( new ImageResponse( transparency( image, markerColor ) ) );
//        context.sendResponse( ProcessorResponse.EOP );
//    }
//
//    
//    public static BufferedImage transparency( Image image, final Color markerColor )
//    throws IOException {
//        long start = System.currentTimeMillis();
//
//        RGBImageFilter filter = new RGBImageFilter() {
//            // the color we are looking for... Alpha bits are set to opaque
//            public int  markerRGB = markerColor.getRGB() | 0xFF000000;
//
//            byte        threshold = 25;
//            
//            double      range = ((double)0xFF) / (3*threshold);
//
//            public final int filterRGB( int x, int y, int rgb ) {
//                Color probe = new Color( rgb );
//                //log.info( "probe=" + probe + ", marker=" + markerColor );
//                
//                // delta values
//                int dRed = markerColor.getRed() - probe.getRed();
//                int dGreen = markerColor.getGreen() - probe.getGreen();
//                int dBlue = markerColor.getBlue() - probe.getBlue();
//                //log.info( "    dRed=" + dRed + ", dGreen=" + dGreen );
//                
//                if (dRed >= 0 && dRed < threshold 
//                        && dGreen >= 0 && dGreen < threshold 
//                        && dBlue >= 0 && dBlue < threshold) {
//                    int alpha = (int)Math.round( range * (dRed + dGreen + dBlue) );
//                    //log.info( "    -> alpha=" + alpha );
//                    
//                    return ((alpha << 24) | 0x00FFFFFF) & rgb;
//                }
//                else {
//                    // nothing to do
//                    return rgb;
//                }
//            }
//        }; 
//
////        BufferedImage bimage = null;
////        if (image instanceof BufferedImage) {
////            bimage = (BufferedImage)image;
////        }
////        else {
////            bimage = new BufferedImage(
////                    image.getHeight( null ), image.getWidth( null ), BufferedImage.TYPE_INT_ARGB );
////            Graphics g = bimage.getGraphics();
////            g.drawImage( image, 0, 0, null );
////            g.dispose();
////        }
//
//        ImageProducer ip = new FilteredImageSource( image.getSource(), filter );
//        Image result = Toolkit.getDefaultToolkit().createImage( ip );
//
//        BufferedImage bresult = new BufferedImage( 
//                image.getHeight( null ), image.getWidth( null ), BufferedImage.TYPE_INT_ARGB );
//        Graphics g = bresult.getGraphics();
//        g.drawImage( result, 0, 0, null );
//        g.dispose();
//
////        // XXX this can surely be done any more clever
////        int width = bimage.getWidth();
////        int height = bimage.getHeight();
////        for (int x=bimage.getMinX(); x<width; x++) {
////            for (int y=bimage.getMinY(); y<height; y++) {
////                int filtered = filter.filterRGB( x, y, bimage.getRGB( x, y ) );
////                result.setRGB( x, y, filtered );
////            }
////        }
//
//        log.debug( "Transparency done. (" + (System.currentTimeMillis()-start) + "ms)" );
//        return bresult;
//    }
//
//    
////    public static Image transparency( Image image, final Color markerColor ) {
////        long start = System.currentTimeMillis();
////
////        // load image data
////        new javax.swing.ImageIcon( image ).getImage();
////
////        if (!(image instanceof BufferedImage)) { 
////            BufferedImage bimage = new BufferedImage( 
////                    image.getHeight( null ), image.getWidth( null ), BufferedImage.TYPE_4BYTE_ABGR );
////            Graphics g = bimage.getGraphics();
////            g.drawImage( image, 0, 0, null );
////            g.dispose();
////            
////            image = bimage;
////        }
////        
////        ImageFilter filter = new RGBImageFilter() {
////            // the color we are looking for... Alpha bits are set to opaque
////            public int markerRGB = markerColor.getRGB() | 0xFF000000;
////
////            public final int filterRGB( int x, int y, int rgb ) {
////                if ((rgb | 0xFF000000) == markerRGB) {
////                    // Mark the alpha bits as zero - transparent
////                    return 0x00FFFFFF & rgb;
////                }
////                else {
////                    // nothing to do
////                    return rgb;
////                }
////            }
////        }; 
////
////        ImageProducer ip = new FilteredImageSource( image.getSource(), filter );
////        Image result = Toolkit.getDefaultToolkit().createImage( ip );
////
////        BufferedImage bImage = new BufferedImage( 
////                result.getHeight( null ), result.getWidth( null ), BufferedImage.TYPE_4BYTE_ABGR );
////
////        Graphics g = bImage.getGraphics();
////        g.drawImage( result, 0, 0, null );
////        g.dispose();
////        
////        log.info( "Transparency done. (" + (System.currentTimeMillis()-start) + "ms)" );
////        return bImage;
////    }
}
