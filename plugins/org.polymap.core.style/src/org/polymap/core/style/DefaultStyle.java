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
package org.polymap.core.style;

import org.opengis.feature.type.FeatureType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.vividsolutions.jts.awt.PointShapeFactory.Point;
import org.polymap.core.style.model.ConstantColor;
import org.polymap.core.style.model.ConstantNumber;
import org.polymap.core.style.model.FeatureStyle;
import org.polymap.core.style.model.PointStyle;

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
        else {
            throw new RuntimeException( "Unkhandled geom type: " + schema.getGeometryDescriptor().getType().getBinding() );
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
    
}
