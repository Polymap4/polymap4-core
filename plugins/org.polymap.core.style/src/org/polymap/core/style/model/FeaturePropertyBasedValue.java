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

import org.polymap.model2.Nullable;
import org.polymap.model2.Property;
import org.polymap.model2.runtime.ValueInitializer;

/**
 * base class for feature property based values.
 *
 * @author Steffen Stundzig
 */
public class FeaturePropertyBasedValue<T>
        extends StylePropertyValue<T> {

    /**
     * Initializes a newly created instance with the given default value.
     */
    public static ValueInitializer<FeaturePropertyBasedValue> defaults() {
        return defaults( "" );
    }


    public static ValueInitializer<FeaturePropertyBasedValue> defaults( final String value ) {
        return new ValueInitializer<FeaturePropertyBasedValue>() {

            @Override
            public FeaturePropertyBasedValue initialize( FeaturePropertyBasedValue proto ) throws Exception {
                proto.value.set( value );
                return proto;
            }
        };
    }

    // instance *******************************************

    /**
     * the name of the feature attribute column
     */
    @Nullable
//    @Concerns(StylePropertyChange.Concern.class)
    public Property<String> value;
}
