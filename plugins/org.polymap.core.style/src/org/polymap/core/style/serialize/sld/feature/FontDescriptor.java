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

import org.geotools.styling.Font;
import org.opengis.filter.expression.Expression;

import org.polymap.core.runtime.config.Config;
import org.polymap.core.style.model.NoValue;
import org.polymap.core.style.model.feature.ConstantFontFamily;
import org.polymap.core.style.model.feature.FontFamily;
import org.polymap.core.style.model.feature.FontStyle;
import org.polymap.core.style.model.feature.FontWeight;
import org.polymap.core.style.serialize.FeatureStyleSerializer.Context;
import org.polymap.core.style.serialize.sld.SLDSerializer;
import org.polymap.core.style.serialize.sld.StyleCompositeSerializer;
import org.polymap.core.style.serialize.sld.SymbolizerDescriptor;

import org.polymap.model2.Immutable;

/**
 * @author Steffen Stundzig
 */
public class FontDescriptor
        extends SymbolizerDescriptor {

//    /**
//     * XXX remind, the font descriptor currently contains Font[]
//     */
//    @Immutable
//    //FontFamily
//    public Config<FontFamily> family;

    @Immutable
    //FontStyle
    public Config<Expression>   style;

    @Immutable
    //Font Weight
    public Config<Expression>   weight;

    @Immutable
//    @DefaultDouble( 10 )
//    @Check(value = NumberRangeValidator.class, args = { "1", "100" })
    // Double
    public Config<Expression>   size;

    @Immutable
    //Font[]
    public Config<Font[]>       fonts;

    @Override
    public FontDescriptor clone() {
        return (FontDescriptor)super.clone();
    }

    
    /**
     * Serialize {@link Font}. Since fonts are handled as special Font[] and not as
     * Expressions, here the Serializer must implement this. Other Values than
     * ConstantFontFamily are currently not supported.
     */
    public static class Serializer
            extends StyleCompositeSerializer<org.polymap.core.style.model.feature.Font,FontDescriptor> {

        public Serializer( Context context ) {
            super( context );
        }

        
        @Override
        protected FontDescriptor createDescriptor() {
            return new FontDescriptor();
        }


        @Override
        public void doSerialize( org.polymap.core.style.model.feature.Font font ) {
            setValue( font.style.get(), ( FontDescriptor sd, Expression value ) -> sd.style.set( value ) );
            setValue( font.weight.get(), ( FontDescriptor sd, Expression value ) -> sd.weight.set( value ) );
            setValue( font.size.get(), ( FontDescriptor sd, Expression value ) -> sd.size.set( value ) );

            if (font.family.get() != null) {
                if (font.family.get() instanceof ConstantFontFamily) {
                    FontFamily fontFamily = ((ConstantFontFamily)font.family.get()).value.get();
                    if (fontFamily != null) {
                        String[] families = fontFamily.families();
                        for (FontDescriptor descriptor : descriptors) {
                            org.geotools.styling.Font[] fonts = new org.geotools.styling.Font[families.length];
                            for (int i = 0; i < families.length; i++) {
                                fonts[i] = SLDSerializer.sf.createFont( SLDSerializer.ff.literal( families[i].trim() ),
                                        descriptor.style.isPresent() ? descriptor.style.get()
                                                : SLDSerializer.ff.literal( FontStyle.normal ),
                                        descriptor.weight.isPresent() ? descriptor.weight.get()
                                                : SLDSerializer.ff.literal( FontWeight.normal ),
                                        descriptor.size.get() );
                                descriptor.fonts.set( fonts );
                            }
                        }
                    }
                }
                else if (font.family.get().getClass().equals( NoValue.class)) {
                    // ignore
                }
                else {
                    throw new UnsupportedOperationException( font.family.get().getClass() + " is not supported" );
                }
            }
        }
    }

}
