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

import java.util.Iterator;

import org.polymap.core.model2.runtime.PropertyInfo;

/**
 * Store backend for collections of: primitive type, String, Date,
 * {@link CompositeState}.
 * <p/>
 * Consider {@link StoreCollectionProperty2} to get control over multi add/remove
 * operations.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface StoreCollectionProperty<T>
        extends Iterable<T>
        /*extends StoreProperty<Collection<T>>, Collection<T>*/ {

    @Override
    public Iterator<T> iterator();
    
    public int size();

    public boolean add( T elm );
    
    /**
     * Creates a new value for this property. For simple properties usually this is
     * just {@link PropertyInfo#getDefaultValue()}. For {@link CompositeState} value this
     * is a new {@link CompositeState}.
     * 
     * @return Newly created value for this property.
     */
    public T createValue();

    public PropertyInfo getInfo();

}
