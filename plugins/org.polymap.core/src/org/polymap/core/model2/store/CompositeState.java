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
package org.polymap.core.model2.store;

import org.polymap.core.model2.Composite;
import org.polymap.core.model2.Entity;
import org.polymap.core.model2.runtime.PropertyInfo;

/**
 * Represents the state of a {@link Composite} in the underlying store. The Composite
 * can be an {@link Entity} or the value of a property. The hierarchy of Composite
 * properties and/or collections thereof is initialized on demand when the properties
 * are actually accessed.
 * <p/>
 * This is used as a factory of {@link StoreProperty} instances by the engine.
 * Implementations are not supposed to cache returned properties. The engine is
 * responsible of properly caching things if necessary.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface CompositeState {

    public Object id();

    /**
     * 
     * 
     * @param info
     * @return Newly created instance of {@link StoreProperty} or
     *         {@link StoreCompositeProperty} or a Collection property depending on
     *         the nature of the value of the property.
     */
    public StoreProperty loadProperty( PropertyInfo info );

    /**
     * The backend of this CompositeState. Th eresult is returned by {@link Composite#state()}.
     */
    public Object getUnderlying();
    
}
