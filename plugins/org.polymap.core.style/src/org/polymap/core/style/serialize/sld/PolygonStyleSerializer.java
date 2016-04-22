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

import java.awt.Color;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.style.model.PolygonStyle;
import org.polymap.core.style.model.StrokeCapStyle;
import org.polymap.core.style.model.StrokeDashStyle;
import org.polymap.core.style.model.StrokeJoinStyle;

/**
 * Serialize {@link PolygonStyle}.
 *
 * @author Steffen Stundzig
 */
public class PolygonStyleSerializer
        extends StyleSerializer<PolygonStyle,PolygonSymbolizerDescriptor> {

    private static Log log = LogFactory.getLog( PolygonStyleSerializer.class );


    @Override
    protected PolygonSymbolizerDescriptor createDescriptor() {
        return new PolygonSymbolizerDescriptor();
    }


    @Override
    public void doSerialize( PolygonStyle style ) {
        setValue( style.strokeWidth.get(),
                ( PolygonSymbolizerDescriptor sd, Double value ) -> sd.strokeWidth.set( value ) );
        setValue( style.strokeOpacity.get(),
                ( PolygonSymbolizerDescriptor sd, Double value ) -> sd.strokeOpacity.set( value ) );
        setValue( style.strokeColor.get(),
                ( PolygonSymbolizerDescriptor sd, Color value ) -> sd.strokeColor.set( value ) );
        setValue( style.fillColor.get(), ( PolygonSymbolizerDescriptor sd, Color value ) -> sd.fillColor.set( value ) );
        setValue( style.fillOpacity.get(),
                ( PolygonSymbolizerDescriptor sd, Double value ) -> sd.fillOpacity.set( value ) );
        setValue( style.strokeCapStyle.get(),
                ( PolygonSymbolizerDescriptor sd, StrokeCapStyle value ) -> sd.strokeCapStyle.set( value ) );
        setValue( style.strokeJoinStyle.get(),
                ( PolygonSymbolizerDescriptor sd, StrokeJoinStyle value ) -> sd.strokeJoinStyle.set( value ) );
        setValue( style.strokeDashStyle.get(),
                ( PolygonSymbolizerDescriptor sd, StrokeDashStyle value ) -> doSerializeDashStyle( sd, value ) );
    }


    private void doSerializeDashStyle( PolygonSymbolizerDescriptor sd, StrokeDashStyle value ) {
        // see
        // http://docs.geoserver.org/stable/en/user/styling/sld-cookbook/lines.html#dashed-line
        Double strokeWidth = sd.strokeWidth.get();
        if (strokeWidth == null) {
            strokeWidth = new Double( 1.0 );
        }
        float dot = strokeWidth.floatValue() / 4;
        float dash = 2 * strokeWidth.floatValue();
        float longDash = 4 * strokeWidth.floatValue();

        switch (value) {
            case dash:
                sd.strokeDashStyle.set( new float[] { dash, dash } );
                break;
            case dashdot:
                sd.strokeDashStyle.set( new float[] { dash, dash, dot, dash } );
                break;
            case dot:
                sd.strokeDashStyle.set( new float[] { dot, dash } );
                break;
            case longdash:
                sd.strokeDashStyle.set( new float[] { longDash, dash, longDash, dash } );
                break;
            case longdashdot:
                sd.strokeDashStyle.set( new float[] { longDash, dash, dot, dash } );
                break;
            case solid:
                // do nothing
                break;
            default:
                throw new RuntimeException( "Unhandled StrokeDashStyle: " + value );
        }
    }

}
