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
package org.polymap.core.runtime.session;

import java.util.concurrent.Callable;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Provides a unique API to the context of the session of the current thread. The
 * different front-end systems (RWT, GeoServer/OGC, WebDAV, etc.) can register a
 * specific {@link ISessionContextProvider}.
 * <p>
 * SessionContext also provides the API and SPI of the application context. This
 * interface is independent from HTTP/Servlet session context API.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class SessionContext {

    private static Log log = LogFactory.getLog( SessionContext.class );

    private static ISessionContextProvider[]     providers = {};
    

    public static void addProvider( ISessionContextProvider provider ) {
        providers = ArrayUtils.add( providers, provider );
    }
    
    
    public static void removeProvider( ISessionContextProvider provider ) {
        providers = ArrayUtils.removeElement( providers, provider );
    }
    
    
    public static final SessionContext current() {
        for (ISessionContextProvider provider : providers) {
            SessionContext context = provider.currentContext();
            if (context != null) {
                return context;
            }
        }
        return null;
    }
    
    
    // API ************************************************
    
    public abstract String getSessionKey();

    public abstract <T> T sessionSingleton( Class<T> type );
    
    /**
     * Executes the given Runnable inside this session context.
     * 
     * @param task The task to be executed. 
     */
    public abstract void execute( Runnable task );

    /**
     * Executes the given Callable inside this session context.
     * 
     * @param task The task to be executed. 
     */
    public abstract <T> T execute( final Callable<T> task ) throws Exception;
    
    public abstract boolean addSessionListener( ISessionListener l );
    
    public abstract boolean removeSessionListener( ISessionListener l );

    /**
     * Binds an object to this session, using the name specified. If an object of the
     * same name is already bound to the session, the object is replaced.
     * 
     * @param string
     * @param value
     */
    public abstract void setAttribute( String key, Object value );
    
    public abstract <T> T getAttribute( String key );

    public abstract boolean isDestroyed();
    
}
