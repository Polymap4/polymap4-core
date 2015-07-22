/* 
 * polymap.org
 * Copyright (C) 2015, Falko Bräutigam. All rights reserved.
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
package org.polymap.core.runtime.config;

/**
 * Provides the {@link #put(Object)} method which allows to chain calls for a fluent call style.
 * <p/>
 * @param <H> - The type of the host class.
 * @param <V> - The type of the value of this property. 
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface Config2<H,V>
        extends Config<V> {

    /**
     * Attempts to set the value of this Property and allow fluent call style.
     * 
     * @see #set(Object)
     * @param newValue
     * @return The host instance to allow fluent call style. The actual return type
     *         can be a sub-class of the host. This allows the compiler to infer the
     *         proper type even if the Property is declared in a super class of the
     *         actual host instance. The call may also use type witness to explicitly
     *         hint the compiler about the return type without the need to cast the
     *         return type.
     */
    public <AH extends H> AH put( V newValue );
    
}
