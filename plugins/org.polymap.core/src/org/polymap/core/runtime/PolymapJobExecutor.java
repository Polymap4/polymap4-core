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

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import net.refractions.udig.core.internal.CorePlugin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class PolymapJobExecutor
        implements ExecutorService {

    private static Log log = LogFactory.getLog( PolymapJobExecutor.class );

    @Override
    public <T> Future<T> submit( final Callable<T> task ) {
        Job job = new Job( "ExecutorJob" ) {
            protected IStatus run( IProgressMonitor monitor ) {
                try {
                    T resultValue = task.call();
                    setProperty( FutureJobAdapter.RESULT_VALUE_NAME, resultValue );
                    return Status.OK_STATUS;
                }
                catch (Throwable e) {
                    return new Status( IStatus.ERROR, CorePlugin.ID, e.getLocalizedMessage(), e );
                }
            }
        };
        job.setSystem( true );
        job.setPriority( UIJob.DEFAULT_PRIORITY );
        job.schedule();
        return new FutureJobAdapter( job );
    }


    @Override
    public <T> Future<T> submit( final Runnable task, T result ) {
        Job job = new Job( "ExecutorJob" ) {
            protected IStatus run( IProgressMonitor monitor ) {
                try {
                    task.run();
                    return Status.OK_STATUS;
                }
                catch (Throwable e) {
                    return new Status( IStatus.ERROR, CorePlugin.ID, e.getLocalizedMessage(), e );
                }
            }
        };
        job.setProperty( FutureJobAdapter.RESULT_VALUE_NAME, result );
        job.setSystem( true );
        job.setPriority( UIJob.DEFAULT_PRIORITY );
        job.schedule();
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
