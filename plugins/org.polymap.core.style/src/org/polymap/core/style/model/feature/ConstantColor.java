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
package org.polymap.core.style.model.feature;

import java.awt.Color;

import org.polymap.core.style.model.StylePropertyChange;

import org.polymap.model2.Concerns;
import org.polymap.model2.Property;
import org.polymap.model2.runtime.ValueInitializer;

/**
 * 
 *
 * @author Falko Bräutigam
 */
public class ConstantColor
        extends ConstantValue<Color> {

    /** Initializes a newly created instance with default values. */
    public static ValueInitializer<ConstantColor> defaults( int r, int g, int b ) {
        return new ValueInitializer<ConstantColor>() {
            @Override
            public ConstantColor initialize( ConstantColor proto ) throws Exception {
                proto.r.set( r );
                proto.g.set( g );
                proto.b.set( b );
                return proto;
            }
        };
    }

    public static ValueInitializer<ConstantColor> defaults( Color color ) {
        return new ValueInitializer<ConstantColor>() {
            @Override
            public ConstantColor initialize( ConstantColor proto ) throws Exception {
                proto.r.set( color.getRed() );
                proto.g.set( color.getGreen() );
                proto.b.set( color.getBlue() );
                return proto;
            }
        };
    }

    // instance *******************************************
    
    @NumberConcern.Range( from=0, to=255 )
    @Concerns( {NumberConcern.class, StylePropertyChange.Concern.class} )
    public Property<Integer>            r;
    
    @NumberConcern.Range( from=0, to=255 )
    @Concerns( {NumberConcern.class, StylePropertyChange.Concern.class} )
    public Property<Integer>            g;
    
    @NumberConcern.Range( from=0, to=255 )
    @Concerns( {NumberConcern.class, StylePropertyChange.Concern.class} )
    public Property<Integer>            b;
    
    
    @Override
    public Color value() {
        return new Color( r.get(), g.get(), b.get() );
    }
    
}
