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

/**
 * @author Steffen Stundzig
 */
public class FillDescriptor
        extends SymbolizerDescriptor {

    @Immutable
    public Config<Color> color;

    @Immutable
    @DefaultDouble(1)
    @Check(value = NumberRangeValidator.class, args = { "0", "1" })
    public Config<Double> opacity;


    @Override
    protected FillDescriptor clone() {
        return (FillDescriptor)super.clone();
    }

}
