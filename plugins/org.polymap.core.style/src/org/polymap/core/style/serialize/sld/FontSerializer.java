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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.style.model.Font;
import org.polymap.core.style.model.FontFamily;
import org.polymap.core.style.model.FontStyle;
import org.polymap.core.style.model.FontWeight;

/**
 * Serialize {@link Font}.
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
        setValue( font.family.get(), ( FontDescriptor sd, FontFamily value ) -> sd.family.set( value ) );
        setValue( font.style.get(), ( FontDescriptor sd, FontStyle value ) -> sd.style.set( value ) );
        setValue( font.weight.get(), ( FontDescriptor sd, FontWeight value ) -> sd.weight.set( value ) );
        setValue( font.size.get(), ( FontDescriptor sd, Double value ) -> sd.size.set( value ) );
    }
}
