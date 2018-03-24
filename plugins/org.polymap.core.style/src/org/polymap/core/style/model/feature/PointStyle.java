/* 
 * polymap.org
 * Copyright (C) 2016, the @authors. All rights reserved.
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

import org.polymap.core.style.model.Style;
import org.polymap.core.style.model.StylePropertyChange;
import org.polymap.core.style.model.StylePropertyValue;
import org.polymap.core.style.model.UIOrder;
import org.polymap.core.style.model.feature.Graphic.WellKnownMark;

import org.polymap.model2.Concerns;
import org.polymap.model2.Description;
import org.polymap.model2.Nullable;
import org.polymap.model2.Property;
import org.polymap.model2.runtime.ValueInitializer;

/**
 * Simple point style. Roughly modelling: 
 * <ul>
 * <li>{@link org.opengis.style.PointSymbolizer}</li>
 * <li>{@link org.opengis.style.Mark}</li>
 * </ul>
 *
 * @author Falko Bräutigam
 * @author Steffen Stundzig
 */
public class PointStyle
        extends Style {

    /** Initializes a newly created instance with default values. */
    @SuppressWarnings("hiding")
    public static final ValueInitializer<PointStyle> defaults = new ValueInitializer<PointStyle>() {
        @Override
        public PointStyle initialize( PointStyle proto ) throws Exception {
            Style.defaults.initialize( proto );
            proto.title.set( "Point/Mark" );
            //proto.graphic.createValue( ConstantGraphic.defaults( WellKnownMark.Circle ) );
            proto.diameter.createValue( ConstantNumber.defaults( 10.0 ) );
            proto.rotation.createValue( ConstantNumber.defaults( 0.0 ) );
            proto.fill.createValue( Fill.defaults );
            proto.stroke.createValue( Stroke.defaults );
            return proto;
        }
    };

    @Nullable
    @UIOrder(10)
    @Description("diameter")
    @NumberRange(defaultValue = 10, from = 0, to = 100, increment = 1.0)
    @Concerns(StylePropertyChange.Concern.class)
    public Property<StylePropertyValue<Double>>      diameter;

    /** One of {@link WellKnownMark} or an external icon/image. */
    @Nullable
    @UIOrder(12)
    @Description("graphic")
    @Concerns(StylePropertyChange.Concern.class)
    public Property<StylePropertyValue<Graphic>>     graphic;

    @Nullable
    @UIOrder(14)
    @Description("rotation")
    @NumberRange(defaultValue = 0, from = 0, to = 359, increment = 5.0)
    @Concerns(StylePropertyChange.Concern.class)
    public Property<StylePropertyValue<Double>>      rotation;

    /** The fill if {@link #graphic} is a mark, otherwise ignored. */
    @UIOrder(20)
    @Description("fill")
    @Concerns(StylePropertyChange.Concern.class)
    public Property<Fill>                            fill;

    /** The stroke if {@link #graphic} is a mark, otherwise ignored. */
    @UIOrder(30)
    @Description("stroke")
    @Concerns(StylePropertyChange.Concern.class)
    public Property<Stroke>                          stroke;
    
}
