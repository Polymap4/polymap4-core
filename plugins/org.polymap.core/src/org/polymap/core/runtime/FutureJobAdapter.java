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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.jobs.Job;

import org.polymap.core.CorePlugin;

/**
 * Provides a {@link Future} interface for a {@link Job} instance.
 * <p/>
 * The result value is read as Job property from the Job.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class FutureJobAdapter<V>
        implements Future<V> {

    public static final QualifiedName   RESULT_VALUE_NAME = new QualifiedName( CorePlugin.PLUGIN_ID, "resultValue" );
    
    private Job                         job;

    
    public FutureJobAdapter( Job job ) {
        this.job = job;
    }
    
    @Override
    public boolean cancel( boolean mayInterruptIfRunning ) {
        return job.cancel();
    }

    @Override
    public V get() throws InterruptedException, ExecutionException {
        job.join();
        return (V)job.getProperty( RESULT_VALUE_NAME );
    }

    @Override
    public V get( long timeout, TimeUnit unit )
    throws InterruptedException, ExecutionException, TimeoutException {
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public boolean isCancelled() {
        IStatus result = job.getResult();
        return result != null ? result.getSeverity() == IStatus.CANCEL : false;
    }

    @Override
    public boolean isDone() {
        return job.getResult() != null;
    }
    
}