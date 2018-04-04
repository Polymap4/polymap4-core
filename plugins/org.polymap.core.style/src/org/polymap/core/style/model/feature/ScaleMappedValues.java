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

import org.polymap.core.style.model.feature.ScaleMappedValues.ScaleRange;

import org.polymap.model2.CollectionProperty;
import org.polymap.model2.Composite;
import org.polymap.model2.Property;

/**
 * 
 *
 * @author Falko Bräutigam
 */
public abstract class ScaleMappedValues<V>
        extends MappedValues<ScaleRange,V> {

    protected CollectionProperty<ScaleRange>   scales;

    /**
     * 
     *
     * @param min The min scale denominator, or 0.
     * @param max The max scale denominator, or {@link Double#POSITIVE_INFINITY}
     * @param value
     */
    public ScaleMappedValues<V> add( double min, double max, V value ) {
        scales.createElement( proto -> {
            proto.min.set( min );
            proto.max.set( max );
            return proto;
        });
        return this;
    }

    @Override
    public MappedValues<ScaleRange,V> add( ScaleRange key, V value ) {
        throw new RuntimeException( "Use add(min, max, value) instead." );
    }

    @Override
    public void clear() {
        scales.clear();
        super.clear();
    }


    /**
     * The scale range of {@link ScaleMappedValues}.
     */
    public static class ScaleRange
            extends Composite {
        
        /** The min scale denominator, or 0. */
        public Property<Double>     min;
        
        /** The max scale denominator, or {@link Double#POSITIVE_INFINITY}. */
        public Property<Double>     max;
    }

}
