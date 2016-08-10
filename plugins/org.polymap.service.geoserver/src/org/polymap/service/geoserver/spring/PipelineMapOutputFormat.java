/* 
 * polymap.org
 * Copyright (C) 2009-2016 Polymap GmbH. All rights reserved.
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;

import org.geoserver.platform.ServiceException;
import org.geoserver.wms.GetMapOutputFormat;
import org.geoserver.wms.MapProducerCapabilities;
import org.geoserver.wms.WMS;
import org.geoserver.wms.WMSMapContent;
import org.geoserver.wms.map.AbstractMapOutputFormat;
import org.geoserver.wms.map.GIFMapResponse;
import org.geoserver.wms.map.GeoTIFFMapResponse;
import org.geoserver.wms.map.ImageUtils;
import org.geoserver.wms.map.JPEGMapResponse;
import org.geoserver.wms.map.PNGMapResponse;
import org.geoserver.wms.map.RenderedImageMap;
import org.geoserver.wms.map.RenderedImageMapOutputFormat;
import org.geoserver.wms.map.RenderedImageMapResponse;
import org.geoserver.wms.map.TIFFMapResponse;
import org.geotools.map.Layer;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;

import org.polymap.core.data.image.GetMapRequest;
import org.polymap.core.data.image.ImageProducer;
import org.polymap.core.data.image.ImageResponse;
import org.polymap.core.data.pipeline.Pipeline;
import org.polymap.core.data.pipeline.ProcessorRequest;
import org.polymap.core.data.pipeline.ProcessorResponse;
import org.polymap.core.data.pipeline.ResponseHandler;
import org.polymap.core.project.ILayer;
import org.polymap.core.runtime.Timer;
import org.polymap.core.runtime.UIJob;

import org.polymap.service.geoserver.GeoServerServlet;

/**
 * A {@link GetMapOutputFormat} that produces {@link RenderedImageMap} instances with
 * an ImageProducer pipeline to be encoded in the constructor supplied MIME-Type.
 * 
 * This {@link GetMapProducer} allows to use the pipelines rendering of POLYMAP
 * via GeoServer.
 * <p>
 * This producer differs from the normal GeoServer {@link GetMapProducer} in that
 * it does not actual render anything but delegates the rendering to the POLYMAP
 * pipeline. Therefore we cannot share the code from GeoServer here.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @author Steffen Stundzig
 * 
 * @see RenderedImageMapOutputFormat
 * @see PNGMapResponse
 * @see GIFMapResponse
 * @see TIFFMapResponse
 * @see GeoTIFFMapResponse
 * @see JPEGMapResponse
 */
public class PipelineMapOutputFormat
        extends AbstractMapOutputFormat {

    public class Result<T> {

        private BufferedImage image;


        public void set( BufferedImage image ) {
            this.image = image;
        }


        public RenderedImage get() {
            return this.image;
        }
    }

    private static final Log                          log                = LogFactory.getLog( PipelineMapOutputFormat.class );

    /** Which format to encode the image in if one is not supplied */
    private static final String                       DEFAULT_MAP_FORMAT = "image/png";

    /** WMS Service configuration * */
    private final WMS                                 wms;

    private GeoServerServlet                          server             = GeoServerServlet.instance.get();

    /**
     * The file extension (minus the .)
     */
    private String                                    extension          = null;

    /**
     * The known producer capabilities
     */
    private final Map<String,MapProducerCapabilities> capabilities       = new HashMap<String,MapProducerCapabilities>();


    /**
     * 
     */
    public PipelineMapOutputFormat( WMS wms ) {
        this( DEFAULT_MAP_FORMAT, wms );
    }


    /**
     * @param the mime type to be written down as an HTTP header when a map of this
     *        format is generated
     */
    public PipelineMapOutputFormat( String mime, WMS wms ) {
        this( mime, new String[] { mime }, wms );
    }


    /**
     * 
     * @param mime the actual MIME Type resulting for the image created using this
     *        output format
     * @param outputFormats the list of output format names to declare in the
     *        GetCapabilities document, does not need to match {@code mime} (e.g., an
     *        output format of {@code image/geotiff8} may result in a map returned
     *        with MIME Type {@code image/tiff})
     * @param wms
     */
    public PipelineMapOutputFormat( String mime, String[] outputFormats, WMS wms ) {
        super( mime, outputFormats );
        this.wms = wms;

        // the capabilities of this produce are actually linked to the map response
        // that is going to
        // be used, this class just generates a rendered image
        final Collection<RenderedImageMapResponse> responses = this.wms.getAvailableMapResponses();
        for (RenderedImageMapResponse response : responses) {
            for (String outFormat : response.getOutputFormats()) {
                if (response.getOutputFormats().contains( outFormat )) {
                    MapProducerCapabilities cap = response.getCapabilities( outFormat );
                    if (cap != null) {
                        capabilities.put( outFormat, cap );
                    }
                }
            }
        }
    }


    /**
     * Returns the extension used for the file name in the content disposition header
     * 
     * @param extension
     */
    public String getExtension() {
        return extension;
    }


    /**
     * Sets the extension used for the file name in the content disposition header
     * 
     * @param extension
     */
    public void setExtension( String extension ) {
        this.extension = extension;
    }


    public MapProducerCapabilities getCapabilities( String format ) {
        return capabilities.get( format );
    }


    /**
     * @see org.geoserver.wms.GetMapOutputFormat#produceMap(org.geoserver.wms.WMSMapContent)
     */
    public final RenderedImageMap produceMap( WMSMapContent mapContent ) throws ServiceException {
        Timer timer = new Timer();

        // single layer? -> request ENCODED_IMAGE
        if (mapContent.layers().size() == 1) {
            Layer mapLayer = mapContent.layers().get( 0 );

            ILayer layer = findLayer( mapLayer );

            try {
                Pipeline pipeline = server.getOrCreatePipeline( layer, ImageProducer.class );
                ProcessorRequest request = prepareProcessorRequest( mapContent );

                final Result<BufferedImage> c = new Result<BufferedImage>();
                server.createPipelineExecutor().execute( pipeline, request, new ResponseHandler() {

                    @Override
                    public void handle( ProcessorResponse pipeResponse ) throws Exception {
                        BufferedImage layerImage = (BufferedImage)((ImageResponse)pipeResponse).getImage();
                        c.set( layerImage );
                    }
                } );
                return buildMap( mapContent, c.get() );

            }
            catch (Exception e) {
                throw new ServiceException( e );
            }
        }

        // multiple layers -> render into one image
        else {
            List<Job> jobs = new ArrayList();
            final Map<Layer,Image> images = new HashMap();

            // run jobs for all layers
            for (final Layer mapLayer : mapContent.layers()) {
                final ILayer layer = findLayer( mapLayer );
                // job
                UIJob job = new UIJob( getClass().getSimpleName() + ": " + mapLayer.getTitle() ) {

                    protected void runWithException( IProgressMonitor monitor )
                            throws Exception {
                        try {
                            Pipeline pipeline = server.getOrCreatePipeline( layer, ImageProducer.class );
                            GetMapRequest request = prepareProcessorRequest( mapContent );
                            server.createPipelineExecutor().execute( pipeline, request, new ResponseHandler() {

                                @Override
                                public void handle( ProcessorResponse pipeResponse ) throws Exception {
                                    BufferedImage layerImage = (BufferedImage)((ImageResponse)pipeResponse).getImage();
                                    images.put( mapLayer, layerImage );
                                }
                            } );
                        }
                        catch (Exception e) {
                            // XXX put a special image in the map
                            e.printStackTrace();
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
                }
                catch (InterruptedException e) {
                    // XXX put a special image in the map
                    log.warn( "", e );
                }
            }

            // put images together (MapContext order)
            Graphics2D g = null;
            try {
                // result image
                BufferedImage result = ImageUtils.createImage( mapContent.getMapWidth(), mapContent.getMapHeight(), null, true );
                g = result.createGraphics();

                // rendering hints
                RenderingHints hints = new RenderingHints( RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY );
                hints.add( new RenderingHints( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON ) );
                hints.add( new RenderingHints( RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON ) );
                g.setRenderingHints( hints );

                for (Layer mapLayer : mapContent.layers()) {
                    Image layerImage = images.get( mapLayer );

                    // load image data
                    // new javax.swing.ImageIcon( image ).getImage();

                    // ILayer layer = findLayer( mapLayer );
                    int rule = AlphaComposite.SRC_OVER;
                    // TODO opacity
                    float alpha = 1;// ((float)layer.getOpacity()) / 100;

                    g.setComposite( AlphaComposite.getInstance( rule, alpha ) );
                    g.drawImage( layerImage, 0, 0, null );
                }
                return buildMap( mapContent, result );
            }
            finally {
                if (g != null) {
                    g.dispose();
                }
            }
        }
    }


    private ILayer findLayer( Layer mapLayer ) {
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
        return layer;
    }


    /**
     * Create a {@link GetMapRequest} for the MapContext.
     * 
     * @param mapContent
     * @throws FactoryException
     */
    protected GetMapRequest prepareProcessorRequest( WMSMapContent mapContent ) throws FactoryException {
        // FIXME we need a long from the header
        String modifiedSince = mapContent.getRequest().getHttpRequestHeader( "If-Modified-Since" );
        log.info( "Request: If-Modified-Since: " + modifiedSince );

        GetMapRequest request = new GetMapRequest( null, // layers
                "EPSG:" + CRS.lookupEpsgCode( mapContent.getCoordinateReferenceSystem(), false ), 
                mapContent.getRenderingArea(), 
                StringUtils.defaultIfEmpty( mapContent.getRequest().getFormat(), DEFAULT_MAP_FORMAT ), 
                mapContent.getMapWidth(), 
                mapContent.getMapHeight(), 
                0L /* Date.valueOf( modifiedSince )*/ );
        return request;
    }


    private Graphics2D getGraphics( final boolean transparent, final Color bgColor,
            final RenderedImage preparedImage, final Map<RenderingHints.Key,Object> hintsMap ) {
        return ImageUtils.prepareTransparency( transparent, bgColor, preparedImage, hintsMap );
    }


    private RenderedImageMap buildMap( final WMSMapContent mapContent, RenderedImage image ) {
        RenderedImageMap map = new RenderedImageMap( mapContent, image, getMimeType() );
        if (extension != null) {
            map.setContentDispositionHeader( mapContent, "." + extension, false );
        }
        return map;
    }
}
