/* 
 * polymap.org
 * Copyright 2010, Falko Br�utigam, and other contributors as indicated
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
 * $Id: $
 */
package org.polymap.core.operation;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.CoreException;

/**
 * This API allows to intercept the execution of other operations. A new factory
 * is registered with the extension point: {@value OperationConcernExtension#EXTENSION_POINT_ID}.
 *
 * @see OperationConcernAdapter
 * @see OperationConcernExtension
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 * @since 3.1
 */
public abstract class IOperationConcernFactory {

    private static List<IOperationConcernFactory>   factories = null;
    
    
    static List<IUndoableOperation> concernsForOperation( IUndoableOperation op,
            OperationInfo info ) {
        // not synchronized, double init is no problem
        if (factories == null) {
            factories = new ArrayList();
            for (OperationConcernExtension ext : OperationConcernExtension.extensions) {
                 try {
                    factories.add( ext.newFactory() );
                }
                catch (CoreException e) {
                    throw new RuntimeException( e );
                }
            }
        }
        //
        List<IUndoableOperation> result = new ArrayList();
        for (IOperationConcernFactory factory : factories) {
            IUndoableOperation concern = factory.newInstance( op, info );
            if (concern != null) {
                result.add( concern );
            }
        }
        return result;
    }
    
    
    // interface ******************************************

    /**
     * Checks if the given operation is supported by this factory and returns a newly
     * created concern instance for it, or null if the given operation is not handled by
     * this factory.
     * <p/>
     * Implementations should return an operation of type {@link OperationConcernAdapter}.
     * 
     * @param op
     * @return The newly created concern, or null.
     */
    public abstract IUndoableOperation newInstance( IUndoableOperation op,
            OperationInfo info );
    
}
