/* 
 * polymap.org
 * Copyright (C) 2016, the @authors. All rights reserved.
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
package org.geotools.renderer.lite;

import java.util.AbstractQueue;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.geotools.renderer.lite.StreamingRenderer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.data.feature.DataSourceProcessor;

/**
 * {@link StreamingRenderer} with async painter thread disabled.
 * <p/>
 * Thread pooling of the painter thread and queuing of the paint requests is
 * non-functional or bad implemented in {@link StreamingRenderer}.
 * <ul>
 * <li>default thread pool is non-functional</li>
 * <li>request blocking queue is not chunked
 * <li>default request blocking queue is way to big</li>
 * </ul>
 * We don't need another thread in P4 anyway as we have async
 * {@link DataSourceProcessor} sub pipeline and tiling.
 *
 * @author Falko Bräutigam
 */
public class NoThreadStreamingRenderer
        extends StreamingRenderer {

    private static final Log log = LogFactory.getLog( NoThreadStreamingRenderer.class );

    
    public NoThreadStreamingRenderer() {
        setThreadPool( new NoThreadExecutorService() );
    }


    @Override
    protected BlockingQueue<RenderingRequest> getRequestsQueue() {
        return new NoThreadBlockingQueue();
    }

    
    /**
     * 
     */
    protected class NoThreadBlockingQueue
            extends AbstractQueue<RenderingRequest>
            implements BlockingQueue<RenderingRequest> {
        
        private int requestCount = 0;
        
        @Override
        public void put( RenderingRequest r ) throws InterruptedException {
            r.execute();
//            if (++requestCount % 100 == 0) {
//                log.info( "   request count: " + requestCount );
//            }
        }
        @Override
        public boolean offer( RenderingRequest e ) {
            throw new RuntimeException( "not yet implemented." );
        }
        @Override
        public RenderingRequest poll() {
            throw new RuntimeException( "not yet implemented." );
        }
        @Override
        public RenderingRequest peek() {
            throw new RuntimeException( "not yet implemented." );
        }
        @Override
        public boolean offer( RenderingRequest e, long timeout, TimeUnit unit ) throws InterruptedException {
            throw new RuntimeException( "not yet implemented." );
        }
        @Override
        public RenderingRequest take() throws InterruptedException {
            throw new RuntimeException( "not yet implemented." );
        }
        @Override
        public RenderingRequest poll( long timeout, TimeUnit unit ) throws InterruptedException {
            throw new RuntimeException( "not yet implemented." );
        }
        @Override
        public int remainingCapacity() {
            throw new RuntimeException( "not yet implemented." );
        }
        @Override
        public int drainTo( Collection<? super RenderingRequest> c ) {
            throw new RuntimeException( "not yet implemented." );
        }
        @Override
        public int drainTo( Collection<? super RenderingRequest> c, int maxElements ) {
            throw new RuntimeException( "not yet implemented." );
        }
        @Override
        public Iterator<RenderingRequest> iterator() {
            throw new RuntimeException( "not yet implemented." );
        }
        @Override
        public int size() {
            throw new RuntimeException( "not yet implemented." );
        }
    }

    
    /**
     * 
     */
    protected class NoThreadExecutorService
            implements ExecutorService {
        
        private Runnable        painter;

        @Override
        public void execute( Runnable command ) {
            assert painter == null;
            painter = command;
        }
        @Override
        public void shutdown() {
        }
        @Override
        public List<Runnable> shutdownNow() {
            return Collections.EMPTY_LIST;
        }
        @Override
        public boolean isShutdown() {
            throw new RuntimeException( "not yet implemented." );
        }
        @Override
        public boolean isTerminated() {
            throw new RuntimeException( "not yet implemented." );
        }
        @Override
        public boolean awaitTermination( long timeout, TimeUnit unit ) throws InterruptedException {
            return true;
        }
        @Override
        public <T> Future<T> submit( Runnable task, T result ) {
            assert painter == null;
            painter = task;
            return new Future<T>() {
                @Override
                public boolean cancel( boolean mayInterruptIfRunning ) {
                    return true;
                }
                @Override
                public boolean isCancelled() {
                    // XXX Auto-generated method stub
                    throw new RuntimeException( "not yet implemented." );
                }
                @Override
                public boolean isDone() {
                    return true;
                }
                @Override
                public T get() throws InterruptedException, ExecutionException {
                    return null;
                }
                @Override
                public T get( long timeout, TimeUnit unit )
                        throws InterruptedException, ExecutionException, TimeoutException {
                    return null;
                }
            };
        }
        @Override
        public <T> Future<T> submit( Callable<T> task ) {
            throw new RuntimeException( "not yet implemented." );
        }
        @Override
        public Future<?> submit( Runnable task ) {
            return submit( task, null );
        }

        @Override
        public <T> List<Future<T>> invokeAll( Collection<? extends Callable<T>> tasks ) throws InterruptedException {
            throw new RuntimeException( "not yet implemented." );
        }
        @Override
        public <T> List<Future<T>> invokeAll( Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit )
                throws InterruptedException {
            throw new RuntimeException( "not yet implemented." );
        }
        @Override
        public <T> T invokeAny( Collection<? extends Callable<T>> tasks )
                throws InterruptedException, ExecutionException {
            throw new RuntimeException( "not yet implemented." );
        }
        @Override
        public <T> T invokeAny( Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit )
                throws InterruptedException, ExecutionException, TimeoutException {
            throw new RuntimeException( "not yet implemented." );
        }
    };

}
