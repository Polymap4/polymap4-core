/* 
 * polymap.org
 * Copyright (C) 2012-2014, Falko Bräutigam. All rights reserved.
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
package org.polymap.core.model2;

import java.util.Collection;

import javax.annotation.Nullable;

import org.polymap.core.model2.runtime.ValueInitializer;

/**
 * Property for {@link Collection} values.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface CollectionProperty<T>
        extends PropertyBase<T>, Collection<T> {

    /**
     * Creates a new element and adds it to this collection. For {@link Composite}
     * properties: this method allows the initialize the {@link Composite} value of
     * this property.
     * 
     * @param initializer The initializer to be called for the prototype of the new
     *        element. This parameter may be null. For {@link Composite} elements
     *        this allows to initialize the properties, inkluding non-
     *        {@link Nullable} properties.
     * @return The value of this property, or a newly created {@link Composite} that
     *         is set as the new value of this property.
     * @throws IllegalStateException If this is not a Composite collection.
     */
    public T createElement( ValueInitializer<T> initializer );

}
