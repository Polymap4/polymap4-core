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

import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.Literal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.style.model.ConstantStrokeDashStyle;
import org.polymap.core.style.model.Stroke;
import org.polymap.core.style.model.StrokeDashStyle;
import org.polymap.core.style.serialize.FeatureStyleSerializer.Context;

/**
 * Serialize {@link Stroke}.
 *
 * @author Falko Bräutigam
 * @author Steffen Stundzig
 */
public class StrokeSerializer
        extends StyleCompositeSerializer<Stroke,StrokeDescriptor> {

    public StrokeSerializer( Context context ) {
        super( context );
    }


    private static Log log = LogFactory.getLog( StrokeSerializer.class );


    @Override
    protected StrokeDescriptor createDescriptor() {
        return new StrokeDescriptor();
    }


    @Override
    public void doSerialize( Stroke stroke ) {
        setValue( stroke.width.get(), ( StrokeDescriptor sd, Expression value ) -> sd.width.set( value ) );
        setValue( stroke.opacity.get(), ( StrokeDescriptor sd, Expression value ) -> sd.opacity.set( value ) );
        setValue( stroke.color.get(), ( StrokeDescriptor sd, Expression value ) -> sd.color.set( value ) );
        setValue( stroke.capStyle.get(), ( StrokeDescriptor sd, Expression value ) -> sd.capStyle.set( value ) );
        setValue( stroke.joinStyle.get(), ( StrokeDescriptor sd, Expression value ) -> sd.joinStyle.set( value ) );
        // must be the last, since it uses the width
        if (stroke.dashStyle.get() != null) {
            if (stroke.dashStyle.get() instanceof ConstantStrokeDashStyle) {
                StrokeDashStyle dashStyle = ((ConstantStrokeDashStyle)stroke.dashStyle.get()).value.get();
                for (StrokeDescriptor sd : descriptors) {
                    serializeDashStyle( sd, dashStyle );
                }
            }
            else {
                throw new UnsupportedOperationException( stroke.dashStyle.get().getClass() + " is not supported" );
            }
        }
    }
    //
    //
    // protected Stroke buildStroke( Config<StrokeDescriptor> stroke ) {
    // // ich bin mir nicht sicher ob mir so eine schreibweise wirklich gefällt
    // return stroke.map( s -> sf.createStroke( ff.literal( s.color.get() ),
    // ff.literal( s.width.get() ),
    // ff.literal( s.opacity.get() ) ) ).orElse( sf.getDefaultStroke() );
    // }


    private void serializeDashStyle( StrokeDescriptor sd, StrokeDashStyle value ) {
        // see
        // http://docs.geoserver.org/stable/en/user/styling/sld-cookbook/lines.html#dashed-line
        Double strokeWidth = new Double( 1.0 );

        Expression strokeWidthExpression = sd.width.get();
        if (strokeWidthExpression instanceof Literal) {
            strokeWidth = (Double)((Literal)strokeWidthExpression).getValue();
        }

        final float dot = strokeWidth.floatValue() / 4;
        final float dash = 2 * strokeWidth.floatValue();
        final float longDash = 4 * strokeWidth.floatValue();
        float[] style = null;
        switch (value) {
            case dash:
                style = new float[] { dash, dash };
                break;
            case dashdot:
                style = new float[] { dash, dash, dot, dash };
                break;
            case dot:
                style = new float[] { dot, dash };
                break;
            case longdash:
                style = new float[] { longDash, dash, longDash, dash };
                break;
            case longdashdot:
                style = new float[] { longDash, dash, dot, dash };
                break;
            case solid:
                // do nothing
                break;
            default:
                throw new RuntimeException( "Unhandled StrokeDashStyle: " + value );
        }
        sd.dashStyle.set( style );
    }
}
