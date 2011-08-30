/* 
 * polymap.org
 * Copyright 2011, Polymap GmbH. All rights reserved.
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
package org.polymap.core.runtime;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class DefaultSessionContext
        extends SessionContext {

    private static Log log = LogFactory.getLog( DefaultSessionContext.class );
    
    private String                  sessionKey;
    
    private Map<String,Object>      attributes = new HashMap();
    
    private ReentrantReadWriteLock  attributesLock = new ReentrantReadWriteLock();
    
    private ListenerList<ISessionListener> listeners = new ListenerList();


    public DefaultSessionContext( String sessionKey ) {
        this.sessionKey = sessionKey;
    }

    
    protected void destroy() {
        log.debug( "destroy(): ..." );
        for (ISessionListener l : listeners.getListeners()) {
            try {
                l.beforeDestroy();
            }
            catch (Exception e) {
                log.warn( "", e );
            }
        }
        listeners.clear();
        attributes.clear();
    }


    public String getSessionKey() {
        return sessionKey;
    }

    
    public final <T> T sessionSingleton( final Class<T> type ) {
        try {
            return LockUtils.withReadLock( attributesLock, new Callable<T>() {
                public T call() throws Exception {
                    T result = (T)attributes.get( type.getName() );
                    if (result == null) {
                        LockUtils.upgrade( attributesLock );
                        
                        result = type.newInstance();
                        attributes.put( type.getName(), result );
                    }
                    return result;
                }
            });
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }
    }


    public boolean addSessionListener( ISessionListener l ) {
        return listeners.add( l );
    }


    public boolean removeSessionListener( ISessionListener l ) {
        return listeners.remove( l );
    }

}
