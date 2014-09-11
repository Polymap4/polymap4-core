/* 
 * polymap.org
 * Copyright (C) 2014, Falko Bräutigam. All rights reserved.
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
package org.polymap.core.model2;

import java.util.Collection;
import java.util.Iterator;

import org.polymap.core.model2.runtime.ValueInitializer;

/**
 * Provides no-op implementations for all methods.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class CollectionPropertyConcernAdapter<T>
        extends PropertyConcernBase<T>
        implements CollectionPropertyConcern<T> {

    protected CollectionProperty<T> delegate() {
        return (CollectionProperty<T>)delegate;
    }

    public T createElement( ValueInitializer<T> initializer ) {
        return delegate().createElement( initializer );
    }

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

    public <V> V[] toArray( V[] a ) {
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
