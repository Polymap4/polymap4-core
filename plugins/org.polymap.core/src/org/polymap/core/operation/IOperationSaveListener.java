/* 
 * polymap.org
 * Copyright 2009, 2011 Polymap GmbH. All rights reserved.
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

import java.util.EventListener;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Implement this interface to get notified when the stack of operations should be
 * saved or discarded. Listeners are registered with the {@link OperationSupport}.
 * 
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @since 3.0
 */
public interface IOperationSaveListener
        extends EventListener {

    /**
     * First phase of the two phase commit protocol. This might throw
     * an exception which causes the entity save to be
     * {@link #rollback(OperationSupport)}.
     * 
     * @param os
     * @throws Exception
     */
    public void prepareSave( OperationSupport os, IProgressMonitor monitor )
    throws Exception;
    
    /**
     * Save a previously prepared changes. Commit and close transactions.
     * 
     * @param os
     */
    public void save( OperationSupport os, IProgressMonitor monitor );


    /**
     * Rollback previously prepared save. Rollback and close transactions. Don't
     * write anything to the underlying store.
     * 
     * @param os
     */
    public void rollback( OperationSupport os, IProgressMonitor monitor );
    
    
    public void revert( OperationSupport os, IProgressMonitor monitor );
    
}
