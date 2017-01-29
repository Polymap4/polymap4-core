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

import java.util.Collections;
import java.util.List;

import org.eclipse.swt.graphics.Color;

import org.polymap.core.style.model.raster.RasterColorMap;
import org.polymap.core.style.serialize.FeatureStyleSerializer.Context;
import org.polymap.core.style.serialize.sld.StylePropertyValueHandler;
import org.polymap.core.style.serialize.sld.SymbolizerDescriptor;

/**
 * 
 *
 * @author Falko Bräutigam
 */
public class RasterColorMapHandler
        extends StylePropertyValueHandler<RasterColorMap,Color> {

    @Override
    public <SD extends SymbolizerDescriptor> List<SD> doHandle( Context context, RasterColorMap spv, SD sd,
            StylePropertyValueHandler.Setter<SD> setter ) {
    
        //setter.set( sd );
        return Collections.singletonList( sd );
    }
    
}
