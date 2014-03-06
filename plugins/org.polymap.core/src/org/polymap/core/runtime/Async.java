/* 
 * polymap.org
 * Copyright (C) 2014, Falko Bräutigam. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.core.runtime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.apache.commons.lang.StringUtils;

/**
 * Helps to call methods of existing objects asynchronously. There are several
 * implementations which provide different ways to execute a task asynchronously.
 * <p/>
 * <b>Usage:</b>
 * 
 * <pre>
 * XXX_Async.on( target ).callback( new Callback&lt;String&gt;() {
 *     public void handle( String result ) {
 *         System.out.println( result );
 *     }
 * }).call().toString();
 * </pre>
 * 
 * For the callback implemented as inner class the call to the target method is
 * written *after* the callback code. This makes the code somewhat cumbersome to
 * read. To get around this limitation the API allows to call target method first and
 * set the callback afterwards.
 * 
 * <pre>
 * Async&lt;Object&gt; async = XXXAsync.on( target );
 * async.call().toString();
 * async.callback( new Callback&lt;String&gt;() {
 *     public void handle( String result ) {
 *         System.out.println( result );
 *     }
 * });
 * </pre>
 * 
 * The API could be a bit simpler without this. However, it seems to be more
 * important to support readable than short code. In this case the target method
 * is called after the {@link #callback(Callback)} has been set.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class Async<T>
        implements InvocationHandler {

    protected T                 delegate;
    
    protected Callback          callback;
    
    protected T                 proxy;
    
    protected Method            method;
    
    protected Object[]          args; 

    
    /**
     * 
     * @param delegate
     * @param callback May be null.
     */
    protected Async( T delegate, Callback callback ) {
        this.delegate = delegate;
        this.callback = callback;
    }

    
    /**
     * Specifies the {@link Callback} to handle the results of the asynchronous
     * method call.
     * 
     * @param _callback The callback to send the result to.
     * @return this
     * @throws Throwable
     */
    public Async<T> callback( Callback _callback ) throws Throwable {
        assert callback == null : "There is a callback set already: " + this.callback;
        this.callback = _callback;
        if (proxy != null) {
            doInvoke();
        }
        return this;
    }


    /**
     * Returns a proxy for the delegate to call the target method on.
     */
    public T call() {
        if (proxy == null) {
            List<Class> interfaces = new ArrayList();
            Class cl = delegate.getClass();
            while (cl != null) {
                interfaces.addAll( Arrays.asList( cl.getInterfaces() ) );
                cl = cl.getSuperclass();
            }

            proxy = (T)Proxy.newProxyInstance( delegate.getClass().getClassLoader(),
                    interfaces.toArray( new Class[interfaces.size()] ),
                    this );
        }
        return proxy;
    }
    
    
    @Override
    public Object invoke( final Object _proxy, final Method _method, final Object[] _args ) throws Throwable {
        assert _proxy == proxy;
        this.method = _method;
        this.args = _args;
        
        if (callback != null) {
            doInvoke();
        }
        return null;
    }
    
    
    protected void doInvoke() throws Throwable {
        String title = StringUtils.capitalize( method.getName() );
        execute( title, new Callable() {
            public Object call() throws Exception {
                try {
                    Object result = method.invoke( delegate, args );
                    callback.handle( result );
                    return null;
                } 
                catch (RuntimeException e) {
                    throw e;
                }
            }
        });
    }
    
    
    /**
     * 
     *
     * @param title The title of the task which is displayed to the user 
     * @param task The task to execute
     * @throws Exception
     */
    protected abstract void execute( String title, Callable task );
    
}
