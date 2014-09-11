/* 
 * polymap.org
 * Copyright (C) 2012-2014, Falko Bräutigam. All rights reserved.
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
package org.polymap.core.data.feature.recordstore;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;

import org.opengis.feature.Property;
import org.opengis.feature.type.PropertyDescriptor;

import org.polymap.core.runtime.recordstore.IRecordState;

/**
 * Collection implementation on top of {@link IRecordState}. There is no in-memory
 * cache of the values - all calls are redirected to the underlying record state.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
abstract class PropertyCollection<T extends Property>
        extends AbstractCollection<T>
        implements Collection<T> {

    private PropertyDescriptor  desc;
    
    private IRecordState        state;

    private StoreKey            baseKey;
    
    private int                 size = -1;
    

    public PropertyCollection( PropertyDescriptor desc, IRecordState state, StoreKey baseKey ) {
        this.desc = desc;
        this.state = state;
        this.baseKey = baseKey;
    }


    protected abstract T valueAt( StoreKey key );
    
    
    @Override
    public boolean add( T prop ) {
        if (size() >= desc.getMaxOccurs()) {
            throw new RuntimeException( "MaxOccurs limit reached for property: " + baseKey + " (" + desc.getMaxOccurs() + ")" );
        }
        else {
            // increment size and put
            state.put( baseKey.appendCollectionIndex( size++ ).toString(), prop.getValue() );
            state.put( baseKey.appendCollectionLength().toString(), size );
            return true;
        }
    }


    @Override
    public int size() {
        if (size == -1) {
            Integer value = state.get( baseKey.appendCollectionLength().toString() );
            size = value != null ? value : 0;
        }
        return size;
    }


    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            
            private int index = 0;

            @Override
            public boolean hasNext() {
                return index < size();
            }

            @Override
            public T next() {
                return valueAt( baseKey.appendCollectionIndex( index++ ) );
            }

            @Override
            public void remove() {
                // XXX Auto-generated method stub
                throw new RuntimeException( "not yet implemented." );
            }
        };
    }
    
}
