/*
 * polymap.org Copyright (C) 2016, the @authors. All rights reserved.
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
package org.polymap.core.style.serialize.sld;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.style.model.PolygonStyle;

/**
 * Serialize {@link PolygonStyle}.
 *
 * @author Steffen Stundzig
 */
public class PolygonStyleSerializer
        extends StyleCompositeSerializer<PolygonStyle,PolygonSymbolizerDescriptor> {

    private static Log log = LogFactory.getLog( PolygonStyleSerializer.class );


    @Override
    protected PolygonSymbolizerDescriptor createDescriptor() {
        return new PolygonSymbolizerDescriptor();
    }


    @Override
    public void doSerialize( PolygonStyle style ) {
        StrokeSerializer strokeSerializer = new StrokeSerializer();
        List<StrokeDescriptor> strokeDescriptors = strokeSerializer.serialize( style.stroke.get() );
        setComposite( strokeDescriptors,
                ( PolygonSymbolizerDescriptor sd, StrokeDescriptor value ) -> sd.stroke.set( value ) );
        
        List<FillDescriptor> fillDescriptors = new FillSerializer().serialize( style.fill.get() );
        setComposite( fillDescriptors,
                ( PolygonSymbolizerDescriptor sd, FillDescriptor value ) -> sd.fill.set( value ) );
    }
}
