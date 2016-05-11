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

import java.util.List;

import org.opengis.filter.expression.Expression;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.style.model.PointStyle;

/**
 * Serialize {@link PointStyle}.
 *
 * @author Falko Bräutigam
 * @author Steffen Stundzig
 */
public class PointStyleSerializer
        extends StyleCompositeSerializer<PointStyle,PointSymbolizerDescriptor> {

    private static Log log = LogFactory.getLog( PointStyleSerializer.class );


    @Override
    protected PointSymbolizerDescriptor createDescriptor() {
        return new PointSymbolizerDescriptor();
    }


    @Override
    public void doSerialize( PointStyle style ) {
        List<StrokeDescriptor> strokeDescriptors = new StrokeSerializer().serialize( style.stroke.get() );
        setComposite( strokeDescriptors,
                ( PointSymbolizerDescriptor sd, StrokeDescriptor value ) -> sd.stroke.set( value ) );

        List<FillDescriptor> fillDescriptors = new FillSerializer().serialize( style.fill.get() );
        setComposite( fillDescriptors,
                ( PointSymbolizerDescriptor sd, FillDescriptor value ) -> sd.fill.set( value ) );
        
        setValue( style.diameter.get(),
                ( PointSymbolizerDescriptor sd, Expression value ) -> sd.diameter.set( value ) );
    }
    
}
