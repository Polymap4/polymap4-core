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

import java.util.Comparator;
import java.util.EventObject;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public final class SourceEventFilter<E extends EventObject>
        implements EventFilter<E> {
    
    /** Compares event source via {@link Object#equals(Object)}. */
    public static final Comparator  Equal = new Comparator() {
        public int compare( Object o1, Object o2 ) {
            return o1.equals( o1 ) ? 0 : 1;
        }
    };
    
    /** Compares event source via == operator. */
    public static final Comparator  Identical = new Comparator() {
        public int compare( Object o1, Object o2 ) {
            return o1 == o2 ? 0 : 1;
        }
    };
    
    /** True if the event source is an instance of the same or derived class as the given object. */
    public static final Comparator  Assignable = new Comparator() {
        public int compare( Object o1, Object o2 ) {
            return o1.getClass().isAssignableFrom( o2.getClass() ) ? 0 : 1;
        }
    };
    
    // instance *******************************************
    
    private Object              source;
    
    private Comparator          compare;

    public SourceEventFilter( Object source ) {
        this( source, Equal );
    }
    
    public SourceEventFilter( Object source, Comparator compare ) {
        assert source != null;
        assert compare != null;
        this.source = source;
        this.compare = compare;
    }

    @Override
    public boolean apply( EventObject ev ) {
        return compare.compare( source, ev.getSource() ) == 0;
    }

}
