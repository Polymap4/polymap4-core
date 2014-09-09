/* 
 * polymap.org
 * Copyright (C) 2013, Falko Bräutigam. All rights reserved.
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
package org.polymap.core.model2.engine;

import java.util.AbstractCollection;
import java.util.Iterator;

import org.polymap.core.model2.CollectionProperty;
import org.polymap.core.model2.runtime.PropertyInfo;
import org.polymap.core.model2.runtime.ValueInitializer;
import org.polymap.core.model2.store.StoreCollectionProperty;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
class CollectionPropertyImpl<T>
        extends AbstractCollection<T>
        implements CollectionProperty<T> {

    protected StoreCollectionProperty<T>    storeProp;
    

    public CollectionPropertyImpl( StoreCollectionProperty storeProp ) {
        this.storeProp = storeProp;
    }

    @Override
    public T createElement( ValueInitializer<T> initializer ) {
        throw new RuntimeException( "not yet..." );
    }

    @Override
    public PropertyInfo getInfo() {
        return storeProp.getInfo();
    }

    // Collection *****************************************
    
    @Override
    public int size() {
        return storeProp.size();
    }

    @Override
    public Iterator<T> iterator() {
        return storeProp.iterator();
    }

    @Override
    public boolean add( T elm ) {
        return storeProp.add( elm );
    }
    
}
