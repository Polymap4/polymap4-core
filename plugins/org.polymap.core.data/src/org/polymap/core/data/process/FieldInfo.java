/* 
 * polymap.org
 * Copyright (C) 2017, the @authors. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.core.data.process;

/**
 * Provides information about a field of a module.
 * 
 * @author <a href="http://mapzone.io">Falko Bräutigam</a>
 */
public interface FieldInfo<T>
        extends BaseInfo {

    /**
     * The type of the the value of this field.
     */
    public abstract Class<T> type();
    
    /**
     * Denotes if this is an input or output field.
     */
    public abstract boolean isInput();
    
    public abstract T getValue( Object module );
    
    public abstract FieldInfo setValue( Object module, T value );
    
    
//    @Override
//    public String toString() {
//        return Joiner.on( "" ).join( "FieldInfo[", 
//                "name=", name.get().orElse( field.getName() ), ", " ,
//                "description=", description.get().orElse( "<empty>" ),
//                "]" );
//    }
    
}
