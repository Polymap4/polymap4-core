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

import org.polymap.core.style.model.ConstantFontFamily;
import org.polymap.core.style.model.Font;
import org.polymap.core.style.model.FontFamily;
import org.polymap.core.style.model.FontStyle;
import org.polymap.core.style.model.FontWeight;

/**
 * Serialize {@link Font}. Since fonts are handled as special Font[] and not as Expressions, here the
 * Serializer must implement this. Other Values than ConstantFontFamily are currently not
 * supported.
 * 
 * @author Steffen Stundzig
 */
public class FontSerializer
        extends StyleCompositeSerializer<Font,FontDescriptor> {

    private static Log log = LogFactory.getLog( FontSerializer.class );


    @Override
    protected FontDescriptor createDescriptor() {
        return new FontDescriptor();
    }


    @Override
    public void doSerialize( Font font ) {
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
            else {
                throw new UnsupportedOperationException( font.family.get().getClass() + " is not supported" );
            }
        }
    }
}
