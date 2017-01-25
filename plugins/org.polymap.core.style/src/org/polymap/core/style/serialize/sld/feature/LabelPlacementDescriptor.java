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
package org.polymap.core.style.serialize.sld.feature;

import org.opengis.filter.expression.Expression;

import org.polymap.core.runtime.config.Config;
import org.polymap.core.style.model.feature.LabelPlacement;
import org.polymap.core.style.serialize.FeatureStyleSerializer.Context;
import org.polymap.core.style.serialize.sld.StyleCompositeSerializer;
import org.polymap.core.style.serialize.sld.SymbolizerDescriptor;

import org.polymap.model2.Immutable;

/**
 * @author Steffen Stundzig
 */
public class LabelPlacementDescriptor
        extends SymbolizerDescriptor {

    @Immutable
    // Point
    public Config<Expression> anchorPointX;

    @Immutable
    // Point
    public Config<Expression> anchorPointY;

    @Immutable
    // Point
    public Config<Expression> displacementX;

    @Immutable
    // Point
    public Config<Expression> displacementY;

    @Immutable
    // @DefaultDouble( 0 )
    // @Check(value = NumberRangeValidator.class, args = { "0", "360" })
    // Double
    public Config<Expression> rotation;

    @Immutable
    // @DefaultDouble( 2 )
    // @Check(value = NumberRangeValidator.class, args = { "0", "100" })
    // Double
    public Config<Expression> offset;


    @Override
    public LabelPlacementDescriptor clone() {
        return (LabelPlacementDescriptor)super.clone();
    }

    
    /**
     * Serialize {@link LabelPlacement}.
     */
    public static class Serializer
            extends StyleCompositeSerializer<LabelPlacement,LabelPlacementDescriptor> {

        public Serializer( Context context ) {
            super( context );
        }

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

}
