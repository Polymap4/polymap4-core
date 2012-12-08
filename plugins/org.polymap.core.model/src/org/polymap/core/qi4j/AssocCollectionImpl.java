/* 
 * polymap.org
 * Copyright 2009, Polymap GmbH, and individual contributors as indicated
 * by the @authors tag.
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
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 * $Id$
 */

package org.polymap.core.qi4j;

import java.util.AbstractCollection;
import java.util.Iterator;
import org.qi4j.api.entity.association.ManyAssociation;

import org.polymap.core.model.AssocCollection;

/**
 * 
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class AssocCollectionImpl<E>
        extends AbstractCollection<E>
        implements AssocCollection<E> {

    private ManyAssociation[]     underlying;
    
    
    public AssocCollectionImpl( ManyAssociation... underlying ) {
        this.underlying = underlying;
    }
    
    public boolean isEmpty() {
        return size() == 0;
    }

    public Iterator<E> iterator() {
        
//        return Iterables.concat( underlying ).iterator();
        
//        if (isEmpty()) {
//            return ListUtils.EMPTY_LIST.iterator();
//        }
//        else {
            return new IteratorImpl();
//        }
    }

    public int size() {
        int result = 0;
        for (ManyAssociation assoc : underlying) {
            result += assoc.count();
        }
        return result;
    }
    
    
    /*
     * 
     */
    class IteratorImpl<T>
            implements Iterator<T> {

        private int             index = 0;
        
        private Iterator<T>     it;
     
        
        public boolean hasNext() {
            if (it != null && it.hasNext()) {
                return true;
            }
            
            it = null;
            while (it == null && index < underlying.length) {
                Iterator next = underlying[ index++ ].iterator();
                if (next.hasNext()) {
                    it = next;
                }
            }
            return it != null && it.hasNext();
        }


        public T next() {
            return it.next();
        }


        public void remove() {
            throw new RuntimeException( "not yet implemented." );
        }
        
    }
    
}
