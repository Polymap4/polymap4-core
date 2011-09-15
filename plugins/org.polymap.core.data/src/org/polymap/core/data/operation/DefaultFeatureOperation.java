/* 
 * polymap.org
 * Copyright 2011, Polymap GmbH. All rights reserved.
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
package org.polymap.core.data.operation;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * An abstract base implementaion of {@link IFeatureOperation} that provides default
 * implementation for most of the interface methods.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class DefaultFeatureOperation
        implements IFeatureOperation {

    protected IFeatureOperationContext      context;


    /**
     * This default implementation always returns true.
     */
    public boolean init( IFeatureOperationContext _context ) {
        this.context = _context;
        return true;
    }

    public IFeatureOperationContext getContext() {
        return context;
    }

    /**
     * This default implementation always returns true.
     */
    public boolean canExecute() {
        return true;
    }

    /**
     * This default implementation always returns false.
     */
    public boolean canRedo() {
        return false;
    }

    /**
     * This default implementation always returns false.
     */
    public boolean canUndo() {
        return false;
    }

    public Status redo( IProgressMonitor monitor )
    throws Exception {
        throw new RuntimeException( "not yet implemented." );
    }


    public Status undo( IProgressMonitor monitor )
    throws Exception {
        throw new RuntimeException( "not yet implemented." );
    }
    
}
