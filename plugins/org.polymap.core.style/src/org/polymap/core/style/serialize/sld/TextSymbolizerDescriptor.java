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

import java.awt.Color;

import org.polymap.core.runtime.config.Check;
import org.polymap.core.runtime.config.Config;
import org.polymap.core.runtime.config.DefaultDouble;
import org.polymap.core.runtime.config.NumberRangeValidator;
import org.polymap.core.style.model.Point;

import org.polymap.model2.Immutable;

/**
 * @author Steffen Stundzig
 */
public class TextSymbolizerDescriptor
        extends SymbolizerDescriptor {

    @Immutable
    public Config<String> textProperty;

    @Immutable
    public Config<Color> color;

    @Immutable
    @DefaultDouble( 1 )
    @Check(value = NumberRangeValidator.class, args = { "0", "1" })
    public Config<Double> opacity;

    @Immutable
    public Config<FontDescriptor> font;

    @Immutable
    @DefaultDouble( 2 )
    @Check(value = NumberRangeValidator.class, args = { "0", "100" })
    public Config<Double> haloWidth;

    @Immutable
    public Config<Color> haloColor;

    @Immutable
    @DefaultDouble( 1 )
    @Check(value = NumberRangeValidator.class, args = { "0", "1" })
    public Config<Double> haloOpacity;

    @Immutable
    public Config<Point> anchorPoint;

    @Immutable
    public Config<Point> displacement;

    @Immutable
    @DefaultDouble( 0 )
    @Check(value = NumberRangeValidator.class, args = { "0", "360" })
    public Config<Double> placementRotation;

    @Immutable
    @DefaultDouble( 2 )
    @Check(value = NumberRangeValidator.class, args = { "0", "100" })
    public Config<Double> placementOffset;

    @Override
    protected TextSymbolizerDescriptor clone() {
        return (TextSymbolizerDescriptor)super.clone();
    }

}
