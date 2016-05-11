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

import org.polymap.model2.Concerns;
import org.polymap.model2.Description;
import org.polymap.model2.Nullable;
import org.polymap.model2.Property;
import org.polymap.model2.runtime.ValueInitializer;

/**
 * @author Steffen Stundzig
 */
public class LabelPlacement
        extends StyleComposite {

    /**
     * Initializes a newly created instance with default values.
     */
    @SuppressWarnings("hiding")
    public static final ValueInitializer<LabelPlacement> defaults = new ValueInitializer<LabelPlacement>() {

        @Override
        public LabelPlacement initialize( LabelPlacement proto ) throws Exception {
            StyleComposite.defaults.initialize( proto );
            return proto;
        }
    };

    @Nullable
    @UIOrder(80)
    @Description("anchorPointX")
    @DoubleRange( from=0, to=Integer.MAX_VALUE, defaultValue=2 )
    @Concerns(StylePropertyChange.Concern.class)
    public Property<StylePropertyValue<Double>> anchorPointX;

    @Nullable
    @UIOrder(90)
    @Description("anchorPointY")
    @DoubleRange( from=0, to=Integer.MAX_VALUE, defaultValue=2 )
    @Concerns(StylePropertyChange.Concern.class)
    public Property<StylePropertyValue<Double>> anchorPointY;

    @Nullable
    @UIOrder(100)
    @Description("displacementX")
    @DoubleRange( from=0, to=Integer.MAX_VALUE, defaultValue=2 )
    @Concerns(StylePropertyChange.Concern.class)
    public Property<StylePropertyValue<Double>> displacementX;

    @Nullable
    @UIOrder(110)
    @Description("displacementY")
    @DoubleRange( from=0, to=Integer.MAX_VALUE, defaultValue=2 )
    @Concerns(StylePropertyChange.Concern.class)
    public Property<StylePropertyValue<Double>> displacementY;

    @Nullable
    @UIOrder(120)
    @Description("rotation")
    @DoubleRange( from=0, to=360, defaultValue=0 )
    @Concerns(StylePropertyChange.Concern.class)
    public Property<StylePropertyValue<Double>> rotation;

    @Nullable
    @UIOrder(130)
    @Description("offset")
    @DoubleRange( from=0, to=100, defaultValue=0 )
    @Concerns(StylePropertyChange.Concern.class)
    public Property<StylePropertyValue<Double>> offset;
}
