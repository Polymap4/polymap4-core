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

import org.polymap.core.model2.runtime.EntityRuntimeContext;
import org.polymap.core.model2.runtime.EntityRuntimeContext.EntityStatus;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class Entity
        extends Composite {

    protected EntityRuntimeContext      context;
    
    public Object id() {
        return context.id();
    }
    
    public Object state() {
        return context.state();
    }
    
    public EntityStatus status() {
        return context.status();
    }


    /**
     * Casts this entity into one of its Mixin types. Mixins are defined via the
     * {@link Mixins} annotation.
     * 
     * @param <T>
     * @param mixinClass
     * @return A mixin of the given type, or null if no such mixin was defined.
     */
    public <T> T as( Class<T> mixinClass ) {
        return context.createMixin( mixinClass );
    }

    
    protected void methodProlog( String methodName, Object... args ) {
        context.methodProlog( methodName, args );
    }
    
}
