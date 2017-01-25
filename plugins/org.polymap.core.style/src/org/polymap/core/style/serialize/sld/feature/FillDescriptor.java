/* 
 * polymap.org
 * Copyright (C) 2017, the @authors. All rights reserved.
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
package org.polymap.core.style.serialize.sld.feature;

import org.opengis.filter.expression.Expression;

import org.polymap.core.runtime.config.Config;
import org.polymap.core.runtime.config.Immutable;
import org.polymap.core.style.model.feature.Fill;
import org.polymap.core.style.serialize.FeatureStyleSerializer.Context;
import org.polymap.core.style.serialize.sld.StyleCompositeSerializer;
import org.polymap.core.style.serialize.sld.SymbolizerDescriptor;

/**
 * 
 */
public class FillDescriptor
        extends SymbolizerDescriptor {

    @Immutable
    public Config<Expression> color;

    @Immutable
    // @DefaultDouble(1)
    // @Check(value = NumberRangeValidator.class, args = { "0", "1" })
    // Double
    public Config<Expression> opacity;


    @Override
    public FillDescriptor clone() {
        return (FillDescriptor)super.clone();
    }
    
    
    /**
     * Serializes {@link Fill} into {@link FillDescriptor}s. 
     */
    public static class Serializer
            extends StyleCompositeSerializer<Fill,FillDescriptor> {

        public Serializer( Context context ) {
            super( context );
        }

        @Override
        protected FillDescriptor createDescriptor() {
            return new FillDescriptor();
        }

        @Override
        public void doSerialize( Fill style ) {
            setValue( style.color.get(), (sd, value) -> sd.color.set( value ) );
            setValue( style.opacity.get(), (sd, value) -> sd.opacity.set( value ) );
        }
    }
    
}