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
public class Font
        extends StyleComposite {

    /**
     * Initializes a newly created instance with default values.
     */
    @SuppressWarnings( "hiding" )
    public static final ValueInitializer<Font>      defaults = new ValueInitializer<Font>() {
        @Override
        public Font initialize( Font proto ) throws Exception {
            proto.family.createValue( ConstantFontFamily.defaults() );
            proto.style.createValue( ConstantFontStyle.defaults() );
            proto.weight.createValue( ConstantFontWeight.defaults() );
            proto.size.createValue( ConstantNumber.defaults( 10.0 ) );
            return proto;
        }
    };

    // @NumberRange( 0.0, 1.0 );
    @Nullable
    @UIOrder( 10 )
    @Description( "Text: font family" )
    @Concerns( StylePropertyChange.Concern.class )
    public Property<StylePropertyValue<FontFamily>> family;

    @Nullable
    @UIOrder( 20 )
    @Description( "Text: font style" )
    @Concerns( StylePropertyChange.Concern.class )
    public Property<StylePropertyValue<FontStyle>>  style;

    @Nullable
    @UIOrder( 30 )
    @Description( "Text: font weight" )
    @Concerns( StylePropertyChange.Concern.class )
    public Property<StylePropertyValue<FontWeight>> weight;

    @Nullable
    @UIOrder( 40 )
    @Description( "Text: font size" )
    @Concerns( StylePropertyChange.Concern.class )
    public Property<StylePropertyValue<Double>>     size;
}
