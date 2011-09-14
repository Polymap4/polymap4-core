/* 
 * polymap.org
 * Copyright 2011, Polymap GmbH. All rights reserved.
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
package org.polymap.core.data.operation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
class FeatureOperationContainer
        extends AbstractOperation {

    private static Log log = LogFactory.getLog( FeatureOperationContainer.class );
    
    private IFeatureOperation           delegate;
    
    private IFeatureOperationContext    context;

    
    public FeatureOperationContainer( IFeatureOperation delegate, IFeatureOperationContext context ) {
        super( delegate.getLabel() );
        this.delegate = delegate;
        this.context = context;
    }

    
    public IStatus execute( IProgressMonitor monitor, IAdaptable info )
    throws ExecutionException {
        try {
            delegate.execute( context, monitor );
            return Status.OK_STATUS;
        }
        catch (Exception e) {
            throw new ExecutionException( "", e );
        }
    }

    
    public IStatus redo( IProgressMonitor monitor, IAdaptable info )
    throws ExecutionException {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

    
    public IStatus undo( IProgressMonitor monitor, IAdaptable info )
    throws ExecutionException {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

    
    public boolean canExecute() {
        return delegate.canExecute();
    }

    public boolean canUndo() {
        return delegate.canUndo();
    }

    public boolean canRedo() {
        return delegate.canRedo();
    }
    
}
