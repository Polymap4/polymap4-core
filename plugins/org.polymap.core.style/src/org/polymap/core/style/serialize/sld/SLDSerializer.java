/*
 * polymap.org Copyright (C) 2016-2017, the @authors. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 3.0 of the License, or (at your option) any later
 * version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */
package org.polymap.core.style.serialize.sld;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.awt.Color;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.styling.*;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.expression.Literal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Lists;

import org.polymap.core.data.DataPlugin;
import org.polymap.core.style.model.FeatureStyle;
import org.polymap.core.style.model.StyleGroup;
import org.polymap.core.style.model.StylePropertyValue;
import org.polymap.core.style.model.feature.LineStyle;
import org.polymap.core.style.model.feature.PointStyle;
import org.polymap.core.style.model.feature.PolygonStyle;
import org.polymap.core.style.model.feature.TextStyle;
import org.polymap.core.style.model.raster.ConstantRasterColorMap;
import org.polymap.core.style.model.raster.RasterColorMapStyle;
import org.polymap.core.style.model.raster.RasterGrayStyle;
import org.polymap.core.style.model.raster.RasterRGBStyle;
import org.polymap.core.style.serialize.FeatureStyleSerializer;
import org.polymap.core.style.serialize.sld.feature.FillDescriptor;
import org.polymap.core.style.serialize.sld.feature.HaloDescriptor;
import org.polymap.core.style.serialize.sld.feature.LabelPlacementDescriptor;
import org.polymap.core.style.serialize.sld.feature.LineSymbolizerDescriptor;
import org.polymap.core.style.serialize.sld.feature.PointSymbolizerDescriptor;
import org.polymap.core.style.serialize.sld.feature.PolygonSymbolizerDescriptor;
import org.polymap.core.style.serialize.sld.feature.StrokeDescriptor;
import org.polymap.core.style.serialize.sld.feature.TextSymbolizerDescriptor;
import org.polymap.core.style.serialize.sld.raster.ColorMapSymbolizerDescriptor;
import org.polymap.core.style.serialize.sld.raster.GrayscaleSymbolizerDescriptor;
import org.polymap.core.style.serialize.sld.raster.RGBSymbolizerDescriptor;
import org.polymap.core.style.serialize.sld.raster.ColorMapSymbolizerDescriptor.DummyExpression;

/**
 * Creates {@link org.geotools.styling.Style} out of a {@link FeatureStyle}
 * description.
 * <p/>
 * <b>Transform the resulting Style into SLD:</b>
 * <pre>
 *   {@link SLDTransformer} styleTransform = new {@link SLDTransformer}();
 *   String xml = styleTransform.transform( style );
 * </pre>
 * 
 * @author Falko Bräutigam
 * @author Steffen Stundzig
 */
public class SLDSerializer
        extends FeatureStyleSerializer<Style> {

    private static final Log log = LogFactory.getLog( SLDSerializer.class );

    public static final StyleFactory sf = CommonFactoryFinder.getStyleFactory( null );

    public static final FilterFactory2 ff = DataPlugin.ff; //;CommonFactoryFinder.getFilterFactory2( null );


    /**
     * Creates {@link org.geotools.styling.Style} in 3 steps:
     * <ol>
     * <li>gather scale and filter descriptions from {@link StyleGroup} hierarchy
     * </li>
     * 
     * <li>transform {@link StylePropertyValue} descriptors into a flat list of
     * {@link SymbolizerDescriptor} instances ("Ausmultiplizieren")</li>
     * 
     * <li>transform into {@link FeatureTypeStyle} and {@link Rule} instances</li>
     * </ol>
     */
    @Override
    public Style serialize( Context context ) {
        FeatureStyle featureStyle = context.featureStyle.get();

        // 1: gather scale and filter descriptions from StyleGroup hierarchy
        // XXX ...

        // 2: create flat list of SymbolizerDescriptor instances per style
        Map<org.polymap.core.style.model.Style,List<SymbolizerDescriptor>> descriptors = new HashMap();
        for (org.polymap.core.style.model.Style style : featureStyle.members()) {
            if (style.active.get()) {
                StyleSerializer serializer = null;
                if (style instanceof PointStyle) {
                    serializer = new PointSymbolizerDescriptor.Serializer( context );
                }
                else if (style instanceof PolygonStyle) {
                    serializer = new PolygonSymbolizerDescriptor.Serializer( context );
                }
                else if (style instanceof TextStyle) {
                    serializer = new TextSymbolizerDescriptor.Serializer( context );
                }
                else if (style instanceof LineStyle) {
                    serializer = new LineSymbolizerDescriptor.Serializer( context );
                }
                else if (style instanceof RasterGrayStyle) {
                    serializer = new GrayscaleSymbolizerDescriptor.Serializer( context );
                }
                else if (style instanceof RasterRGBStyle) {
                    serializer = new RGBSymbolizerDescriptor.Serializer( context );
                }
                else if (style instanceof RasterColorMapStyle) {
                    serializer = new ColorMapSymbolizerDescriptor.Serializer( context );
                }
                else {
                    throw new RuntimeException( "Unhandled Style type: " + style.getClass().getName() );
                }
                descriptors.put( style, serializer.serialize( style ) );
            }
        }
        
        // 3: transform into FeatureTypeStyle and Rule instances
        // XXX build a FTS regarding SymbolizerDescriptor#zIndex
        Style result = sf.createStyle();
        for (org.polymap.core.style.model.Style style : descriptors.keySet()) {
            FeatureTypeStyle fts = sf.createFeatureTypeStyle();
            fts.setName( style.title.get() );
            fts.getDescription().setAbstract( style.description.get() );

            for (SymbolizerDescriptor descriptor : descriptors.get( style )) {
                fts.rules().add( createRule( descriptor ) );
            }
            result.featureTypeStyles().add( fts );
        }
        
        return result;
    }


    protected Rule createRule( SymbolizerDescriptor descriptor ) {
        Rule rule = sf.createRule();

        if (descriptor instanceof LineSymbolizerDescriptor) {
            rule.symbolizers().addAll( buildLineStyle( (LineSymbolizerDescriptor)descriptor ) );
        }
        else if (descriptor instanceof PointSymbolizerDescriptor) {
            rule.symbolizers().add( buildPointStyle( (PointSymbolizerDescriptor)descriptor ) );
        }
        else if (descriptor instanceof PolygonSymbolizerDescriptor) {
            rule.symbolizers().add( buildPolygonStyle( (PolygonSymbolizerDescriptor)descriptor ) );
        }
        else if (descriptor instanceof TextSymbolizerDescriptor) {
            rule.symbolizers().add( buildTextStyle( (TextSymbolizerDescriptor)descriptor ) );
        }
        else if (descriptor instanceof GrayscaleSymbolizerDescriptor) {
            rule.symbolizers().add( buildGrayscaleRasterStyle( (GrayscaleSymbolizerDescriptor)descriptor ) );
        }
        else if (descriptor instanceof RGBSymbolizerDescriptor) {
            rule.symbolizers().add( buildRGBRasterStyle( (RGBSymbolizerDescriptor)descriptor ) );
        }
        else if (descriptor instanceof ColorMapSymbolizerDescriptor) {
            rule.symbolizers().add( buildColorMapRasterStyle( (ColorMapSymbolizerDescriptor)descriptor ) );
        }
        else {
            throw new RuntimeException( "Unhandled SymbolizerDescriptor type: " + descriptor.getClass().getName() );
        }

        descriptor.filter.ifPresent( filter -> 
                rule.setFilter( filter ) );

        descriptor.scale.ifPresent( scale -> {
            if (scale.getLeft() != null) {
                rule.setMinScaleDenominator( scale.getLeft() );
            }
            if (scale.getRight() != null) {
                rule.setMaxScaleDenominator( scale.getRight() );
            }
        });
        return rule;
    }


    protected List<LineSymbolizer> buildLineStyle( LineSymbolizerDescriptor descriptor ) {
        List<LineSymbolizer> lines = Lists.newArrayList();
        Stroke fill = buildStroke( descriptor.fill.get() );
        Stroke stroke = buildStroke( descriptor.stroke.get() );
        if (stroke != null) {
            if (fill != null) {
                stroke.setWidth( ff.add( fill.getWidth(), ff.multiply( ff.literal( 2 ), stroke.getWidth() ) ) );
            }
            lines.add( sf.createLineSymbolizer( stroke, null ) );
        }
        if (fill != null) {
            lines.add( sf.createLineSymbolizer( fill, null ) );
        }
        return lines;
    }


    protected PointSymbolizer buildPointStyle( PointSymbolizerDescriptor descriptor ) {
        Graphic gr = sf.createDefaultGraphic();

        Mark mark = sf.getCircleMark();
        mark.setStroke( buildStroke( descriptor.stroke.get() ) );

        mark.setFill( buildFill( descriptor.fill.get() ) );

        gr.graphicalSymbols().clear();
        gr.graphicalSymbols().add( mark );
        gr.setSize( descriptor.diameter.get() );
        return sf.createPointSymbolizer( gr, null );
    }


    protected PolygonSymbolizer buildPolygonStyle( PolygonSymbolizerDescriptor descriptor ) {
        Stroke stroke = buildStroke( descriptor.stroke.get() );
        Fill fill = buildFill( descriptor.fill.get() );
        return sf.createPolygonSymbolizer( stroke, fill, null );
    }


    protected TextSymbolizer buildTextStyle( TextSymbolizerDescriptor descriptor ) {
        Fill foreground = null;
        if (descriptor.color.isPresent()) {
            foreground = sf.createFill( descriptor.color.get() );
            if (descriptor.opacity.isPresent()) {
                foreground.setOpacity( descriptor.opacity.get() );
            }
        }
        Halo background = buildHalo( descriptor.halo.get() );
        LabelPlacement placement = buildLabelPlacement( descriptor.labelPlacement.get() );
        Font[] fonts = null;
        if (descriptor.font.get().fonts.isPresent()) {
            fonts = descriptor.font.get().fonts.get();
        }
        return sf.createTextSymbolizer( foreground, fonts, background, descriptor.text.get(), placement, null );
    }

    
    protected RasterSymbolizer buildGrayscaleRasterStyle( GrayscaleSymbolizerDescriptor descriptor ) {
        RasterSymbolizer result = sf.createRasterSymbolizer();
        String band = ((Literal)descriptor.grayBand.get()).getValue().toString();
        result.setChannelSelection( sf.channelSelection( 
                sf.createSelectedChannelType( band, sf.createContrastEnhancement() ) ) );
        return result;
    }

    
    protected RasterSymbolizer buildRGBRasterStyle( RGBSymbolizerDescriptor descriptor ) {
        RasterSymbolizer result = sf.createRasterSymbolizer();
        String redBand = ((Literal)descriptor.redBand.get()).getValue().toString();
        String greenBand = ((Literal)descriptor.greenBand.get()).getValue().toString();
        String blueBand = ((Literal)descriptor.blueBand.get()).getValue().toString();
        
        result.setChannelSelection( sf.channelSelection( 
                sf.createSelectedChannelType( redBand, sf.createContrastEnhancement() ),
                sf.createSelectedChannelType( greenBand, sf.createContrastEnhancement() ),
                sf.createSelectedChannelType( blueBand, sf.createContrastEnhancement() ) ) );
        return result;
    }

    
    protected RasterSymbolizer buildColorMapRasterStyle( ColorMapSymbolizerDescriptor descriptor ) {
        RasterSymbolizer result = sf.createRasterSymbolizer();
        
        ColorMapImpl colormap = new ColorMapImpl();
        colormap.setType( ColorMap.TYPE_RAMP );
        for (ConstantRasterColorMap.Entry entry : ((DummyExpression)descriptor.colorMap.get()).entries) {
            ColorMapEntryImpl newEntry = new ColorMapEntryImpl();
            Color color = new Color( entry.r.get(), entry.g.get(), entry.b.get() );
            newEntry.setColor( ff.literal( color ));
            newEntry.setQuantity( ff.literal( entry.value.get() ) );
            colormap.addColorMapEntry( newEntry );
        }
        
        //ColorMap colormap = sf.colorMap( ff.literal( "Rasterdata" ) );
        
        result.setColorMap( colormap );
        return result;
    }


    protected LabelPlacement buildLabelPlacement( LabelPlacementDescriptor lpd ) {
        if (lpd != null && ((lpd.anchorPointX.isPresent() && lpd.anchorPointY.isPresent())
                || (lpd.displacementX.isPresent() && lpd.displacementY.isPresent()) || lpd.rotation.isPresent())) {
            AnchorPoint anchorPoint = null;
            if (lpd.anchorPointX.isPresent() && lpd.anchorPointY.isPresent()) {
                anchorPoint = sf.createAnchorPoint( lpd.anchorPointX.get(), lpd.anchorPointY.get() );
            }
            Displacement displacement = null;
            if (lpd.displacementX.isPresent() && lpd.displacementY.isPresent()) {
                displacement = sf.createDisplacement( lpd.displacementX.get(), lpd.displacementY.get() );
            }
            // XXX decide if we have a label placement or a line placement
            return sf.createPointPlacement( anchorPoint, displacement, lpd.rotation.get() );
            // sf.createLinePlacement( lpd.offset.get() );
        }
        return null;
    }


    protected Halo buildHalo( final HaloDescriptor hd ) {
        // check if one of the expressions is present
        if (hd != null && (hd.color.isPresent() || hd.width.isPresent() || hd.opacity.isPresent())) {
            Halo halo = sf.createHalo( sf.createFill( hd.color.get() ),
                    hd.width.isPresent() ? hd.width.get() : ff.literal( 2.0 ) );
            if (hd.opacity.isPresent()) {
                halo.getFill().setOpacity( hd.opacity.get() );
            }
            return halo;
        }
        return null;
    }


    protected Stroke buildStroke( final StrokeDescriptor sd ) {
        if (sd != null && (sd.color.isPresent() || sd.width.isPresent() || sd.opacity.isPresent())) {
            Stroke stroke = sf.createStroke( sd.color.get(), sd.width.get(), sd.opacity.get() );

            if (sd.strokeStyle.isPresent()) {
                stroke.setLineJoin( sd.strokeStyle.get().joinStyle.get() );
                stroke.setLineCap( sd.strokeStyle.get().capStyle.get() );
                if (sd.strokeStyle.get().dashStyle.get() != null) {
                    stroke.setDashArray( sd.strokeStyle.get().dashStyle.get() );
                    stroke.setDashOffset( ff.literal( 0 ) );
                }
            }
            return stroke;
        }
        return null;
    }


    protected Fill buildFill( final FillDescriptor fd ) {
        if (fd != null && (fd.color.isPresent() || fd.opacity.isPresent())) {
            return sf.createFill( fd.color.get(), fd.opacity.get() );
        }
        return null;
    }
    
}
