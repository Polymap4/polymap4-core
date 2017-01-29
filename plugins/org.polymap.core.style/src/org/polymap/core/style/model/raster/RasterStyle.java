/*
 * polymap.org Copyright (C) 2017, the @authors. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 3.0 of the License, or (at your option) any later
 * version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */
package org.polymap.core.style.model.raster;

import org.geotools.coverage.grid.GridCoverage2D;

import org.polymap.core.style.model.Style;
import org.polymap.core.style.model.StylePropertyChange;
import org.polymap.core.style.model.StylePropertyValue;
import org.polymap.core.style.model.UIOrder;
import org.polymap.core.style.model.feature.NumberRange;

import org.polymap.model2.Concerns;
import org.polymap.model2.Description;
import org.polymap.model2.Nullable;
import org.polymap.model2.Property;
import org.polymap.model2.runtime.ValueInitializer;

/**
 * Abstract base class of all styles that work on {@link GridCoverage2D} data. 
 * 
 * @see {@link org.opengis.style.RasterSymbolizer}.
 * @author Falko Bräutigam
 */
public abstract class RasterStyle
        extends Style {

    /**
     * Initializes a newly created instance with default values.
     */
    @SuppressWarnings( "hiding" )
    public static final ValueInitializer<RasterStyle>  defaults = new ValueInitializer<RasterStyle>() {
        @Override
        public RasterStyle initialize( RasterStyle proto ) throws Exception {
            Style.defaults.initialize( proto );
            proto.title.set( "[Raster style]" );
            return proto;
        }
    };

    @Nullable
    @UIOrder( 100 )
    @Description( "opacity" )
    @NumberRange( from=0, to=1, defaultValue=1, increment=0.1 )
    @Concerns( StylePropertyChange.Concern.class )
    public Property<StylePropertyValue<Double>> opacity;
    
}
