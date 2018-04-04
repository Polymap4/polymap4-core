/* 
 * polymap.org
 * Copyright (C) 2016-2018, the @authors. All rights reserved.
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
package org.polymap.core.style.model.feature;

import java.awt.Color;

import org.polymap.core.style.model.StyleComposite;
import org.polymap.core.style.model.StylePropertyChange;
import org.polymap.core.style.model.StylePropertyValue;
import org.polymap.core.style.model.UIOrder;

import org.polymap.model2.Concerns;
import org.polymap.model2.Description;
import org.polymap.model2.Nullable;
import org.polymap.model2.Property;
import org.polymap.model2.runtime.ValueInitializer;

/**
 * 
 * @author Falko Bräutigam
 * @author Steffen Stundzig
 */
public class Stroke
        extends StyleComposite {

    /** Initializes a newly created instance with default values. */
    @SuppressWarnings("hiding")
    public static final ValueInitializer<Stroke> defaults = new ValueInitializer<Stroke>() {
        @Override
        public Stroke initialize( Stroke proto ) throws Exception {
            StyleComposite.defaults.initialize( proto );
            proto.width.createValue( ConstantNumber.defaults( 0.5 ) );
            proto.opacity.createValue( ConstantNumber.defaults( 1.0 ) );
            proto.strokeStyle.createValue( StrokeStyle.defaults );
            return proto;
        }
    };

    @Nullable
    @UIOrder(10)
    @Description("width")
    @NumberRange(defaultValue = 0.5, from = 0, to = 100, increment = 0.1)
    @Concerns(StylePropertyChange.Concern.class)
    public Property<StylePropertyValue<Double>>  width;

    @Nullable
    @UIOrder(20)
    @Description("color")
    @Concerns(StylePropertyChange.Concern.class)
    public Property<StylePropertyValue<Color>>   color;

    @Nullable
    @UIOrder(30)
    @Description("opacity")
    @NumberRange(defaultValue = 1, from = 0, to = 1, increment = 0.1)
    @Concerns(StylePropertyChange.Concern.class)
    public Property<StylePropertyValue<Double>>  opacity;

    @UIOrder(40)
    @Description("strokeStyle")
    public Property<StrokeStyle>                 strokeStyle;

}
