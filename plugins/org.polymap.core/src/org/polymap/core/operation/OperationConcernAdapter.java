/* 
 * polymap.org
 * Copyright 2010-2012, Falko Bräutigam. All rights reserved.
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
package org.polymap.core.operation;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

/**
 * Base class of operation concern implementations. Provides a default implementation
 * of all operation methods. All methods delegate the call to the next concern in the
 * chain by default.
 * <p/>
 * Implemented methods must not call one of the locally defined method except {@link #getInfo()}.
 * Instead the Operation given to the ctor has to be used.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @since 3.1
 */
public abstract class OperationConcernAdapter
        implements IUndoableOperation {

    private IUndoableOperation      next;

    protected abstract OperationInfo getInfo();
    
    /**
     * Cache the next pointer. getInfo().next() must not be called more than once
     * to prevent concernIndex to get out of order.
     */
    private IUndoableOperation next() {
        return next = (next == null) ? getInfo().next() : next;
    }
    
    // IUndoableOperation *********************************
    
    public void addContext( IUndoContext context ) {
        next().addContext( context );
    }

    public boolean canExecute() {
        return next().canExecute();
    }

    public boolean canRedo() {
        return next().canRedo();
    }

    public boolean canUndo() {
        return next().canUndo();
    }

    public void dispose() {
        next().dispose();
    }

    
    /**
     * Concern implementations should check the {@link IStatus} returned by
     * <code>next().execute( monitor, info )</code>. If the upstream operation
     * returnes Cancel or Error state then the concern should return this status
     * without executing as the upstream operation has unefined state.
     */
    public IStatus execute( IProgressMonitor monitor, IAdaptable info )
            throws ExecutionException {
        return next().execute( monitor, info );
    }

    public IUndoContext[] getContexts() {
        return next().getContexts();
    }

    public String getLabel() {
        return next().getLabel();
    }

    public boolean hasContext( IUndoContext context ) {
        return next().hasContext( context );
    }

    public IStatus redo( IProgressMonitor monitor, IAdaptable info )
            throws ExecutionException {
        return next().redo( monitor, info );
    }

    public void removeContext( IUndoContext context ) {
        next().removeContext( context );
    }

    public IStatus undo( IProgressMonitor monitor, IAdaptable info )
            throws ExecutionException {
        return next().undo( monitor, info );
    }
    
}
