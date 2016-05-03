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
public class ConstantStrokeDashStyle
        extends StylePropertyValue<StrokeDashStyle> {

    /**
     * Initializes a newly created instance with default values.
     */
    public static ValueInitializer<ConstantStrokeDashStyle> defaults() {
        return defaults( StrokeDashStyle.solid );
    }


    public static ValueInitializer<ConstantStrokeDashStyle> defaults( final StrokeDashStyle style ) {
        return new ValueInitializer<ConstantStrokeDashStyle>() {

            @Override
            public ConstantStrokeDashStyle initialize( ConstantStrokeDashStyle proto ) throws Exception {
                proto.value.set( style );
                return proto;
            }
        };
    }

    @Concerns(StylePropertyChange.Concern.class)
    public Property<StrokeDashStyle> value;
}
