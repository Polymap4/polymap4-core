/* 
 * polymap.org
 * Copyright (C) 2012-2016, Falko Br�utigam. All rights reserved.
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

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.runtime.config.Config2;
import org.polymap.core.runtime.config.Configurable;
import org.polymap.core.runtime.config.DefaultBoolean;
import org.polymap.core.runtime.config.Mandatory;

/**
 * Runs tasks in {@link UIJob}s.
 *
 * @see UIJob
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
public class JobExecutor
        extends Configurable
        implements ExecutorService {

    /** The default instance returned by {@link #instance}. */
    private static final JobExecutor instance = new JobExecutor().uiUpdate.put( false );

    /** The default instance returned by {@link #withProgress}. */
    private static final JobExecutor withProgress = new JobExecutor().uiUpdate.put( true );

    /** The default instance with {@link #uiUpdate} disabled. */
    public static JobExecutor instance() {
        return instance;
    }
    
    /** The default instance with {@link #uiUpdate} enabled. */
    public static JobExecutor withProgress() {
        return withProgress;
    }
    

    // instance *******************************************
    
    /** Use {@link UIJob#scheduleWithUIUpdate(long)} or {@link UIJob#schedule()}. */
    @Mandatory
    @DefaultBoolean( false )
    public Config2<JobExecutor,Boolean> uiUpdate;
    
    
    public JobExecutor() {
    }

    
    protected UIJob schedule( UIJob job ) {
        if (uiUpdate.get()) {
            job.scheduleWithUIUpdate();
        } 
        else {
            job.setSystem( true );
            job.schedule();
        }
        return job;
    }

    
    @Override
    public <T> Future<T> submit( final Callable<T> task ) {
        UIJob job = new UIJob( "JobExecutor" ) {
            @Override
            protected void runWithException( IProgressMonitor monitor ) throws Exception {
                T resultValue = task.call();
                setProperty( FutureJobAdapter.RESULT_VALUE_NAME, resultValue );
            }
        };
        schedule( job );
        return new FutureJobAdapter( job );
    }


    @Override
    public <T> Future<T> submit( final Runnable task, T result ) {
        UIJob job = new UIJob( "ExecutorJob" ) {
            @Override
            protected void runWithException( IProgressMonitor monitor ) throws Exception {
                task.run();
            }
        };
        job.setProperty( FutureJobAdapter.RESULT_VALUE_NAME, result );
        schedule( job );
        return new FutureJobAdapter( job );
    }


    @Override
    public Future<?> submit( final Runnable task ) {
        return submit( task, null );
    }


    @Override
    public void execute( final Runnable command ) {
        submit( command );
    }


    @Override
    public <T> List<Future<T>> invokeAll( Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit )
            throws InterruptedException {
        throw new RuntimeException( "not yet implemented." );
    }


    @Override
    public <T> List<Future<T>> invokeAll( Collection<? extends Callable<T>> tasks )
            throws InterruptedException {
        throw new RuntimeException( "not yet implemented." );
    }


    @Override
    public <T> T invokeAny( Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit )
            throws InterruptedException, ExecutionException, TimeoutException {
        throw new RuntimeException( "not yet implemented." );
    }


    @Override
    public <T> T invokeAny( Collection<? extends Callable<T>> tasks )
            throws InterruptedException, ExecutionException {
        throw new RuntimeException( "not yet implemented." );
    }


    // service handling ***********************************

    @Override
    public boolean awaitTermination( long timeout, TimeUnit unit )
            throws InterruptedException {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public boolean isShutdown() {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public boolean isTerminated() {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public void shutdown() {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public List<Runnable> shutdownNow() {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }
    
}
