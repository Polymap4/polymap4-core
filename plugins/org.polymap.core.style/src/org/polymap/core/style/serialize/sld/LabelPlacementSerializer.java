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

import org.polymap.core.style.model.LabelPlacement;

/**
 * Serialize {@link LabelPlacement}.
 *
 * @author Steffen Stundzig
 */
public class LabelPlacementSerializer
        extends StyleCompositeSerializer<LabelPlacement,LabelPlacementDescriptor> {

    private static Log log = LogFactory.getLog( LabelPlacementSerializer.class );


    @Override
    protected LabelPlacementDescriptor createDescriptor() {
        return new LabelPlacementDescriptor();
    }


    @Override
    public void doSerialize( LabelPlacement style ) {
        setValue( style.anchorPointX.get(),
                ( LabelPlacementDescriptor sd, Expression value ) -> sd.anchorPointX.set( value ) );
        setValue( style.anchorPointY.get(),
                ( LabelPlacementDescriptor sd, Expression value ) -> sd.anchorPointY.set( value ) );
        setValue( style.displacementX.get(),
                ( LabelPlacementDescriptor sd, Expression value ) -> sd.displacementX.set( value ) );
        setValue( style.displacementY.get(),
                ( LabelPlacementDescriptor sd, Expression value ) -> sd.displacementY.set( value ) );
        setValue( style.rotation.get(),
                ( LabelPlacementDescriptor sd, Expression value ) -> sd.rotation.set( value ) );
        setValue( style.offset.get(),
                ( LabelPlacementDescriptor sd, Expression value ) -> sd.offset.set( value ) );
    }
}
