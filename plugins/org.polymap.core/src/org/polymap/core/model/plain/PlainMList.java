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

package org.polymap.core.model.plain;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Iterator;

import org.polymap.core.model.MList;


/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
class PlainMList<T>
        extends AbstractCollection<T>
        implements MList<T> {

    private ArrayList<T>            l;
    
    
    PlainMList( ArrayList<T> l ) {
        super();
        this.l = l;
    }

    public Iterator<T> iterator() {
        return  l.iterator();
    }

    public int size() {
        return l.size();
    }

    public boolean isEmpty() {
        return l.isEmpty();
    }

}
