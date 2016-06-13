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
package org.polymap.core.style.model;

import org.polymap.model2.Concerns;
import org.polymap.model2.Description;
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

    /**
     * Initializes a newly created instance with default values.
     */
    @SuppressWarnings("hiding")
    public static final ValueInitializer<PointStyle> defaults = new ValueInitializer<PointStyle>() {
        @Override
        public PointStyle initialize( PointStyle proto ) throws Exception {
            Style.defaults.initialize( proto );
            proto.title.set( "Point/Mark" );
            proto.fill.createValue( Fill.defaults );
            proto.stroke.createValue( Stroke.defaults );
//            proto.diameter.createValue( ConstantNumber.defaults( 8.0 ) );
            return proto;
        }
    };

    @UIOrder(10)
    @Description("diameter")
    @DoubleRange(defaultValue = 8)
    @Concerns(StylePropertyChange.Concern.class)
    public Property<StylePropertyValue<Double>> diameter;

    @UIOrder(20)
    @Description("fill")
    public Property<Fill> fill;

    @UIOrder(30)
    @Description("stroke")
    public Property<Stroke> stroke;
}
