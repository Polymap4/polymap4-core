/* 
 * polymap.org
 * Copyright (C) 2018, the @authors. All rights reserved.
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
package org.polymap.core.style.serialize.sld2;

import org.geotools.styling.FeatureTypeStyle;

import org.polymap.core.style.model.feature.Stroke;
import org.polymap.core.style.serialize.FeatureStyleSerializer.Context;

/**
 * 
 *
 * @author Falko Bräutigam
 */
public class StrokeCompositeSerializer
        extends StyleCompositeSerializer<Stroke,org.geotools.styling.Stroke> {

    public StrokeCompositeSerializer( Context context ) {
        super( context );
    }

    @Override
    public void serialize( Stroke stroke, FeatureTypeStyle fts ) {
        set( fts, stroke.color, (value,sym) -> sym.setColor( value ) );
        set( fts, stroke.opacity, (value,sym) -> sym.setOpacity( value ) );
        set( fts, stroke.width, (value,sym) -> sym.setWidth( value ) );
        // XXX linestyle
    }
    
}
