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
package org.polymap.core.runtime.entity;

import java.util.EventListener;

import org.eclipse.swt.widgets.Display;

import org.polymap.core.runtime.SessionContext;
import org.polymap.core.runtime.event.Event;
import org.polymap.core.runtime.event.EventHandler;

/**
 * Register this listener type with {@link EntityStateTracker} to get informed
 * about changes of entities and/or features changed in this or another session.
 *
 * @see EntityStateTracker
 */
public interface IEntityStateListener
        extends EventListener {

    /**
     * Entity and/or feature has been changed.
     * <p/>
     * This method is called from a dedicated job/thread. The {@link SessionContext}
     * is mapped which was active when this listener has been registered. Code that updates the
     * UI should be properly wrapped inside {@link Display#asyncExec(Runnable)}.
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
    @EventHandler(scope=Event.Scope.JVM)
    public void modelChanged( EntityStateEvent ev );

}