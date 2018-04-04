/* 
 * polymap.org
 * Copyright (C) 2017-2018, the @authors. All rights reserved.
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.polymap.core.style.model.StylePropertyValue;
import org.polymap.core.style.model.raster.ConstantRasterColorMap.Entry;

import org.polymap.model2.CollectionProperty;

/**
 * The target class of a {@link StylePropertyValue} member declaration.
 *
 * @author Falko Bräutigam
 */
public class RasterColorMap
        implements Iterable<Entry> {

    private List<Entry>     entries;
    
    public RasterColorMap( CollectionProperty<Entry> entries ) {
        this.entries = new ArrayList( entries );
    }

    @Override
    public Iterator<Entry> iterator() {
        return entries.iterator();
    }
    
}
