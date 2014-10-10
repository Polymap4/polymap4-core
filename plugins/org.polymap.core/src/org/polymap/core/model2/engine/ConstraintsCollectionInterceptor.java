/* 
 * polymap.org
 * Copyright (C) 2014, Falko Bräutigam. All rights reserved.
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
package org.polymap.core.model2.engine;

import java.util.Collection;
import java.util.Iterator;

import org.polymap.core.model2.CollectionProperty;
import org.polymap.core.model2.engine.EntityRepositoryImpl.EntityRuntimeContextImpl;
import org.polymap.core.model2.runtime.EntityRuntimeContext.EntityStatus;
import org.polymap.core.model2.runtime.ModelRuntimeException;
import org.polymap.core.model2.runtime.ValueInitializer;

/**
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
final class ConstraintsCollectionInterceptor<T>
        extends ConstraintsInterceptor<T>
        implements CollectionProperty<T> {

    public ConstraintsCollectionInterceptor( CollectionProperty<T> delegate, EntityRuntimeContextImpl context ) {
        super( delegate, context );
    }

    protected CollectionProperty<T> coll() {
        return (CollectionProperty<T>)delegate;
    }

    @Override
    public T createElement( ValueInitializer<T> initializer ) {
        if (isImmutable) {
            throw new ModelRuntimeException( "Property is @Immutable: " + fullPropName() );
        }
        context.raiseStatus( EntityStatus.MODIFIED );
        return coll().createElement( initializer );
    }


    // Collection *****************************************
    
    @Override
    public boolean add( T e ) {
        if (isImmutable) {
            throw new ModelRuntimeException( "Property is @Immutable: " + fullPropName() );
        }
        if (coll().add( e )) {
            context.raiseStatus( EntityStatus.MODIFIED );
            return true;
        }
        else {
            return false;
        }
    }

    @Override
    public boolean addAll( Collection<? extends T> c ) {
        if (isImmutable) {
            throw new ModelRuntimeException( "Property is @Immutable: " + fullPropName() );
        }
        if (coll().addAll( c )) {
            context.raiseStatus( EntityStatus.MODIFIED );
            return true;
        }
        else {
            return false;
        }
    }

    @Override
    public boolean remove( Object o ) {
        if (isImmutable) {
            throw new ModelRuntimeException( "Property is @Immutable: " + fullPropName() );
        }
        if (coll().remove( o )) {
            context.raiseStatus( EntityStatus.MODIFIED );
            return true;
        }
        else {
            return false;
        }
    }

    @Override
    public boolean removeAll( Collection<?> c ) {
        if (isImmutable) {
            throw new ModelRuntimeException( "Property is @Immutable: " + fullPropName() );
        }
        if (coll().removeAll( c )) {
            context.raiseStatus( EntityStatus.MODIFIED );
            return true;
        }
        else {
            return false;
        }
    }

    @Override
    public boolean retainAll( Collection<?> c ) {
        if (isImmutable) {
            throw new ModelRuntimeException( "Property is @Immutable: " + fullPropName() );
        }
        if (coll().retainAll( c )) {
            context.raiseStatus( EntityStatus.MODIFIED );
            return true;
        }
        else {
            return false;
        }
    }

    @Override
    public Iterator<T> iterator() {
        if (!isImmutable) {
            return coll().iterator();
        }
        else {
            return new Iterator<T>() {
                private Iterator<T> it = coll().iterator();
                @Override
                public boolean hasNext() {
                    return it.hasNext();
                }
                @Override
                public T next() {
                    return it.next();
                }
                @Override
                public void remove() {
                    throw new ModelRuntimeException( "Property is @Immutable: " + fullPropName() );
                }
            };
        }
    }

    @Override
    public int size() {
        return coll().size();
    }

    @Override
    public boolean isEmpty() {
        return coll().isEmpty();
    }

    @Override
    public boolean contains( Object o ) {
        return coll().contains( o );
    }

    @Override
    public Object[] toArray() {
        return coll().toArray();
    }

    @Override
    public <V> V[] toArray( V[] a ) {
        return coll().toArray( a );
    }

    @Override
    public boolean containsAll( Collection<?> c ) {
        return coll().containsAll( c );
    }

    @Override
    public void clear() {
        coll().clear();
    }

    @Override
    public boolean equals( Object o ) {
        return coll().equals( o );
    }

    @Override
    public int hashCode() {
        return coll().hashCode();
    }

    @Override
    public String toString() {
        return coll().toString();
    }

}
