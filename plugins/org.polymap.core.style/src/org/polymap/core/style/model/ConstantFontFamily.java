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
public class ConstantFontFamily
        extends StylePropertyValue<FontFamily> {

    /**
     * Initializes a newly created instance with default values.
     */
    public static ValueInitializer<ConstantFontFamily> defaults() {
        return defaults( FontFamily.sansSerif );
    }


    public static ValueInitializer<ConstantFontFamily> defaults( final FontFamily family ) {
        return new ValueInitializer<ConstantFontFamily>() {

            @Override
            public ConstantFontFamily initialize( final ConstantFontFamily proto ) throws Exception {
                proto.value.set( family );
                return proto;
            }
        };
    }

    @Concerns(StylePropertyChange.Concern.class)
    public Property<FontFamily> value;
}
