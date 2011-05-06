/* 
 * polymap.org
 * Copyright 2011, Falko Bräutigam, and other contributors as
 * indicated by the @authors tag. All rights reserved.
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class LogOperationConcernFactory
        extends IOperationConcernFactory {

    private static Log log = LogFactory.getLog( LogOperationConcernFactory.class );


    public IUndoableOperation newInstance( final IUndoableOperation op, final OperationInfo info ) {

        return new OperationConcernAdapter() {
            
            public IStatus execute( IProgressMonitor monitor, IAdaptable _info )
            throws ExecutionException {
                try {
                    log.info( "START: " + op.getClass().getCanonicalName() + " --------------------" );
                    return info.next().execute( monitor, _info );
                }
                finally {
                    log.info( "STOP: " + op.getClass().getName() + " --------------------" );
                }
            }

            public IStatus redo( IProgressMonitor monitor, IAdaptable _info )
                    throws ExecutionException {
                log.info( "Operation : " + op.getClass().getName() );
                return info.next().redo( monitor, info );
            }

            public IStatus undo( IProgressMonitor monitor, IAdaptable _info )
                    throws ExecutionException {
                log.info( "Operation : " + op.getClass().getName() );
                return info.next().undo( monitor, info );
            }

            protected OperationInfo getInfo() {
                return info;
            }
            
        };
    }

}
