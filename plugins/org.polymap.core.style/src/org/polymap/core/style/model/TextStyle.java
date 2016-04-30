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
    @SuppressWarnings( "hiding" )
    public static final ValueInitializer<TextStyle> defaults = new ValueInitializer<TextStyle>() {

        @Override
        public TextStyle initialize( TextStyle proto )
                throws Exception {
            Style.defaults.initialize( proto );
            proto.title.set( "Text" );
            return proto;
        }
    };

    // @NumberRange( 0.0, 1.0 );
    @Nullable
    @UIOrder(20)
    @Description("Text: property")
    @Concerns(StylePropertyChange.Concern.class)
    public Property<StylePropertyValue<String>> textProperty;

    @Nullable
    @UIOrder(10)
    @Description("Text: color")
    @Concerns(StylePropertyChange.Concern.class)
    public Property<StylePropertyValue<Color>> color;

    // @NumberRange( 0.0, 1.0 );
    @Nullable
    @UIOrder(20)
    @Description("Text: font")
    public Property<Font> font;

    // @NumberRange( 0.0, 1.0 );
    @Nullable
    @UIOrder(20)
    @Description("Text: opacity")
    @Concerns(StylePropertyChange.Concern.class)
    public Property<StylePropertyValue<Double>> opacity;

    @Nullable
    @UIOrder(30)
    @Description("Background: width")
    @Concerns(StylePropertyChange.Concern.class)
    public Property<StylePropertyValue<Double>> haloWidth;

    @Nullable
    @UIOrder(40)
    @Description("Background: color ")
    @Concerns(StylePropertyChange.Concern.class)
    public Property<StylePropertyValue<Color>> haloColor;

    @Nullable
    @UIOrder(50)
    @Description("Background: opacity")
    @Concerns(StylePropertyChange.Concern.class)
    public Property<StylePropertyValue<Double>> haloOpacity;

    @Nullable
    @UIOrder(50)
    @Description("Anchor: x,y")
    @Concerns(StylePropertyChange.Concern.class)
    public Property<StylePropertyValue<Point>> anchorPoint;

    @Nullable
    @UIOrder(50)
    @Description("Displacement: x,y")
    @Concerns(StylePropertyChange.Concern.class)
    public Property<StylePropertyValue<Point>> displacement;

    @Nullable
    @UIOrder(50)
    @Description("Label: Rotation")
    @Concerns(StylePropertyChange.Concern.class)
    public Property<StylePropertyValue<Double>> placementRotation;

    @Nullable
    @UIOrder(50)
    @Description("Line Label: Offset")
    @Concerns(StylePropertyChange.Concern.class)
    public Property<StylePropertyValue<Double>> placementOffset;

}
