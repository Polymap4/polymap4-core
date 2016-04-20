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

import java.io.IOException;
import org.opengis.filter.Filter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Collections2;

import org.polymap.model2.CollectionProperty;
import org.polymap.model2.runtime.ValueInitializer;

/**
 * Describes a constant number as style property value.
 *
 * @author Falko Bräutigam
 */
public class FilterMappedNumbers<T extends Number>
        extends StylePropertyValue<T> {

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
    
    public CollectionProperty<Number>           values;
    
    public CollectionProperty<String>           filters;
    
    
    public Collection<Filter> filters() {
        return Collections2.transform( filters, encoded -> {
            try {
                return ConstantFilter.decode( encoded );
            }
            catch (Exception e) {
                throw new RuntimeException( e );
            }
        });
    }
    
    
    public FilterMappedNumbers add( T number, Filter filter ) throws IOException {
        values.add( number );
        filters.add( ConstantFilter.encode( filter ) );
        return this;
    }
    
}
