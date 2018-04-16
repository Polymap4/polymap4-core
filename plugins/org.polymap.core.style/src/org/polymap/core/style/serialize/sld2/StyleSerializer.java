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

import org.geotools.styling.Symbolizer;

import org.polymap.core.style.model.Style;
import org.polymap.core.style.serialize.FeatureStyleSerializer.Context;

/**
 * 
 * @param <T> The type of the {@link Style} to serialize.
 * @param <R> The type of the resulting {@link Symbolizer}.
 * @author Falko Bräutigam
 */
public abstract class StyleSerializer<T extends Style,R>
        extends StyleCompositeSerializer<T,R>{

    public StyleSerializer( Context context ) {
        super( context );
    }

    
    public abstract void serialize( T style, org.geotools.styling.Style result );

}
