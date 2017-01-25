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
package org.polymap.core.style.model.raster;

import org.eclipse.swt.graphics.Color;

import org.polymap.core.style.model.StylePropertyChange;
import org.polymap.core.style.model.StylePropertyValue;
import org.polymap.core.style.model.UIOrder;
import org.polymap.core.style.model.feature.NumberRange;

import org.polymap.model2.CollectionProperty;
import org.polymap.model2.Composite;
import org.polymap.model2.Concerns;
import org.polymap.model2.Immutable;
import org.polymap.model2.Nullable;
import org.polymap.model2.Property;

/**
 * 
 *
 * @author Falko Bräutigam
 */
public class RasterColorMap
        extends StylePropertyValue<Color> {

    @Nullable
    @Concerns(StylePropertyChange.Concern.class)
    public Property<String>                 fake;

    // XXX Collections are not supported yet, use force-fire-fake prop?
    //@Concerns( StylePropertyChange.Concern.class )
    public CollectionProperty<ColorMapEntry> entries;

    /**
     * 
     */
    public static class ColorMapEntry
            extends Composite {
        
        @Immutable
        public Property<Double>     bandValue;
        
        @Immutable
        public Property<Color>      color;
        
        @Immutable
        @Nullable
        @UIOrder(20)
        @NumberRange(to = 1, defaultValue = 1, increment = 0.1)
        @Concerns(StylePropertyChange.Concern.class)
        public Property<Double>     opacity;
        
    }
    
}
