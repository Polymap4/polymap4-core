/* 
 * polymap.org
 * Copyright (C) 2015, Falko Bräutigam. All rights reserved.
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
package org.polymap.core.ui;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import org.eclipse.swt.widgets.Display;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.polymap.core.CorePlugin;
import org.polymap.core.ui.StatusDispatcher.Style;

/**
 * Executes a given task in the UI (aka display) thread. The actual display thread is
 * determined via {@link UIUtils#sessionDisplay()}.
 */
public class UIThreadExecutor<V>
        implements Runnable, Future<V> {
    
    // error handlers *************************************
    
    /**
     * An error handler that ignores any Exception all together. 
     */
    public static Consumer<Throwable> ignoreError() {
        return e -> {};   
    }

    /**
     * An error handler that uses the {@link StatusDispatcher} to show an error
     * message to the user.
     */
    public static final Consumer<Throwable> showErrorMsg( String msg ) {
        return e -> StatusDispatcher.handle( new Status( IStatus.ERROR, CorePlugin.PLUGIN_ID, msg, e ), Style.SHOW );
    }
    
    /**
     * An error handler that uses the {@link StatusDispatcher} to log the Exception.
     */
    public static final Consumer<Throwable> logErrorMsg( String msg ) {
        return e -> StatusDispatcher.handle( new Status( IStatus.ERROR, CorePlugin.PLUGIN_ID, msg, e ), Style.LOG );
    }
    
    /**
     * An error handler that wraps Exceptions in a {@link RuntimeException} and
     * throws it inside the display thread.
     */
    public static final Consumer<Throwable> runtimeException() {
        return e -> { 
            if (e instanceof RuntimeException) {
                throw (RuntimeException)e;
            }
            else if (e instanceof Error) {
                throw (Error)e;
            }
            else {
                throw new RuntimeException( e );
            }
        };
    }

    // static API *****************************************
    
    /**
     * Executes a given task in the UI thread by calling
     * {@link Display#asyncExec(Runnable)}.
     *
     * @param task The task to execute
     * @param errorHandlers
     * @return A {@link Future} to wait for and get the result or cancel execution.
     */
    public static <V> UIThreadExecutor<V> async( Callable<V> task, Consumer<Throwable>... errorHandlers ) {
        UIThreadExecutor<V> executor = new UIThreadExecutor<V>( task, errorHandlers );
        UIUtils.sessionDisplay().asyncExec( executor );
        return executor;
    }

    
    /**
     * Same as {@link #async(Callable, Consumer)} but checks if the current thread is
     * the display thread. If yes thean immediately executes the task. 
     *
     * @see #async(Callable, Consumer)
     */
    public static <V> Future<V> asyncFast( Callable<V> task, Consumer<Throwable>... errorHandlers ) {
        UIThreadExecutor<V> executor = new UIThreadExecutor<V>( task, errorHandlers );
        if (Display.getCurrent() == null) {
            UIUtils.sessionDisplay().asyncExec( executor );
            return executor;
        }
        else {
            executor.run();
            return executor;
        }
    }

    
    /**
     * Executes a given task in the UI thread by calling
     * {@link Display#syncExec(Runnable)}.
     *
     * @param task The task to execute
     * @param errorHandlers
     * @return A {@link Future} to wait for and get the result or cancel execution.
     */
    public static <V> Future<V> sync( Callable<V> task, Consumer<Throwable>... errorHandlers ) {
        UIThreadExecutor<V> executor = new UIThreadExecutor<V>( task, errorHandlers );
        UIUtils.sessionDisplay().syncExec( executor );
        return executor;
    }

    
    /**
     * Same as {@link #sync(Callable, Consumer)} but checks if the current thread is
     * the display thread. If yes thean immediately executes the task. 
     *
     * @see #sync(Callable, Consumer)
     */
    public static <V> UIThreadExecutor<V> syncFast( Callable<V> task, Consumer<Throwable>... errorHandlers ) {
        UIThreadExecutor<V> executor = new UIThreadExecutor<V>( task, errorHandlers );
        if (Display.getCurrent() == null) {
            UIUtils.sessionDisplay().syncExec( executor );
            return executor;
        }
        else {
            executor.run();
            return executor;
        }
    }

    
    // instance *******************************************
    
    private Consumer<Throwable>[]   errorHandlers;

    private final Callable<V>       task;
    
    private volatile V              result;
    
    private volatile Throwable      error;
    
    private volatile boolean        cancelled;

    
    public UIThreadExecutor( Callable<V> task, Consumer<Throwable>... errorHandlers  ) {
        this.task = task;
        this.errorHandlers = errorHandlers;
    }

    @Override
    public boolean cancel( boolean mayInterruptIfRunning ) {
        if (mayInterruptIfRunning) {
            throw new UnsupportedOperationException( "Interrupting UI thread is not possible." );
        }
        if (isDone()) {
            return false;
        }
        return cancelled = true;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public boolean isDone() {
        return result != null || error != null;
    }

    /**
     * Exceptions should be handled by the error handlers, consider using {@link #result()}.
     */
    @Override
    public V get() throws InterruptedException, ExecutionException {
        try {
            return get( -1, null );
        }
        catch (TimeoutException e) {
            throw new RuntimeException( "This must never happen." );
        }
    }
    
    @Override
    public V get( long timeout, TimeUnit unit ) throws InterruptedException, ExecutionException, TimeoutException {
        if (error != null) {
            throw new ExecutionException( error );
        }
        waitForCompletion( timeout, unit );
        return result;
    }

    /**
     * Waits if necessary for the computation to complete, and then retrieves its
     * result. Same as {@link #get()} but ignores any Exception.
     * 
     * @return {@link Optional#empty()} if the task returned <code>null</code> *or*
     *         if the task threw an Exception. In case of Exception the error handlers
     *         should handle everything and maybe throw a {@link RuntimeException} so that
     *         code that asks for the result is never executed.
     */
    public Optional<V> result() {
        try {
            return Optional.ofNullable( get() );
        }
        catch (RuntimeException|Error e) {
            throw e;
        }
        catch (ExecutionException|InterruptedException e) {
            return Optional.empty();
        }
    }

    public Optional<Throwable> error() {
        throw new RuntimeException( "not yet..." );
    }
    
    protected void waitForCompletion( long timeout, TimeUnit unit ) {
        if (result == null) {
            throw new UnsupportedOperationException( "Waiting for result on async executed task is not yet suppported :(" );
        }        
    }
    
    @Override
    public void run() {
        try {
            if (!cancelled) {
                result = task.call();
            }
        }
        catch (Throwable e) {
            error = e;
            Arrays.stream( errorHandlers ).forEach( handler -> handler.accept( error ) );
        }
    }
}
