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
import java.util.concurrent.Callable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.runtime.SessionContext;

/**
 * Preserves the {@link SessionContext} of a subscribed listener
 * and restores this context for every event dispatch
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
class SessioningListener
        extends DecoratingListener {

    private static Log log = LogFactory.getLog( SessioningListener.class );
    
    private SessionContext                  session;
    
    private Object                          mapKey;

    /**
     * 
     * @param delegate
     * @param mapKey
     */
    public SessioningListener( EventListener delegate, Object mapKey ) {
        super( delegate );
        assert mapKey != null;
        this.session = SessionContext.current();
        this.mapKey = mapKey;
        assert session != null;
    }

    @Override
    public void handleEvent( final EventObject ev ) throws Exception {
        if (session != null) {
            if (!session.isDestroyed()) {
                session.execute( new Callable() {
                    public Object call() throws Exception {
                        delegate.handleEvent( ev );
                        return null;
                    }
                });
            }
            else {
                log.debug( "Removing event handler for destroyed session: " + session.getClass().getSimpleName() );
                EventManager.instance().removeKey( mapKey );
                session = null;
                delegate = null;
            }
        }
    }

}
