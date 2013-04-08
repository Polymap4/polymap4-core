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
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class FeatureOperationContainer
        extends AbstractOperation {

    private static Log log = LogFactory.getLog( FeatureOperationContainer.class );
    
    private IFeatureOperation           delegate;
    
    
    public FeatureOperationContainer( IFeatureOperation delegate, String label ) {
        super( label );
        this.delegate = delegate;
        
        // add this to context
        DefaultOperationContext context = (DefaultOperationContext)delegate.getContext();
        context.addAdapter( new IAdaptable() {
            public Object getAdapter( Class adapter ) {
                if (adapter.equals( IUndoableOperation.class )) {
                    return FeatureOperationContainer.this;
                }
                return null;
            }
        });
    }

    
    public IFeatureOperation getDelegate() {
        return delegate;
    }


    public IStatus execute( IProgressMonitor monitor, IAdaptable info )
    throws ExecutionException {
        try {
            // add info to context
            DefaultOperationContext context = (DefaultOperationContext)delegate.getContext();
            context.addAdapter( info );
            
            // preset task name without beginTask()
            monitor.setTaskName( getLabel() );
            
            switch (delegate.execute( monitor )) {
                case OK :
                    return Status.OK_STATUS;
                case Cancel :
                    return Status.CANCEL_STATUS;
                default:
                    return Status.CANCEL_STATUS;
            }
        }
        catch (InterruptedException e) {
            return Status.CANCEL_STATUS;
        }
        catch (ExecutionException e) {
            throw e;
        }
        catch (Exception e) {
            throw new ExecutionException( e.getLocalizedMessage(), e );
        }
    }

    
    public IStatus redo( IProgressMonitor monitor, IAdaptable info )
    throws ExecutionException {
        try {
            // preset task name without beginTask()
            monitor.setTaskName( getLabel() );
            
            switch (delegate.redo( monitor )) {
                case OK :
                    return Status.OK_STATUS;
                case Cancel :
                    return Status.CANCEL_STATUS;
                default:
                    return Status.CANCEL_STATUS;
            }
        }
        catch (InterruptedException e) {
            return Status.CANCEL_STATUS;
        }
        catch (ExecutionException e) {
            throw e;
        }
        catch (Exception e) {
            throw new ExecutionException( e.getLocalizedMessage(), e );
        }
    }

    
    public IStatus undo( IProgressMonitor monitor, IAdaptable info )
    throws ExecutionException {
        try {
            // preset task name without beginTask()
            monitor.setTaskName( getLabel() );
            
            switch (delegate.undo( monitor )) {
                case OK :
                    return Status.OK_STATUS;
                case Cancel :
                    return Status.CANCEL_STATUS;
                default:
                    return Status.CANCEL_STATUS;
            }
        }
        catch (InterruptedException e) {
            return Status.CANCEL_STATUS;
        }
        catch (ExecutionException e) {
            throw e;
        }
        catch (Exception e) {
            throw new ExecutionException( e.getLocalizedMessage(), e );
        }
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
