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
import java.util.concurrent.ExecutorService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Asynchronously execute methods via an {@link ExecutorService}. 
 *
 * @see Async
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ExecutorAsync<T>
        extends Async<T> {

    private static Log log = LogFactory.getLog( ExecutorAsync.class );
    
    private static ExecutorService      defaultExecutor;
    
    
    public static <T> Async<T> on( ExecutorService executor, T delegate, Callback callback ) {
        assert delegate != null;
        return new ExecutorAsync( executor, delegate, callback );
    }
    
    
    public static <T> Async<T> on( T delegate, Callback callback ) {
        assert delegate != null;
        return new ExecutorAsync( null, delegate, callback );
    }


    // instance *******************************************
    
    private ExecutorService     executor;
    
    protected ExecutorAsync( ExecutorService executor, T delegate, Callback callback ) {
        super( delegate, callback );
        this.executor = executor != null ? executor : defaultExecutor;
    }

    
    @Override
    protected void execute( String title, final Callable task ) {
        executor.execute( new Runnable() {
            public void run() {
                try {
                    task.call();
                }
                catch (Exception e) {
                    log.error( "", e );
                }
            }
        });
    }
    
}
