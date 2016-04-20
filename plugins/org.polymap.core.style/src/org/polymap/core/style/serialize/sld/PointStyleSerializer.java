/* 
 * polymap.org
 * Copyright (C) 2016, the @authors. All rights reserved.
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
package org.polymap.core.style.serialize.sld;

import java.awt.Color;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.style.model.PointStyle;

/**
 * Serialize {@link PointStyle}.
 *
 * @author Falko Bräutigam
 */
public class PointStyleSerializer
        extends StyleSerializer<PointStyle,PointSymbolizerDescriptor> {

    private static Log log = LogFactory.getLog( PointStyleSerializer.class );


    @Override
    protected PointSymbolizerDescriptor createDescriptor() {
        return new PointSymbolizerDescriptor();
    }


    @Override
    public void doSerialize( PointStyle style ) {
        setValue( style.strokeWidth.get(), (PointSymbolizerDescriptor sd, Double value) -> sd.strokeWidth.set( value ) );
        setValue( style.strokeOpacity.get(), (PointSymbolizerDescriptor sd, Double value) -> sd.strokeOpacity.set( value ) );
        setValue( style.strokeColor.get(), (PointSymbolizerDescriptor sd, Color value) -> sd.strokeColor.set( value ) );
        setValue( style.fillColor.get(), (PointSymbolizerDescriptor sd, Color value) -> sd.fillColor.set( value ) );
        setValue( style.fillOpacity.get(), (PointSymbolizerDescriptor sd, Double value) -> sd.fillOpacity.set( value ) );
    }
    
}
