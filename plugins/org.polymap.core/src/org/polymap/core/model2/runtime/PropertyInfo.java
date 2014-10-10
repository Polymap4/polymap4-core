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

import javax.annotation.Nullable;

import org.polymap.core.model2.Composite;
import org.polymap.core.model2.Immutable;
import org.polymap.core.model2.NameInStore;
import org.polymap.core.model2.Property;
import org.polymap.core.model2.PropertyBase;

/**
 * Provides runtime information about a {@link Property}.
 * 
 * @see CompositeInfo
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface PropertyInfo<T> {

    /**
     * The name of the property.
     */
    public String getName();

    /**
     * The name of this property in the underlying store. Usually this is defined
     * via {@link NameInStore}.
     */
    public String getNameInStore();


    /**
     * The type this property was declared with.
     * <ul>
     * <li><code>Property&lt;String&gt;</code> : <code>String.class</code></li>
     * <li><code>CompositeProperty&lt;Person&gt;</code> : <code>Person.class</code></li>
     * <li><code>CollectionProperty&lt;String&gt;</code> : <code>String.class</code></li>
     * </ul>
     * For Collection properties {@link #getMaxOccurs()} returns a value greater 0.
     * 
     * @return The type this property was declared with.
     */
    public Class<T> getType();

    public boolean isAssociation();

    //public CompositeInfo getDeclaringComposite();

    public T getDefaultValue();

    /**
     * True if the {@link Property} was marked as {@link Immutable}.
     */
    public boolean isImmutable();

    /**
     * True if the {@link Property} was marked as {@link Nullable}.
     */
    public boolean isNullable();

    /**
     * True if the {@link Property} is {@link @Computed}.
     */
    public boolean isComputed();

    /**
     * True if the {@link Property} is {@link @Queryable}.
     */
    public boolean isQueryable();

    /**
     *
     */
    public int getMaxOccurs();
    
    /**
     * Provides access to the {@link Property} instance of a given Composite. Allows
     * to get/set the value of the property.
     */
    public PropertyBase<T> get( Composite composite );
    
}