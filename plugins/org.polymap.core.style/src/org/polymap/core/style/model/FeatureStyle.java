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

import org.opengis.feature.type.FeatureType;

import com.vividsolutions.jts.geom.Geometry;

import org.polymap.model2.CollectionProperty;
import org.polymap.model2.Entity;
import org.polymap.model2.Property;
import org.polymap.model2.runtime.ValueInitializer;

/**
 * Describes the visual representation (aka style) of a {@link FeatureType} and its
 * {@link Geometry}. This is <b>not tied</b> to any particular backend (SLD,
 * OpenLayers). It rather models the "user experience" we want to achieve.
 *
 * @author Falko Bräutigam
 */
public class FeatureStyle
        extends Entity {

    public static FeatureStyle          TYPE;
    

    /**
     * Initializes a newly created instance with default values.
     */
    public static ValueInitializer<FeatureStyle> defaults = (FeatureStyle proto) -> {
        proto.styles.createValue( StyleGroup.defaults );
        return proto;
    };

    
    // instance *******************************************
    
    /**
     * The root group of styles.
     */
    public Property<StyleGroup>         styles;
    
    /**
     * Shortcut to <code>styles.get().members</code>.
     */
    public CollectionProperty<Style> members() {
        return styles.get().members;
    }
    
}
