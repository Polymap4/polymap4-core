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

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface StoreSPI {

    public void init( StoreRuntimeContext context );
    
    public void close();

    public Object stateId( Object state );


    /**
     * Creates a new Property defined by the given descriptor.
     * <p/>
     * This method is responsible of creating properties of primitive types only.
     * Composite types and Collections are handled by the engine.
     * {@link PropertyDescriptor#getParent()} decribes the position of this Property
     * in the type hierarchy.
     * 
     * @param descriptor
     * @return Newly created Property.
     */
//    public Property createProperty( PropertyDescriptor descriptor );

    public StoreUnitOfWork createUnitOfWork();

}
