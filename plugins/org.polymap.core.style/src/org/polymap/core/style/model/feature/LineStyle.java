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
package org.polymap.core.style.model.feature;

import org.polymap.core.style.model.Style;
import org.polymap.core.style.model.StylePropertyChange;
import org.polymap.core.style.model.UIOrder;

import org.polymap.model2.Concerns;
import org.polymap.model2.Description;
import org.polymap.model2.Property;
import org.polymap.model2.runtime.ValueInitializer;

/**
 * Simple line style with a stroke.
 * <ul>
 * <li>{@link org.opengis.style.LineSymbolizer}</li>
 * <li>{@link org.opengis.style.Mark}</li>
 * </ul>
 *
 * @author Steffen Stundzig
 */
public class LineStyle
        extends Style {

    /**
     * Initializes a newly created instance with default values.
     */
    @SuppressWarnings("hiding")
    public static final ValueInitializer<LineStyle> defaults = new ValueInitializer<LineStyle>() {

        @Override
        public LineStyle initialize( LineStyle proto ) throws Exception {
            Style.defaults.initialize( proto );
            proto.title.set( "Line style" );
            proto.fill.createValue( Stroke.defaults );
            proto.stroke.createValue( Stroke.defaults );
            return proto;
        }
    };

    @UIOrder(10)
    @Description("fill")
    @Concerns(StylePropertyChange.Concern.class)
    public Property<Stroke> fill;

    @UIOrder(20)
    @Description("stroke")
    @Concerns(StylePropertyChange.Concern.class)
    public Property<Stroke> stroke;
//
//    @Nullable
//    @UIOrder(30)
//    @Description("offset")
//    @DoubleRange(from = 0, to = Double.MAX_VALUE, defaultValue = 8)
//    @Concerns(StylePropertyChange.Concern.class)
//    public Property<StylePropertyValue<Double>> offset;
}
