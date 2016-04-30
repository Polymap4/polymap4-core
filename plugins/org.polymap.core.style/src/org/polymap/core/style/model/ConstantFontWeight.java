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
import org.polymap.model2.Property;
import org.polymap.model2.runtime.ValueInitializer;

/**
 * 
 *
 * @author Steffen Stundzig
 */
public class ConstantFontWeight
        extends StylePropertyValue<FontWeight> {

    /**
     * Initializes a newly created instance with default values.
     */
    public static ValueInitializer<ConstantFontWeight> defaults() {
        return defaults( FontWeight.normal );
    }


    public static ValueInitializer<ConstantFontWeight> defaults( final FontWeight weight ) {
        return new ValueInitializer<ConstantFontWeight>() {

            @Override
            public ConstantFontWeight initialize( final ConstantFontWeight proto ) throws Exception {
                proto.value.set( weight );
                return proto;
            }
        };
    }

    @Concerns(StylePropertyChange.Concern.class)
    public Property<FontWeight> value;
}
