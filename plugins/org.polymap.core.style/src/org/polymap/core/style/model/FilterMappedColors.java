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
import java.util.List;

import java.awt.Color;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Lists;

import org.polymap.model2.CollectionProperty;
import org.polymap.model2.runtime.ValueInitializer;

/**
 * Colors mapped filters.
 * 
 * @author Steffen Stundzig
 */
public class FilterMappedColors
        extends FilterMappedValues<Color> {

    private static Log log = LogFactory.getLog( FilterMappedColors.class );


    /**
     * Initializes a newly created instance with default values.
     */
    public static ValueInitializer<FilterMappedColors> defaults() {
        return new ValueInitializer<FilterMappedColors>() {

            @Override
            public FilterMappedColors initialize( FilterMappedColors proto ) throws Exception {
                return proto;
            }
        };
    }

    // instance *******************************************

    // @Concerns( StylePropertyChange.Concern.class )
    public CollectionProperty<Integer> colorValues;


    @Override
    public FilterMappedColors addValue( Color value ) {
        colorValues.add( value.getRed() );
        colorValues.add( value.getGreen() );
        colorValues.add( value.getBlue() );
        return this;
    }


    @Override
    public List<Color> values() {
        List<Color> colors = Lists.newArrayList();
        Iterator<Integer> iterator = colorValues.iterator();
        while (iterator.hasNext()) {
            colors.add( new Color( iterator.next(), iterator.next(), iterator.next() ) );
        }
        return colors;
    }
}
