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

import org.polymap.core.model.MId;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
class PlainMId
        implements MId {

    private long            id;

    PlainMId( long id ) {
        super();
        this.id = id;
    }

    
    public PlainMId( String serialized ) {
        this.id = Long.parseLong( serialized );
    }


    public String toString() {
        return String.valueOf( id );
    }


    public int compareTo( Object o ) {
        long rhs_id = ((PlainMId)o).id;
        if (id < rhs_id) {
            return -1;
        }
        else if (id == rhs_id) {
            return 0;
        }
        else {
            return 1;
        }
    }


    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int)(id ^ (id >>> 32));
        return result;
    }


    public boolean equals( Object obj ) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof PlainMId)) {
            return false;
        }
        PlainMId other = (PlainMId)obj;
        if (id != other.id) {
            return false;
        }
        return true;
    }
    
}
