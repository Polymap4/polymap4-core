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

import org.polymap.model2.Concerns;
import org.polymap.model2.Property;
import org.polymap.model2.runtime.ValueInitializer;

/**
 * Provides a constant number as style property value.
 *
 * @author Falko Bräutigam
 */
public class ConstantBoolean
        extends StylePropertyValue<Boolean> {

    /**
     * 
     */
    public static final ValueInitializer<ConstantBoolean> defaultsTrue = new ValueInitializer<ConstantBoolean>() {
        @Override
        public ConstantBoolean initialize( ConstantBoolean proto ) throws Exception {
            proto.value.set( true );
            return proto;
        }
    };

    /**
     * 
     */
    public static final ValueInitializer<ConstantBoolean> defaultsFalse = new ValueInitializer<ConstantBoolean>() {
        @Override
        public ConstantBoolean initialize( ConstantBoolean proto ) throws Exception {
            proto.value.set( true );
            return proto;
        }
    };

    // instance *******************************************
    
    @Concerns(StylePropertyChange.Concern.class)
    public Property<Boolean>            value;
    
}
