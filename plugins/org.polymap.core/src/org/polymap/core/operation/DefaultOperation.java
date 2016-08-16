/* 
 * polymap.org
 * Copyright (C) 2015-2016, Falko Bräutigam. All rights reserved.
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
package org.polymap.core.operation;

import java.util.concurrent.Callable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

/**
 * Provides a general base class for operations. Provides exception handling. Does
 * not support undo/redo by default.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class DefaultOperation
        extends AbstractOperation {

    private static Log log = LogFactory.getLog( DefaultOperation.class );

    public DefaultOperation( String label ) {
        super( label );
    }
    
    protected abstract IStatus doExecute( IProgressMonitor monitor, IAdaptable info ) throws Exception;
    
    protected IStatus doUndo( IProgressMonitor monitor, IAdaptable info ) throws Exception {
        throw new RuntimeException( "not implemented." );        
    }
    
    protected IStatus doRedo( IProgressMonitor monitor, IAdaptable info ) throws Exception {
        throw new RuntimeException( "not implemented." );        
    }

    private IStatus handleException( Callable<IStatus> task ) throws ExecutionException {
        try {
            return task.call();
        }
        catch (ExecutionException e) {
            throw e;
        }        
        catch (Exception e) {
            throw new ExecutionException( e.getMessage(), e );
        }        
    }
    
    @Override
    public final IStatus execute( IProgressMonitor monitor, IAdaptable info ) throws ExecutionException {
        return handleException( () -> doExecute( monitor, info ) );
    }

    @Override
    public final IStatus redo( IProgressMonitor monitor, IAdaptable info ) throws ExecutionException {
        return handleException( () -> doRedo( monitor, info ) );
    }

    @Override
    public final IStatus undo( IProgressMonitor monitor, IAdaptable info ) throws ExecutionException {
        return handleException( () -> doUndo( monitor, info ) );
    }

    /**
     * {@inheritDoc}
     * <p/>
     * This default implementation always return <b>false</b>.
     */
    @Override
    public boolean canRedo() {
        return false;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * This default implementation always return <b>false</b>.
     */
    @Override
    public boolean canUndo() {
        return false;
    }
    
}
