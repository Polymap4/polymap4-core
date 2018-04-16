/* 
 * polymap.org
 * Copyright (C) 2018, the @authors. All rights reserved.
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
 * 
 * @author Falko Bräutigam
 */
public class Displacement
        extends StyleComposite {

    /** Initializes a newly created instance with default values. */
    public static final ValueInitializer<Displacement> defaults( int offsetX, int offsetY, int offsetSize ) {
        return new ValueInitializer<Displacement>() {
            @Override
            public Displacement initialize( Displacement proto ) throws Exception {
                StyleComposite.defaults.initialize( proto );
                proto.offsetX.createValue( ConstantNumber.defaults( offsetX ) );
                proto.offsetY.createValue( ConstantNumber.defaults( offsetY ) );
                proto.offsetSize.createValue( ConstantNumber.defaults( offsetSize ) );
                return proto;
            }
        };
    }

    /** Pixel offset. */
    @Nullable
    @UIOrder(10)
    @Description("offsetX")
    @NumberRange(defaultValue = 0, from = -100, to = 100, increment = 1)
    @Concerns(StylePropertyChange.Concern.class)
    public Property<StylePropertyValue<Integer>> offsetX;

    /** Pixel offset. */
    @Nullable
    @UIOrder(20)
    @Description("offsetY")
    @NumberRange(defaultValue = 0, from = -100, to = 100, increment = 1)
    @Concerns(StylePropertyChange.Concern.class)
    public Property<StylePropertyValue<Integer>> offsetY;

    @Nullable
    @UIOrder(30)
    @Description("offsetSize")
    @NumberRange(defaultValue = 0, from = -100, to = 100, increment = 1)
    @Concerns(StylePropertyChange.Concern.class)
    public Property<StylePropertyValue<Integer>> offsetSize;

}
