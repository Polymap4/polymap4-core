/* 
 * polymap.org
 * Copyright 2011, Falko Bräutigam, and other contributors as
 * indicated by the @authors tag. All rights reserved.
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

import java.util.HashSet;
import java.util.Set;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A weak listener reference can be used whereever the listener interface is directly
 * implemented by the caller or if the listener is implemented by an inner class and the
 * instance is a member of the caller.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class WeakListener
        implements InvocationHandler {

    private static Log log = LogFactory.getLog( WeakListener.class );


    /**
     * Creates a proxy for the given listener. The proxy forwards all method calls to the
     * delegate. But it holds just a {@link WeakReference} to the delegate, so that the
     * reference in the {@link ListenerList} does not prevent the listener from being
     * reclaimed by the GC.
     * 
     * @param delegate
     * @return A proxy that implements all interfaces from the delegate.
     */
    public static <T> T forListener( T delegate ) {
        assert delegate != null;
        log.info( "forListener(): " + delegate.getClass().getName() );
        
        // find all interfaces
        Set<Class> interfaces = new HashSet();
        Class<? extends Object> cl = delegate.getClass();
        while (cl != Object.class) {
            Class[] ifs = cl.getInterfaces();
            for (int i=0; i<ifs.length; i++) {
                //log.info( "    " + ifs[i] );
                interfaces.add( ifs[i] );
            }
            cl = cl.getSuperclass();
        }
        interfaces.add( ListenerReference.class );
        
        // build the proxy
        Object proxy = Proxy.newProxyInstance( 
                delegate.getClass().getClassLoader(), 
                interfaces.toArray( new Class[ interfaces.size() ]), 
                new WeakListener( delegate ) );
        return (T)proxy;
    }

    
    // instance *******************************************
    
    private WeakReference       ref;
    
    
    protected WeakListener( Object delegate ) {
        ref = new WeakReference( delegate );
    }

    public Object invoke( Object proxy, Method method, Object[] args )
    throws Throwable {
        Object delegate = ref.get();
        
        if (method.getName().equals( "getDelegate" )) {
            return delegate;
        }
        else {
            if (delegate == null) {
                log.warn( "" );
                return null;
            }
            Method delegateMethod = delegate.getClass().getMethod( method.getName(), method.getParameterTypes() );
            return delegateMethod.invoke( delegate, args );
        }
    }
    
}
