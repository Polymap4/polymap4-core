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
import org.polymap.model2.Property;
import org.polymap.model2.runtime.ValueInitializer;

/**
 * 
 *
 * @author Falko Bräutigam
 */
public class RasterColorMapStyle
        extends RasterStyle {

    /**
     * Initializes a newly created instance with default values.
     */
    @SuppressWarnings( "hiding" )
    public static final ValueInitializer<RasterColorMapStyle>  defaults = new ValueInitializer<RasterColorMapStyle>() {
        @Override
        public RasterColorMapStyle initialize( RasterColorMapStyle proto ) throws Exception {
            RasterStyle.defaults.initialize( proto );
            proto.title.set( "Colormap" );
            return proto;
        }
    };

    // instance *******************************************
    
    //@Nullable
    @UIOrder( 10 )
    @Description( "band" )
    @Concerns( StylePropertyChange.Concern.class )
    public Property<StylePropertyValue<RasterBand>>     band;
    
    //@Nullable
    @UIOrder( 20 )
    @Description( "colorMap" )
    @Concerns( StylePropertyChange.Concern.class )
    public Property<StylePropertyValue<RasterColorMap>> colorMap;    
    
}
