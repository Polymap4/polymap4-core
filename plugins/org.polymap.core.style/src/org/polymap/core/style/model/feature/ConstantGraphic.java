/* 
 * polymap.org
 * Copyright (C) 2018, the @authors. All rights reserved.
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

import org.polymap.core.style.model.StylePropertyChange;
import org.polymap.core.style.model.feature.Graphic.WellKnownMark;

import org.polymap.model2.Concerns;
import org.polymap.model2.Property;
import org.polymap.model2.runtime.ValueInitializer;

/**
 * 
 *
 * @author Falko Bräutigam
 */
public class ConstantGraphic
        extends ConstantValue<Graphic> {
    
    /** Initializes a newly created instance with default values. */
    public static ValueInitializer<ConstantGraphic> defaults( WellKnownMark mark ) {
        return new ValueInitializer<ConstantGraphic>() {
            @Override
            public ConstantGraphic initialize( ConstantGraphic proto ) throws Exception {
                proto.markOrName.set( mark.name() );
                return proto;
            }
        };
    }

    /** Initializes a newly created instance with default values. */
    public static ValueInitializer<ConstantGraphic> defaults( String graphicName ) {
        return new ValueInitializer<ConstantGraphic>() {
            @Override
            public ConstantGraphic initialize( ConstantGraphic proto ) throws Exception {
                proto.markOrName.set( graphicName );
                return proto;
            }
        };
    }

    // instance *******************************************
    
    /** {@link WellKnownMark} or the URL of an external image. */
    @Concerns(StylePropertyChange.Concern.class)
    public Property<String>             markOrName;
    
    
    @Override
    public Graphic value() {
        return new Graphic( markOrName.get() );
    }

}
