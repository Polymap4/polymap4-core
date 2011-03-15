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

package org.polymap.core.model;

import java.util.EventObject;
import java.util.LinkedList;
import java.util.List;

import java.beans.PropertyChangeEvent;

/**
 * Collection of {@link PropertyChangeEvent}s collected while execution of an
 * operation.
 * 
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class ModelChangeEvent
        extends EventObject {

    private List<PropertyChangeEvent>       events = new LinkedList();
    
    private List<String>                    created = new LinkedList();
    
    private List<String>                    removed = new LinkedList();
    
    
    public ModelChangeEvent( Object source ) {
        super( source );
    }

    public void addEvent( PropertyChangeEvent ev ) {
        events.add( ev );
    }
    
    public Iterable<PropertyChangeEvent> events() {
        return events;
    }

    public void addRemoved( String id ) {
        removed.add( id );
    }
    
    public Iterable<String> removed() {
        return removed;
    }
    
}
