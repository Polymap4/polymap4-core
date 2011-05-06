/* 
 * polymap.org
 * Copyright 2010, Falko Bräutigam, and other contributors as indicated
 * by the @authors tag.
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
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class OperationConcernAdapter
        implements IUndoableOperation {


    protected abstract OperationInfo getInfo();
    

    // IUndoableOperation *********************************
    
    public void addContext( IUndoContext context ) {
        getInfo().next().addContext( context );
    }

    public boolean canExecute() {
        return getInfo().next().canExecute();
    }

    public boolean canRedo() {
        return getInfo().next().canRedo();
    }

    public boolean canUndo() {
        return getInfo().next().canUndo();
    }

    public void dispose() {
        getInfo().next().dispose();
    }

    public IStatus execute( IProgressMonitor monitor, IAdaptable info )
            throws ExecutionException {
        return getInfo().next().execute( monitor, info );
    }

    public IUndoContext[] getContexts() {
        return getInfo().next().getContexts();
    }

    public String getLabel() {
        return getInfo().next().getLabel();
    }

    public boolean hasContext( IUndoContext context ) {
        return getInfo().next().hasContext( context );
    }

    public IStatus redo( IProgressMonitor monitor, IAdaptable info )
            throws ExecutionException {
        return getInfo().next().redo( monitor, info );
    }

    public void removeContext( IUndoContext context ) {
        getInfo().next().removeContext( context );
    }

    public IStatus undo( IProgressMonitor monitor, IAdaptable info )
            throws ExecutionException {
        return getInfo().next().undo( monitor, info );
    }
    
}
