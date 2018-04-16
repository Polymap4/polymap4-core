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

import java.util.LinkedList;
import java.util.UUID;
import java.util.stream.Collectors;

import org.opengis.feature.Feature;
import org.opengis.filter.Filter;

import com.vividsolutions.jts.geom.Geometry;

import org.polymap.core.style.model.feature.ConstantFilter;
import org.polymap.core.style.model.feature.Displacement;

import org.polymap.model2.CollectionProperty;
import org.polymap.model2.Composite;
import org.polymap.model2.Concerns;
import org.polymap.model2.DefaultValue;
import org.polymap.model2.Description;
import org.polymap.model2.Nullable;
import org.polymap.model2.Property;
import org.polymap.model2.runtime.UnitOfWork;
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
            proto.id.set( UUID.randomUUID().toString() );
            proto.visibleIf.createValue( ConstantFilter.defaults( true ) );
            proto.zPriority.set( (int)System.currentTimeMillis() );
            proto.displacement.createValue( Displacement.defaults( 0, 0, 0 ) );
            return proto;
        }
    };

    /**
     * Makes Style referencable (by other Styles, for shadow for example).
     */
    @Nullable
    @Concerns( StylePropertyChange.Concern.class )
    public Property<String>                     id;
    
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
     * The order of rendering.
     */
    @DefaultValue( "0" )
    @Concerns( StylePropertyChange.Concern.class )
    public Property<Integer>                    zPriority;
    
    /**
     * The condition that makes this style active/visible. The value might be a
     * constant or a complex filter expression.
     * <p/>
     * Defaults to: {@link ConstantFilter#defaultTrue}
     */
    @UIOrder( 0 )
    @Description( "activeIf" )
    @Concerns( StylePropertyChange.Concern.class )
    public Property<StylePropertyValue<Filter>> visibleIf;

    @Nullable
    @UIOrder( 1000 )
    @Description( "displacement" )
    @Concerns( StylePropertyChange.Concern.class )
    public Property<Displacement>               displacement;


    public UnitOfWork belongsTo() {
        return context.getUnitOfWork();
    }
    
    
    /**
     * The {@link FeatureStyle} entity this {@link Style} is part of.
     * <p/>
     * The name of {@link FeatureStyle} is subject to change in next versions. This
     * method alreadfy reflects new name.
     */
    public FeatureStyle mapStyle() {
        return context.getEntity();
    }
    
    /**
     * Identity check. Necessary to make UI work with Style instances.
     */
    @Override
    public boolean equals( Object obj ) {
        return obj == this;
    }
    
    /**
     * Update the {@link #zPriority} of this Style and siblings. Inserts the receiver
     * <code>before/after</code> the given <code>other</other> Style of the same parent.
     *
     * @param other The Style to indert the receiver before/after.
     * @param before
     */
    public void setZPriority( Style other, boolean before ) {
        FeatureStyle parent = context.getCompositePart( FeatureStyle.class );
        LinkedList<Style> sorted = parent.styles.get().members.stream()
                .filter( s -> s != this )
                .sorted( (s1,s2) -> s1.zPriority.get().compareTo( s2.zPriority.get() ) )
                .collect( Collectors.toCollection( () -> new LinkedList() ) );
        int index = sorted.indexOf( other );
        sorted.add( before ? index : index+1, this );
        
        assert parent.styles.get().members.size() == sorted.size();
        for (int i=0; i<sorted.size(); i++) {
            sorted.get( i ).zPriority.set( i );
        }
    }

    /**
     * The minimum {@link #zPriority} of this Style and all of its siblings.
     */
    public Integer minZPriority() {
        FeatureStyle parent = context.getCompositePart( FeatureStyle.class );
        return parent.styles.get().members.stream().mapToInt( s -> s.zPriority.get() ).min().orElse( 1 );
    }
    
}
