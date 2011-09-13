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

import org.polymap.core.model.event.ModelChangeTracker;
import org.polymap.core.model.event.ModelHandle;

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
        return entity.isDirty();
    }
    
    
    public boolean isConcurrentlyDirty() {
        ModelHandle key = ModelHandle.instance( entity.id(), entity.getEntityType().getName() );
        return ModelChangeTracker.instance().isConcurrentlyTracked( key );            
    }
    
    
    public boolean isConflicting() {
        ModelHandle key = ModelHandle.instance( entity.id(), entity.getEntityType().getName() );
        Long timestamp = entity._lastModified().get();
        
        if (timestamp == null) {
            return false;
        }
        
        return ModelChangeTracker.instance().isConflicting( key, timestamp );            
    }

}