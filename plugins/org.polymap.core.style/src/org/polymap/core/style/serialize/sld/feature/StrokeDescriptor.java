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
import org.polymap.core.runtime.config.Immutable;
import org.polymap.core.style.model.feature.Stroke;
import org.polymap.core.style.serialize.FeatureStyleSerializer.Context;
import org.polymap.core.style.serialize.sld.StyleCompositeSerializer;
import org.polymap.core.style.serialize.sld.SymbolizerDescriptor;

/**
 * 
 *
 * @author Falko Bräutigam
 * @author Steffen Stundzig
 */
public class StrokeDescriptor
        extends SymbolizerDescriptor {

    @Immutable
    public Config<Expression>   width;

    @Immutable
    public Config<Expression>   color;

    @Immutable
    // @DefaultDouble(1)
    // @Check( value=NumberRangeValidator.class, args={"0","1"} )
    public Config<Expression>   opacity;

    @Immutable
    public Config<StrokeStyleDescriptor> strokeStyle;


    @Override
    public StrokeDescriptor clone() {
        return (StrokeDescriptor)super.clone();
    }
    
    
    /**
     * Serialize {@link Stroke}.
     */
    public static class Serializer
            extends StyleCompositeSerializer<Stroke,StrokeDescriptor> {

        public Serializer( Context context ) {
            super( context );
        }

        @Override
        protected StrokeDescriptor createDescriptor() {
            return new StrokeDescriptor();
        }

        @Override
        public void doSerialize( Stroke stroke ) {
            setValue( stroke.width.get(), (sd, value) -> sd.width.set( value ) );
            setValue( stroke.opacity.get(), (sd, value) -> sd.opacity.set( value ) );
            setValue( stroke.color.get(), (sd, value) -> sd.color.set( value ) );
            
            setComposite( new StrokeStyleDescriptor.Serializer( context() ).serialize( stroke.width.get(), stroke.strokeStyle.get() ),
                    (StrokeDescriptor sd, StrokeStyleDescriptor value) -> sd.strokeStyle.set( value ) );
        }
    }

}
