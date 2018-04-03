/* 
 * polymap.org
 * Copyright (C) 2017, the @authors. All rights reserved.
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
package org.polymap.core.style.model.raster;

import org.polymap.core.style.model.StylePropertyChange;
import org.polymap.core.style.model.feature.ConstantValue;
import org.polymap.core.style.model.feature.NumberConcern;
import org.polymap.core.style.model.feature.NumberRange;

import org.polymap.model2.CollectionProperty;
import org.polymap.model2.Composite;
import org.polymap.model2.Concerns;
import org.polymap.model2.Nullable;
import org.polymap.model2.Property;
import org.polymap.model2.runtime.ValueInitializer;

/**
 *
 *
 * @author Falko Bräutigam
 */
public class ConstantRasterColorMap
        extends ConstantValue<RasterColorMap> {

    /**
     * Initializes a newly created instance with the given default value.
     */
    public static ValueInitializer<ConstantRasterColorMap> defaults() {
        return new ValueInitializer<ConstantRasterColorMap>() {
            @Override public ConstantRasterColorMap initialize( ConstantRasterColorMap proto ) throws Exception {
                return proto;
            }
        };
    }
    

    // instance *******************************************
    
    //@Nullable
    //@Concerns( StylePropertyChange.Concern.class )
    public CollectionProperty<Entry>        entries;
    
    @Override
    public RasterColorMap value() {
        return new RasterColorMap( entries );
    }
    

    /**
     * 
     */
    public static class Entry
            extends Composite {

        public Property<Double>             value;
    
        @NumberConcern.Range( from=0, to=255 )
        @Concerns( {NumberConcern.class, StylePropertyChange.Concern.class} )
        public Property<Integer>            r;
        
        @NumberConcern.Range( from=0, to=255 )
        @Concerns( {NumberConcern.class, StylePropertyChange.Concern.class} )
        public Property<Integer>            g;
        
        @NumberConcern.Range( from=0, to=255 )
        @Concerns( {NumberConcern.class, StylePropertyChange.Concern.class} )
        public Property<Integer>            b;

        @Nullable
        @NumberRange( from=0, to=1, defaultValue=1, increment=0.1 )
        @Concerns( StylePropertyChange.Concern.class )
        public Property<Double>             opacity;

    }
    
}
