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
 * 
 *
 * @author Falko Bräutigam
 */
public class RasterRGBStyle
        extends RasterStyle {

    /**
     * Initializes a newly created instance with default values.
     */
    @SuppressWarnings( "hiding" )
    public static final ValueInitializer<RasterRGBStyle>  defaults = new ValueInitializer<RasterRGBStyle>() {
        @Override
        public RasterRGBStyle initialize( RasterRGBStyle proto ) throws Exception {
            RasterStyle.defaults.initialize( proto );
            proto.title.set( "RGB" );
            return proto;
        }
    };

    // instance *******************************************
    
    @Nullable
    @UIOrder( 10 )
    @Description( "redBand" )
    @Concerns( StylePropertyChange.Concern.class )
    public Property<StylePropertyValue<RasterBand>> redBand;

    @Nullable
    @UIOrder( 20 )
    @Description( "greenBand" )
    @Concerns( StylePropertyChange.Concern.class )
    public Property<StylePropertyValue<RasterBand>> greenBand;

    @Nullable
    @UIOrder( 30 )
    @Description( "blueBand" )
    @Concerns( StylePropertyChange.Concern.class )
    public Property<StylePropertyValue<RasterBand>> blueBand;

}
