/* 
 * polymap.org
 * Copyright (C) 2016-2018, the @authors. All rights reserved.
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
import org.polymap.core.style.model.StylePropertyValue;

import org.polymap.model2.Concerns;
import org.polymap.model2.Nullable;
import org.polymap.model2.Property;
import org.polymap.model2.runtime.ValueInitializer;

/**
 * A style property that uses values from a Feature attribute.
 *
 * @author Steffen Stundzig
 * @author Falko Bräutigam
 */
public class AttributeValue<T>
        extends StylePropertyValue<T> {

    /**
     * Initializes a newly created instance with the given default value.
     */
    public static <R> ValueInitializer<AttributeValue<R>> defaults( String attributeName, R min, R max ) {
        return new ValueInitializer<AttributeValue<R>>() {
            @Override
            public AttributeValue initialize( AttributeValue proto ) throws Exception {
                proto.attributeName.set( attributeName );
                proto.minimumValue.set( min );
                proto.maximumValue.set( max );
                return proto;
            }
        };
    }

    // instance *******************************************

    /**
     * The name of the feature attribute.
     */
    @Nullable
    @Concerns( StylePropertyChange.Concern.class )
    public Property<String>         attributeName;
 
    @Nullable
    //@NumberRange(defaultValue=0, from=-1, to=-1, increment=-1)
    @Concerns( StylePropertyChange.Concern.class )
    public Property<T>              minimumValue;

    @Nullable
    //@NumberRange(defaultValue=10000, from=-1, to=-1, increment=-1)
    @Concerns( StylePropertyChange.Concern.class )
    public Property<T>              maximumValue;

}
