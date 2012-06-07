/* 
 * polymap.org
 * Copyright 2012, Polymap GmbH. All rights reserved.
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
package org.polymap.core.mapeditor.operations;

import org.eclipse.core.commands.operations.IUndoableOperation;

import org.polymap.core.operation.IOperationConcernFactory;
import org.polymap.core.operation.OperationInfo;
import org.polymap.core.project.operations.OpenMapOperation;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class OpenMapConcernFactory
        extends IOperationConcernFactory {

    public IUndoableOperation newInstance( IUndoableOperation op, OperationInfo info ) {
        if (op instanceof OpenMapOperation) {
            return new OpenMapConcern( (OpenMapOperation)op, info );
        }
        return null;
    }

}
