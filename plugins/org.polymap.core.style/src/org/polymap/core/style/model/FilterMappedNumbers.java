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

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.model2.CollectionProperty;
import org.polymap.model2.runtime.ValueInitializer;

/**
 * Numbers mapped on filters
 *
 * @author Falko Bräutigam
 * @author Steffen Stundzig
 */
public class FilterMappedNumbers<T extends Number>
        extends FilterMappedValues<T> {

    private static Log log = LogFactory.getLog( FilterMappedNumbers.class );

    /**
     * Initializes a newly created instance with default values.
     */
    public static <R extends Number> ValueInitializer<FilterMappedNumbers<R>> defaults() {
        return new ValueInitializer<FilterMappedNumbers<R>>() {
            @Override
            public FilterMappedNumbers<R> initialize( FilterMappedNumbers<R> proto ) throws Exception {
                return proto;
            }
        };
    }
    

    // instance *******************************************
    
    // XXX Collections are not supported yet, use force-fire-fake prop?
    //@Concerns( StylePropertyChange.Concern.class )
    public CollectionProperty<Number>           numberValues;


    @Override
    protected FilterMappedValues addValue( T value ) {
        numberValues.add( value );
        return this;
    }


    @Override
    public List<T> values() {
        return numberValues.stream().map( number -> (T)number ).collect( Collectors.toList() );
    }
    
}
