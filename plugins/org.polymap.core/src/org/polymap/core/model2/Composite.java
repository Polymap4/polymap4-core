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
package org.polymap.core.model2;

import org.polymap.core.model2.runtime.CompositeInfo;
import org.polymap.core.model2.runtime.EntityRuntimeContext;

/**
 * A Composite is the base abstraction for defining a domain model. A Composite
 * consists of a number of Properties. Properties can have primitive or Composite
 * values or a Collection thereof. Properties are declared as {@link Property}
 * members.
 * <p/>
 * Runtime information about an instance of a Composite can be retrieved by calling
 * {@link #info()}.
 * <p/>
 * A Composite can be an {@link Entity}, a Mixin or a complex Property.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class Composite {

    protected EntityRuntimeContext      context;
    

    public Object state() {
        return context.getState().getUnderlying();
    }
    

    /**
     * 
     */
    public CompositeInfo info() {
        return context.getRepository().infoOf( getClass() );
    }
    
}
