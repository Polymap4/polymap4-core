/* 
 * polymap.org
 * Copyright (C) 2016, the @authors. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.core.style.serialize.sld;

import java.util.List;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Graphic;
import org.geotools.styling.Mark;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.SLDTransformer;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;
import org.opengis.filter.FilterFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Iterables;

import org.polymap.core.style.model.FeatureStyle;
import org.polymap.core.style.model.PointStyle;
import org.polymap.core.style.model.StyleGroup;
import org.polymap.core.style.model.StylePropertyValue;
import org.polymap.core.style.serialize.FeatureStyleSerializer;

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
 */
public class SLDSerializer
        extends FeatureStyleSerializer<Style> {

    private static Log log = LogFactory.getLog( SLDSerializer.class );

    public static final StyleFactory        sf = CommonFactoryFinder.getStyleFactory( null );
    
    public static final FilterFactory       ff = CommonFactoryFinder.getFilterFactory( null );

    /** The result of this serializer. */
    private Style                           sld;
    

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
        FeatureStyle featureStyle = context.featureStyle();

        // XXX 1: gather scale and filter descriptions from StyleGroup hierarchy
        // ...
        
        // 2: create flat list of SymbolizerDescriptor instances
        PointStyle ps = (PointStyle)Iterables.getOnlyElement( featureStyle.members() );
        PointStyleSerializer serializer = new PointStyleSerializer();
        List<? extends SymbolizerDescriptor> descriptors = serializer.serialize( ps );
        
        // 3: transform into FeatureTypeStyle and Rule instances
        sld = sf.createStyle();
        for (SymbolizerDescriptor descriptor : descriptors) {
            if (descriptor instanceof PointSymbolizerDescriptor) {
                sld.featureTypeStyles().add( buildPointStyle( (PointSymbolizerDescriptor)descriptor ) );
            }
            else {
                throw new RuntimeException( "Unhandled SymbolizerDescriptor type: " + descriptor.getClass().getName() );
            }
        }
        return sld;
    }


    protected FeatureTypeStyle buildPointStyle( PointSymbolizerDescriptor descriptor ) {
        Graphic gr = sf.createDefaultGraphic();

        Mark mark = sf.getCircleMark();
        mark.setStroke( sf.createStroke( 
                ff.literal( descriptor.strokeColor.get() ), 
                ff.literal( descriptor.strokeWidth.get() ),
                ff.literal( descriptor.strokeOpacity.get() ) ) );
        
        //mark.setFill( sf.createFill( ff.literal( Color.YELLOW ) ) );

        gr.graphicalSymbols().clear();
        gr.graphicalSymbols().add( mark );
        gr.setSize( ff.literal( 8 ) );

        /*
         * Setting the geometryPropertyName arg to null signals that we want to draw
         * the default geometry of features
         */
        PointSymbolizer sym = sf.createPointSymbolizer( gr, null );

        // Rule
        Rule rule = sf.createRule();
        rule.setName( descriptor.description.get() );
        rule.symbolizers().add( sym );
        rule.setFilter( descriptor.filter.get() );

        return sf.createFeatureTypeStyle( new Rule[] { rule } );
    }
    
}
