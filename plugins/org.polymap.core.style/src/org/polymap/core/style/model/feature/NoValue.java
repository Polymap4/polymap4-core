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
import org.polymap.model2.Property;
import org.polymap.model2.runtime.ValueInitializer;

/**
 * Provides support for an empty value.
 *
 * @author Steffen Stundzig
 */
public class NoValue
        extends StylePropertyValue<Object> {

    public static ValueInitializer defaults() {
        return new ValueInitializer<NoValue>() {
            @Override
            public NoValue initialize( NoValue proto ) throws Exception {
                return proto;
            }
        };
    }

    /** Only to trigger the events in the editor. */
    @Concerns({ StylePropertyChange.Concern.class })
    public Property<Object> noValue;
}
