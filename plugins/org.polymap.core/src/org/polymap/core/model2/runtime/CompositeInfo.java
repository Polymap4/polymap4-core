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

import java.util.Collection;

import org.polymap.core.model2.Composite;
import org.polymap.core.model2.NameInStore;

/**
 * Provides runtime information about a {@link Composite}.
 * 
 * @see PropertyInfo
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface CompositeInfo<T extends Composite> {

    /**
     * The name of the Composite.
     */
    public String getName();

    /**
     * The name of this property in the underlying store. Usually this is defined
     * via {@link NameInStore}.
     */
    public String getNameInStore();
    
    /**
     * The type this Composite was declared with. 
     */
    public Class<T> getType();

    /**
     * Collection of Mixins of this composite.
     *
     * @return An empty collection of the Composite does not have Mixins.
     */
    public Collection<Class<? extends Composite>> getMixins();

//    public Entity getEntity();

    /**
     * True if all properties of the Composite are immutable.
     */
    public boolean isImmutable();

    public Collection<PropertyInfo> getProperties();

    public PropertyInfo getProperty( String name );
    
}