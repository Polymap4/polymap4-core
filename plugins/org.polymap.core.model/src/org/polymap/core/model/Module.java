/*
 * polymap.org 
 * Copyright 2009-2011, Falko Bräutigam, and individual contributors as
 * indicated by the @authors tag. All rights reserved.
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */
package org.polymap.core.model;

import java.beans.PropertyChangeListener;

import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;

import org.eclipse.core.runtime.jobs.Job;

import org.polymap.core.model.event.IModelStoreListener;
import org.polymap.core.model.event.IModelChangeListener;
import org.polymap.core.model.event.IEventFilter;
import org.polymap.core.model.event.ModelEventManager;

/**
 * Provides the API of one module of the domain model. A {@link Module} instance
 * is bound to one user session. A {@link Module} provides:
 * <ul>
 * <li>methods to register change listeners</li>
 * <li>methods to commit/rollback/register change listeners</li>
 * <li>domain specific factory methods for entities
 * </ul>
 * 
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @since 3.0
 */
public interface Module {

    /**
     * Commit all changes of this module since last commit or rollback. This
     * will send all the changes down to the underlying entity stores. This
     * clears the list of nested change sets and start with a new change set.
     * 
     * @throws ConcurrentModificationException
     * @throws UnitOfWorkCompletionException
     */
    public void commitChanges() 
            throws ConcurrentModificationException, CompletionException;
    
    /**
     * Discard all changes of this module since last commit.
     */
    public void revertChanges();


    /**
     * See
     * {@link ModelEventManager#addPropertyChangeListener(PropertyChangeListener, IEventFilter)}
     * for details.
     * 
     * @param l The listener to add.
     * @param f The filter to apply.
     * @see ModelEventManager#addPropertyChangeListener(PropertyChangeListener,
     *      IEventFilter)
     */
    public void addPropertyChangeListener( PropertyChangeListener l, IEventFilter f );

    public void removePropertyChangeListener( PropertyChangeListener l );


    /**
     * See
     * {@link ModelEventManager#addModelChangeListener(IModelChangeListener, IEventFilter)}
     * for details.
     * 
     * @param l The listener to add.
     * @param f The filter to apply.
     * @see ModelEventManager#addModelChangeListener(IModelChangeListener,
     *      IEventFilter)
     */
    public void addModelChangeListener( IModelChangeListener l, IEventFilter f );
    
    public void removeModelChangeListener( IModelChangeListener l );


    /**
     * Adds the given event listener. Has no effect if the same listener is
     * already registerd. The given listener might be stored in a
     * {@link WeakReference}, the caller has to make sure that a strong
     * reference exists as long as the listener should receive events.
     * <p>
     * The listener is probably called from a {@link Job}. So proper
     * synchronization with the UI has to be done.
     */
    public void addModelStoreListener( IModelStoreListener l );

    public void removeModelStoreListener( IModelStoreListener l );

}
