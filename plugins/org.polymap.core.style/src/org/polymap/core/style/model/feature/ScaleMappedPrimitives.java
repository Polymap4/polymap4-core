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

import org.polymap.model2.CollectionProperty;
import org.polymap.model2.runtime.ValueInitializer;

/**
 * Any primitive types that are directly handled by the model framework: {@link Boolean},
 * {@link Number}s, {@link Date}, {@link String}.

 * @param <V> Primitive types.
 * @author Falko Bräutigam
 */
public class ScaleMappedPrimitives<V>
        extends ScaleMappedValues<V> {

    /** Initializes a newly created instance with default values. */
    public static <R extends Number> ValueInitializer<ScaleMappedPrimitives<R>> defaults() {
        return new ValueInitializer<ScaleMappedPrimitives<R>>() {
            @Override public ScaleMappedPrimitives<R> initialize( ScaleMappedPrimitives<R> proto ) throws Exception {
                return proto;
            }
        };
    }
    
    // instance *******************************************
    
    protected CollectionProperty<V>     values;

    
    @Override
    public ScaleMappedValues<V> add( double min, double max, V value ) {
        values.add( value );
        return super.add( min, max, value );
    }

    @Override
    public List<Mapped<ScaleRange,V>> values() {
        return values( scales, values, (scale,value) -> new Mapped( scale, value ) );
    }
    
    @Override
    public void clear() {
        values.clear();
        super.clear();
    }

}
