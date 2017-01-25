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

import org.polymap.core.style.model.StylePropertyChange;
import org.polymap.core.style.model.StylePropertyValue;

import org.polymap.model2.CollectionProperty;
import org.polymap.model2.Concerns;
import org.polymap.model2.Nullable;
import org.polymap.model2.Property;
import org.polymap.model2.runtime.ValueInitializer;

/**
 * Conditions based numbers.
 * 
 * @author Steffen Stundzig
 */
public class ScaleMappedNumbers<T extends Number>
        extends StylePropertyValue<T> {

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

    // XXX Collection change events are not supported yet, use force-fire-fake prop?
    @Nullable
    @Concerns(StylePropertyChange.Concern.class)
    public Property<String>             fake;

    @Nullable
    @Concerns(StylePropertyChange.Concern.class)
    public Property<Number>             defaultNumberValue;

    // @Concerns( StylePropertyChange.Concern.class )
    public CollectionProperty<Number>   numberValues;

    // @Concerns( StylePropertyChange.Concern.class )
    public CollectionProperty<Number>   scales;


    public ScaleMappedNumbers add( final Double number, final Double scale ) {
        numberValues.add( number );
        scales.add( scale );
        return this;
    }


    public List<T> numbers() {
        return numberValues.stream().map( number -> (T)number ).collect( Collectors.toList() );
    }


    public List<Number> scales() {
        return scales.stream().collect( Collectors.toList() );
    }
}
