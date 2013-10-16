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

import java.util.Collection;
import java.util.Iterator;

import org.polymap.core.model2.CollectionProperty;
import org.polymap.core.model2.runtime.ValueInitializer;
import org.polymap.core.model2.store.StoreCollectionProperty;
import org.polymap.core.model2.store.StoreProperty;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
class CollectionPropertyImpl<T>
        extends PropertyImpl<Collection<T>>
        implements CollectionProperty<T> {

    public CollectionPropertyImpl( StoreProperty storeProp ) {
        super( storeProp );
    }

    protected StoreCollectionProperty<T> delegate() {
        return (StoreCollectionProperty<T>)super.delegate();
    }
    
    @Override
    public T createElement( ValueInitializer<T> initializer ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

    
    // Collection *****************************************
    
    public int size() {
        return delegate().size();
    }

    public boolean isEmpty() {
        return delegate().isEmpty();
    }

    public boolean contains( Object o ) {
        return delegate().contains( o );
    }

    public Iterator<T> iterator() {
        return delegate().iterator();
    }

    public Object[] toArray() {
        return delegate().toArray();
    }

    public <A> A[] toArray( A[] a ) {
        return delegate().toArray( a );
    }

    public boolean add( T e ) {
        return delegate().add( e );
    }

    public boolean remove( Object o ) {
        return delegate().remove( o );
    }

    public boolean containsAll( Collection<?> c ) {
        return delegate().containsAll( c );
    }

    public boolean addAll( Collection<? extends T> c ) {
        return delegate().addAll( c );
    }

    public boolean removeAll( Collection<?> c ) {
        return delegate().removeAll( c );
    }

    public boolean retainAll( Collection<?> c ) {
        return delegate().retainAll( c );
    }

    public void clear() {
        delegate().clear();
    }

}
