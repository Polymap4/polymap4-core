/* 
 * polymap.org
 * Copyright 2009, Polymap GmbH, and individual contributors as indicated
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 * $Id$
 */

package org.polymap.core.qi4j;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.qi4j.api.concern.ConcernOf;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.structure.Module;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * This concern ensures that operations properly signals start/complete/cancel
 * to the {@link ProjectRepository}.
 * 
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public abstract class OperationBoundsConcern
        extends ConcernOf<IUndoableOperation>
        implements IUndoableOperation {

    private static Log log = LogFactory.getLog( OperationBoundsConcern.class );

    @Structure Module           _module;
    

    public IStatus execute( IProgressMonitor monitor, IAdaptable info )
    throws ExecutionException {
        log.info( "Starting operation ... (" + hashCode() + ")" + " [" + Thread.currentThread().getId() + "]" );
        QiModule applied = null;
        try {
            // start operation
            applied = Qi4jPlugin.Session.instance().resolveModule( _module );
            applied.newChangeSet();
            applied.startOperation();
            
            // do the operation
            IStatus result = next.execute( monitor, info );

            // end operation
            if (applied != null) {
                applied.endOperation( true );
            }

            log.info( "    operation completed." );
            return result;
        }
        catch (Throwable e) {
            if (applied != null) {
                applied.endOperation( false );
            }
            log.info( "    operation canceled." );

            if (e instanceof ExecutionException) {
                throw (ExecutionException)e;
            } 
            else {
                throw new ExecutionException( e.getMessage(), e );
            }
        }
    }


    public IStatus undo( IProgressMonitor monitor, IAdaptable info )
    throws ExecutionException {
        log.info( "Undoing operation ... (" + hashCode() + ")" + " [" + Thread.currentThread().getId() + "]" );
        QiModule applied = null;
        try {
            applied = Qi4jPlugin.Session.instance().resolveModule( _module );
            
            if (applied != null) {
                applied.startOperation();
                applied.discardChangeSet();
                applied.endOperation( true );
            }

            log.info( "    operation completed." );
            return Status.OK_STATUS;
        }
        catch (Throwable e) {
            if (applied != null) {
                applied.endOperation( false );
            }
            log.info( "    operation canceled." );
            throw new ExecutionException( e.getMessage(), e );
        }
    }
    

    public IStatus redo( IProgressMonitor monitor, IAdaptable info )
    throws ExecutionException {
        log.info( "..." );
        return execute( monitor, info );
    }


}
