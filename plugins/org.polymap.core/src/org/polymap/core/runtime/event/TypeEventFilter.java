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
package org.polymap.core.runtime.event;

import java.util.EventObject;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public final class TypeEventFilter<E extends EventObject>
        implements EventFilter<E> {
    
    private Class<E>            type;

    
    public TypeEventFilter( Class<E> type ) {
        assert type != null;
        this.type = type;
    }

    @Override
    public boolean apply( EventObject ev ) {
        return type.isAssignableFrom( ev.getClass() );
    }

    @Override
    public String toString() {
        return "TypeEventFilter [type=" + type.getSimpleName() + "]";
    }
    
}
