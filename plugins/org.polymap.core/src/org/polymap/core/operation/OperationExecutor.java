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

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.widgets.Display;

import org.eclipse.core.commands.operations.IUndoableOperation;

import org.polymap.core.runtime.Polymap;

/**
 * Implements chained execution of an operation and its concerns as provided by
 * {@link IOperationConcernFactory}.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @since 3.1
 */
class OperationExecutor
        implements InvocationHandler, OperationInfo {

    private static Log log = LogFactory.getLog( OperationExecutor.class );
    
    
    public static OperationExecutor newInstance( IUndoableOperation op ) {
        return new OperationExecutor( op );
    }
    

    // instance *******************************************
    
    private IUndoableOperation          op;
    
    private List<IUndoableOperation>    concerns;
    
    private Display                     display;
    
    private int                         concernIndex = 0;

    private ReentrantLock               lock = new ReentrantLock();
        
    
    protected OperationExecutor( IUndoableOperation op ) {
        super();
        this.op = op;
        this.concerns = IOperationConcernFactory.concernsForOperation( op, this );
        
        // check concerns
        for (IUndoableOperation concern : concerns) {
            if (!(concern instanceof OperationConcernAdapter)) {
                throw new IllegalArgumentException( "Operation concern does not implement OperationConcernAdapter: " + concern );
            }
        }

        this.display = Polymap.getSessionDisplay();
        assert this.display != null;
    }

    
    public IUndoableOperation getOperation() {
        return (IUndoableOperation)Proxy.newProxyInstance( 
                op.getClass().getClassLoader(),
                new Class[] {IUndoableOperation.class},
                this );
    }

    
    public OperationInfo getInfo() {
        return this;
    }
    
    
    public Object invoke( Object proxy, Method method, Object[] args )
    throws Throwable {
        if (method.getName().equals( "equals" )) {
            return proxy == args[0];
        }
        else if (method.getName().equals( "hashCode" )) {
            return hashCode();
        }
        else {
            try {
                if (lock.isHeldByCurrentThread()) {
                    throw new IllegalStateException( "Reentrant calls from operation and/or concerns into same operation are not allowed." );
                }
                lock.lockInterruptibly();
                
                concernIndex = 0;
                return method.invoke( next(), args );
            }
            catch (InvocationTargetException e) {
                throw e.getTargetException();
            }
            finally {
                lock.unlock();
            }
        }
    }

    
    // OperationInfo **************************************

    
    public IUndoableOperation next() {
        return concernIndex < concerns.size() ? concerns.get( concernIndex++ ) : op; 
    }

    
    public Object getAdapter( Class adapter ) {
        if (Display.class.isAssignableFrom( adapter)  ) {
            return display;
        }
        else {
            return null;
        }
    }

}
