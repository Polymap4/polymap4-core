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

import org.polymap.core.model2.engine.UnitOfWorkImpl;
import org.polymap.core.model2.runtime.EntityRuntimeContext.EntityStatus;
import org.polymap.core.model2.runtime.UnitOfWork;

/**
 * An Entity is a directly instantiable {@link Composite} with an {@link #id()
 * identifier}.
 * 
 * @see Composite
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class Entity
        extends Composite {
    
    public Object id() {
        return context.getState().id();
    }
    
    public EntityStatus status() {
        return context.getStatus();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[id=" + id() + ",status=" + status() + ",state=" + state() + "]" ;
    }

    /**
     * By default two entities are {@link #equals(Object) equal} only if they are the
     * same object. That is, even two {@link Entity} instances refering the same state
     * are not equal if they were instantiated in differnet {@link UnitOfWork}s.
     */
    @Override
    public boolean equals( Object obj ) {
        return this == obj;
    }

    /**
     * Casts this entity into one of its Mixin types. Mixins are defined via the
     * {@link Mixins} annotation or at runtime. Creating runtime Mixins is not as
     * efficient as Mixins defined via the annotation.
     * 
     * @param <T>
     * @param mixinClass
     * @return A mixin of the given type, or null if no such mixin was defined.
     */
    public <T extends Composite> T as( Class<T> mixinClass ) {
        return ((UnitOfWorkImpl)context.getUnitOfWork()).mixin( mixinClass, this );
    }

    protected void methodProlog( String methodName, Object... args ) {
        context.methodProlog( methodName, args );
    }
    
}
