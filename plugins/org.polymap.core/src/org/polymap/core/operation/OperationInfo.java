package org.polymap.core.operation;

import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IAdaptable;

/**
 *
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 */
public interface OperationInfo
        extends IAdaptable {

    IUndoableOperation next();
    
}