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

    @Override
    public int hashCode() {
        return delegate().hashCode();
    }

    @Override
    public boolean equals( Object o ) {
        if (o instanceof CollectionProperty) {
            return getInfo() == ((CollectionProperty)o).getInfo();
        }
        else if (o instanceof Collection) {
            return ((Collection)o).containsAll( delegate() );
        }
        else {
            return false;
        }
    }

    @Override
    public String toString() {
        return delegate().toString();
    }
    

    // Collection *****************************************
    
    @Override
    public int size() {
        return delegate().size();
    }

    @Override
    public boolean isEmpty() {
        return delegate().isEmpty();
    }

    @Override
    public boolean contains( Object o ) {
        return delegate().contains( o );
    }

    @Override
    public Iterator<T> iterator() {
        return delegate().iterator();
    }

    @Override
    public Object[] toArray() {
        return delegate().toArray();
    }

    @Override
    public <V> V[] toArray( V[] a ) {
        return delegate().toArray( a );
    }

    @Override
    public boolean add( T e ) {
        return delegate().add( e );
    }

    @Override
    public boolean remove( Object o ) {
        return delegate().remove( o );
    }

    @Override
    public boolean containsAll( Collection<?> c ) {
        return delegate().containsAll( c );
    }

    @Override
    public boolean addAll( Collection<? extends T> c ) {
        return delegate().addAll( c );
    }

    @Override
    public boolean removeAll( Collection<?> c ) {
        return delegate().removeAll( c );
    }

    @Override
    public boolean retainAll( Collection<?> c ) {
        return delegate().retainAll( c );
    }

    @Override
    public void clear() {
        delegate().clear();
    }
    
}
