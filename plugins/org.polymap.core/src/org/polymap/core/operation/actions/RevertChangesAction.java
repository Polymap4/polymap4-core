/* 
 * polymap.org
 * Copyright 2009-2013, Polymap GmbH. All rights reserved.
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
package org.polymap.core.operation.actions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import org.eclipse.core.commands.operations.IOperationHistoryListener;
import org.eclipse.core.commands.operations.OperationHistoryEvent;

import org.polymap.core.CorePlugin;
import org.polymap.core.operation.IOperationSaveListener;
import org.polymap.core.operation.OperationSupport;
import org.polymap.core.workbench.PolymapWorkbench;

/**
 * 
 * <p/>
 * Revert does not work with all {@link IOperationSaveListener} providers yet.
 * Therefore this action is disabled yet.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @since 3.0
 */
public class RevertChangesAction
        implements IWorkbenchWindowActionDelegate, IOperationHistoryListener {

    private static Log log = LogFactory.getLog( RevertChangesAction.class );

    /** The action we are working for. Can I just store this and use in modelChanged() ? */
    private IAction                 action;
    
    private OperationSupport        operationSupport;

    
    public void init( IWorkbenchWindow window ) {
        operationSupport = OperationSupport.instance();
        operationSupport.addOperationHistoryListener( this );
    }

    
    public void dispose() {
        operationSupport.removeOperationHistoryListener( this );
        
    }


    public void historyNotification( OperationHistoryEvent ev ) {
        if (action != null) {
            action.setToolTipText( "Operations: " + operationSupport.undoHistorySize() );
        }
    }


    public void run( IAction _action ) {
        try {
            operationSupport.revertChanges();
        }
        catch (Exception e) {
            PolymapWorkbench.handleError( CorePlugin.PLUGIN_ID, this, e.getLocalizedMessage(), e );
        }
    }

    
    public void selectionChanged( IAction _action, ISelection _selection ) {
        this.action = _action;
    }
    
}
