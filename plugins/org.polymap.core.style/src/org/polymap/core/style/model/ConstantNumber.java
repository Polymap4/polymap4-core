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

import org.polymap.model2.Property;
import org.polymap.model2.runtime.TypedValueInitializer;

/**
 * Describes a constant number as style property value.
 *
 * @author Falko Bräutigam
 */
public class ConstantNumber
        extends StylePropertyValue {

    /**
     * Initializes a newly created instance with default values.
     */
    public static TypedValueInitializer<ConstantNumber> defaults( int value ) {
        return new TypedValueInitializer<ConstantNumber>() {
            @Override
            public ConstantNumber initialize( ConstantNumber proto ) throws Exception {
                proto.value.set( value );
                return proto;
            }
        };
    }
    

    // instance *******************************************
    
    public Property<Number>             value;
    
}
