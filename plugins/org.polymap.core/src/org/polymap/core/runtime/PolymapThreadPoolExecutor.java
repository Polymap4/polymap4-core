/* 
 * polymap.org
 * Copyright 2012, Falko Bräutigam. All rights reserved.
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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class PolymapThreadPoolExecutor
        extends ThreadPoolExecutor
        implements ThreadFactory {

    private static Log log = LogFactory.getLog( PolymapThreadPoolExecutor.class );
    
    
    public static PolymapThreadPoolExecutor newInstance() {
        // policy:
        //   - first: spawn up to proc*8 threads (stopped when idle for 60s)
        //   - then: queue 3times more jobs (for faster feeding workers
        //   - then: block until queue can take another job
        int procs = Runtime.getRuntime().availableProcessors();
        final int nThreads = procs * 8;
        final int maxThreads = nThreads * 5;
        
        // Proper queue bound semantics but just half the task througput of
        // LinkedBlockingQueue in PerfTest
       // BlockingQueue queue = new ArrayBlockingQueue( nThreads * 5 );
        
        // Fastest. But unfortunatelle does not limit the number of queued
        // task, which might result in OOM or similar problem.
        final LinkedBlockingQueue queue = new LinkedBlockingQueue( /*nThreads * 3*/ );
        
        // Slowest queue. Does not feed the workers well if pool size exeeds.
       // BlockingQueue queue = new SynchronousQueue();
        
        final PolymapThreadPoolExecutor result = new PolymapThreadPoolExecutor( 
                nThreads, maxThreads, 3*60, queue );
        result.allowCoreThreadTimeOut( true );

        // async queue bounds checker for unbound LinkedQueue
        Thread boundsChecker = new Thread( "ThreadPoolBoundsChecker" ) {
            public void run() {
                int bound = result.getCorePoolSize() * 3;
                while (queue != null) {
                    try {
                        Thread.sleep( 1000 );
                        if (queue.size() > bound
                                && result.getCorePoolSize() < maxThreads) {
                            
                            int n = result.getCorePoolSize() + nThreads;
                            log.info( "Queue remaining capacity == 0 -> increasing core pool size: " + n );
                            result.setCorePoolSize( n );
                        }
                    }
                    catch (Throwable e) {
                        log.warn( "", e );
                    }
                }
            }
        };
        boundsChecker.setPriority( Thread.MIN_PRIORITY );
        boundsChecker.start();
        
        return result;
    }
    
    
    // instance *******************************************
    
    private final AtomicInteger     threadNumber = new AtomicInteger( 1 );
    
    private static AtomicBoolean    queueFull = new AtomicBoolean();
    
    
    public PolymapThreadPoolExecutor( int minPoolSize, int maxPoolSize, int keepAliveSecs, BlockingQueue queue ) {
        super( minPoolSize, maxPoolSize, keepAliveSecs, TimeUnit.SECONDS, queue );
        
        // thread factory
        setThreadFactory( this );

        // rejected? -> wait and try again
        setRejectedExecutionHandler( new RejectedExecutionHandler() {
            public void rejectedExecution( Runnable r, ThreadPoolExecutor executor ) {
                //log.warn( "Unable to queue task: " + r );
                // wait (a long time) and try again (until StackOverflow)
                synchronized (queueFull) {
                    queueFull.set( true );
                    try {
                        queueFull.wait( 1000 );
                    }
                    catch (InterruptedException e) {
                    }
                }
                executor.execute( r );
            }
        });

    }


    protected void afterExecute( Runnable r, Throwable t ) {
        super.afterExecute( r, t );
        if (queueFull.get()) {
            synchronized (queueFull) {
                queueFull.set( false );
                queueFull.notifyAll();
            }
        }
    }

    
    public Thread newThread( Runnable r ) {
        String prefix = "polymap-worker-";
        Thread t = new Thread( r, prefix + threadNumber.getAndIncrement() );
        t.setDaemon( false );
        t.setPriority( Thread.NORM_PRIORITY - 1 );
        return t;
    }
    
}
