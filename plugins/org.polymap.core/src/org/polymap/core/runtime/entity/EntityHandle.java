/* 
 * polymap.org
 * Copyright 2011, Polymap GmbH. All rights reserved.
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
package org.polymap.core.runtime.entity;

/**
 * A surrogate of an entity, feature, object of a model.
 * 
 * @since 3.1
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class EntityHandle {

    public static EntityHandle instance( String id, String type ) {
        return new EntityHandle( id, type );
    }

    // instance *******************************************

    String              type;
    
    String              id;

    
    EntityHandle( String id, String type ) {
        assert id != null : "id must not be null.";
        assert type != null : "type must not be null.";
        this.id = id;
        this.type = type;
    }

    
    public int hashCode() {
        return id.hashCode();
    }

    
    public boolean equals( Object obj ) {
        if (this == obj) {
            return true;
        }
        if (obj == null || obj.getClass() != EntityHandle.class) {
            return false;
        }
        EntityHandle other = (EntityHandle)obj;
        return id.equals( other.id ) && type.equals( other.type );
    }

    
    public String toString() {
        return "EntityHandle [id=" + id + ", type=" + type + "]";
    }
    
}