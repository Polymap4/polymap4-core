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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.style.model.Stroke;
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
        setComposite( new StrokeStyleSerializer(context()).serialize( stroke.width.get(), stroke.strokeStyle.get() ),
                ( StrokeDescriptor sd, StrokeStyleDescriptor value ) -> sd.strokeStyle.set( value ) );
    }
}
