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
 *
 * $Id: $
 */
package org.polymap.core.operation;

import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.operation.OperationSupport.OperationInfo;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @version ($Revision$)
 */
public abstract class OperationConcernAdapter
        implements IOperationConcern {

    public void afterExecute( IUndoableOperation op, IProgressMonitor monitor, OperationInfo info ) {
    }

    public void afterRedo( IUndoableOperation op, IProgressMonitor monitor, OperationInfo info ) {
    }

    public void afterUndo( IUndoableOperation op, IProgressMonitor monitor, OperationInfo info ) {
    }

    public boolean beforeExecute( IUndoableOperation op, IProgressMonitor monitor,
            OperationInfo info ) {
        return true;
    }

    public boolean beforeRedo( IUndoableOperation op, IProgressMonitor monitor, OperationInfo info ) {
        return true;
    }

    public boolean beforeUndo( IUndoableOperation op, IProgressMonitor monitor, OperationInfo info ) {
        return true;
    }

}
