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

import javax.xml.transform.TransformerException;

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
import org.geotools.styling.SLDTransformer;
import org.geotools.styling.Stroke;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;
import org.opengis.feature.type.FeatureType;
import org.opengis.filter.FilterFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
    
    private static final Log log = LogFactory.getLog( DefaultStyles.class );
    
    protected static final StyleFactory     sf = CommonFactoryFinder.getStyleFactory( null );
    
    protected static final FilterFactory    ff = CommonFactoryFinder.getFilterFactory( null );
    

    public static Style createStyleWithFTS() {
        Style style = sf.createStyle();
        FeatureTypeStyle fts = sf.createFeatureTypeStyle( new Rule[] {} );
        style.featureTypeStyles().add( fts );
    	return style;
    }


    public static Style findStyle( FeatureSource fs ) {
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
    public static Style createAllStyle() {
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
    public static Style createPolygonStyle( Style style ) {
        // create a partially opaque outline stroke
        Stroke stroke = sf.createStroke(
                ff.literal(Color.DARK_GRAY),
                ff.literal(0.5),
                ff.literal(0.5));

        // create a partial opaque fill
        Fill fill = sf.createFill(
                ff.literal(Color.CYAN),
                ff.literal(0.5));

        /*
         * Setting the geometryPropertyName arg to null signals that we want to
         * draw the default geometry of features
         */
        PolygonSymbolizer sym = sf.createPolygonSymbolizer(stroke, fill, null);

        Rule rule = sf.createRule();
        
        rule.setName( "Rule for PolygonSymbolizer" );
        rule.symbolizers().add(sym);
       
        style.featureTypeStyles().get(0).rules().add(rule);
        return style;
    }
    
    
    /**
     * Create a Style to draw line features as thin blue lines
     */
    public static Style createLineStyle( Style style ) {
        Stroke stroke = sf.createStroke(
                ff.literal(Color.DARK_GRAY),
                ff.literal(0.5));

        /*
         * Setting the geometryPropertyName arg to null signals that we want to
         * draw the default geometry of features
         */
        LineSymbolizer sym = sf.createLineSymbolizer(stroke, null);

        Rule rule = sf.createRule();
        rule.symbolizers().add(sym);
        rule.setName( "Rule for LineSymbolizer" );

        style.featureTypeStyles().get(0).rules().add(rule);
        return style;
    }

    
    /**
     * Create a Style to draw point features as circles with blue outlines
     * and cyan fill
     */
    public static Style createPointStyle( Style style ) {
        Graphic gr = sf.createDefaultGraphic();

        Mark mark = sf.getCircleMark();
        mark.setStroke( sf.createStroke( ff.literal( Color.RED ), ff.literal( 1.5 ) ) );
        mark.setFill( sf.createFill( ff.literal( Color.YELLOW ) ) );

        gr.graphicalSymbols().clear();
        gr.graphicalSymbols().add( mark );
        gr.setSize( ff.literal( 8 ) );

        /*
         * Setting the geometryPropertyName arg to null signals that we want to draw
         * the default geometry of features
         */
        PointSymbolizer sym = sf.createPointSymbolizer( gr, null );

        Rule rule = sf.createRule();
        rule.symbolizers().add( sym );
        rule.setName( "Rule for PointSymbolizer" );

        style.featureTypeStyles().get( 0 ).rules().add( rule );
        return style;
    }

    
    public static String serialize( Style style ) {
        try {
            SLDTransformer styleTransform = new SLDTransformer();
            styleTransform.setIndentation( 4 );
            styleTransform.setOmitXMLDeclaration( false );
            return styleTransform.transform( style );
        }
        catch (TransformerException e) {
            throw new RuntimeException( "Unable to transform style.", e );
        }
    }

}
