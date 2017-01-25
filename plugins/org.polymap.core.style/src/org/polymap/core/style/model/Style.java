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

import org.opengis.feature.Feature;
import org.opengis.filter.Filter;

import com.vividsolutions.jts.geom.Geometry;

import org.polymap.model2.CollectionProperty;
import org.polymap.model2.Composite;
import org.polymap.model2.Concerns;
import org.polymap.model2.DefaultValue;
import org.polymap.model2.Description;
import org.polymap.model2.Property;
import org.polymap.model2.runtime.ValueInitializer;

/**
 * Describes the visual representation of the {@link Geometry} of a {@link Feature}.
 * This is <b>not tied</b> to any particular backend (SLD, OpenLayers). It rather
 * models the "user experience" we want to achieve.
 *
 * @author Falko Bräutigam
 */
public abstract class Style
        extends StyleComposite {

    /**
     * Initializes a newly created instance with default values.
     */
    @SuppressWarnings( "hiding" )
    public static final ValueInitializer<Style> defaults = new ValueInitializer<Style>() {
        @Override
        public Style initialize( Style proto ) throws Exception {
            StyleComposite.defaults.initialize( proto );
            proto.visibleIf.createValue( ConstantFilter.defaultTrue );
            return proto;
        }
    };

    /**
     * Allow the user to give this style a title in order to better distinguish between
     * the styles in the {@link StyleGroup} hierarchy.
     */
    @DefaultValue( "[No title]" )
    @Description( "title" )
    @Concerns( StylePropertyChange.Concern.class )
    public Property<String>                     title;
    
    /**
     * User provided description of the style.
     */
    @DefaultValue( "[No description]" )
    @Description( "description" )
    @Concerns( StylePropertyChange.Concern.class )
    public Property<String>                     description;

    /**
     * Allows the user to deactivate this style in order to test render result with
     * actually removing it.
     */
    @DefaultValue( "true" )
    @Concerns( StylePropertyChange.Concern.class )
    public Property<Boolean>                    active;
    
    /**
     * Due to limitations of {@link Composite} {@link CollectionProperty}s it
     * is better to flag removed instances rather than actually removing them.
     */
    @DefaultValue( "false" )
    @Concerns( StylePropertyChange.Concern.class )
    public Property<Boolean>                    removed;
    
    /**
     * Describes the condition that makes this style is generally active/visible. The
     * value might be a constant or a complex filter expression.
     * <p/>
     * Defaults to: {@link ConstantFilter#defaultTrue}
     */
    @UIOrder( 0 )
    @Description( "activeIf" )
    @Concerns( StylePropertyChange.Concern.class )
    public Property<StylePropertyValue<Filter>> visibleIf;

//    /**
//     * This allows to bring structure to the (possibly many) styles of an
//     * {@link FeatureStyle}. Used to have the notion of "folders" or something like
//     * that in the UI. Not sure about this yet :)
//     */
//    public CollectionProperty<String>           tags;


    /**
     * Identity check. Necessary to make UI work with Style instances.
     */
    @Override
    public boolean equals( Object obj ) {
        return obj == this;
    }
    
}
