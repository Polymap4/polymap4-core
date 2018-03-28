/* 
 * polymap.org
 * Copyright (C) 2018, the @authors. All rights reserved.
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

import java.util.Date;
import java.util.List;

import org.opengis.filter.Filter;

import org.polymap.model2.CollectionProperty;
import org.polymap.model2.runtime.ValueInitializer;

/**
 * Any primitive types that are directly handled by the model framework: {@link Boolean},
 * {@link Number}s, {@link Date} and {@link String}.
 *
 * @param <V>
 * @author Falko Bräutigam
 */
public class FilterMappedPrimitives<V>
        extends FilterMappedValues<V> {

    /** Initializes a newly created instance with default values. */
    public static <R extends Number> ValueInitializer<FilterMappedPrimitives<R>> defaults() {
        return new ValueInitializer<FilterMappedPrimitives<R>>() {
            @Override public FilterMappedPrimitives<R> initialize( FilterMappedPrimitives<R> proto ) throws Exception {
                return proto;
            }
        };
    }

    // instance *******************************************
    
    protected CollectionProperty<V> values;
    
//    /**
//     * 
//     */
//    public Property<V>              valueRangeMin;
//    
//    public Property<V>              valueRangeMax;

    
    @Override
    public MappedValues<Filter,V> add( Filter key, V value ) {
        values.add( value );
        return super.add( key, value );
    }

    @Override
    public List<Mapped<Filter,V>> values() {
        return values( encodedFilters, values, (encodedFilter,value) -> 
                new Mapped( ConstantFilter.decode( encodedFilter ), value ) );
    }

    @Override
    public void clear() {
        values.clear();
        super.clear();
    }

}
