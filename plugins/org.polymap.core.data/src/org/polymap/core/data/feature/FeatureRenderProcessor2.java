/* 
 * polymap.org
 * Copyright (C) 2009-2016, Polymap GmbH. All rights reserved.
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
package org.polymap.core.data.feature;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import static java.awt.image.BufferedImage.TYPE_4BYTE_ABGR;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.data.FeatureSource;
import org.geotools.filter.function.EnvFunction;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.FeatureLayer;
import org.geotools.map.MapContent;
import org.geotools.renderer.RenderListener;
import org.geotools.renderer.lite.NoThreadStreamingRenderer;
import org.geotools.renderer.lite.RendererUtilities;
import org.geotools.renderer.lite.StreamingRenderer;
import org.geotools.styling.Style;
import org.opengis.feature.simple.SimpleFeature;
import org.polymap.core.data.PipelineFeatureSource;
import org.polymap.core.data.image.GetLegendGraphicRequest;
import org.polymap.core.data.image.GetMapRequest;
import org.polymap.core.data.image.ImageProducer;
import org.polymap.core.data.image.ImageResponse;
import org.polymap.core.data.pipeline.DataSourceDescription;
import org.polymap.core.data.pipeline.Pipeline;
import org.polymap.core.data.pipeline.PipelineExecutor.ProcessorContext;
import org.polymap.core.data.pipeline.PipelineIncubator;
import org.polymap.core.data.pipeline.PipelineProcessorSite;
import org.polymap.core.data.pipeline.TerminalPipelineProcessor;
import org.polymap.core.runtime.CachedLazyInit;
import org.polymap.core.runtime.Lazy;

/**
 * This processor renders features using the geotools {@link StreamingRenderer}. The
 * features are fetched through a sub pipeline for usecase {@link FeaturesProducer}.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class FeatureRenderProcessor2
        implements TerminalPipelineProcessor, ImageProducer {

    private static final Log log = LogFactory.getLog( FeatureRenderProcessor2.class );

    /** Site property key to retrieve {@link #style}. */
    public static final String          STYLE_SUPPLIER = "styleSupplier";
    
    private PipelineProcessorSite       site;
    
    private Lazy<Pipeline>              pipeline;

    private Lazy<FeatureSource>         fs;

    private Supplier<Style>             style;  // = new CachedLazyInit( () -> new DefaultStyles().findStyle( fs.get() ) );

    
    @Override
    public void init( @SuppressWarnings("hiding") PipelineProcessorSite site ) throws Exception {
        this.site = site;
        
        // styleSupplier
        style = site.getProperty( STYLE_SUPPLIER );
        if (style == null) {
            log.warn( "No style for resource: " + site.dsd.get().resourceName.get() );
            style = () -> DefaultStyles.findStyle( fs.get() );
        }
        
        // pipeline
        this.pipeline = new CachedLazyInit( () -> {
            try {
                PipelineIncubator incubator = site.incubator.get();
                DataSourceDescription dsd = new DataSourceDescription( site.dsd.get() );
                return incubator.newPipeline( FeaturesProducer.class, dsd, null );
            }
            catch (Exception e) {
                throw new RuntimeException( e );
            }            
        });
        
        // fs
        this.fs = new CachedLazyInit( () -> {
            return new PipelineFeatureSource( pipeline.get() );
        });
    }


    @Override
    public boolean isCompatible( DataSourceDescription dsd ) {
        // we are compatible to everything a feature pipeline can be build for
        if (new DataSourceProcessor().isCompatible( dsd )) {
            return true;
        }
        return false;
    }


    @Override
    public void getMapRequest( GetMapRequest request, ProcessorContext context ) throws Exception {
        long start = System.currentTimeMillis();

        // result
        BufferedImage result = new BufferedImage( request.getWidth(), request.getHeight(), TYPE_4BYTE_ABGR );
        result.setAccelerationPriority( 1 );
        final Graphics2D g = result.createGraphics();
        //      log.info( "IMAGE: accelerated=" + result.getCapabilities( g.getDeviceConfiguration() ).isAccelerated() );

        MapContent mapContent = new MapContent();
        try {
            // MapContent
            mapContent.getViewport().setCoordinateReferenceSystem( request.getBoundingBox().getCoordinateReferenceSystem() );
            mapContent.addLayer( new FeatureLayer( fs.get(), style.get() ) );

            StreamingRenderer renderer = new NoThreadStreamingRenderer();

            // error handler
            renderer.addRenderListener( new RenderListener() {
                @Override
                public void featureRenderer( SimpleFeature feature ) {
                }
                @Override
                public void errorOccurred( Exception e ) {
                    if (e.getMessage().contains( "Error transforming bbox" )
                            || e.getMessage().contains( "too close to a pole" )) {
                        log.warn( "Renderer: " + e.getMessage() );
                    }
                    else {
                        log.error( "Renderer error: ", e );
                        drawErrorMsg( g, "Unable to render.", e );
                    }
                }
            });

            // rendering hints
            RenderingHints hints = new RenderingHints(
                    RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_SPEED );
            hints.add( new RenderingHints(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON ) );
            hints.add( new RenderingHints(
                    RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON ) );

            // geoserver compatibility to support *env* in SLD functions
            double scale = RendererUtilities.calculateOGCScale( request.getBoundingBox(), request.getWidth(), Collections.EMPTY_MAP);
            EnvFunction.setLocalValue( "wms_scale_denominator", scale );
            
            renderer.setJava2DHints( hints );
            // g.setRenderingHints( hints );

            // render params
            Map rendererParams = new HashMap();
            rendererParams.put( "optimizedDataLoadingEnabled", Boolean.TRUE );
            renderer.setRendererHints( rendererParams );

            renderer.setMapContent( mapContent );
            Rectangle paintArea = new Rectangle( request.getWidth(), request.getHeight() );
            renderer.paint( g, paintArea, request.getBoundingBox() );
        }
        catch (Throwable e) {
            log.error( "Renderer error: ", e );
            drawErrorMsg( g, "Unable to render.", e );
        }
        finally {
            mapContent.dispose();
            EnvFunction.clearLocalValues();
            if (g != null) { g.dispose(); }
        }
        log.debug( "   ...done: (" + (System.currentTimeMillis()-start) + "ms)." );

        context.sendResponse( new ImageResponse( result ) );
    }


    @Override
    public void getLegendGraphicRequest( GetLegendGraphicRequest request, ProcessorContext context ) throws Exception {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    @Override
    public void getBoundsRequest( GetBoundsRequest request, ProcessorContext context ) throws Exception {
        ReferencedEnvelope result = request.query.isPresent()
                ? fs.get().getBounds( request.query.get() )
                : fs.get().getBounds();
        context.sendResponse( new GetBoundsResponse( result ) );
    }


    protected void drawErrorMsg( Graphics2D g, String msg, Throwable e ) {
        g.setColor( Color.RED );
        g.setStroke( new BasicStroke( 1 ) );
        Font font = g.getFont().deriveFont( Font.PLAIN, 10 );
        g.setFont( font );
        if (msg != null) {
            g.drawString( msg, 0, 0 );
        }
        if (e != null) {
            g.drawString( e.getMessage(), 0, 20 );
        }
    }


    /**
     * The pipeline used by this processor. This is created on demand and cached.
     */
    public Pipeline pipeline() {
        return pipeline.get();
    }
    
}
