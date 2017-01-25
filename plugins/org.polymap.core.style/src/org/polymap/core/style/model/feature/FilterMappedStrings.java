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

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.model2.CollectionProperty;
import org.polymap.model2.runtime.ValueInitializer;

/**
 * Strings mapped on filters
 *
 * @author Steffen Stundzig
 */
public class FilterMappedStrings
        extends FilterMappedValues<String> {

    private static Log log = LogFactory.getLog( FilterMappedStrings.class );


    /**
     * Initializes a newly created instance with default values.
     */
    public static ValueInitializer<FilterMappedStrings> defaults() {
        return new ValueInitializer<FilterMappedStrings>() {

            @Override
            public FilterMappedStrings initialize( FilterMappedStrings proto ) throws Exception {
                return proto;
            }
        };
    }

    // instance *******************************************

    // XXX Collections are not supported yet, use force-fire-fake prop?
    // @Concerns( StylePropertyChange.Concern.class )
    public CollectionProperty<String> stringValues;


    @Override
    protected FilterMappedValues addValue( String value ) {
        stringValues.add( value );
        return this;
    }


    @Override
    public List<String> values() {
        return stringValues.stream().collect( Collectors.toList() );
    }

}
