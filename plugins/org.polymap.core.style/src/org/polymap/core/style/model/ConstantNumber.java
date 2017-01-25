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
 * Describes a constant number as style property value.
 *
 * @author Falko Bräutigam
 */
public class ConstantNumber<T extends Number>
        extends StylePropertyValue<T> {

    /**
     * Initializes a newly created instance with the given default value.
     */
    public static <N extends Number> ValueInitializer<ConstantNumber<N>> defaults( N value ) {
        return new ValueInitializer<ConstantNumber<N>>() {
            @Override
            public ConstantNumber initialize( ConstantNumber proto ) throws Exception {
                proto.constantNumber.set( value );
                return proto;
            }
        };
    }
    

    // instance *******************************************
    
    //@Nullable
    @Concerns( StylePropertyChange.Concern.class )
    public Property<Number>             constantNumber;
    
}
