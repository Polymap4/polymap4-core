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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.vividsolutions.jts.geom.Geometry;

import org.polymap.model2.CollectionProperty;
import org.polymap.model2.Composite;
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
        extends Composite {

    private static Log log = LogFactory.getLog( Style.class );
    
    /**
     * Initializes a newly created instance with default values.
     */
    public static final ValueInitializer<Style> defaults = new ValueInitializer<Style>() {
        @Override
        public Style initialize( Style proto ) throws Exception {
            proto.activeIf.createValue( ConstantFilter.defaultTrue );
            return proto;
        }
    };
    

    /**
     * Describes the condition that makes this style is active/visible. The value
     * might be a constant or a complex filter expression.
     * <p/>
     * Defaults to: {@link ConstantFilter#defaultTrue}
     */
    @Description( "Active if..." )
    public Property<StylePropertyValue<Filter>> activeIf;

    /**
     * This allows to bring structure to the (possibly many) styles of an
     * {@link FeatureStyle}. Used to have the notion of "folders" or something like
     * that in the UI. Not sure about this yet :)
     */
    public CollectionProperty<String>           tags;
    
}
