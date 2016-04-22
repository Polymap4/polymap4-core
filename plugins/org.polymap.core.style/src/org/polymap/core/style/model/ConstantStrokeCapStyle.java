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

import org.polymap.model2.Property;
import org.polymap.model2.runtime.ValueInitializer;

/**
 * 
 *
 * @author Steffen Stundzig
 */
public class ConstantStrokeCapStyle
        extends StylePropertyValue<StrokeCapStyle> {

    /**
     * Initializes a newly created instance with default values.
     */
    public static ValueInitializer<ConstantStrokeCapStyle> defaults() {
        return defaults( StrokeCapStyle.round );
    }


    public static ValueInitializer<ConstantStrokeCapStyle> defaults( final StrokeCapStyle style ) {
        return new ValueInitializer<ConstantStrokeCapStyle>() {

            @Override
            public ConstantStrokeCapStyle initialize( ConstantStrokeCapStyle proto ) throws Exception {
                proto.capStyle.set( style );
                return proto;
            }
        };
    }

    public Property<StrokeCapStyle> capStyle;
}
