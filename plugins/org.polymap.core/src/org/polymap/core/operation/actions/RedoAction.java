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
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 * $Id$
 */
package org.polymap.core.operation.actions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import org.eclipse.core.commands.operations.IOperationHistoryListener;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.commands.operations.OperationHistoryEvent;

import org.polymap.core.CorePlugin;
import org.polymap.core.operation.OperationSupport;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.workbench.PolymapWorkbench;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class RedoAction
        implements IWorkbenchWindowActionDelegate, IOperationHistoryListener {

    private static Log log = LogFactory.getLog( RedoAction.class );

    private boolean                 enabled = false;
    
    /** The action we are working for. Can I just store this and use in modelChanged() ? */
    private IAction                 action;
    
    /** The original tooltip text of the {@link #action}. */
    private String                  actionTooltip;
    
    private String                  actionText;
    
    private OperationSupport        operationSupport;

    
    public void init( IWorkbenchWindow window ) {
        operationSupport = OperationSupport.instance();
        operationSupport.addOperationHistoryListener( this );
    }

    
    public void dispose() {
        operationSupport.removeOperationHistoryListener( this );
        
    }


    public void historyNotification( OperationHistoryEvent ev ) {
        //log.info( "History changed: ev= " + ev );
        Polymap.getSessionDisplay().asyncExec( new Runnable() {
            public void run() {
                IUndoableOperation op = operationSupport.getRedoOperation();
                enabled = op != null && op.canRedo();
                if (action != null) {
                    action.setEnabled( enabled );

                    action.setToolTipText( enabled
                            ? actionTooltip + " " + op.getLabel() 
                                    : actionTooltip );
                    action.setText( enabled
                            ? actionText + " " + op.getLabel() 
                                    : actionText );
                }
            }
        });
    }


    public void run( IAction _action ) {
        try {
            operationSupport.redo();
        }
        catch (Exception e) {
            PolymapWorkbench.handleError( CorePlugin.PLUGIN_ID, this, e.getLocalizedMessage(), e );
        }
    }

    
    public void selectionChanged( IAction _action, ISelection _selection ) {
        this.action = _action;
        if (actionTooltip == null) {
            actionTooltip = action.getToolTipText(); 
            actionText = action.getText();
        }
        log.debug( "Selection changed. enabled= " + enabled + ", action= " + action );

        // check if we have a pending change
        if (action.isEnabled() != enabled) {
            action.setEnabled( enabled );
        }
    }
    
}
