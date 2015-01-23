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
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

/**
 * Asynchronously execute methods in a newly created {@link Thread}.
 *  
 * @see Async
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ThreadAsync<T>
        extends Async<T> {

    private static Log log = LogFactory.getLog( ThreadAsync.class );
    

    public static <T> Async<T> on( T delegate, Callback callback ) {
        assert delegate != null;
        return new ThreadAsync( delegate, callback );
    }
    
    
    // instance *******************************************

    protected ThreadAsync( T delegate, Callback callback ) {
        super( delegate, callback );
    }

    
    @Override
    protected Future<T> execute( String title, final Callable task ) {
        FutureTask<T> futureTask = new FutureTask<T>( task );
        new Thread( futureTask, title ).start();
        return futureTask;
    }
    
}
