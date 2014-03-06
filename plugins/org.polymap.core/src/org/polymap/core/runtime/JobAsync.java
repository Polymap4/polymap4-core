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

import java.util.concurrent.Callable;

import java.lang.reflect.InvocationTargetException;
import org.apache.commons.lang.StringUtils;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Asynchronously execute methods in an {@link UIJob}. 
 *
 * @see Async
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class JobAsync<T>
        extends Async<T> {

    public static <T> JobAsync<T> on( T delegate, Callback callback ) {
        assert delegate != null;
        return new JobAsync( delegate, callback );
    }
    
    
    // instance *******************************************
    
    protected JobAsync( T delegate, Callback callback ) {
        super( delegate, callback );
    }

    
    protected void doInvoke() throws Throwable {
        String title = StringUtils.capitalize( method.getName() );

        new UIJob( title ) {
            protected void runWithException( IProgressMonitor monitor ) throws Exception {
                // substitute IProgressMonitor in args
                Class<?>[] argTypes = method.getParameterTypes();
                for (int i=0; i<argTypes.length; i++) {
                    if (IProgressMonitor.class.isAssignableFrom( argTypes[i] ) && args[i] == null) {
                        args[i] = monitor;
                    }
                }
                // invoke delegate and callback
                try {
                    Object result = method.invoke( delegate, args );
                    callback.handle( result );
                } 
                catch (InvocationTargetException e) {
                    throw (Exception)e.getTargetException();
                }
            }
        }.schedule();
    }

    
    @Override
    protected void execute( String title, final Callable task ) {
        throw new RuntimeException( "Never called." );
    }
    
}
