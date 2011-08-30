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
import java.util.concurrent.locks.ReentrantReadWriteLock;

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

    private Map<String,DefaultSessionContext>   contexts = new HashMap();
    
    private ReentrantReadWriteLock              contextsLock = new ReentrantReadWriteLock();
    
    private ThreadLocal<SessionContext>         currentContext = new ThreadLocal();


    /**
     * Map the current thread to the context with the given sessionKey. If no context
     * exists yet, then a new one is created.
     * 
     * @param sessionKey
     */
    public void mapContext( final String sessionKey ) {
        LockUtils.withReadLock( contextsLock, new Runnable() {
            public void run() {
                DefaultSessionContext context = contexts.get( sessionKey );
                log.debug( "mapContext(): sessionKey= " + sessionKey + ", current= " + context );
                if (context == null) {
                    LockUtils.upgrade( contextsLock );
                    
                    context = newContext( sessionKey );
                    contexts.put( sessionKey, context );
                }
                currentContext.set( context );
            }
        });    
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


    protected DefaultSessionContext newContext( String sessionKey ) {
        return new DefaultSessionContext( sessionKey );
    }
    
    
    /**
     * Destroy the context for the given sessionKey.
     * 
     * @param sessionKey
     */
    public void destroyContext( final String sessionKey ) {
        LockUtils.withReadLock( contextsLock, new Runnable() {
            public void run() {
                LockUtils.upgrade( contextsLock );

                DefaultSessionContext context = contexts.remove( sessionKey );
                if (context != null) {
                    context.destroy();
                }
                else {
                    log.warn( "No context for sessionKey: " + sessionKey + "!" );
                }
                currentContext.set( context );
            }
        });    
            
    }
    
    
    public final SessionContext currentContext() {
        return currentContext.get();
    }
    
}
