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

import java.awt.Color;

import org.opengis.filter.expression.Expression;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.style.model.Point;
import org.polymap.core.style.model.TextStyle;

/**
 * Serialize {@link TextStyle}.
 *
 * @author Steffen Stundzig
 */
public class TextStyleSerializer
        extends StyleCompositeSerializer<TextStyle,TextSymbolizerDescriptor> {

    private static Log log = LogFactory.getLog( TextStyleSerializer.class );


    @Override
    protected TextSymbolizerDescriptor createDescriptor() {
        return new TextSymbolizerDescriptor();
    }


    @Override
    public void doSerialize( TextStyle style ) {
        FontSerializer fontSerializer = new FontSerializer();
        List<FontDescriptor> fontDescriptors = fontSerializer.serialize( style.font.get() );
        setComposite( fontDescriptors, ( TextSymbolizerDescriptor sd, FontDescriptor value ) -> sd.font.set( value ) );

        setExpressionValue( style.textProperty.get(),
                ( TextSymbolizerDescriptor sd, Expression value ) -> sd.text.set( value ) );
        setValue( style.color.get(), ( TextSymbolizerDescriptor sd, Color value ) -> sd.color.set( value ) );
        setValue( style.opacity.get(), ( TextSymbolizerDescriptor sd, Double value ) -> sd.opacity.set( value ) );
        setValue( style.haloWidth.get(), ( TextSymbolizerDescriptor sd, Double value ) -> sd.haloWidth.set( value ) );
        setValue( style.haloColor.get(), ( TextSymbolizerDescriptor sd, Color value ) -> sd.haloColor.set( value ) );
        setValue( style.haloOpacity.get(),
                ( TextSymbolizerDescriptor sd, Double value ) -> sd.haloOpacity.set( value ) );
        setValue( style.anchorPoint.get(),
                ( TextSymbolizerDescriptor sd, Point value ) -> sd.anchorPoint.set( value ) );
        setValue( style.displacement.get(),
                ( TextSymbolizerDescriptor sd, Point value ) -> sd.displacement.set( value ) );
        setValue( style.placementRotation.get(),
                ( TextSymbolizerDescriptor sd, Double value ) -> sd.placementRotation.set( value ) );
        setValue( style.placementOffset.get(),
                ( TextSymbolizerDescriptor sd, Double value ) -> sd.placementOffset.set( value ) );
    }
}
