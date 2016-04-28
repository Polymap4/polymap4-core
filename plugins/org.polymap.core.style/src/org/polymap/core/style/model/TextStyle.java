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
public class TextStyle
        extends Style {

    /**
     * Initializes a newly created instance with default values.
     */
    @SuppressWarnings("hiding")
    public static final ValueInitializer<TextStyle>  defaults = new ValueInitializer<TextStyle>() {
        @Override
        public TextStyle initialize( TextStyle proto ) throws Exception {
            Style.defaults.initialize( proto );
            proto.title.set( "Text" );
            return proto;
        }
    };

    @Nullable
    @UIOrder(10)
    @Description("Text: color")
    @Concerns(StylePropertyChange.Concern.class)
    public Property<StylePropertyValue<Color>>           color;

    // @NumberRange( 0.0, 1.0 );
    @Nullable
    @UIOrder(20)
    @Description("Text: opacity")
    @Concerns(StylePropertyChange.Concern.class)
    public Property<StylePropertyValue<Double>>          opacity;

    @Nullable
    @UIOrder(30)
    @Description("Outer stroke: width")
    @Concerns(StylePropertyChange.Concern.class)
    public Property<StylePropertyValue<Double>>          strokeWidth;

    @Nullable
    @UIOrder(40)
    @Description("Text: Label placement")
    @Concerns(StylePropertyChange.Concern.class)
    public Property<StylePropertyValue<LabelPlacement>>           labelPlacement;

    @Nullable
    @UIOrder(50)
    @Description("Outer stroke: opacity")
    @Concerns(StylePropertyChange.Concern.class)
    public Property<StylePropertyValue<Double>>          strokeOpacity;

    @Nullable
    @UIOrder(60)
    @Description("Outer stroke: cap style")
    @Concerns(StylePropertyChange.Concern.class)
    public Property<StylePropertyValue<StrokeCapStyle>>  strokeCapStyle;

    @Nullable
    @UIOrder(70)
    @Description("Outer stroke: dash style")
    @Concerns(StylePropertyChange.Concern.class)
    public Property<StylePropertyValue<StrokeDashStyle>> strokeDashStyle;

    @Nullable
    @UIOrder(80)
    @Description("Outer stroke: join style")
    @Concerns(StylePropertyChange.Concern.class)
    public Property<StylePropertyValue<StrokeJoinStyle>> strokeJoinStyle;
}
