/* 
 * polymap.org
 * Copyright (C) 2016-2018, the @authors. All rights reserved.
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

import java.awt.Color;

import org.opengis.filter.Filter;

import org.polymap.model2.CollectionProperty;
import org.polymap.model2.runtime.ValueInitializer;

/**
 * 
 * @author Falko Bräutigam
 */
public class FilterMappedColors
        extends FilterMappedValues<Color> {

    /** Initializes a newly created instance with default values. */
    public static <R extends Number> ValueInitializer<FilterMappedColors> defaults() {
        return new ValueInitializer<FilterMappedColors>() {
            @Override public FilterMappedColors initialize( FilterMappedColors proto ) throws Exception {
                return proto;
            }
        };
    }

    // instance *******************************************
    
    // @Concerns( StylePropertyChange.Concern.class )
    protected CollectionProperty<Integer>       rgbas;


    @Override
    public MappedValues<Filter,Color> add( Filter key, Color value ) {
        rgbas.add( value.getRGB() );
        return super.add( key, value );
    }

    @Override
    public List<Mapped<Filter,Color>> values() {
        return values( encodedFilters, rgbas, (encodedFilter,rgba) ->
                new Mapped( FilterStyleProperty.decode( encodedFilter ), new Color( rgba ) ) );
    }

    @Override
    public void clear() {
        rgbas.clear();
        super.clear();
    }

}
