/* 
 * polymap.org
 * Copyright (C) 2012-2013, Falko Bräutigam. All rights reserved.
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

import org.polymap.core.runtime.recordstore.IRecordState;

/**
 * Collection implementation on top of {@link IRecordState} of the feature of the
 * given {@link RProperty}. There is no in-memory cache of the values - all calls are
 * redirected to the underlying record state.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
class PropertyCollection<T>
        extends AbstractCollection<T>
        implements Collection<T> {

    private RProperty           prop;

    private int                 size = -1;
    
    
    protected PropertyCollection( RProperty prop ) {
        this.prop = prop;
    }
    
    
    protected T valueAt( int index ) {
        return prop.feature.state.get( prop.key.appendCollectionIndex( index ).toString() );
    }
    
    
    @Override
    public boolean add( T value ) {
        size(); // init size
        if (size == prop.getDescriptor().getMaxOccurs()) {
            throw new RuntimeException( "MaxOccurs limit reached for property: " + prop.getName() + " (" + prop.getDescriptor().getMaxOccurs() + ")" );
        }
        else {
            // increment size and put
            prop.feature.state.put( prop.key.appendCollectionIndex( size++ ).toString(), value );
            prop.feature.state.put( prop.key.appendCollectionLength().toString(), size );
            return true;
        }
    }


    @Override
    public int size() {
        if (size == -1) {
            Integer value = prop.feature.state.get( prop.key.appendCollectionLength().toString() );
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
                return valueAt( index++ );
            }

            @Override
            public void remove() {
                // XXX Auto-generated method stub
                throw new RuntimeException( "not yet implemented." );
            }
        };
    }
    
}
