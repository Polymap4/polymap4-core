/* 
 * polymap.org
 * Copyright 2011, Falko Bräutigam, and other contributors as
 * indicated by the @authors tag. All rights reserved.
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
package org.polymap.core.qi4j.event;

import org.qi4j.api.unitofwork.NoSuchEntityException;

import org.polymap.core.runtime.entity.EntityStateTracker;
import org.polymap.core.runtime.entity.EntityHandle;

/**
 * Uniform API to access the modification status of an entity.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @since 3.1
 */
public class EntityChangeStatus {
    
    public static EntityChangeStatus forEntity( ModelChangeSupport entity ) {
        return new EntityChangeStatus( entity );
    }
    
    
    // instance *******************************************
    
    private ModelChangeSupport      entity;
    
    
    protected EntityChangeStatus( ModelChangeSupport entity ) {
        this.entity = entity;
    }

    /**
     * True if this entity has uncommited changes in the local session.
     */
    public boolean isDirty() {
        try {
            return entity.isDirty();
        }
        catch (NoSuchEntityException e) {
            return true;
        }
    }
    
    
    public boolean isConcurrentlyDirty() {
        EntityHandle key = EntityHandle.instance( entity.id(), entity.getEntityType().getName() );
        return EntityStateTracker.instance().isConcurrentlyTracked( key );            
    }
    
    
    public boolean isConflicting() {
        EntityHandle key = EntityHandle.instance( entity.id(), entity.getEntityType().getName() );
        // older versions have Integer
        Number lastModified = entity._lastModified().get();
        Long timestamp = lastModified != null
                ? lastModified.longValue()
                : null;

        if (timestamp == null) {
            return false;
        }
        
        return EntityStateTracker.instance().isConflicting( key, timestamp );            
    }

}