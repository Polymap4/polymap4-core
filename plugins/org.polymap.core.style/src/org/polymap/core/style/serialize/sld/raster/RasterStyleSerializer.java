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
package org.polymap.core.style.serialize.sld.raster;

import org.polymap.core.style.model.raster.RasterStyle;
import org.polymap.core.style.serialize.FeatureStyleSerializer.Context;
import org.polymap.core.style.serialize.sld.StyleSerializer;

/**
 * 
 *
 * @author Falko Bräutigam
 */
public class RasterStyleSerializer
        extends StyleSerializer<RasterStyle,RasterSymbolizerDescriptor> {

    public RasterStyleSerializer( Context context ) {
        super( context );
    }

    
    @Override
    protected RasterSymbolizerDescriptor createStyleDescriptor() {
        return new RasterSymbolizerDescriptor();
    }

    
    @Override
    protected void doSerializeStyle( RasterStyle style ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

}
