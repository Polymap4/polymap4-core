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
            proto.title.set( "Text style" );
            proto.font.createValue( Font.defaults );
            proto.halo.createValue( Halo.defaults );
            proto.labelPlacement.createValue( LabelPlacement.defaults );
            return proto;
        }
    };

    @Nullable
    @UIOrder(10)
    @Description("property")
    @Concerns(StylePropertyChange.Concern.class)
    public Property<StylePropertyValue<String>> property;

    @Nullable
    @UIOrder(20)
    @Description("color")
    @Concerns(StylePropertyChange.Concern.class)
    public Property<StylePropertyValue<Color>> color;

    @Nullable
    @UIOrder(30)
    @Description("opacity")
    @NumberRange(to = 1, defaultValue = 1, increment = 0.1)
    @Concerns(StylePropertyChange.Concern.class)
    public Property<StylePropertyValue<Double>> opacity;

    @UIOrder(40)
    @Description("font")
    public Property<Font> font;

    @UIOrder(50)
    @Description("halo")
    public Property<Halo> halo;

    @UIOrder(60)
    @Description("labelPlacement")
    public Property<LabelPlacement> labelPlacement;
}
