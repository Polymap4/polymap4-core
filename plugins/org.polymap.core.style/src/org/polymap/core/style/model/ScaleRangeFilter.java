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

import org.opengis.filter.Filter;

import org.polymap.model2.Concerns;
import org.polymap.model2.Nullable;
import org.polymap.model2.Property;
import org.polymap.model2.runtime.ValueInitializer;

/**
 * @author Steffen Stundzig
 */
public class ScaleRangeFilter
        extends StylePropertyValue<Filter> {

    public static ValueInitializer<ScaleRangeFilter> defaults() {
        return defaults( 1, 80000000 );
    }


    public static ValueInitializer<ScaleRangeFilter> defaults( final Integer minScale, final Integer maxScale ) {
        return new ValueInitializer<ScaleRangeFilter>() {

            @Override
            public ScaleRangeFilter initialize( ScaleRangeFilter proto ) throws Exception {
                proto.minScale.set( minScale );
                proto.maxScale.set( maxScale );
                return proto;
            }
        };
    }

    @Nullable
    @Concerns(StylePropertyChange.Concern.class)
    public Property<Integer> minScale;

    @Nullable
    @Concerns(StylePropertyChange.Concern.class)
    public Property<Integer> maxScale;

}
