/* 
 * polymap.org
 * Copyright (C) 2011-2013, Falko Bräutigam. All rigths reserved.
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
package org.polymap.core.runtime.mp;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class Producer<T>
        extends AbstractCollection<T>
        implements Collection<T> {

    
    public abstract T produce();
    
    public abstract int size();
    
    
    public Iterator<T> iterator() {
        return new ProducerIterator();
    }

    
    /*
     * 
     */
    class ProducerIterator
            implements Iterator<T> {

        private volatile int    count = 0;
        
        private int             size = size();
        

        public final boolean hasNext() {
            return count < size;
        }


        public final T next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            ++count;
            return produce();
        }


        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
