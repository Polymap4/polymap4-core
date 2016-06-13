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

import org.opengis.filter.expression.Expression;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.style.model.TextStyle;
import org.polymap.core.style.serialize.FeatureStyleSerializer.Context;

/**
 * Serialize {@link TextStyle}.
 *
 * @author Steffen Stundzig
 */
public class TextStyleSerializer
        extends StyleSerializer<TextStyle,TextSymbolizerDescriptor> {

    public TextStyleSerializer( Context context ) {
        super( context );
    }


    private static Log log = LogFactory.getLog( TextStyleSerializer.class );


    @Override
    protected TextSymbolizerDescriptor createStyleDescriptor() {
        return new TextSymbolizerDescriptor();
    }


    @Override
    public void doSerializeStyle( TextStyle style ) {
        setComposite( new FontSerializer(context()).serialize( style.font.get() ),
                ( TextSymbolizerDescriptor sd, FontDescriptor value ) -> sd.font.set( value ) );
        setComposite( new HaloSerializer(context()).serialize( style.halo.get() ),
                ( TextSymbolizerDescriptor sd, HaloDescriptor value ) -> sd.halo.set( value ) );
        setComposite( new LabelPlacementSerializer(context()).serialize( style.labelPlacement.get() ),
                ( TextSymbolizerDescriptor sd, LabelPlacementDescriptor value ) -> sd.labelPlacement.set( value ) );
        setValue( style.property.get(), ( TextSymbolizerDescriptor sd, Expression value ) -> sd.text.set( value ) );
        setValue( style.color.get(), ( TextSymbolizerDescriptor sd, Expression value ) -> sd.color.set( value ) );
        setValue( style.opacity.get(), ( TextSymbolizerDescriptor sd, Expression value ) -> sd.opacity.set( value ) );
    }
}
