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
package org.polymap.core.style.model;

import java.util.Iterator;

import org.opengis.style.FeatureTypeStyle;
import org.opengis.style.Rule;

import com.google.common.collect.Iterators;

import org.polymap.model2.CollectionProperty;
import org.polymap.model2.CollectionPropertyConcernAdapter;
import org.polymap.model2.Concerns;
import org.polymap.model2.runtime.ValueInitializer;

/**
 * A logical group of styles. The activity/visibility of all member styles is
 * determined by the value of the {@link Style#active} of the group. A
 * {@link StyleGroup} allows to create a logical structured (human readable)
 * hierarchy of styles.
 * <p/>
 * This is different from {@link FeatureTypeStyle} and {@link Rule} as they do
 * deliver parameters to the renderer and are *not* or even worst half way suited for
 * logical grouping.
 *
 * @author Falko Bräutigam
 */
public class StyleGroup
        extends Style {

    /**
     * Initializes a newly created instance with default values.
     */
    @SuppressWarnings("hiding")
    public static final ValueInitializer<StyleGroup> defaults = new ValueInitializer<StyleGroup>() {
        @Override
        public StyleGroup initialize( StyleGroup proto ) throws Exception {
            Style.defaults.initialize( proto );
            return proto;
        }
    };

    @Concerns( FilterRemovedConcern.class )
    public CollectionProperty<Style>        members;
    
    
    /**
     * 
     */
    public static class FilterRemovedConcern
            extends CollectionPropertyConcernAdapter<Style> {

        @Override
        public Style createElement( ValueInitializer initializer ) {
            return ((CollectionProperty<Style>)delegate).createElement( initializer );
        }

        @Override
        public Iterator<Style> iterator() {
            Iterator<Style> unfiltered = ((CollectionProperty<Style>)delegate).iterator();
            return Iterators.filter( unfiltered, style -> !style.removed.get() );
        }

        // XXX used by UI content provider; due to bugs on Model2 toArray()
        // does not call iterator()
        // (http://github.com/Polymap4/polymap4-model/issues/issue/12)
        @Override
        public Object[] toArray() {
            return Iterators.toArray( iterator(), Object.class );
        }

        @Override
        public int size() {
            return Iterators.size( iterator() );
        }
    }
    
}
