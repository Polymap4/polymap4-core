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

import static org.polymap.core.style.serialize.sld.SLDSerializer.ff;

import java.util.Collections;
import java.util.List;

import org.polymap.core.style.model.raster.ConstantRasterColorMapType;
import org.polymap.core.style.serialize.FeatureStyleSerializer.Context;
import org.polymap.core.style.serialize.sld.StylePropertyValueHandler;
import org.polymap.core.style.serialize.sld.SymbolizerDescriptor;

/**
 * 
 *
 * @author Falko Bräutigam
 */
public class ConstantRasterColorMapTypeHandler
        extends StylePropertyValueHandler<ConstantRasterColorMapType,Integer> {

    @Override
    public <SD extends SymbolizerDescriptor> List<SD> doHandle( Context context, ConstantRasterColorMapType src, SD sd,
            StylePropertyValueHandler.Setter<SD> setter ) {
        
        src.type.opt().ifPresent( type -> setter.set( sd, ff.literal( type ) ) );
        return Collections.singletonList( sd );
    }

}
