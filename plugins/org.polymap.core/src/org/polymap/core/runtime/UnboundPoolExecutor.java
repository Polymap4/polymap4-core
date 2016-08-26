/* 
 * polymap.org
 * Copyright 2013, Falko Bräutigam. All rights reserved.
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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A thread pool executor with (nearly) unbound pool size and no queue.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class UnboundPoolExecutor {

    private static Log log = LogFactory.getLog( UnboundPoolExecutor.class );

    public static int           DEFAULT_THREAD_PRIORITY = Thread.NORM_PRIORITY - 1;
    
    public static int           MAX_THREADS_PER_PROC = 16;
    
    
    public static ExecutorService newInstance() {
        // thread factory
        ThreadFactory threadFactory = new ThreadFactory() {
            volatile int threadNumber = 0;
            
            @Override
            public Thread newThread( Runnable r ) {
                Thread t = new Thread( r, "polymap-worker-" + threadNumber++ );
                t.setDaemon( false );
                t.setPriority( DEFAULT_THREAD_PRIORITY );
                return t;
            }
        };

        // thread pool
        final int procs = Runtime.getRuntime().availableProcessors();
        final int maxThreads = procs * MAX_THREADS_PER_PROC;        
        ThreadPoolExecutor executor = new ThreadPoolExecutor( procs, maxThreads,
                // SynchronousQueue is what we want to have here (is LinkedBlockingQueue faster?)
                180L, TimeUnit.SECONDS, new SynchronousQueue(), threadFactory );
        
        executor.allowCoreThreadTimeOut( false );
        
        // rejected? -> wait and try again
        executor.setRejectedExecutionHandler( new RejectedExecutionHandlers.Blocking() );
        
        return executor;
    }
    
}
