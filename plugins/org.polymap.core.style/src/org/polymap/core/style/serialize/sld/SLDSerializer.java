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

import java.util.ArrayList;
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
import org.opengis.filter.FilterFactory;

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

    public static final FilterFactory ff = CommonFactoryFinder.getFilterFactory( null );

    /** The result of this serializer. */
    private Style sld;


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

        List<SymbolizerDescriptor> descriptors = new ArrayList<SymbolizerDescriptor>();
        // 2: create flat list of SymbolizerDescriptor instances
        for (org.polymap.core.style.model.Style style : featureStyle.members()) {
            // skip deactivated
            if (!style.active.get()) {
            }
            // Point
            else if (PointStyle.class.isInstance( style )) {
                descriptors.addAll( new PointStyleSerializer().serialize( (PointStyle)style ) );
            }
            // Polygon
            else if (PolygonStyle.class.isInstance( style )) {
                descriptors.addAll( new PolygonStyleSerializer().serialize( (PolygonStyle)style ) );
            }
            // Text
            else if (TextStyle.class.isInstance( style )) {
                descriptors.addAll( new TextStyleSerializer().serialize( (TextStyle)style ) );
            }
            // Line
            else if (LineStyle.class.isInstance( style )) {
                descriptors.addAll( new LineStyleSerializer().serialize( (LineStyle)style ) );
            }
            else {
                throw new RuntimeException( "Unhandled Style type: " + style.getClass().getName() );
            }
        }

        sld = sf.createStyle();
        // 3: transform into FeatureTypeStyle and Rule instances
        for (SymbolizerDescriptor descriptor : descriptors) {
            sld.featureTypeStyles().add( buildStyle( descriptor ) );
        }
        return sld;
    }


    private FeatureTypeStyle buildStyle( final SymbolizerDescriptor descriptor ) {
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
        rule.setName( descriptor.description.get() );
        rule.symbolizers().addAll( sym );

        descriptor.filter.ifPresent( f -> rule.setFilter( f ) );
        descriptor.scale.ifPresent( scale -> {
            rule.setMinScaleDenominator( scale.getLeft() );
            rule.setMaxScaleDenominator( scale.getRight() );
        } );

        return sf.createFeatureTypeStyle( new Rule[] { rule } );
    }


    protected List<LineSymbolizer> buildLineStyle( LineSymbolizerDescriptor descriptor ) {
        // Graphic gr = sf.createDefaultGraphic();
        /*
         * Setting the geometryPropertyName arg to null signals that we want to draw
         * the default geometry of features
         */
        List<LineSymbolizer> lines = Lists.newArrayList();
        lines.add( sf.createLineSymbolizer( buildStroke( descriptor.line.get() ), null ) );
        
        if (descriptor.stroke.isPresent()) {
            LineSymbolizer top = sf.createLineSymbolizer( buildStroke( descriptor.stroke.get() ), null );
            top.setPerpendicularOffset(
                    descriptor.offset.get() != null ? descriptor.offset.get() : ff.literal( 5.0 ) );
            lines.add( top );
            
            LineSymbolizer bottom = sf.createLineSymbolizer( buildStroke( descriptor.stroke.get() ), null );
            bottom.setPerpendicularOffset(
                    descriptor.offset.get() != null ? ff.multiply( ff.literal( -1 ), descriptor.offset.get()) : ff.literal( -5.0 ) );
            lines.add( bottom );
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
        /*
         * Setting the geometryPropertyName arg to null signals that we want to draw
         * the default geometry of features
         */
        return sf.createPointSymbolizer( gr, null );
    }


    protected PolygonSymbolizer buildPolygonStyle( PolygonSymbolizerDescriptor descriptor ) {
        // Graphic gr = sf.createDefaultGraphic();

        Stroke stroke = buildStroke( descriptor.stroke.get() );

        Fill fill = buildFill( descriptor.fill.get() );
        /*
         * Setting the geometryPropertyName arg to null signals that we want to draw
         * the default geometry of features
         */
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
        Halo background = null;
        if (descriptor.haloColor.isPresent()) {
            background = sf.createHalo( sf.createFill( descriptor.haloColor.get() ),
                    descriptor.haloWidth.isPresent() ? descriptor.haloWidth.get() : ff.literal( 2.0 ) );
            if (descriptor.haloOpacity.isPresent()) {
                background.getFill().setOpacity( descriptor.haloOpacity.get() );
            }
        }

        AnchorPoint anchorPoint = null;
        if (descriptor.anchorPointX.isPresent()) {
            anchorPoint = sf.createAnchorPoint( ff.literal( descriptor.anchorPointX.get() ),
                    descriptor.anchorPointY.get() );
        }
        Displacement displacement = null;
        if (descriptor.displacementX.isPresent()) {
            displacement = sf.createDisplacement( descriptor.displacementX.get(), descriptor.displacementY.get() );
        }

        LabelPlacement placement = null;
        // XXX decide if we have a label placement or a line placement
        placement = sf.createPointPlacement( anchorPoint, displacement, descriptor.placementRotation.get() );
        sf.createLinePlacement( descriptor.placementOffset.get() );

        Font[] fonts = null;
        if (descriptor.font.isPresent()) {
            fonts = descriptor.font.get().fonts.get();
        }
        return sf.createTextSymbolizer( foreground, fonts, background, descriptor.text.get(), placement, null );
    }


    private Stroke buildStroke( StrokeDescriptor descriptor ) {
        Stroke stroke = null;
        if (descriptor != null) {
            stroke = sf.createStroke( descriptor.color.get(), descriptor.width.get(), descriptor.opacity.get() );

            stroke.setLineJoin( descriptor.joinStyle.get() );
            stroke.setLineCap( descriptor.capStyle.get() );
            if (descriptor.dashStyle.get() != null) {
                stroke.setDashArray( descriptor.dashStyle.get() );
                stroke.setDashOffset( ff.literal( 0 ) );
            }
        }
        return stroke;
    }


    private Fill buildFill( FillDescriptor descriptor ) {
        Fill fill = null;
        if (descriptor != null) {
            fill = sf.createFill( descriptor.color.get(), descriptor.opacity.get() );
        }
        return fill;
    }
}
