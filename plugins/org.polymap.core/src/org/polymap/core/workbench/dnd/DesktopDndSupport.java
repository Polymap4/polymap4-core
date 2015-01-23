/* 
 * polymap.org
 * Copyright 2012, Polymap GmbH. All rights reserved.
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
package org.polymap.core.workbench.dnd;

import java.util.List;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import org.polymap.core.runtime.ListenerList;
import org.polymap.core.runtime.SessionSingleton;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class DesktopDndSupport
        extends SessionSingleton {

    public static DesktopDndSupport instance() {
        return instance( DesktopDndSupport.class );
    }

    // instance *******************************************

    private static Log log = LogFactory.getLog( DesktopDndSupport.class );
    
    private ListenerList<DesktopDropListener> listeners = new ListenerList(); 
    

    public boolean addDropListener( DesktopDropListener l ) {
//        SessionContext context = SessionContext.current();
//        assert context != null;
//        
//        ListenerList<DesktopDropListener> listeners = sessionListeners.get( context );
//        if (listeners == null) {
//            listeners = new ListenerList();
//            sessionListeners.put( context, listeners );
//        }
        return listeners.add( l );
    }

    
    public boolean removeDropListener( DesktopDropListener l ) {
//        SessionContext context = SessionContext.current();
//        assert context != null;
//        
//        ListenerList<DesktopDropListener> listeners = sessionListeners.get( context );
//        if (listeners == null) {
//            return false;
//        }
        return listeners.remove( l );
    }

    
    /**
     * Fires the given events to the registered listeners. Must be called
     * from within UI thread.
     *  
     * @param events
     */
    protected void fireEvents( List<DesktopDropEvent> events ) {
        for (DesktopDropListener l : listeners) {
            l.onDrop( events );                    
        }
    }
    
}
