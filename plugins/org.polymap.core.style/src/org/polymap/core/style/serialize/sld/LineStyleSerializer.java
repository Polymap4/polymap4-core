/*
 * polymap.org Copyright (C) 2016-2017, the @authors. All rights reserved.
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

import org.polymap.core.style.model.LineStyle;
import org.polymap.core.style.model.PolygonStyle;
import org.polymap.core.style.serialize.FeatureStyleSerializer.Context;

/**
 * Serialize {@link PolygonStyle}.
 *
 * @author Steffen Stundzig
 */
public class LineStyleSerializer
        extends StyleSerializer<LineStyle,LineSymbolizerDescriptor> {

    public LineStyleSerializer( Context context ) {
        super( context );
    }


    @Override
    protected LineSymbolizerDescriptor createStyleDescriptor() {
        return new LineSymbolizerDescriptor();
    }


    @Override
    public void doSerializeStyle( final LineStyle style ) {
        setComposite( new StrokeSerializer(context()).serialize( style.fill.get() ),
                ( LineSymbolizerDescriptor sd, StrokeDescriptor value ) -> sd.fill.set( value ) );
        setComposite( new StrokeSerializer(context()).serialize( style.stroke.get() ),
                ( LineSymbolizerDescriptor sd, StrokeDescriptor value ) -> sd.stroke.set( value ) );
        // setValue( style.offset.get(), ( LineSymbolizerDescriptor sd, Expression
        // value ) -> sd.offset.set( value ) );
    }
}
