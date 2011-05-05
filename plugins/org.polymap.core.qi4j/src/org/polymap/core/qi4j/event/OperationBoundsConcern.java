/*
 * polymap.org 
 * Copyright 2009-2011, Polymap GmbH, and individual contributors as indicated
 * by the @authors tag.
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
package org.polymap.core.qi4j.event;

import java.beans.PropertyChangeEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.concern.ConcernOf;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

/**
 * This concern ensures that operations properly signals start/end to the module
 * of the entity and it provides general undo functionality based on the
 * {@link PropertyChangeEvent}s fired during operation.
 * 
 * @deprecated Functionality has been moved to {@link AbstractModelChangeOperation}. 
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @since 3.0
 */
public abstract class OperationBoundsConcern
        extends ConcernOf<IUndoableOperation>
        implements IUndoableOperation {

    private static Log log = LogFactory.getLog( OperationBoundsConcern.class );
    

    protected OperationBoundsConcern() {
        super();
    }


    public IStatus execute( IProgressMonitor monitor, IAdaptable info )
    throws ExecutionException {
        //log.info( "Starting operation ... (" + hashCode() + ")" + " [" + Thread.currentThread().getId() + "]" );
        return next.execute( monitor, info );
    }


    public IStatus undo( IProgressMonitor monitor, IAdaptable info )
    throws ExecutionException {
        //log.info( "Undoing operation ... (" + hashCode() + ")" + " [" + Thread.currentThread().getId() + "]" );
        return next.redo( monitor, info );
    }
    

    public IStatus redo( IProgressMonitor monitor, IAdaptable info )
    throws ExecutionException {
        log.info( "..." );
        return next.redo( monitor, info );
    }

}
