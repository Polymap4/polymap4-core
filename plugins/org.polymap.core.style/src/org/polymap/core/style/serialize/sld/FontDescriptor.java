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

import org.geotools.styling.Font;
import org.opengis.filter.expression.Expression;

import org.polymap.core.runtime.config.Config;
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
    protected FontDescriptor clone() {
        return (FontDescriptor)super.clone();
    }

}
