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
package org.polymap.core.catalog.qi4j;

import java.lang.reflect.Method;

import org.qi4j.api.concern.GenericConcern;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.structure.Module;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.polymap.core.model.ModelProperty;
import org.polymap.core.model.TransientProperty;
import org.polymap.core.operation.OperationSupport;
import org.polymap.core.qi4j.Qi4jPlugin;
import org.polymap.core.qi4j.QiModule;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.catalog.Messages;

/**
 * Add operation bounds to single entity methods, let them act as operations
 * without the need to call them from an explicite operation implementation.
 * 
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class MethodOperationBoundsConcern
        extends GenericConcern {

    private static Log log = LogFactory.getLog( MethodOperationBoundsConcern.class );

    @This EntityComposite   composite;
    
    @Structure Module       _module;

    
    public Object invoke( Object proxy, Method method, Object[] args )
            throws Throwable {
        // any property annotation present?
        ModelProperty a = method.getAnnotation( ModelProperty.class );
        TransientProperty a2 = method.getAnnotation( TransientProperty.class );

        
        if (a == null && a2 == null
                // do not start operation when outside session (while init)
                || Polymap.getSessionDisplay() == null) {
            return next.invoke( proxy, method, args );
        }

        // call with operation        
        log.info( "Starting operation ... (" + hashCode() + ")" + " [" + Thread.currentThread().getId() + "]" );
        QiModule applied = null;
        boolean operationStarted = false;
        try {
            // start operation
            applied = Qi4jPlugin.Session.instance().resolveModule( _module );
            if (applied != null && !applied.hasOperation()) { 
                applied.newChangeSet();
                applied.startOperation();
                operationStarted = true;
            }
            
            // call underlying
            Object result = next.invoke( proxy, method, args );

            // end operation
            if (operationStarted) {
                applied.endOperation( true );
            }

            // operation
            String label = Messages.get( composite.type().getSimpleName() + "_" + method.getName() );
            OperationSupport.instance().execute( 
                    new MethodOperation( label, applied ), false, false );

            log.info( "    operation completed." );
            return result;
        }
        catch (Throwable e) {
            if (operationStarted) {
                applied.discardChangeSet();
                applied.endOperation( false );
            }
            log.info( "    operation canceled." );
            throw e;
        }
    }

    
    /**
     * This is a "fake" operation that does nothing but helps us to take
     * part of the operation undo/redo system.
     */
    static class MethodOperation
            extends AbstractOperation
            implements IUndoableOperation {

        private QiModule        module;
        
        
        public MethodOperation( String label, QiModule module ) {
            super( label );
            this.module = module;
        }

        public IStatus execute( IProgressMonitor monitor, IAdaptable info )
                throws ExecutionException {
            // the work has been done by the concern
            return Status.OK_STATUS;
        }

        public IStatus undo( IProgressMonitor monitor, IAdaptable info )
                throws ExecutionException {
            module.startOperation();
            module.discardChangeSet();
            module.endOperation( true );
            
            log.debug( "    operation completed." );
            return Status.OK_STATUS;
        }

        public boolean canRedo() {
            return false;
        }

        public IStatus redo( IProgressMonitor monitor, IAdaptable info )
                throws ExecutionException {
            return Status.CANCEL_STATUS;
        }
        
    }

}
