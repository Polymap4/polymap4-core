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

import java.util.Objects;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Rule;
import org.geotools.styling.Symbolizer;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.runtime.config.Config;
import org.polymap.core.runtime.config.Configurable;
import org.polymap.core.runtime.config.DefaultString;
import org.polymap.core.runtime.config.Immutable;
import org.polymap.core.style.model.Style;

/**
 * Beschreibt einen <b>SLD {@link Symbolizer}</b>, inklusive {@link #zIndex} (im SLD
 * {@link FeatureTypeStyle}) und {@link #scale} (im SLD {@link Rule}).
 * <p/>
 * Descriptoren sind Zwischenschritt beim Serialisieren: sammelt alle Einstellungen,
 * um aus einem {@link Style} einen SLD-Symbolizer zu machen. Hilft beim
 * "Ausmultiplizieren", in dem er einfach zu kopieren ist, wobei bei jedem Schritt
 * mehr Properties gesetzt werden. Diese Klasse verbirgt dabei die Komplexität von
 * {@link FeatureTypeStyle} und {@link Rule}.
 *
 * @author Falko Bräutigam
 */
public abstract class SymbolizerDescriptor
        extends Configurable
        implements Cloneable {

    private static Log log = LogFactory.getLog( SymbolizerDescriptor.class );

    public static final FilterFactory       ff = CommonFactoryFinder.getFilterFactory( null );

    /** 
     * Some description to be placed in the resulting SLD to help human reader
     * to grok the structure of the SLD.
     */
    @DefaultString( "Automatically generated by the Polymap4 styler" )
    public Config<String>                   description;

    public Config<Pair<Integer,Integer>>    scale;

    public Config<Filter>                   filter;

    @Immutable
    public Config<Integer>                  zIndex;

    
    @Override
    protected SymbolizerDescriptor clone() {
        try {
            Class cl = getClass();
            Constructor ctor = cl.getConstructor( new Class[] {} );
            SymbolizerDescriptor clone = (SymbolizerDescriptor)ctor.newInstance( new Object[] {} );
            
            while (cl != null) {
                for (Field f : cl.getDeclaredFields()) {
                    f.setAccessible( true );
                    if (Config.class.isAssignableFrom( f.getType() )) {
                       copy( (Config)f.get( this ), (Config)f.get( clone ) );    
                    }
                }
                cl = cl.getSuperclass();
            }
            return clone;
        }
        catch (RuntimeException e) {
            throw e;
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }
    }
    
    
    protected <T> void copy( Config<T> from, Config<T> to ) {
        T newValue = from.get();
        // prevent exceptions for @Immutable properties
        if (!Objects.equals( newValue, to.get() )) {
            to.set( newValue );
        }
    }
    
    
    /**
     * Append (AND) the given filter to the currently set {@link #filter} of this
     * descriptor.
     */
    public void filterAnd( Filter f ) {
        Filter current = (Filter)filter.get();
        filter.set( current == null || current.equals( Filter.INCLUDE )
                ? f : ff.and( current, f ) );
    }
    
}
