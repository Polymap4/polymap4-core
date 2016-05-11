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

import org.polymap.core.runtime.config.Config;

import org.polymap.model2.Immutable;

/**
 * @author Steffen Stundzig
 */
public class TextSymbolizerDescriptor
        extends SymbolizerDescriptor {

    @Immutable
    // String
    public Config<Expression> text;

    @Immutable
    // Color
    public Config<Expression> color;

    @Immutable
    // @DefaultDouble( 1 )
    // @Check(value = NumberRangeValidator.class, args = { "0", "1" })
    // Double
    public Config<Expression> opacity;

    @Immutable
    public Config<FontDescriptor> font;

    @Immutable
    // @DefaultDouble( 2 )
    // @Check(value = NumberRangeValidator.class, args = { "0", "100" })
    // Double
    public Config<Expression> haloWidth;

    @Immutable
    // Color
    public Config<Expression> haloColor;

    @Immutable
    // @DefaultDouble( 1 )
    // @Check(value = NumberRangeValidator.class, args = { "0", "1" })
    // Double
    public Config<Expression> haloOpacity;

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
    public Config<Expression> placementRotation;

    @Immutable
    // @DefaultDouble( 2 )
    // @Check(value = NumberRangeValidator.class, args = { "0", "100" })
    // Double
    public Config<Expression> placementOffset;


    @Override
    protected TextSymbolizerDescriptor clone() {
        return (TextSymbolizerDescriptor)super.clone();
    }

}
