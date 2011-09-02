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

import java.lang.reflect.Constructor;

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
        listeners = null;
        attributes.clear();
        attributes = null;
        sessionKey = null;
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
                        
                        Constructor constructor = type.getDeclaredConstructor( new Class[] {} );
                        if (constructor.isAccessible()) {
                            result = type.newInstance();
                        }
                        else {
                            constructor.setAccessible( true );
                            result = (T)constructor.newInstance( new Object[] {} );
                        }

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


    public <T> T execute( final Callable<T> task ) 
    throws Exception {
        SessionContext current = DefaultSessionContextProvider.currentContext.get();
        if (current != null) {
            if (current.getSessionKey().equals( sessionKey )) {
                throw new IllegalStateException( "Un/mapping same session context more than once is not supported yet." );
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

    
    public void execute( final Runnable task ) {
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
    
    
    public boolean addSessionListener( ISessionListener l ) {
        log.debug( "addListener(): " + l );
        return listeners.add( l );
    }


    public boolean removeSessionListener( ISessionListener l ) {
        return listeners.remove( l );
    }


    public Object getAttribute( String key ) {
        try {
            attributesLock.readLock().lock();
            return attributes.get( key );
        }
        finally {
            attributesLock.readLock().unlock();            
        }
    }


    public void setAttribute( String key, Object value ) {
        try {
            attributesLock.writeLock().lock();
            attributes.put( key, value );
        }
        finally {
            attributesLock.writeLock().unlock();            
        }
    }

}
