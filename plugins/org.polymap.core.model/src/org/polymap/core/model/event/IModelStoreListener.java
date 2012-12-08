/* 
 * polymap.org
 * Copyright 2011-2012, Polymap GmbH. All rights reserved.
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
package org.polymap.core.model.event;

import java.util.EventListener;

import org.polymap.core.model.Module;
import org.polymap.core.runtime.SessionContext;

/**
 * Register this listener type with {@link ModelChangeTracker} to get informed
 * about changes of entities and/or features changed in this or another session.
 *
 * @see ModelChangeTracker
 */
public interface IModelStoreListener
        extends EventListener {

    /**
     * Checks if the session of this listener is still valid. If false then
     * this listener is removed. As the listeners are stored globally, this
     * is a potential memory leak.
     */
    public boolean isValid();


    /**
     * Entity and/or feature has been changed.
     * <p/>
     * This method is called from a dedicated job. For this call the
     * {@link SessionContext} is mapped that was active when this listener has been
     * {@link ModelChangeTracker#addListener(IModelStoreListener) registered}.
     * <p/>
     * Called when:
     * <ul>
     * <li>any global operation has been completed (pending changes in a
     * {@link ModelChangeSet}</li>
     * <li>any global operation has been undone (discard of a {@link ModelChangeSet}</li>
     * <li>any global {@link Module} has been commited</li>
     * </ul>
     * This method must never block. Implementations have to be thread save and aware
     * of reentrance of the method.
     */
    public void modelChanged( ModelStoreEvent ev );

}