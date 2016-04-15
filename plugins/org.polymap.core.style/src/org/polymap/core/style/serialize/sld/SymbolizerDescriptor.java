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

import org.opengis.filter.Filter;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.runtime.config.Config;
import org.polymap.core.runtime.config.Configurable;
import org.polymap.core.runtime.config.Immutable;
import org.polymap.core.style.model.Style;

/**
 * Zwischenschritt beim Serialisieren: sammelt alle Einstellungen, um aus einem {@link Style} einen
 * SLD-Symbolizer zu machen. Hilft beim "Ausmultiplizieren", in dem er einfach zu kopieren ist, wobei bei
 * jedem Schritt mehr Properties gesetzt werden.
 *
 * @author Falko Bräutigam
 */
public abstract class SymbolizerDescriptor
        extends Configurable
        implements Cloneable {

    private static Log log = LogFactory.getLog( SymbolizerDescriptor.class );

    @Immutable
    public Config<Pair<Integer,Integer>>    scale;
    
    @Immutable
    public Config<Filter>                   filter;

    @Immutable
    public Config<Integer>                  zIndex;

    
    @Override
    protected SymbolizerDescriptor clone() throws CloneNotSupportedException {
        SymbolizerDescriptor clone = (SymbolizerDescriptor)super.clone();
        clone.scale.set( scale.get() );
        clone.filter.set( filter.get() );
        return clone;
    }
    
}
