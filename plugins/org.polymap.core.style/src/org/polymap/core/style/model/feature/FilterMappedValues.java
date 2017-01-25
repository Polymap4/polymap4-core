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

import java.io.IOException;

import org.opengis.filter.Filter;

import org.polymap.core.style.model.StylePropertyChange;
import org.polymap.core.style.model.StylePropertyValue;

import org.polymap.model2.CollectionProperty;
import org.polymap.model2.Concerns;
import org.polymap.model2.Nullable;
import org.polymap.model2.Property;

/**
 * Base class for all fitler mapped values, like numbers and colors.
 * 
 * @author Falko Bräutigam
 * @author Steffen Stundzig
 */
public abstract class FilterMappedValues<T>
        extends StylePropertyValue<T> {

    @Nullable
    @Concerns(StylePropertyChange.Concern.class)
    public Property<String> fake;

    // XXX Collections are not supported yet, use force-fire-fake prop?
    //@Concerns( StylePropertyChange.Concern.class )
    public CollectionProperty<String> encodedFilters;


    public List<Filter> filters() {
        return encodedFilters.stream().map( encoded -> decode( encoded ) ).collect( Collectors.toList() );
    }


    private Filter decode( final String encoded ) {
        try {
            return ConstantFilter.decode( encoded );
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }
    }


    public FilterMappedValues add( final Filter filter, final T value ) throws IOException {
        encodedFilters.add( ConstantFilter.encode( filter ) );
        return addValue( value );
    }


    protected abstract FilterMappedValues addValue( T value );


    public abstract List<T> values();
}
