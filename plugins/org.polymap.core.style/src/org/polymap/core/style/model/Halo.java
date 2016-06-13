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
package org.polymap.core.style.model;

import java.awt.Color;

import org.polymap.model2.Concerns;
import org.polymap.model2.Description;
import org.polymap.model2.Nullable;
import org.polymap.model2.Property;
import org.polymap.model2.runtime.ValueInitializer;

/**
 * @author Steffen Stundzig
 */
public class Halo
        extends StyleComposite {

    /**
     * Initializes a newly created instance with default values.
     */
    @SuppressWarnings("hiding")
    public static final ValueInitializer<Halo> defaults = new ValueInitializer<Halo>() {

        @Override
        public Halo initialize( Halo proto ) throws Exception {
            StyleComposite.defaults.initialize( proto );
            return proto;
        }
    };

    @Nullable
    @UIOrder(50)
    @Description("width")
    @DoubleRange( from=0, to=1000, defaultValue=2 )
    @Concerns(StylePropertyChange.Concern.class)
    public Property<StylePropertyValue<Double>> width;

    @Nullable
    @UIOrder(60)
    @Description("color")
    @Concerns(StylePropertyChange.Concern.class)
    public Property<StylePropertyValue<Color>> color;

    @Nullable
    @UIOrder(70)
    @Description("opacity")
    @DoubleRange(to = 1, defaultValue = 1, increment = 0.1)
    @Concerns(StylePropertyChange.Concern.class)
    public Property<StylePropertyValue<Double>> opacity;
}
