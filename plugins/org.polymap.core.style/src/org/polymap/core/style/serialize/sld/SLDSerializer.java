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
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Fill;
import org.geotools.styling.Graphic;
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

import org.polymap.core.runtime.config.Config;
import org.polymap.core.style.model.FeatureStyle;
import org.polymap.core.style.model.PointStyle;
import org.polymap.core.style.model.PolygonStyle;
import org.polymap.core.style.model.StyleGroup;
import org.polymap.core.style.model.StylePropertyValue;
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

    private static Log                log = LogFactory.getLog( SLDSerializer.class );

    public static final StyleFactory  sf  = CommonFactoryFinder.getStyleFactory( null );

    public static final FilterFactory ff  = CommonFactoryFinder.getFilterFactory( null );

    /** The result of this serializer. */
    private Style                     sld;


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
                PointStyle ps = (PointStyle)style;
                PointStyleSerializer serializer = new PointStyleSerializer();
                descriptors.addAll( serializer.serialize( ps ) );
            }
            // Polygon
            else if (PolygonStyle.class.isInstance( style )) {
                PolygonStyle ps = (PolygonStyle)style;
                PolygonStyleSerializer serializer = new PolygonStyleSerializer();
                descriptors.addAll( serializer.serialize( ps ) );
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
        Symbolizer sym = null;
        if (descriptor instanceof PointSymbolizerDescriptor) {
            sym = buildPointStyle( (PointSymbolizerDescriptor)descriptor );
        }
        else if (descriptor instanceof PolygonSymbolizerDescriptor) {
            sym = buildPolygonStyle( (PolygonSymbolizerDescriptor)descriptor );
        }
        else {
            throw new RuntimeException( "Unhandled SymbolizerDescriptor type: " + descriptor.getClass().getName() );
        }

        // Rule
        Rule rule = sf.createRule();
        rule.setName( descriptor.description.get() );
        rule.symbolizers().add( sym );

        descriptor.filter.ifPresent( f -> rule.setFilter( f ) );
        descriptor.scale.ifPresent( scale -> {
            rule.setMinScaleDenominator( scale.getLeft() );
            rule.setMaxScaleDenominator( scale.getRight() );
        } );

        return sf.createFeatureTypeStyle( new Rule[] { rule } );
    }


    protected Stroke buildStroke( Config<StrokeDescriptor> stroke ) {
        // ich bin mir nicht sicher ob mir so eine schreibweise wirklich gefällt
        return stroke.map( s -> 
                sf.createStroke( ff.literal( s.color.get() ), ff.literal( s.width.get() ), ff.literal( s.opacity.get() ) ) )
                .orElse( sf.getDefaultStroke() );
    }
    
    
    protected PointSymbolizer buildPointStyle( PointSymbolizerDescriptor descriptor ) {
        Graphic gr = sf.createDefaultGraphic();

        Mark mark = sf.getCircleMark();
        mark.setStroke( buildStroke( descriptor.stroke ) );

        mark.setFill( sf.createFill( ff.literal( descriptor.fillColor.get() ), ff.literal( descriptor.fillOpacity.get() ) ) );

        gr.graphicalSymbols().clear();
        gr.graphicalSymbols().add( mark );
        gr.setSize( ff.literal( 8 ) );

        /*
         * Setting the geometryPropertyName arg to null signals that we want to draw
         * the default geometry of features
         */
        return sf.createPointSymbolizer( gr, null );
    }


    protected PolygonSymbolizer buildPolygonStyle( PolygonSymbolizerDescriptor descriptor ) {
        // Graphic gr = sf.createDefaultGraphic();

        Stroke stroke = createStroke( descriptor.stroke.get() );

        Fill fill = sf.createFill( ff.literal( descriptor.fillColor.get() ),
                ff.literal( descriptor.fillOpacity.get() ) );
        /*
         * Setting the geometryPropertyName arg to null signals that we want to draw
         * the default geometry of features
         */
        return sf.createPolygonSymbolizer( stroke, fill, null );
    }


    private Stroke createStroke( StrokeDescriptor descriptor ) {
        Stroke stroke = null;
        if (descriptor != null) {
            stroke = sf.createStroke( ff.literal( descriptor.color.get() ),
                    ff.literal( descriptor.width.get() ), ff.literal( descriptor.opacity.get() ) );

            stroke.setLineJoin( ff.literal( descriptor.joinStyle.get() ) );
            stroke.setLineCap( ff.literal( descriptor.capStyle.get() ) );
            if (descriptor.dashStyle.get() != null) {
                stroke.setDashArray( descriptor.dashStyle.get() );
                stroke.setDashOffset( ff.literal( 0 ) );
            }
        }
        return stroke;
    }
}
