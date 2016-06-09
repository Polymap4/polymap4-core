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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import java.awt.Color;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Lists;

import org.polymap.model2.CollectionProperty;
import org.polymap.model2.Concerns;
import org.polymap.model2.Property;
import org.polymap.model2.runtime.ValueInitializer;

/**
 * Conditions based numbers.
 * 
 * @author Steffen Stundzig
 */
public class ExpressionMappedColors
        extends ExpressionMappedValues<Color> {

    private static Log log = LogFactory.getLog( ExpressionMappedColors.class );


    /**
     * Initializes a newly created instance with default values.
     */
    public static ValueInitializer<ExpressionMappedColors> defaults() {
        return new ValueInitializer<ExpressionMappedColors>() {

            @Override
            public ExpressionMappedColors initialize( ExpressionMappedColors proto ) throws Exception {
                return proto;
            }
        };
    }

    // instance *******************************************

    @NumberConcern.Range(from = 0, to = 255)
    @Concerns({ NumberConcern.class, StylePropertyChange.Concern.class })
    public Property<Integer> defaultR;

    @NumberConcern.Range(from = 0, to = 255)
    @Concerns({ NumberConcern.class, StylePropertyChange.Concern.class })
    public Property<Integer> defaultG;

    @NumberConcern.Range(from = 0, to = 255)
    @Concerns({ NumberConcern.class, StylePropertyChange.Concern.class })
    public Property<Integer> defaultB;

    // @Concerns( StylePropertyChange.Concern.class )
    public CollectionProperty<Integer> colorValues;


    @Override
    public ExpressionMappedColors add( Color value ) {
        colorValues.add( value.getRed() );
        colorValues.add( value.getGreen() );
        colorValues.add( value.getBlue() );
        return this;
    }


    @Override
    public Color defaultValue() {
        return defaultR.get() != null ? new Color( defaultR.get(), defaultG.get(), defaultB.get() ) : null;
    }


    @Override
    public Collection<Color> values() {
        List<Color> colors = Lists.newArrayList();
        Iterator<Integer> iterator = colorValues.iterator();
        while (iterator.hasNext()) {
            colors.add( new Color( iterator.next(), iterator.next(), iterator.next() ) );
        }
        return colors;
    }


    public void setDefaultColor( Color color ) {
        if (color != null) {
            defaultR.set( color.getRed() );
            defaultG.set( color.getGreen() );
            defaultB.set( color.getBlue() );
        }
        else {
            defaultR.set( null );
            defaultG.set( null );
            defaultB.set( null );
        }
    }
}
