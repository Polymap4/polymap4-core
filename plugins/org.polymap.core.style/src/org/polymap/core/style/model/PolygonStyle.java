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
 * Simple polygon style. Roughly modelling:
 * <ul>
 * <li>{@link org.opengis.style.PolygonSymbolizer}</li>
 * <li>{@link org.opengis.style.Mark}</li>
 * </ul>
 *
 * @author Steffen Stundzig
 */
public class PolygonStyle
        extends Style {

    /**
     * Initializes a newly created instance with default values.
     */
    @SuppressWarnings("hiding")
    public static final ValueInitializer<PolygonStyle>  defaults = new ValueInitializer<PolygonStyle>() {
        @Override
        public PolygonStyle initialize( PolygonStyle proto ) throws Exception {
            Style.defaults.initialize( proto );
            proto.title.set( "Polygon" );
            proto.stroke.createValue( Stroke.defaults );
            return proto;
        }
    };

    @Nullable
    @UIOrder(10)
    @Description("fillColor")
    @Concerns(StylePropertyChange.Concern.class)
    public Property<StylePropertyValue<Color>>           fillColor;

    // @NumberRange( 0.0, 1.0 );
    @Nullable
    @UIOrder(20)
    @Description("fillOpacity")
    @DoubleRange( from=0, to=1, defaultValue=1 )
    @Concerns(StylePropertyChange.Concern.class)
    public Property<StylePropertyValue<Double>>          fillOpacity;

    @UIOrder( 30 )
    @Description( "stroke" )
    public Property<Stroke>                     stroke;
}
