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
 */
package org.polymap.core.qi4j.event;

import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.concern.GenericConcern;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.injection.scope.This;

import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.polymap.core.model.ModelProperty;
import org.polymap.core.model.TransientProperty;
import org.polymap.core.operation.OperationSupport;
import org.polymap.core.runtime.Polymap;

/**
 * Add operation bounds to single entity methods, let them act as operations
 * without the need to call them from an explicite operation implementation.
 * 
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @since 3.1
 */
public class MethodOperationBoundsConcern
        extends GenericConcern {

    private static Log log = LogFactory.getLog( MethodOperationBoundsConcern.class );

    @This 
    private EntityComposite     composite;
    
    
    public Object invoke( final Object proxy, final Method method, final Object[] args )
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
        String label = "Ausführen: " + composite.type().getSimpleName() + "." + method.getName();
        MethodOperation op = new MethodOperation( label ) {
            protected IStatus doExecute( IProgressMonitor monitor, IAdaptable info )
            throws Exception {
                try {
                    Object result = next.invoke( proxy, method, args );
                    setMethodResult( result );
                    return Status.OK_STATUS;
                }
                catch (Exception e) {
                    throw e;
                }
                catch (Error e) {
                    throw e;
                }
                catch (Throwable e) {
                    throw new RuntimeException( e );
                }
            }
        };
        OperationSupport.instance().execute( op, false, false );

        log.info( "    operation completed." );
        return op.methodResult;
    }

    
    /**
     * This is a "fake" operation that does nothing but helps us to take
     * part of the operation undo/redo system.
     */
    static abstract class MethodOperation
            extends AbstractModelChangeOperation
            implements IUndoableOperation {

        protected Object                methodResult;
        

        public MethodOperation( String label ) {
            super( label );
        }
        
        protected Object getMethodResult() {
            return methodResult;
        }
        
        protected void setMethodResult( Object methodResult ) {
            this.methodResult = methodResult;
        }
        
    }

}
