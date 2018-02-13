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

import java.util.Optional;

import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IAdaptable;

/**
 *
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface OperationInfo
        extends IAdaptable {

    /**
     * The next operation concern in the chain, or the operation itself.
     */
    public IUndoableOperation next();
    
    /**
     * More convenient version of {@link #getAdapter(Class)}.
     */
    public default <R> Optional<R> adapter( Class<? extends R> type ) {
        return Optional.ofNullable( (R)getAdapter( type ) );
    }
}