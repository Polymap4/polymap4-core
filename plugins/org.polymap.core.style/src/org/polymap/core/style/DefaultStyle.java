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
package org.polymap.core.style;

import java.util.Collection;
import java.util.Random;

import java.awt.Color;

import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.PropertyDescriptor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import org.polymap.core.style.model.ConstantColor;
import org.polymap.core.style.model.ConstantFontFamily;
import org.polymap.core.style.model.ConstantFontStyle;
import org.polymap.core.style.model.ConstantFontWeight;
import org.polymap.core.style.model.ConstantNumber;
import org.polymap.core.style.model.ConstantStrokeCapStyle;
import org.polymap.core.style.model.ConstantStrokeDashStyle;
import org.polymap.core.style.model.ConstantStrokeJoinStyle;
import org.polymap.core.style.model.FeatureStyle;
import org.polymap.core.style.model.LineStyle;
import org.polymap.core.style.model.PointStyle;
import org.polymap.core.style.model.PolygonStyle;
import org.polymap.core.style.model.PropertyString;
import org.polymap.core.style.model.TextStyle;

/**
 * Factory of simple default feature styles with some random settings.
 *
 * @author Falko Br�utigam
 * @author Steffen Stundzig
 */
public class DefaultStyle {

    private static Log log = LogFactory.getLog( DefaultStyle.class );

    public static Random        rand = new Random();
    

    public static FeatureStyle create( FeatureStyle fs, FeatureType schema ) {
        if (schema.getGeometryDescriptor() != null) {
            Class<?> geometryType = schema.getGeometryDescriptor().getType().getBinding();
            if (Point.class.isAssignableFrom( geometryType ) || MultiPoint.class.isAssignableFrom( geometryType )) {
                fillPointStyle( fs );
                fillTextStyle( fs, schema );
            }
            else if (Polygon.class.isAssignableFrom( geometryType )
                    || MultiPolygon.class.isAssignableFrom( geometryType )) {
                fillPolygonStyle( fs );
                fillTextStyle( fs, schema );
            }
            else if (LineString.class.isAssignableFrom( geometryType )
                    || MultiLineString.class.isAssignableFrom( geometryType )) {
                fillLineStyle( fs );
                fillTextStyle( fs, schema );
            }
            else if (Geometry.class.isAssignableFrom( geometryType )) {
                // add all
                fillPointStyle( fs );
                fillPolygonStyle( fs );
                fillLineStyle( fs );
                fillTextStyle( fs, schema );
            }
            else {
                throw new RuntimeException(
                        "Unhandled geom type: " + schema.getGeometryDescriptor().getType().getBinding() );
            }
        }
        return fs;
    }
    

    public static FeatureStyle createAllStyles( FeatureStyle fs ) {
        fillLineStyle( fs );
        fillPointStyle( fs );
        fillPolygonStyle( fs );
        fillTextStyle( fs, null );
        return fs;
    }

    public static LineStyle fillLineStyle( FeatureStyle fs ) {
        LineStyle line = fs.members().createElement( LineStyle.defaults );
        line.fill.get().width.createValue( ConstantNumber.defaults( 5.0 ) );
        line.fill.get().color.createValue( ConstantColor.defaults( randomColor() ) );
        line.fill.get().opacity.createValue( ConstantNumber.defaults( 1.0 ) );
        return line;
    }
    
    public static PointStyle fillPointStyle( FeatureStyle fs ) {
        PointStyle point = fs.members().createElement( PointStyle.defaults );
        point.diameter.createValue( ConstantNumber.defaults( 8.0 ) );
        point.fill.get().color.createValue( ConstantColor.defaults( randomColor() ) );
        point.fill.get().opacity.createValue( ConstantNumber.defaults( 1.0 ) );
        point.stroke.get().color.createValue( ConstantColor.defaults( randomColor() ) );
        point.stroke.get().width.createValue( ConstantNumber.defaults( 1.0 ) );
        point.stroke.get().opacity.createValue( ConstantNumber.defaults( 1.0 ) );
        point.stroke.get().strokeStyle.get().capStyle.createValue( ConstantStrokeCapStyle.defaults() );
        point.stroke.get().strokeStyle.get().dashStyle.createValue( ConstantStrokeDashStyle.defaults() );
        point.stroke.get().strokeStyle.get().joinStyle.createValue( ConstantStrokeJoinStyle.defaults() );
        return point;
    }


    public static PolygonStyle fillPolygonStyle( FeatureStyle fs ) {
        PolygonStyle polygon = fs.members().createElement( PolygonStyle.defaults );
        polygon.fill.get().color.createValue( ConstantColor.defaults( randomColor() ) );
        polygon.fill.get().opacity.createValue( ConstantNumber.defaults( 0.5 ) );
        polygon.stroke.get().color.createValue( ConstantColor.defaults( randomColor() ) );
        polygon.stroke.get().width.createValue( ConstantNumber.defaults( 1.0 ) );
        polygon.stroke.get().opacity.createValue( ConstantNumber.defaults( 1.0 ) );
        polygon.stroke.get().strokeStyle.get().capStyle.createValue( ConstantStrokeCapStyle.defaults() );
        polygon.stroke.get().strokeStyle.get().dashStyle.createValue( ConstantStrokeDashStyle.defaults() );
        polygon.stroke.get().strokeStyle.get().joinStyle.createValue( ConstantStrokeJoinStyle.defaults() );
        return polygon;
    }
    
    
    public static Color randomColor() {
        int from = 50, range = 150;
        return new Color( 
                from + rand.nextInt( range ), 
                from + rand.nextInt( range ),
                from + rand.nextInt( range ) );
    }


    public static TextStyle fillTextStyle( FeatureStyle fs, FeatureType schema ) {
        TextStyle text = fs.members().createElement( TextStyle.defaults );
        text.font.get().family.createValue( ConstantFontFamily.defaults() );
        text.font.get().style.createValue( ConstantFontStyle.defaults() );
        text.font.get().weight.createValue( ConstantFontWeight.defaults() );
        text.font.get().size.createValue( ConstantNumber.defaults( 10.0 ) );
        text.color.createValue( ConstantColor.defaults( Color.BLACK ) );

        if (schema != null) {
            Collection<PropertyDescriptor> schemaDescriptors = schema.getDescriptors();
            GeometryDescriptor geometryDescriptor = schema.getGeometryDescriptor();
            for (PropertyDescriptor descriptor : schemaDescriptors) {
                if (geometryDescriptor == null || !geometryDescriptor.equals( descriptor )) {
                    if (String.class.isAssignableFrom( descriptor.getType().getBinding() )) {
                        text.property.createValue( PropertyString.defaults( descriptor.getName().getLocalPart() ) );
                        break;
                    }
                }
            }
        }
        return text;
    }
}
