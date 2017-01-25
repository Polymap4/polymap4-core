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
import org.polymap.core.style.model.feature.TextStyle;
import org.polymap.core.style.serialize.FeatureStyleSerializer.Context;
import org.polymap.core.style.serialize.sld.StyleSerializer;
import org.polymap.core.style.serialize.sld.SymbolizerDescriptor;
import org.polymap.model2.Immutable;

/**
 * 
 * @author Steffen Stundzig
 */
public class TextSymbolizerDescriptor
        extends SymbolizerDescriptor {

    // String
    @Immutable
    public Config<Expression>       text;

    // Color
    @Immutable
    public Config<Expression>       color;

    @Immutable
    // @DefaultDouble( 1 )
    // @Check(value = NumberRangeValidator.class, args = { "0", "1" })
    // Double
    public Config<Expression>       opacity;

    @Immutable
    public Config<FontDescriptor>   font;

    @Immutable
    public Config<HaloDescriptor>   halo;

    @Immutable
    public Config<LabelPlacementDescriptor> labelPlacement;


    @Override
    public TextSymbolizerDescriptor clone() {
        return (TextSymbolizerDescriptor)super.clone();
    }

    
    /**
     * Serialize {@link TextStyle}.
     *
     * @author Steffen Stundzig
     */
    public static class Serializer
            extends StyleSerializer<TextStyle,TextSymbolizerDescriptor> {

        public Serializer( Context context ) {
            super( context );
        }

        @Override
        protected TextSymbolizerDescriptor createStyleDescriptor() {
            return new TextSymbolizerDescriptor();
        }


        @Override
        public void doSerializeStyle( TextStyle style ) {
            setComposite( new FontDescriptor.Serializer( context() ).serialize( style.font.get() ),
                    (TextSymbolizerDescriptor sd, FontDescriptor value) -> sd.font.set( value ) );
            
            setComposite( new HaloDescriptor.Serializer( context() ).serialize( style.halo.get() ),
                    (TextSymbolizerDescriptor sd, HaloDescriptor value) -> sd.halo.set( value ) );
            
            setComposite( new LabelPlacementDescriptor.Serializer( context() ).serialize( style.labelPlacement.get() ),
                    ( TextSymbolizerDescriptor sd, LabelPlacementDescriptor value ) -> sd.labelPlacement.set( value ) );
            
            setValue( style.property.get(), (TextSymbolizerDescriptor sd, Expression value) -> sd.text.set( value ) );
            setValue( style.color.get(), (TextSymbolizerDescriptor sd, Expression value) -> sd.color.set( value ) );
            setValue( style.opacity.get(), (TextSymbolizerDescriptor sd, Expression value) -> sd.opacity.set( value ) );
        }
    }

}
