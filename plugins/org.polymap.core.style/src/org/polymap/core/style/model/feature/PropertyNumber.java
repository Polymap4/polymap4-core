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

import org.polymap.core.style.model.StylePropertyChange;

import org.polymap.model2.Concerns;
import org.polymap.model2.Nullable;
import org.polymap.model2.Property;
import org.polymap.model2.runtime.ValueInitializer;

/**
 * Describes a feature property based number.
 *
 * @author Steffen Stundzig
 */
public class PropertyNumber<T extends Number>
        extends PropertyValue<T> {
    

    /**
     * Initializes a newly created instance with the given default value.
     */
    public static ValueInitializer<PropertyNumber> defaults( final String value, final Double minimum, final Double maximum ) {
        return new ValueInitializer<PropertyNumber>() {

            @Override
            public PropertyNumber initialize( PropertyNumber proto ) throws Exception {
                proto.propertyName.set( value );
                proto.minimumValue.set( minimum );
                proto.maximumValue.set( maximum );
                return proto;
            }
        };
    }

    // instance *******************************************

    @Nullable
    @NumberRange(defaultValue=0)
    @Concerns( StylePropertyChange.Concern.class )
    public Property<Double> minimumValue;

    @Nullable
    @NumberRange(defaultValue=10000)
    @Concerns( StylePropertyChange.Concern.class )
    public Property<Double> maximumValue;
    
}
