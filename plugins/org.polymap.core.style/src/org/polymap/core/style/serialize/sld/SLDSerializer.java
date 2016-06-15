/*
 * polymap.org Copyright (C) 2016, the @authors. All rights reserved.
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

import java.util.List;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.styling.AnchorPoint;
import org.geotools.styling.Displacement;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Fill;
import org.geotools.styling.Font;
import org.geotools.styling.Graphic;
import org.geotools.styling.Halo;
import org.geotools.styling.LabelPlacement;
import org.geotools.styling.LineSymbolizer;
import org.geotools.styling.Mark;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.SLDTransformer;
import org.geotools.styling.Stroke;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;
import org.geotools.styling.Symbolizer;
import org.opengis.filter.FilterFactory2;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Lists;

import org.polymap.core.style.model.FeatureStyle;
import org.polymap.core.style.model.LineStyle;
import org.polymap.core.style.model.PointStyle;
import org.polymap.core.style.model.PolygonStyle;
import org.polymap.core.style.model.StyleGroup;
import org.polymap.core.style.model.StylePropertyValue;
import org.polymap.core.style.model.TextStyle;
import org.polymap.core.style.serialize.FeatureStyleSerializer;

/**
 * Creates {@link org.geotools.styling.Style} out of a {@link FeatureStyle}
 * description.
 * <p/>
 * <b>Transform the resulting Style into SLD:</b>
 * 
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

    private static Log log = LogFactory.getLog( SLDSerializer.class );

    public static final StyleFactory sf = CommonFactoryFinder.getStyleFactory( null );

    public static final FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2( null );


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

        // XXX 1: gather scale and filter descriptions from StyleGroup hierarchy
        // ...

        // 2: create flat list of SymbolizerDescriptor instances per style and add a
        // feature type style for each
        final Style sld = sf.createStyle();
        for (org.polymap.core.style.model.Style style : featureStyle.members()) {
            // skip deactivated
            if (!style.active.get()) {
            }
            else {
                sld.featureTypeStyles().add( buildStyle( style, context ) );
            }
        }

        return sld;
    }


    private FeatureTypeStyle buildStyle( final org.polymap.core.style.model.Style style, final Context context ) {
        FeatureTypeStyle featureTypeStyle = sf.createFeatureTypeStyle();
        featureTypeStyle.setName( style.title.get() );
        featureTypeStyle.getDescription().setAbstract( style.description.get() );
        // featureTypeStyle.getDescription().setTitle( style.title.get() );

        for (SymbolizerDescriptor descriptor : serializeStyle( style, context )) {
            List<Symbolizer> sym = Lists.newArrayList();
            if (descriptor instanceof LineSymbolizerDescriptor) {
                sym.addAll( buildLineStyle( (LineSymbolizerDescriptor)descriptor ) );
            }
            else if (descriptor instanceof PointSymbolizerDescriptor) {
                sym.add( buildPointStyle( (PointSymbolizerDescriptor)descriptor ) );
            }
            else if (descriptor instanceof PolygonSymbolizerDescriptor) {
                sym.add( buildPolygonStyle( (PolygonSymbolizerDescriptor)descriptor ) );
            }
            else if (descriptor instanceof TextSymbolizerDescriptor) {
                sym.add( buildTextStyle( (TextSymbolizerDescriptor)descriptor ) );
            }
            else {
                throw new RuntimeException( "Unhandled SymbolizerDescriptor type: " + descriptor.getClass().getName() );
            }

            // Rule
            Rule rule = sf.createRule();
            //rule.setName( descriptor.description.get() );
            rule.symbolizers().addAll( sym );

            descriptor.filter.ifPresent( f -> rule.setFilter( f ) );
            descriptor.scale.ifPresent( scale -> {
                rule.setMinScaleDenominator( scale.getLeft() );
                rule.setMaxScaleDenominator( scale.getRight() );
            } );
            featureTypeStyle.rules().add( rule );
        }
        return featureTypeStyle;
    }


    private List<? extends SymbolizerDescriptor> serializeStyle( org.polymap.core.style.model.Style style,
            Context context ) {
        if (PointStyle.class.isInstance( style )) {
            return new PointStyleSerializer( context ).serialize( (PointStyle)style );
        }
        // Polygon
        else if (PolygonStyle.class.isInstance( style )) {
            return new PolygonStyleSerializer( context ).serialize( (PolygonStyle)style );
        }
        // Text
        else if (TextStyle.class.isInstance( style )) {
            return new TextStyleSerializer( context ).serialize( (TextStyle)style );
        }
        // Line
        else if (LineStyle.class.isInstance( style )) {
            return new LineStyleSerializer( context ).serialize( (LineStyle)style );
        }
        else {
            throw new RuntimeException( "Unhandled Style type: " + style.getClass().getName() );
        }
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


    private Symbolizer buildTextStyle( TextSymbolizerDescriptor descriptor ) {
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


    private LabelPlacement buildLabelPlacement( LabelPlacementDescriptor lpd ) {
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


    private Halo buildHalo( final HaloDescriptor hd ) {
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


    private Stroke buildStroke( final StrokeDescriptor sd ) {
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


    private Fill buildFill( final FillDescriptor fd ) {
        if (fd != null && (fd.color.isPresent() || fd.opacity.isPresent())) {
            return sf.createFill( fd.color.get(), fd.opacity.get() );
        }
        return null;
    }
}
