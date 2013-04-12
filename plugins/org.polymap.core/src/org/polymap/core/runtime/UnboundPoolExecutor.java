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

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * A thread pool executor with (nearly) unbound pool size.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class UnboundPoolExecutor {

    private static Log log = LogFactory.getLog( UnboundPoolExecutor.class );

    public static int           DEFAULT_THREAD_PRIORITY = Thread.NORM_PRIORITY - 1;
    
    public static int           MAX_THREADS_PER_PROC = 32;
    
    
    public static ExecutorService newInstance() {
        final int procs = Runtime.getRuntime().availableProcessors();
        final int maxThreads = procs * MAX_THREADS_PER_PROC;
        
        // thread factory
        ThreadFactory threadFactory = new ThreadFactory() {
            volatile int threadNumber = 0;
            public Thread newThread( Runnable r ) {
                String prefix = "polymap-";
                Thread t = new Thread( r, prefix + threadNumber++ );
                t.setDaemon( false );
                t.setPriority( DEFAULT_THREAD_PRIORITY );
                return t;
            }
        };

        // thread pool
        ThreadPoolExecutor executor = new ThreadPoolExecutor( procs, maxThreads,
                180L, TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>(),
                threadFactory );
        
        // rejected? -> wait and try again
        executor.setRejectedExecutionHandler( new RejectedExecutionHandler() {
            Random rand = new Random();
            public void rejectedExecution( Runnable r, ThreadPoolExecutor _executor ) {
                do {
                    try {
                        Thread.sleep( rand.nextInt( 1000 ) + 100 );
                    } 
                    catch (InterruptedException e) {}
                }
                while (_executor.getActiveCount() >= maxThreads);

                _executor.execute( r );
            }
        });
        
        //executor.allowCoreThreadTimeOut( true );        
        return executor;
    }
    
}
