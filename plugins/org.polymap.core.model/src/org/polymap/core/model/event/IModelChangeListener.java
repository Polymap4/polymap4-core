/* 
 * polymap.org
 * Copyright 2009-2012, Polymap GmbH, and individual contributors
 * as indicated by the @authors tag.
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

import java.beans.PropertyChangeEvent;

import org.eclipse.core.commands.operations.IUndoableOperation;

/**
 * Receives events from all entities of all modules of the current session. A
 * {@link ModelChangeEvent} event is fired after an {@link IUndoableOperation} has
 * finished. The {@link ModelChangeEvent} collects all
 * {@link PropertyChangeEvent}s fired during the operation.
 * 
 * @see ModelEventManager
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @since 3.0
 */
public interface IModelChangeListener
        extends EventListener {

    /**
     * Fired after an {@link IUndoableOperation} has finished. The
     * {@link ModelChangeEvent} collects all {@link PropertyChangeEvent}s fired
     * during the operation.
     * 
     * @param ev
     */
    public void modelChanged( ModelChangeEvent ev );
    
}
