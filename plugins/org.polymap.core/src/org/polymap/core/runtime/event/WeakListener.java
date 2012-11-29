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

import java.lang.ref.WeakReference;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class WeakListener
       // extends DecoratingListener 
        implements EventListener {

    private static Log log = LogFactory.getLog( WeakListener.class );

    private WeakReference<EventListener>    delegateRef;
    
    private Object                          mapKey;


    /**
     * The {@link WeakListener} has to be the last in the chain. The given listener
     * has to be referenced by any application code (not just another chained
     * decorating listener).
     * 
     * @param delegate
     * @param mapKey
     */
    public WeakListener( EventListener delegate, Object mapKey ) {
        assert delegate != null;
        assert mapKey != null;
        this.delegateRef = new WeakReference( delegate );
        this.mapKey = mapKey;
    }


    @Override
    public void handleEvent( EventObject ev ) throws Exception {
        if (delegateRef != null) {
            EventListener delegate = delegateRef.get();
            if (delegate == null) {
                System.out.print( "+" );
                delegateRef = null;
                EventListener removed = EventManager.instance().removeKey( mapKey );
//                if (removed == null) {
//                    log.warn( "Unable to remove reclaimed listener for key: " + mapKey );
//                }
            }
            else {
                delegate.handleEvent( ev );
            }
        }
    }
    
}
