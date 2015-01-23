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

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

import java.lang.reflect.Constructor;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class DefaultSessionContext
        extends SessionContext {

    private static Log log = LogFactory.getLog( DefaultSessionContext.class );
    
    private String                          sessionKey;
    
    private ConcurrentHashMap<String,Object> attributes = new ConcurrentHashMap( 32, 0.75f, 4 );
    
    //private ReentrantReadWriteLock  attributesLock = new ReentrantReadWriteLock();
    
    private ListenerList<ISessionListener>  listeners = new ListenerList();


    public DefaultSessionContext( String sessionKey ) {
        assert sessionKey != null;
        this.sessionKey = sessionKey;
    }

    
    @Override
    public int hashCode() {
        return sessionKey.hashCode();
    }


    @Override
    public boolean equals( Object obj ) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof DefaultSessionContext) {
            DefaultSessionContext other = (DefaultSessionContext)obj;
            return sessionKey.equals( other.sessionKey );
        }
        return false;
    }


    protected void destroy() {
        log.debug( "destroy(): ..." );
        checkDestroyed();
        for (ISessionListener l : listeners.getListeners()) {
            try {
                l.beforeDestroy();
            }
            catch (Exception e) {
                log.warn( "", e );
            }
        }
        listeners = null;
        attributes = null;
        // keep sessionKey for debugging/logging
        //sessionKey = null;
    }

    
    @Override
    public boolean isDestroyed() {
        return attributes == null;
    }


    private void checkDestroyed() {
        if (isDestroyed()) {
            throw new IllegalStateException( "Session context is destroyed: " + sessionKey );
        }
    }
    
    
    @Override
    public String getSessionKey() {
        checkDestroyed();
        return sessionKey;
    }

    
    @Override
    public final <T> T sessionSingleton( final Class<T> type ) {
        assert type != null;
        checkDestroyed();
        try {
            T result = (T)attributes.get( type.getName() );
            if (result == null) {

                // create an instance (without write lock)
                Constructor constructor = type.getDeclaredConstructor( new Class[] {} );
                if (constructor.isAccessible()) {
                    result = type.newInstance();
                }
                else {
                    constructor.setAccessible( true );
                    result = (T)constructor.newInstance( new Object[] {} );
                }

                Object old = attributes.putIfAbsent( type.getName(), result );
                // as there is no lock we have to check after put what object was added actually
                result = old != null ? (T)old : result;
            }
            return result;
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }
    }


    @Override
    public <T> T execute( final Callable<T> task ) throws Exception {
        checkDestroyed();
        SessionContext current = DefaultSessionContextProvider.currentContext.get();
        if (current != null) {
            if (current.getSessionKey().equals( sessionKey )) {
                return task.call();
               // throw new IllegalStateException( "Un/mapping same session context more than once is not supported yet." );
            }
            else {
                throw new IllegalStateException( "Another context is mapped to this thread: " + current.getSessionKey() );                
            }
        }

        try {
            DefaultSessionContextProvider.currentContext.set( this );
            return task.call();
        }
        finally {
            DefaultSessionContextProvider.currentContext.set( null );            
        }
    }

    
    @Override
    public void execute( final Runnable task ) {
        checkDestroyed();
        try {
            execute( new Callable() {
                public Object call() throws Exception {
                    task.run();
                    return null;
                }
            });
        }
        catch (RuntimeException e) {
            throw e;
        }
        catch (Exception e) {
            throw new RuntimeException( "Should never happen.", e );
        }
    }
    
    
    @Override
    public boolean addSessionListener( ISessionListener l ) {
        log.debug( "addListener(): " + l );
        checkDestroyed();
        return listeners.add( l );
    }


    @Override
    public boolean removeSessionListener( ISessionListener l ) {
        checkDestroyed();
        return listeners.remove( l );
    }


    @Override
    public <T> T getAttribute( String key ) {
        checkDestroyed();
        return (T)attributes.get( key );
    }


    @Override
    public void setAttribute( String key, Object value ) {
        checkDestroyed();
        attributes.put( key, value );
    }


    @Override
    public String toString() {
        return "DefaultSessionContext[sessionKey=" + sessionKey + ", attributes=" + attributes + "]";
    }
    
}
