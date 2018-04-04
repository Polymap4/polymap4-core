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

import org.polymap.model2.Concerns;
import org.polymap.model2.Property;
import org.polymap.model2.runtime.ValueInitializer;

/**
 *
 *
 * @author Falko Bräutigam
 */
public class ConstantRasterBand
        extends ConstantValue<RasterBand> {

    /**
     * Initializes a newly created instance with the given default value.
     */
    public static ValueInitializer<ConstantRasterBand> defaults( int band ) {
        return new ValueInitializer<ConstantRasterBand>() {
            @Override public ConstantRasterBand initialize( ConstantRasterBand proto ) throws Exception {
                proto.band.set( band );
                return proto;
            }
        };
    }
    

    // instance *******************************************
    
    //@Nullable
    @Concerns( StylePropertyChange.Concern.class )
    public Property<Integer>        band;


    @Override
    public RasterBand value() {
        return new RasterBand( band.get() );
    }
    
}
