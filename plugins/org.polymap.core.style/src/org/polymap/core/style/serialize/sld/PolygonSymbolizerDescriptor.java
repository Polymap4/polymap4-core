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
import org.polymap.core.runtime.config.Immutable;
import org.polymap.core.runtime.config.NumberRangeValidator;
import org.polymap.core.style.model.StrokeCapStyle;
import org.polymap.core.style.model.StrokeJoinStyle;

/**
 * 
 *
 * @author Steffen Stundzig
 */
public class PolygonSymbolizerDescriptor
        extends SymbolizerDescriptor {

    @Immutable
    public Config<Double>          strokeWidth;

    @Immutable
    public Config<Color>           strokeColor;

    @Immutable
    @DefaultDouble(1)
    @Check(value = NumberRangeValidator.class, args = { "0", "1" })
    public Config<Double>          strokeOpacity;

    @Immutable
    public Config<Color>           fillColor;

    @Immutable
    @DefaultDouble(1)
    @Check(value = NumberRangeValidator.class, args = { "0", "1" })
    public Config<Double>          fillOpacity;

    @Immutable
    public Config<StrokeCapStyle>  strokeCapStyle;

    @Immutable
    public Config<StrokeJoinStyle> strokeJoinStyle;

    @Immutable
    public Config<float[]>         strokeDashStyle;


    @Override
    protected PolygonSymbolizerDescriptor clone() {
        return (PolygonSymbolizerDescriptor)super.clone();
    }

}
