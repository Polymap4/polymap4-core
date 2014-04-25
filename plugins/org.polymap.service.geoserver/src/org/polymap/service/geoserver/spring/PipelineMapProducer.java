/* 
 * polymap.org
 * Copyright 2009,2012 Polymap GmbH. All rights reserved.
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
package org.polymap.service.geoserver.spring;

import static org.apache.commons.lang.StringUtils.defaultIfEmpty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import org.vfny.geoserver.wms.GetMapProducer;
import org.vfny.geoserver.wms.WmsException;
import org.vfny.geoserver.wms.responses.AbstractRasterMapProducer;
import org.vfny.geoserver.wms.responses.ImageUtils;
import org.vfny.geoserver.wms.responses.map.gif.GIFMapProducer;
import org.vfny.geoserver.wms.responses.map.jpeg.JPEGMapProducer;
import org.vfny.geoserver.wms.responses.map.png.PNGMapProducer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.opengis.referencing.FactoryException;

import org.geotools.map.MapLayer;
import org.geotools.referencing.CRS;

import org.geoserver.platform.ServiceException;
import org.geoserver.wms.WMS;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;

import org.polymap.core.data.image.EncodedImageResponse;
import org.polymap.core.data.image.GetMapRequest;
import org.polymap.core.data.image.ImageResponse;
import org.polymap.core.data.pipeline.Pipeline;
import org.polymap.core.data.pipeline.ProcessorRequest;
import org.polymap.core.data.pipeline.ProcessorResponse;
import org.polymap.core.data.pipeline.ResponseHandler;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.LayerUseCase;
import org.polymap.core.runtime.Timer;
import org.polymap.core.runtime.UIJob;
import org.polymap.service.geoserver.GeoServerWms;

/**
 * This {@link GetMapProducer} allows to use the pipelines rendering of POLYMAP
 * via GeoServer.
 * <p>
 * This producer differs from the normal GeoServer {@link GetMapProducer} in that
 * it does not actual render anything but delegates the rendering to the POLYMAP
 * pipeline. Therefore we cannot share the code from GeoServer here.
 * 
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @since 3.0
 */
public class PipelineMapProducer
        extends AbstractRasterMapProducer {

    private static final Log log = LogFactory.getLog( PipelineMapProducer.class );

    /** the only MIME type this map producer supports */
    static final String         MIME_TYPE = "image/png";

    static final String[]       OUTPUT_FORMATS = {MIME_TYPE, "image/jpeg", "image/gif" };

    private WMS                 wms;
    
    private GeoServerLoader     loader;

    
    public PipelineMapProducer( WMS wms, GeoServerLoader loader ) {
        super( MIME_TYPE, OUTPUT_FORMATS );
        log.debug( "INIT: " + wms.getServiceInfo().getId() );
        this.wms = wms;
        this.loader = loader;
    }


    public void produceMap()
            throws WmsException {
        // do nothing
    }


    public void writeTo( final OutputStream out ) throws ServiceException, IOException {
        Timer timer = new Timer();
        
        // single layer? -> request ENCODED_IMAGE
        if (mapContext.getLayerCount() == 1) {
            MapLayer mapLayer = mapContext.getLayers()[0];
            ILayer layer = loader.findLayer( mapLayer );
            try {
                Pipeline pipeline = loader.getOrCreatePipeline( layer, LayerUseCase.ENCODED_IMAGE );

                ProcessorRequest request = prepareProcessorRequest(); 
                pipeline.process( request, new ResponseHandler() {
                    public void handle( ProcessorResponse pipeResponse ) throws Exception {
                        
                        HttpServletResponse response = GeoServerWms.response.get();
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
        
        // several layers -> render into one image
        else {
            List<Job> jobs = new ArrayList();
            final Map<MapLayer, Image> images = new HashMap();
            
            // run jobs for all layers
            for (final MapLayer mapLayer : mapContext.getLayers()) {
                final ILayer layer = loader.findLayer( mapLayer );
                // job
                UIJob job = new UIJob( getClass().getSimpleName() + ": " + layer.getLabel() ) {
                    protected void runWithException( IProgressMonitor monitor )
                    throws Exception {
                        try {
                            Pipeline pipeline = loader.getOrCreatePipeline( layer, LayerUseCase.IMAGE );

                            GetMapRequest targetRequest = prepareProcessorRequest();
                            pipeline.process( targetRequest, new ResponseHandler() {
                                public void handle( ProcessorResponse pipeResponse )
                                throws Exception {
                                    Image layerImage = ((ImageResponse)pipeResponse).getImage();
                                    images.put( mapLayer, layerImage );
                                }
                            });
                        }
                        catch (Exception e) {
                            // XXX put a special image in the map
                            log.warn( "", e );
                            images.put( mapLayer, null );
                            throw e;
                        }
                    }
                };
                job.schedule();
                jobs.add( job );
            }

            // join jobs
            for (Job job : jobs) {
                try {
                    job.join();
                } catch (InterruptedException e) {
                    // XXX put a special image in the map
                    log.warn( "", e );
                }
            }
                    
            // put images together (MapContext order)
            Graphics2D g = null;
            try {
                // result image
                BufferedImage result = ImageUtils.createImage( mapContext.getMapWidth(), mapContext.getMapHeight(), null, true );
                g = result.createGraphics();

                // rendering hints
                RenderingHints hints = new RenderingHints(
                        RenderingHints.KEY_RENDERING,
                        RenderingHints.VALUE_RENDER_QUALITY );
                hints.add( new RenderingHints(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON ) );
                hints.add( new RenderingHints(
                        RenderingHints.KEY_TEXT_ANTIALIASING,
                        RenderingHints.VALUE_TEXT_ANTIALIAS_ON ) );
                g.setRenderingHints( hints );

                for (MapLayer mapLayer : mapContext.getLayers()) {
                    Image layerImage = images.get( mapLayer );

                    // load image data
//                  new javax.swing.ImageIcon( image ).getImage();

                    ILayer layer = loader.findLayer( mapLayer );
                    int rule = AlphaComposite.SRC_OVER;
                    float alpha = ((float)layer.getOpacity()) / 100;

                    g.setComposite( AlphaComposite.getInstance( rule, alpha ) );
                    g.drawImage( layerImage, 0, 0, null );
                }
                encodeImage( result, out );
            }
            finally {
                if (g != null) { g.dispose(); }
            }
        }
    }


    public void formatImageOutputStream( RenderedImage _image, OutputStream outStream )
            throws WmsException, IOException {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    /**
     * Create a {@link GetMapRequest} for the MapContext.
     * @throws FactoryException 
     */
    protected GetMapRequest prepareProcessorRequest() 
    throws FactoryException {
        long modifiedSince = mapContext.getRequest().getHttpServletRequest().getDateHeader( "If-Modified-Since" );
        log.debug( "Request: If-Modified-Since: " + modifiedSince );
    
        GetMapRequest request = new GetMapRequest( 
                null, //layers 
                "EPSG:" + CRS.lookupEpsgCode( mapContext.getCoordinateReferenceSystem(), false ),
                mapContext.getAreaOfInterest(), 
                defaultIfEmpty( mapContext.getRequest().getFormat(), MIME_TYPE ), 
                mapContext.getMapWidth(), 
                mapContext.getMapHeight(),
                modifiedSince );
        return request;
    }
    
    
    protected void encodeImage( BufferedImage _image, OutputStream out ) 
    throws WmsException, IOException {
        Timer timer = new Timer();
        
        // use GeoServer code to encode result image
        String requestFormat = mapContext.getRequest().getFormat();
        log.debug( "encodeImage(): request format= " + requestFormat );

        if (requestFormat == null || requestFormat.equals( "image/png" )) {
            PNGMapProducer producer = new PNGMapProducer( wms );
            producer.setMapContext( mapContext );
            producer.formatImageOutputStream( _image, out );
        }
        else if (requestFormat.equals( "image/gif" )) {
            GIFMapProducer producer = new GIFMapProducer( wms );
            producer.setMapContext( mapContext );
            producer.formatImageOutputStream( _image, out );
        }
        else if (requestFormat.equals( "image/jpeg" )) {
            JPEGMapProducer producer = new JPEGMapProducer( wms );
            producer.setMapContext( mapContext );
            producer.formatImageOutputStream( _image, out );
        }
        log.debug( "    done. (" + timer.elapsedTime() + "ms)" );
    }
    
}
