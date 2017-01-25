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
import org.polymap.core.style.model.StylePropertyValue;
import org.polymap.core.style.model.UIOrder;

import org.polymap.model2.Concerns;
import org.polymap.model2.Description;
import org.polymap.model2.Nullable;
import org.polymap.model2.Property;
import org.polymap.model2.runtime.ValueInitializer;

/**
 * Raster style that maps one specified raster band to the grayscale image channel.
 *
 * @author Falko Bräutigam
 */
public class RasterGrayStyle
        extends RasterStyle {

    /**
     * Initializes a newly created instance with default values.
     */
    @SuppressWarnings( "hiding" )
    public static final ValueInitializer<RasterGrayStyle>  defaults = new ValueInitializer<RasterGrayStyle>() {
        @Override
        public RasterGrayStyle initialize( RasterGrayStyle proto ) throws Exception {
            RasterStyle.defaults.initialize( proto );
            return proto;
        }
    };

    // instance *******************************************
    
    @Nullable
    @UIOrder( 10 )
    @Description( "grayBand" )
    @Concerns( StylePropertyChange.Concern.class )
    public Property<StylePropertyValue<RasterBand>> grayBand;

}
