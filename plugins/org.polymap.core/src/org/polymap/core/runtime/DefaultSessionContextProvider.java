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

import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class DefaultSessionContextProvider
        implements ISessionContextProvider {

    private static Log log = LogFactory.getLog( DefaultSessionContextProvider.class );

    /**
     * This is static to allow {@link DefaultSessionContext} to execute a task.
     * However, several DefaultSessionContextProviders may exists. They share this
     * ThreadLocal which leads to problems if more than one DefaultSessionContext is
     * mapped to the same thread. This is valid but not allowed with this static
     * ThreadLocal.
     */
    static final ThreadLocal<SessionContext>    currentContext = new ThreadLocal();

    private ConcurrentHashMap<String,DefaultSessionContext> contexts = new ConcurrentHashMap( 32, 0.75f, 4 );
    
    //private ReentrantReadWriteLock              contextsLock = new ReentrantReadWriteLock();
    

    /**
     * Map the current thread to the context with the given sessionKey. If no context
     * exists yet, then a new one is created.
     * 
     * @param sessionKey
     * @param create Indicates that a new context should be created if no context
     *        exists for the given key. Otherwise an exception is thrown.
     */
    public void mapContext( final String sessionKey, final boolean create ) {
        SessionContext current = currentContext();
        if (current != null) {
            if (current.getSessionKey().equals( sessionKey )) {
                throw new IllegalStateException( "Un/mapping same session context more than once is not supported yet." );
            }
            else {
                throw new IllegalStateException( "Another context is mapped to this thread: " + current.getSessionKey() );                
            }
        }
        
        DefaultSessionContext context = contexts.get( sessionKey );
        log.debug( "mapContext(): sessionKey= " + sessionKey + ", current= " + context );
        if (context == null) {
            if (create) {
                context = newContext( sessionKey );
                log.info( "new session context: " + sessionKey );
                DefaultSessionContext old = contexts.putIfAbsent( sessionKey, context );
                // no lock -> check after put
                context = old != null ? old : context;
            }
            else {
                throw new IllegalStateException( "No such session context: " + sessionKey );
            }
        }
        currentContext.set( context );
    }


    /**
     * Release the current thread from the mapped context.
     * 
     * @throws IllegalStateException If the current thread is not mapped to a
     *         context.
     */
    public void unmapContext() {
        SessionContext context = currentContext.get();
        if (context == null) {
            throw new IllegalStateException( "No context bound to this thread." );
        }
        currentContext.set( null );
    }

    
//    public void inContext( String sessionKey, Runnable task ) {
//        try {
//            mapContext( sessionKey, false );
//            task.run();
//        }
//        finally {
//            unmapContext();
//        }
//    }

    
    protected DefaultSessionContext newContext( String sessionKey ) {
        log.debug( "newSessionContext(): " + sessionKey );
        return new DefaultSessionContext( sessionKey );
    }
    
    
    /**
     * Destroy the context for the given sessionKey.
     * 
     * @param sessionKey
     */
    public void destroyContext( final String sessionKey ) {
        log.debug( "destroyContext(): " + sessionKey );
        DefaultSessionContext context = contexts.get( sessionKey );
        if (context != null) {
            mapContext( context.getSessionKey(), false );

            context.destroy();
            unmapContext();

            // remove after destroy
            contexts.remove( sessionKey );
        }
        else {
            log.warn( "No context for sessionKey: " + sessionKey + "!" );
        }
    }
    
    
    public final SessionContext currentContext() {
        return currentContext.get();
    }
    
}
