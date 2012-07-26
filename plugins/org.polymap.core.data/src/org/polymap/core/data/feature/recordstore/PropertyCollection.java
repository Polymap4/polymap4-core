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
package org.polymap.core.data.feature.recordstore;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
abstract class PropertyCollection<T>
        extends AbstractCollection<T>
        implements Collection<T> {

    private RProperty           prop;

    private int                 size = -1;
    
    private Object[]            values;
    
    
    protected PropertyCollection( RProperty prop ) {
        this.prop = prop;
    }
    
    
    protected abstract Object valueAt( int index );
    
    
    public Iterator<T> iterator() {
        // FIXME implement
        throw new RuntimeException( "not yet implemented" );
    }

    
    public int size() {
        if (size == -1) {
            size = prop.feature.state.get( prop.key.appendCollectionLength().toString() );
        }
        return size;
    }

}
