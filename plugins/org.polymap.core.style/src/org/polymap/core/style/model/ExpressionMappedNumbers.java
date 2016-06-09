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

import java.util.Collection;

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
public class ExpressionMappedNumbers<T extends Number>
        extends ExpressionMappedValues<T> {

    private static Log log = LogFactory.getLog( ExpressionMappedNumbers.class );

    /**
     * Initializes a newly created instance with default values.
     */
    public static <R extends Number> ValueInitializer<ExpressionMappedNumbers<R>> defaults() {
        return new ValueInitializer<ExpressionMappedNumbers<R>>() {
            @Override
            public ExpressionMappedNumbers<R> initialize( ExpressionMappedNumbers<R> proto ) throws Exception {
                return proto;
            }
        };
    }
    

    // instance *******************************************
    

    @Concerns( StylePropertyChange.Concern.class )
    public Property<Number> defaultNumberValue;
    
    //@Concerns( StylePropertyChange.Concern.class )
    public CollectionProperty<Number> numberValues;


    @Override
    public ExpressionMappedNumbers add( T value ) {
        numberValues.add( value );
        return this;
    }


    @Override
    public T defaultValue() {
        return (T)defaultNumberValue.get();
    }


    @Override
    public Collection<T> values() {
        return (Collection<T>)numberValues;
    }
}
