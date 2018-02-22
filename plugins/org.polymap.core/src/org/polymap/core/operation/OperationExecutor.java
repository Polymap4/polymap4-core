/* 
 * polymap.org
 * Copyright 2011-2018, Falko Bräutigam. All rights reserved.
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

import java.util.ArrayList;
import java.util.List;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.widgets.Display;

import org.eclipse.core.commands.operations.IUndoableOperation;

import org.polymap.core.ui.UIUtils;

/**
 * Implements chained execution of an operation and its concerns as provided by
 * {@link IOperationConcernFactory}.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
class OperationExecutor
        implements InvocationHandler {

    private static final Log log = LogFactory.getLog( OperationExecutor.class );
    
    public static OperationExecutor newInstance( IUndoableOperation op ) {
        return new OperationExecutor( op );
    }
    

    // instance *******************************************
    
    private IUndoableOperation          op;
    
    
    /** Chain of operation concerns - last element is the operation itself. */
    private List<IUndoableOperation>    chain;
    
    private Display                     display;

    /**
     * The next index in the list of Concerns. {@link ThreadLocal} allows this to be
     * called from different threads.
     */
    //private ThreadLocal<AtomicInteger>  concernIndex = new ThreadLocal();

    
    protected OperationExecutor( IUndoableOperation op ) {
        this.op = op;
        
        // create concerns
        this.chain = new ArrayList();
        int index = 0;
        for (OperationConcernExtension ext : OperationConcernExtension.all() ) {
            OperationInfoImpl info = newOperationInfo( index );            
            IUndoableOperation concern = ext.newInstance().newInstance( op, info );
            if (concern != null ) {
                chain.add( concern );
                index ++;
            }
        }
        // check concerns type
        assert chain.stream().allMatch( c -> c instanceof OperationConcernAdapter ) : "Operation concern does not implement OperationConcernAdapter.";

        this.chain.add( op );
        
        this.display = UIUtils.sessionDisplay();
        assert this.display != null;
    }


    public OperationInfoImpl newOperationInfo( int index ) {
        return new OperationInfoImpl( index );
    }

    
    public IUndoableOperation operation() {
        return (IUndoableOperation)Proxy.newProxyInstance( 
                op.getClass().getClassLoader(),
                new Class[] {IUndoableOperation.class},
                this );
    }

    
    public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable {
        if (method.getName().equals( "equals" )) {
            return proxy == args[0];
        }
        else if (method.getName().equals( "hashCode" )) {
            return hashCode();
        }
        else {
            try {
                return method.invoke( chain.get( 0 ), args );
            }
            catch (InvocationTargetException e) {
                throw e.getTargetException();
            }
        }
    }


    /**
     * 
     */
    protected class OperationInfoImpl
            implements OperationInfo {

        private int         concernIndex;
        
        protected OperationInfoImpl( int concernIndex ) {
            this.concernIndex = concernIndex;
        }

        public IUndoableOperation next() {
            assert concernIndex >= 0 && concernIndex < chain.size() : "No such index in the operation concern chain: " + concernIndex;
            return chain.get( concernIndex+1 );
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
    
}
