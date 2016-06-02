/* 
 * polymap.org
 * Copyright (C) 2009-2015 Polymap GmbH. All rights reserved.
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
package org.polymap.service.geoserver.spring;

import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import org.geoserver.platform.ServiceException;
import org.geoserver.wms.MapProducerCapabilities;
import org.geoserver.wms.WMS;
import org.geoserver.wms.WMSMapContent;
import org.geoserver.wms.map.RenderedImageMapResponse;
import org.geotools.map.Layer;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.data.image.EncodedImageProducer;
import org.polymap.core.data.image.EncodedImageResponse;
import org.polymap.core.data.image.GetMapRequest;
import org.polymap.core.data.pipeline.Pipeline;
import org.polymap.core.data.pipeline.ProcessorRequest;
import org.polymap.core.data.pipeline.ProcessorResponse;
import org.polymap.core.data.pipeline.ResponseHandler;
import org.polymap.core.project.ILayer;
import org.polymap.core.runtime.Timer;

import org.polymap.service.geoserver.GeoServerServlet;

/**
 * This {@link GetMapProducer} allows to use the pipelines rendering of POLYMAP
 * via GeoServer.
 * <p>
 * This producer differs from the normal GeoServer {@link GetMapProducer} in that
 * it does not actual render anything but delegates the rendering to the POLYMAP
 * pipeline. Therefore we cannot share the code from GeoServer here.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class PipelineMapResponse
        extends RenderedImageMapResponse {

    private static final Log log = LogFactory.getLog( PipelineMapResponse.class );

    /** the only MIME type this map producer supports */
    static final String         MIME_TYPE = "image/png";

    static final String[]       OUTPUT_FORMATS = {MIME_TYPE, "image/jpeg" };

    private GeoServerServlet    server = GeoServerServlet.instance.get();


    public PipelineMapResponse( WMS wms ) {
        super( OUTPUT_FORMATS, wms );
        log.debug( "INIT: " + wms.getServiceInfo().getId() );
    }
    

    @Override
    public MapProducerCapabilities getCapabilities( String outputFormat ) {
        return new MapProducerCapabilities( true, false, false, true, null );    
    }


    @Override
    public void formatImageOutputStream( RenderedImage image, OutputStream out, WMSMapContent mapContent )
            throws ServiceException, IOException {
        Timer timer = new Timer();
        
        // single layer? -> request ENCODED_IMAGE
        if (mapContent.layers().size() == 1) {
            Layer mapLayer = mapContent.layers().get( 0 );
            
            ILayer layer = null;
            String layerName = StringUtils.substringAfterLast( mapLayer.getTitle(), ":" );
            for (ILayer l : server.map.layers) {
                if (l.label.get().equals( layerName )) {
                    layer = l;
                    break;
                }
            }
            if (layer == null) {
                throw new RuntimeException( "No such layer for title: " + mapLayer.getTitle() );
            }
            
            try {
                Pipeline pipeline = server.getOrCreatePipeline( layer, EncodedImageProducer.class );
                ProcessorRequest request = prepareProcessorRequest( mapContent );
                server.createPipelineExecutor().execute( pipeline, request, new ResponseHandler() {
                    @Override
                    public void handle( ProcessorResponse pipeResponse ) throws Exception {
                        HttpServletResponse response = GeoServerServlet.response.get();
                        if (pipeResponse == EncodedImageResponse.NOT_MODIFIED) {
                            log.info( "Response: 304!" );
                            response.setStatus( 304 );
                        }
                        else {
                            long lastModified = ((EncodedImageResponse)pipeResponse).getLastModified();
                            // allow caches and browser clients to cache for 1h
                            //response.setHeader( "Cache-Control", "public,max-age=3600" );
                            if (lastModified > 0) {
                                response.setHeader( "Cache-Control", "no-cache,must-revalidate" );
                                response.setDateHeader( "Last-Modified", lastModified );
                            }
                            else {
                                response.setHeader( "Cache-Control", "no-cache,must-revalidate" );
                                response.setDateHeader( "Expires", 0 );
                            }

                            byte[] chunk = ((EncodedImageResponse)pipeResponse).getChunk();
                            int len = ((EncodedImageResponse)pipeResponse).getChunkSize();
                            out.write( chunk, 0, len );
                        }
                    }
                });
                log.debug( "    flushing response stream. (" + timer.elapsedTime() + "ms)" );
                out.flush();
            }
            catch (IOException e) {
                throw e;
            }
            catch (Exception e) {
                throw new IOException( e );
            }
        }
        
        // multiple layers -> render into one image
        else {
            // FIXME multiple layers
            throw new RuntimeException( "Multiple layers are not supported yet." );
            
//            List<Job> jobs = new ArrayList();
//            final Map<MapLayer, Image> images = new HashMap();
//            
//            // run jobs for all layers
//            for (final MapLayer mapLayer : mapContent.getLayers()) {
//                final ILayer layer = loader.findLayer( mapLayer );
//                // job
//                UIJob job = new UIJob( getClass().getSimpleName() + ": " + layer.getLabel() ) {
//                    protected void runWithException( IProgressMonitor monitor )
//                    throws Exception {
//                        try {
//                            Pipeline pipeline = loader.getOrCreatePipeline( layer, LayerUseCase.IMAGE );
//
//                            GetMapRequest targetRequest = prepareProcessorRequest();
//                            pipeline.process( targetRequest, new ResponseHandler() {
//                                @Override
//                                public void handle( ProcessorResponse pipeResponse ) throws Exception {
//                                    Image layerImage = ((ImageResponse)pipeResponse).getImage();
//                                    images.put( mapLayer, layerImage );
//                                }
//                            });
//                        }
//                        catch (Exception e) {
//                            // XXX put a special image in the map
//                            log.warn( "", e );
//                            images.put( mapLayer, null );
//                            throw e;
//                        }
//                    }
//                };
//                job.schedule();
//                jobs.add( job );
//            }
//
//            // join jobs
//            for (Job job : jobs) {
//                try {
//                    job.join();
//                } catch (InterruptedException e) {
//                    // XXX put a special image in the map
//                    log.warn( "", e );
//                }
//            }
//                    
//            // put images together (MapContext order)
//            Graphics2D g = null;
//            try {
//                // result image
//                BufferedImage result = ImageUtils.createImage( mapContent.getMapWidth(), mapContent.getMapHeight(), null, true );
//                g = result.createGraphics();
//
//                // rendering hints
//                RenderingHints hints = new RenderingHints(
//                        RenderingHints.KEY_RENDERING,
//                        RenderingHints.VALUE_RENDER_QUALITY );
//                hints.add( new RenderingHints(
//                        RenderingHints.KEY_ANTIALIASING,
//                        RenderingHints.VALUE_ANTIALIAS_ON ) );
//                hints.add( new RenderingHints(
//                        RenderingHints.KEY_TEXT_ANTIALIASING,
//                        RenderingHints.VALUE_TEXT_ANTIALIAS_ON ) );
//                g.setRenderingHints( hints );
//
//                for (MapLayer mapLayer : mapContent.getLayers()) {
//                    Image layerImage = images.get( mapLayer );
//
//                    // load image data
////                  new javax.swing.ImageIcon( image ).getImage();
//
//                    ILayer layer = loader.findLayer( mapLayer );
//                    int rule = AlphaComposite.SRC_OVER;
//                    float alpha = ((float)layer.getOpacity()) / 100;
//
//                    g.setComposite( AlphaComposite.getInstance( rule, alpha ) );
//                    g.drawImage( layerImage, 0, 0, null );
//                }
//                encodeImage( result, out );
//            }
//            finally {
//                if (g != null) { g.dispose(); }
//            }
        }
    }


    /**
     * Create a {@link GetMapRequest} for the MapContext.
     * @param mapContent 
     * @throws FactoryException 
     */
    protected GetMapRequest prepareProcessorRequest( WMSMapContent mapContent ) throws FactoryException {
        // FIXME we need a long from the header
        String modifiedSince = mapContent.getRequest().getHttpRequestHeader( "If-Modified-Since" );
        log.info( "Request: If-Modified-Since: " + modifiedSince );
    
        GetMapRequest request = new GetMapRequest( 
                null, //layers 
                "EPSG:" + CRS.lookupEpsgCode( mapContent.getCoordinateReferenceSystem(), false ),
                mapContent.getRenderingArea(), 
                StringUtils.defaultIfEmpty( mapContent.getRequest().getFormat(), MIME_TYPE ), 
                mapContent.getMapWidth(), 
                mapContent.getMapHeight(),
                0L /*Date.valueOf( modifiedSince )*/ );
        return request;
    }
    
    
//    protected void encodeImage( BufferedImage _image, OutputStream out ) throws WmsException, IOException {
//        Timer timer = new Timer();
//        
//        // use GeoServer code to encode result image
//        String requestFormat = mapContent.getRequest().getFormat();
//        log.debug( "encodeImage(): request format= " + requestFormat );
//
//        if (requestFormat == null || requestFormat.equals( "image/png" )) {
//            PNGMapProducer producer = new PNGMapProducer( wms );
//            producer.setMapContext( mapContent );
//            producer.formatImageOutputStream( _image, out );
//        }
//        else if (requestFormat.equals( "image/gif" )) {
//            GIFMapProducer producer = new GIFMapProducer( wms );
//            producer.setMapContext( mapContent );
//            producer.formatImageOutputStream( _image, out );
//        }
//        else if (requestFormat.equals( "image/jpeg" )) {
//            JPEGMapProducer producer = new JPEGMapProducer( wms );
//            producer.setMapContext( mapContent );
//            producer.formatImageOutputStream( _image, out );
//        }
//        log.debug( "    done. (" + timer.elapsedTime() + "ms)" );
//    }
    
}
