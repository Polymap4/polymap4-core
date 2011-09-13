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
package org.polymap.core.model.event;

/**
 * A surrogate of an entity, feature, object of a model.
 * 
 * @since 3.1
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ModelHandle {

    public static ModelHandle instance( String id, String type ) {
        return new ModelHandle( id, type );
    }

    // instance *******************************************

    String              type;
    
    String              id;

    
    ModelHandle( String id, String type ) {
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
        if (obj == null || obj.getClass() != ModelHandle.class) {
            return false;
        }
        ModelHandle other = (ModelHandle)obj;
        return id.equals( other.id ) && type.equals( other.type );
    }

    
    public String toString() {
        return "ModelHandle [id=" + id + ", type=" + type + "]";
    }
    
}