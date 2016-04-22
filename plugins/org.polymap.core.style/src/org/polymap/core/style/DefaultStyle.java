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

import org.opengis.feature.type.FeatureType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.vividsolutions.jts.awt.PointShapeFactory.Point;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

import org.polymap.core.style.model.ConstantColor;
import org.polymap.core.style.model.ConstantNumber;
import org.polymap.core.style.model.ConstantStrokeCapStyle;
import org.polymap.core.style.model.ConstantStrokeDashStyle;
import org.polymap.core.style.model.ConstantStrokeJoinStyle;
import org.polymap.core.style.model.FeatureStyle;
import org.polymap.core.style.model.PointStyle;
import org.polymap.core.style.model.PolygonStyle;

/**
 * Factory of simple default feature styles with some random settings.
 *
 * @author Falko Bräutigam
 */
public class DefaultStyle {

    private static Log log = LogFactory.getLog( DefaultStyle.class );


    public static FeatureStyle create( FeatureStyle fs, FeatureType schema ) {
        if (Point.class.isAssignableFrom( schema.getGeometryDescriptor().getType().getBinding() )) {
            return createPointStyle( fs, schema );
        }
        if (Polygon.class.isAssignableFrom( schema.getGeometryDescriptor().getType().getBinding() )
                || MultiPolygon.class.isAssignableFrom( schema.getGeometryDescriptor().getType().getBinding() )) {
            return createPolygonStyle( fs, schema );
        }
        else {
            throw new RuntimeException(
                    "Unhandled geom type: " + schema.getGeometryDescriptor().getType().getBinding() );
        }
    }


    public static FeatureStyle createPointStyle( FeatureStyle fs, FeatureType schema ) {
        PointStyle point = fs.members().createElement( PointStyle.defaults );

        point.fillColor.createValue( ConstantColor.defaults( 200, 0, 0 ) );
        point.fillOpacity.createValue( ConstantNumber.defaults( 0.5 ) );
        point.strokeColor.createValue( ConstantColor.defaults( 100, 100, 100 ) );
        point.strokeWidth.createValue( ConstantNumber.defaults( 2.0 ) );
        point.strokeOpacity.createValue( ConstantNumber.defaults( 1.0 ) );
        return fs;
    }


    public static FeatureStyle createPolygonStyle( FeatureStyle fs, FeatureType schema ) {
        PolygonStyle polygon = fs.members().createElement( PolygonStyle.defaults );

        polygon.fillColor.createValue( ConstantColor.defaults( 200, 0, 0 ) );
        polygon.fillOpacity.createValue( ConstantNumber.defaults( 0.5 ) );
        polygon.strokeColor.createValue( ConstantColor.defaults( 100, 100, 100 ) );
        polygon.strokeWidth.createValue( ConstantNumber.defaults( 2.0 ) );
        polygon.strokeOpacity.createValue( ConstantNumber.defaults( 1.0 ) );
        polygon.strokeCapStyle.createValue( ConstantStrokeCapStyle.defaults() );
        polygon.strokeDashStyle.createValue( ConstantStrokeDashStyle.defaults() );
        polygon.strokeJoinStyle.createValue( ConstantStrokeJoinStyle.defaults() );
        return fs;
    }
}
