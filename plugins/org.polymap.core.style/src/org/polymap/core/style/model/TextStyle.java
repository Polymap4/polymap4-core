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
    public static final ValueInitializer<TextStyle> defaults = new ValueInitializer<TextStyle>() {

        @Override
        public TextStyle initialize( TextStyle proto ) throws Exception {
            Style.defaults.initialize( proto );
            proto.title.set( "Text" );
            proto.font.createValue( Font.defaults );
            return proto;
        }
    };

    @Nullable
    @UIOrder(10)
    @Description("property")
    @Concerns(StylePropertyChange.Concern.class)
    public Property<StylePropertyValue<String>> textProperty;

    @Nullable
    @UIOrder(20)
    @Description("textColor")
    @Concerns(StylePropertyChange.Concern.class)
    public Property<StylePropertyValue<Color>> color;

    @Nullable
    @UIOrder(30)
    @Description("textOpacity")
    @DoubleRange( from=0, to=1, defaultValue=1 )
    @Concerns(StylePropertyChange.Concern.class)
    public Property<StylePropertyValue<Double>> opacity;

    @UIOrder(40)
    @Description("font")
    public Property<Font> font;

    @Nullable
    @UIOrder(50)
    @Description("backgroundWidth")
    @DoubleRange( from=0, to=100, defaultValue=2 )
    @Concerns(StylePropertyChange.Concern.class)
    public Property<StylePropertyValue<Double>> haloWidth;

    @Nullable
    @UIOrder(60)
    @Description("backgroundColor")
    @Concerns(StylePropertyChange.Concern.class)
    public Property<StylePropertyValue<Color>> haloColor;

    @Nullable
    @UIOrder(70)
    @Description("backgroundOpacity")
    @DoubleRange( from=0, to=1, defaultValue=1 )
    @Concerns(StylePropertyChange.Concern.class)
    public Property<StylePropertyValue<Double>> haloOpacity;

    @Nullable
    @UIOrder(80)
    @Description("labelAnchor")
    @Concerns(StylePropertyChange.Concern.class)
    public Property<StylePropertyValue<Point>> anchorPoint;

    @Nullable
    @UIOrder(90)
    @Description("labelDisplacement")
    @Concerns(StylePropertyChange.Concern.class)
    public Property<StylePropertyValue<Point>> displacement;

    @Nullable
    @UIOrder(100)
    @Description("labelRotation")
    @DoubleRange( from=0, to=360, defaultValue=0 )
    @Concerns(StylePropertyChange.Concern.class)
    public Property<StylePropertyValue<Double>> placementRotation;

    @Nullable
    @UIOrder(110)
    @Description("labelOffset")
    @DoubleRange( from=0, to=100, defaultValue=0 )
    @Concerns(StylePropertyChange.Concern.class)
    public Property<StylePropertyValue<Double>> placementOffset;

}
