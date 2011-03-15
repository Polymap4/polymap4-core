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
package org.polymap.core.services.geoserver.spring;

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

import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.catalog.IService;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import org.polymap.core.data.image.EncodedImageResponse;
import org.polymap.core.data.image.GetMapRequest;
import org.polymap.core.data.image.ImageResponse;
import org.polymap.core.data.pipeline.DefaultPipelineIncubator;
import org.polymap.core.data.pipeline.IPipelineIncubator;
import org.polymap.core.data.pipeline.Pipeline;
import org.polymap.core.data.pipeline.PipelineIncubationException;
import org.polymap.core.data.pipeline.ProcessorRequest;
import org.polymap.core.data.pipeline.ProcessorResponse;
import org.polymap.core.data.pipeline.ResponseHandler;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.LayerUseCase;
import org.polymap.core.services.geoserver.GeoServerPlugin;

/**
 * This {@link GetMapProducer} allows to use the pipelines rendering of POLYMAP
 * via GeoServer.
 * <p>
 * This producer differs from the normal GeoServer {@link GetMapProducer} in that
 * it does not actual render anything but delegates the rendering to the POLYMAP
 * pipeline. Therefore we cannot share the code from GeoServer here.
 * 
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
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

    private IPipelineIncubator  pipelineIncubator = new DefaultPipelineIncubator();
    
    
    public PipelineMapProducer( WMS wms, GeoServerLoader loader ) {
        super( MIME_TYPE, OUTPUT_FORMATS );
        log.debug( "INIT ***" );
        this.wms = wms;
        this.loader = loader;
    }


    public void produceMap()
            throws WmsException {
        // do nothing
    }


    public void writeTo( final OutputStream out )
            throws ServiceException, IOException {
        long start = System.currentTimeMillis();
        
        // single layer? -> request ENCODED_IMAGE
        if (mapContext.getLayerCount() == 1) {
            MapLayer mapLayer = mapContext.getLayers()[0];
            ILayer layer = findLayer( mapLayer );
            try {
                Pipeline pipeline = getOrCreatePipeline( layer, LayerUseCase.ENCODED_IMAGE );

                ProcessorRequest request = prepareProcessorRequest(); 
                pipeline.process( request, new ResponseHandler() {
                    public void handle( ProcessorResponse pipeResponse )
                            throws Exception {
                        byte[] chunk = ((EncodedImageResponse)pipeResponse).getChunk();
                        int len = ((EncodedImageResponse)pipeResponse).getChunkSize();
                        out.write( chunk, 0, len );
                    }
                });
                log.debug( "    flushing response stream. (" + (System.currentTimeMillis()-start) + "ms)" );
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
                final ILayer layer = findLayer( mapLayer );
                // job
                Job job = new Job( getClass().getSimpleName() + ": " + layer.getLabel() ) {
                    protected IStatus run( IProgressMonitor monitor ) {
                        try {
                            Pipeline pipeline = getOrCreatePipeline( layer, LayerUseCase.IMAGE );

                            GetMapRequest targetRequest = prepareProcessorRequest();
                            pipeline.process( targetRequest, new ResponseHandler() {
                                public void handle( ProcessorResponse pipeResponse )
                                throws Exception {
                                    Image layerImage = ((ImageResponse)pipeResponse).getImage();
                                    images.put( mapLayer, layerImage );
                                }
                            });
                            return Status.OK_STATUS;
                        }
                        catch (Exception e) {
                            // XXX put a special image in the map
                            log.warn( "", e );
                            images.put( mapLayer, null );
                            return new Status( Status.ERROR, GeoServerPlugin.PLUGIN_ID, "", e );
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

                    ILayer layer = findLayer( mapLayer );
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
     * Creates a new processing {@link Pipeline} for the given {@link ILayer}
     * and usecase.
     * <p>
     * XXX The result needs to be cached
     * 
     * @throws IOException 
     * @throws PipelineIncubationException 
     */
    protected Pipeline getOrCreatePipeline( ILayer layer, LayerUseCase usecase ) 
    throws IOException, PipelineIncubationException {
        IService service = findService( layer );
        Pipeline pipeline = pipelineIncubator.newPipeline( 
                usecase, layer.getMap(), layer, service );
        return pipeline;
    }


    /**
     * Find the corresponding {@link ILayer} for the given {@link MapLayer} of
     * the MapContext.
     */
    protected ILayer findLayer( MapLayer mapLayer ) {
        log.debug( "findLayer(): mapContext=" + mapContext + ", mapLayer= " + mapLayer );
        
        ILayer layer = loader.getLayer( mapLayer.getTitle() );
        
//        FeatureSource<? extends FeatureType, ? extends Feature> fs = mapLayer.getFeatureSource();
//        PipelineDataStore pds = (PipelineDataStore)fs.getDataStore();
//        ILayer layer = pds.getFeatureSource().getPipeline().getLayers().iterator().next();
        
        return layer;
    }

    
    protected IService findService( ILayer layer ) 
    throws IOException {
        IGeoResource res = layer.getGeoResource();
        if (res == null) {
            throw new ServiceException( "Unable to find geo resource of layer: " + layer );
        }
        // XXX give a reasonable monitor; check state 
        IService service = res.service( null );
        log.debug( "service: " + service );
        return service;
    }
    
    
    /**
     * Create a {@link GetMapRequest} for the MapContext.
     * @throws FactoryException 
     */
    protected GetMapRequest prepareProcessorRequest() 
    throws FactoryException {
        GetMapRequest request = new GetMapRequest( 
                null, //layers 
                "EPSG:" + CRS.lookupEpsgCode( mapContext.getCoordinateReferenceSystem(), false ),
                mapContext.getAreaOfInterest(), 
                getContentType(), 
                mapContext.getMapWidth(), 
                mapContext.getMapHeight() );
        return request;
    }
    
    
    protected void encodeImage( BufferedImage _image, OutputStream out ) 
    throws WmsException, IOException {
        long start = System.currentTimeMillis();
        
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
        log.debug( "    done. (" + (System.currentTimeMillis()-start) + "ms)" );

    }
    
}
