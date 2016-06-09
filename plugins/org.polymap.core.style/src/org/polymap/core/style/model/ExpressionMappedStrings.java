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
public class ExpressionMappedStrings
        extends ExpressionMappedValues<String> {

    private static Log log = LogFactory.getLog( ExpressionMappedStrings.class );

    /**
     * Initializes a newly created instance with default values.
     */
    public static  ValueInitializer<ExpressionMappedStrings> defaults() {
        return new ValueInitializer<ExpressionMappedStrings>() {
            @Override
            public ExpressionMappedStrings initialize( ExpressionMappedStrings proto ) throws Exception {
                return proto;
            }
        };
    }
    

    // instance *******************************************
    

    @Concerns( StylePropertyChange.Concern.class )
    public Property<String> defaultStringValue;
    
    //@Concerns( StylePropertyChange.Concern.class )
    public CollectionProperty<String> stringValues;


    @Override
    public ExpressionMappedStrings add( String value ) {
        stringValues.add( value );
        return this;
    }


    @Override
    public String defaultValue() {
        return defaultStringValue.get();
    }


    @Override
    public Collection<String> values() {
        return stringValues;
    }
}
