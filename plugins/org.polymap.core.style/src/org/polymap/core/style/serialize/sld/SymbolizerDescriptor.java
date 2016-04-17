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

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Rule;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.runtime.config.Config;
import org.polymap.core.runtime.config.Configurable;
import org.polymap.core.style.model.Style;

/**
 * Zwischenschritt beim Serialisieren: sammelt alle Einstellungen, um aus einem
 * {@link Style} einen SLD-Symbolizer zu machen. Hilft beim "Ausmultiplizieren", in
 * dem er einfach zu kopieren ist, wobei bei jedem Schritt mehr Properties gesetzt
 * werden. Diese Klasse verbirgt die Komplexität von {@link FeatureTypeStyle} und
 * {@link Rule}.
 *
 * @author Falko Bräutigam
 */
public abstract class SymbolizerDescriptor
        extends Configurable
        implements Cloneable {

    private static Log log = LogFactory.getLog( SymbolizerDescriptor.class );

    public static final FilterFactory       ff = CommonFactoryFinder.getFilterFactory( null );

    public Config<Pair<Integer,Integer>>    scale;
    
    public Config<Filter>                   filter;

    public Config<Integer>                  zIndex;

    
    @Override
    protected SymbolizerDescriptor clone() {
        try {
            SymbolizerDescriptor clone = (SymbolizerDescriptor)super.clone();
            clone.scale.set( scale.get() );
            clone.filter.set( filter.get() );
            return clone;
        }
        catch (CloneNotSupportedException e) {
            throw new RuntimeException( e );
        }
    }
    
    
    /**
     * Append (AND) the given filter to the currently set {@link #filter} of this
     * descriptor.
     */
    public void filterAnd( Filter f ) {
        Filter current = filter.get();
        filter.set( current == null || current.equals( Filter.INCLUDE )
                ? f : ff.and( current, f ) );
    }
    
}
