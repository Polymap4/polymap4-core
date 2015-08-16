/* 
 * polymap.org
 * Copyright (C) 2009-2015, Polymap GmbH. All rights reserved.
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

import java.awt.Color;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.data.FeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Fill;
import org.geotools.styling.Graphic;
import org.geotools.styling.LineSymbolizer;
import org.geotools.styling.Mark;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.Stroke;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;
import org.opengis.feature.type.FeatureType;
import org.opengis.filter.FilterFactory;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Simple default feature rendering styles. 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class DefaultStyles {
    
    private static Log log = LogFactory.getLog( DefaultStyles.class );
    
    protected StyleFactory        styleFactory = CommonFactoryFinder.getStyleFactory( null );
    
    protected FilterFactory       filterFactory = CommonFactoryFinder.getFilterFactory( null );
    

    public Style createStyleWithFTS() {
        Style style = styleFactory.createStyle();
        FeatureTypeStyle fts = styleFactory.createFeatureTypeStyle( new Rule[] {} );
        style.featureTypeStyles().add( fts );
    	return style;
    }


    public Style findStyle( FeatureSource fs ) {
        FeatureType schema = fs.getSchema();
        log.debug( "    geometry type: " + schema.getGeometryDescriptor() );
        
        Style style = createStyleWithFTS();

        try {
            Class geomType = schema.getGeometryDescriptor().getType().getBinding();
            if (Polygon.class.isAssignableFrom(geomType)
                    || MultiPolygon.class.isAssignableFrom(geomType)) {
                return createPolygonStyle( style );
            } 
            else if (LineString.class.isAssignableFrom(geomType)
                    || MultiLineString.class.isAssignableFrom(geomType)) {
                return createLineStyle( style );
            } 
            else {
                log.warn( "No style for geomType: " + geomType.getName() );
                return createPointStyle( style );
            }
        }
        catch (Exception e) {
            log.warn( "", e );
            return createAllStyle();
        }        
    }


    /**
     * Create a style that matches for everything.
     * 
     * @return Newly created style.
     */
    public Style createAllStyle() {
        Style style = createStyleWithFTS();
        createPolygonStyle( style );
        createLineStyle( style );
        createPointStyle( style );
        return style;
    }


    /**
     * Create a Style to draw polygon features with a thin blue outline and
     * a cyan fill
     */
    private Style createPolygonStyle( Style style ) {

        // create a partially opaque outline stroke
        Stroke stroke = styleFactory.createStroke(
                filterFactory.literal(Color.DARK_GRAY),
                filterFactory.literal(0.5),
                filterFactory.literal(0.5));

        // create a partial opaque fill
        Fill fill = styleFactory.createFill(
                filterFactory.literal(Color.CYAN),
                filterFactory.literal(0.5));

        /*
         * Setting the geometryPropertyName arg to null signals that we want to
         * draw the default geometry of features
         */
        PolygonSymbolizer sym = styleFactory.createPolygonSymbolizer(stroke, fill, null);

        Rule rule = styleFactory.createRule();
        
        rule.setName("Rule for PolygonSymbolizer");
        rule.symbolizers().add(sym);
       
        style.featureTypeStyles().get(0).rules().add(rule);

        
        return style;
    }
    
    /**
     * Create a Style to draw line features as thin blue lines
     */
    private Style createLineStyle( Style style ) {
        Stroke stroke = styleFactory.createStroke(
                filterFactory.literal(Color.DARK_GRAY),
                filterFactory.literal(0.5));

        /*
         * Setting the geometryPropertyName arg to null signals that we want to
         * draw the default geometry of features
         */
        LineSymbolizer sym = styleFactory.createLineSymbolizer(stroke, null);

        Rule rule = styleFactory.createRule();
        rule.symbolizers().add(sym);
        rule.setName("Rule for LineSymbolizer");

        style.featureTypeStyles().get(0).rules().add(rule);
        return style;
    }

    /**
     * Create a Style to draw point features as circles with blue outlines
     * and cyan fill
     */
    private Style createPointStyle( Style style ) {
        Graphic gr = styleFactory.createDefaultGraphic();

        Mark mark = styleFactory.getCircleMark();
        mark.setStroke( styleFactory.createStroke( filterFactory.literal( Color.RED ), filterFactory.literal( 1.5 ) ) );
        mark.setFill( styleFactory.createFill( filterFactory.literal( Color.YELLOW ) ) );

        gr.graphicalSymbols().clear();
        gr.graphicalSymbols().add( mark );
        gr.setSize( filterFactory.literal( 8 ) );

        /*
         * Setting the geometryPropertyName arg to null signals that we want to draw
         * the default geometry of features
         */
        PointSymbolizer sym = styleFactory.createPointSymbolizer( gr, null );

        Rule rule = styleFactory.createRule();
        rule.symbolizers().add( sym );
        rule.setName( "Rule for PointSymbolizer" );

        style.featureTypeStyles().get( 0 ).rules().add( rule );
        return style;
    }

}
