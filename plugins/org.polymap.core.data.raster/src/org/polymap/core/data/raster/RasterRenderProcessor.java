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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;

import org.geotools.coverage.GridSampleDimension;
import org.geotools.coverage.grid.GridCoverage2D;
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
import org.geotools.styling.ChannelSelection;
import org.geotools.styling.ContrastEnhancement;
import org.geotools.styling.RasterSymbolizer;
import org.geotools.styling.SLD;
import org.geotools.styling.SelectedChannelType;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.FilterFactory2;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.style.ContrastMethod;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.data.feature.GetBoundsRequest;
import org.polymap.core.data.feature.GetBoundsResponse;
import org.polymap.core.data.image.GetLegendGraphicRequest;
import org.polymap.core.data.image.GetMapRequest;
import org.polymap.core.data.image.ImageProducer;
import org.polymap.core.data.image.ImageResponse;
import org.polymap.core.data.pipeline.DataSourceDescription;
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
    
    private Style                       style;

    
    @Override
    public void init( PipelineProcessorSite site ) throws Exception {
        reader = (GridCoverage2DReader)site.dsd.get().service.get();
        coverageName = site.dsd.get().resourceName.get();

        style = createRGBStyle( reader );
        if (style == null) {
            log.warn( "Error creating RGB style, trying greyscale..." );
            style = createGreyscaleStyle( 1 );
        }
    }


    @Override
    public boolean isCompatible( DataSourceDescription dsd ) {
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
            mapContent.addLayer( new GridReaderLayer( reader, style ) );
        
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
     * This method examines the names of the sample dimensions in the provided
     * coverage looking for "red...", "green..." and "blue..." (case insensitive
     * match). If these names are not found it uses bands 1, 2, and 3 for the
     * red, green and blue channels. It then sets up a raster symbolizer and
     * returns this wrapped in a Style.
     * 
     * @return A new Style object containing a raster symbolizer set up for RGB
     *         image.
     */
    protected Style createRGBStyle( @SuppressWarnings( "hiding" ) GridCoverage2DReader reader ) {
        GridCoverage2D cov = null;
        try {
            cov = reader.read( null );
        }
        catch (IOException giveUp) {
            throw new RuntimeException( giveUp );
        }
        
        // We need at least three bands to create an RGB style
        int numBands = cov.getNumSampleDimensions();
        if (numBands < 3) {
            return null;
        }
        // Get the names of the bands
        String[] sampleDimensionNames = new String[numBands];
        for (int i = 0; i < numBands; i++) {
            GridSampleDimension dim = cov.getSampleDimension( i );
            sampleDimensionNames[i] = dim.getDescription().toString();
        }
        final int RED = 0, GREEN = 1, BLUE = 2;
        int[] channelNum = { -1, -1, -1 };
        // We examine the band names looking for "red...", "green...", "blue...".
        // Note that the channel numbers we record are indexed from 1, not 0.
        for (int i = 0; i < numBands; i++) {
            String name = sampleDimensionNames[i].toLowerCase();
            if (name != null) {
                if (name.matches( "red.*" )) {
                    channelNum[RED] = i + 1;
                }
                else if (name.matches( "green.*" )) {
                    channelNum[GREEN] = i + 1;
                }
                else if (name.matches( "blue.*" )) {
                    channelNum[BLUE] = i + 1;
                }
            }
        }
        // If we didn't find named bands "red...", "green...", "blue..."
        // we fall back to using the first three bands in order
        if (channelNum[RED] < 0 || channelNum[GREEN] < 0 || channelNum[BLUE] < 0) {
            channelNum[RED] = 1;
            channelNum[GREEN] = 2;
            channelNum[BLUE] = 3;
        }
        // Now we create a RasterSymbolizer using the selected channels
        SelectedChannelType[] sct = new SelectedChannelType[cov.getNumSampleDimensions()];
        ContrastEnhancement ce = sf.contrastEnhancement( ff.literal( 1.0 ), ContrastMethod.NORMALIZE );
        for (int i = 0; i < 3; i++) {
            sct[i] = sf.createSelectedChannelType( String.valueOf( channelNum[i] ), ce );
        }
        RasterSymbolizer sym = sf.getDefaultRasterSymbolizer();
//        ChannelSelection sel = sf.channelSelection( sct[RED], sct[GREEN], sct[BLUE] );
//        sym.setChannelSelection( sel );
    
        return SLD.wrapSymbolizers( sym );
    }


    /**
     * Create a Style to display the specified band of the GeoTIFF image as a
     * greyscale layer.
     * <p>
     * This method is a helper for createGreyScale() and is also called directly by
     * the displayLayers() method when the application first starts.
     * 
     * @param band the image band to use for the greyscale display
     * 
     * @return a new Style instance to render the image in greyscale
     */
    protected Style createGreyscaleStyle( int band ) {
        ContrastEnhancement ce = sf.contrastEnhancement( ff.literal( 1.0 ), ContrastMethod.NORMALIZE );
        SelectedChannelType sct = sf.createSelectedChannelType( String.valueOf( band ), ce );

        RasterSymbolizer sym = sf.getDefaultRasterSymbolizer();
        ChannelSelection sel = sf.channelSelection( sct );
        sym.setChannelSelection( sel );

        return SLD.wrapSymbolizers( sym );
    }

}
