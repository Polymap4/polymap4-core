/* 
 * polymap.org
 * Copyright 2012, Polymap GmbH. All rights reserved.
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
package org.polymap.core.data.util;

import org.opengis.filter.identity.Identifier;

import com.google.common.base.Function;

/**
 * Static helpers to be used with {@link Identifier}. 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class Identifiers {

    /**
     * Transforms {@link Identifier} into String by calling
     * {@link Identifier#getID()}.toString().
     */
    public static Function<Identifier,String> asString() {
        return new Function<Identifier,String>() {
            public String apply( Identifier input ) {
                return input.getID().toString();
            }
        };
    }
    
}
