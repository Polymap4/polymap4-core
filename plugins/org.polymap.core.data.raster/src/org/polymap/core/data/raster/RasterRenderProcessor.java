/* 
 * polymap.org
 * Copyright (C) 2010-2016, Polymap GmbH. All rights reserved.
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
package org.polymap.core.data.raster;

import static java.awt.RenderingHints.KEY_ANTIALIASING;
import static java.awt.RenderingHints.KEY_RENDERING;
import static java.awt.RenderingHints.KEY_TEXT_ANTIALIASING;
import static java.awt.RenderingHints.VALUE_ANTIALIAS_ON;
import static java.awt.RenderingHints.VALUE_RENDER_QUALITY;
import static java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON;
import static java.awt.image.BufferedImage.TYPE_4BYTE_ABGR;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.coverage.grid.io.GridCoverage2DReader;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.filter.function.EnvFunction;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.GridReaderLayer;
import org.geotools.map.MapContent;
import org.geotools.renderer.RenderListener;
import org.geotools.renderer.lite.StreamingRenderer;
import org.geotools.styling.RasterSymbolizer;
import org.geotools.styling.SLD;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.FilterFactory2;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.data.feature.FeatureRenderProcessor2;
import org.polymap.core.data.feature.GetBoundsRequest;
import org.polymap.core.data.feature.GetBoundsResponse;
import org.polymap.core.data.image.GetLegendGraphicRequest;
import org.polymap.core.data.image.GetMapRequest;
import org.polymap.core.data.image.ImageProducer;
import org.polymap.core.data.image.ImageResponse;
import org.polymap.core.data.pipeline.DataSourceDescriptor;
import org.polymap.core.data.pipeline.PipelineExecutor.ProcessorContext;
import org.polymap.core.data.pipeline.PipelineProcessorSite;
import org.polymap.core.data.pipeline.TerminalPipelineProcessor;

/**
 * This processor renders raster data of {@link AbstractGridCoverage2DReader} using
 * the geotools {@link StreamingRenderer}.
 * 
 * @author Falko Bräutigam
 */
public class RasterRenderProcessor
        implements ImageProducer, TerminalPipelineProcessor {

    private static final Log log = LogFactory.getLog( RasterRenderProcessor.class );

    public static final StyleFactory    sf = CommonFactoryFinder.getStyleFactory( null );
    
    public static final FilterFactory2  ff = CommonFactoryFinder.getFilterFactory2( null );

    private GridCoverage2DReader        reader;

    private String                      coverageName;
    
    private Supplier<Style>             style;

    
    @Override
    public void init( PipelineProcessorSite site ) throws Exception {
        reader = (GridCoverage2DReader)site.dsd.get().service.get();
        coverageName = site.dsd.get().resourceName.get();
        
        // styleSupplier
        style = FeatureRenderProcessor2.STYLE_SUPPLIER.rawopt( site ).orElseGet( () -> {
            log.warn( "No style for resource: " + site.dsd.get().resourceName.get() );
            return () -> createGreyscaleStyle( 1 );
        });
    }


    @Override
    public boolean isCompatible( DataSourceDescriptor dsd ) {
        return dsd.service.get() instanceof AbstractGridCoverage2DReader;
    }


    @Override
    public void getMapRequest( GetMapRequest request, ProcessorContext context ) throws Exception {
        // result
        BufferedImage result = new BufferedImage( request.getWidth(), request.getHeight(), TYPE_4BYTE_ABGR );
        final Graphics2D g = result.createGraphics();

        MapContent mapContent = new MapContent();
        try {
            mapContent.getViewport().setCoordinateReferenceSystem( request.getBoundingBox().getCoordinateReferenceSystem() );
            mapContent.addLayer( new GridReaderLayer( reader, style.get() ) );
        
            // renderer
            StreamingRenderer renderer = new StreamingRenderer();
    
            // error handler
            renderer.addRenderListener( new RenderListener() {
                public void featureRenderer( SimpleFeature feature ) {
                }
                public void errorOccurred( Exception e ) {
                    log.error( "Renderer error: ", e );
                    drawErrorMsg( g, "Fehler bei der Darstellung.", e );
                }
            });
    
            // rendering hints
            RenderingHints hints = new RenderingHints( KEY_RENDERING, VALUE_RENDER_QUALITY );
            hints.add( new RenderingHints( KEY_ANTIALIASING, VALUE_ANTIALIAS_ON ) );
            hints.add( new RenderingHints( KEY_TEXT_ANTIALIASING, VALUE_TEXT_ANTIALIAS_ON ) );
            renderer.setJava2DHints( hints );
    
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
            drawErrorMsg( g, null, e );
        }
        finally {
            mapContent.dispose();
            EnvFunction.clearLocalValues();
            if (g != null) { g.dispose(); }
        }
        context.sendResponse( new ImageResponse( result ) );
    }


    @Override
    public void getLegendGraphicRequest( GetLegendGraphicRequest request, ProcessorContext context ) throws Exception {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    @Override
    public void getBoundsRequest( GetBoundsRequest request, ProcessorContext context ) throws Exception {
        GeneralEnvelope envelope = reader.getOriginalEnvelope();
        if (envelope != null) {
            context.sendResponse( new GetBoundsResponse( new ReferencedEnvelope( envelope ) ) );
            return;
        } 
        CoordinateReferenceSystem crs = reader.getCoordinateReferenceSystem();
        if (crs != null) {
            context.sendResponse( new GetBoundsResponse( new ReferencedEnvelope( crs ) ) );
            return;
        }
        throw new IllegalStateException( "No bounds founds." );
    }


    protected void drawErrorMsg( Graphics2D g, String msg, Throwable e ) {
        g.setColor( Color.RED );
        g.setStroke( new BasicStroke( 1 ) );
        g.getFont().deriveFont( Font.BOLD, 12 );
        if (msg != null) {
            g.drawString( msg, 10, 10 );
        }
        if (e != null) {
            g.drawString( e.toString(), 10, 30 );
        }
    }


    /**
     * Create a Style to display the specified band of the GeoTIFF image as a
     * greyscale layer.
     * 
     * @param band the image band to use for the greyscale display
     * @return a new Style instance to render the image in greyscale
     */
    protected Style createGreyscaleStyle( int band ) {
//        ContrastEnhancement ce = sf.contrastEnhancement( ff.literal( 1.0 ), ContrastMethod.NORMALIZE );
//        SelectedChannelType sct = sf.createSelectedChannelType( String.valueOf( band ), ce );

        RasterSymbolizer sym = sf.getDefaultRasterSymbolizer();
//        ChannelSelection sel = sf.channelSelection( sct );
//        sym.setChannelSelection( sel );

        return SLD.wrapSymbolizers( sym );
    }

}
