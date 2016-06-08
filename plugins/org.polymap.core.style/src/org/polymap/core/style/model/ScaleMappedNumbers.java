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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.model2.CollectionProperty;
import org.polymap.model2.Concerns;
import org.polymap.model2.Property;
import org.polymap.model2.runtime.ValueInitializer;

/**
 * Conditions based numbers.
 * 
 * @author Steffen Stundzig
 */
public class ScaleMappedNumbers<T extends Number>
        extends StylePropertyValue<T> {

    private static Log log = LogFactory.getLog( ScaleMappedNumbers.class );

    /**
     * Initializes a newly created instance with default values.
     */
    public static <R extends Number> ValueInitializer<ScaleMappedNumbers<R>> defaults() {
        return new ValueInitializer<ScaleMappedNumbers<R>>() {
            @Override
            public ScaleMappedNumbers<R> initialize( ScaleMappedNumbers<R> proto ) throws Exception {
                return proto;
            }
        };
    }
    

    // instance *******************************************
    
    @Concerns( StylePropertyChange.Concern.class )
    public Property<Number> defaultNumberValue;
    
    //@Concerns( StylePropertyChange.Concern.class )
    public CollectionProperty<Number> numberValues;
    
    //@Concerns( StylePropertyChange.Concern.class )
    public CollectionProperty<Number> scales;
    
    public ScaleMappedNumbers add(final Double number, final Double scale) {
        numberValues.add( number );
        scales.add( scale );
        return this;
    }
}
