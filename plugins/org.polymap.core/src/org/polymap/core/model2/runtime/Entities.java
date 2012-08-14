/* 
 * polymap.org
 * Copyright 2012, Falko Bräutigam. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.core.model2.runtime;

import com.google.common.base.Function;

import org.polymap.core.model2.Entity;

/**
 * Static filters, functions and helpers to work with {@link Entity}s. 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class Entities {

    /**
     * Provides a {@link Function} that transforms {@link Entity} into its underlying
     * state.
     */
    public static <T> Function<Entity,T> toStates( Class<T> stateType ) {
        return new Function<Entity,T>() {
            public T apply( Entity input ) {
                return (T)input.state();
            }
        };
    }

}
