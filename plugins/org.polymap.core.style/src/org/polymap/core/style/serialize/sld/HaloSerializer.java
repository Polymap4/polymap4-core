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

import org.opengis.filter.expression.Expression;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.style.model.Halo;

/**
 * Serialize {@link Halo}.
 *
 * @author Steffen Stundzig
 */
public class HaloSerializer
        extends StyleCompositeSerializer<Halo,HaloDescriptor> {

    private static Log log = LogFactory.getLog( HaloSerializer.class );


    @Override
    protected HaloDescriptor createDescriptor() {
        return new HaloDescriptor();
    }


    @Override
    public void doSerialize( Halo style ) {
        setValue( style.width.get(), ( HaloDescriptor sd, Expression value ) -> sd.width.set( value ) );
        setValue( style.color.get(), ( HaloDescriptor sd, Expression value ) -> sd.color.set( value ) );
        setValue( style.opacity.get(), ( HaloDescriptor sd, Expression value ) -> sd.opacity.set( value ) );
    }
}
