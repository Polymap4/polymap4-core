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
package org.polymap.core.project.ui.layer;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.widgets.Event;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionDelegate;

import org.eclipse.core.commands.ExecutionException;

import org.polymap.core.model.security.ACL;
import org.polymap.core.model.security.ACLUtils;
import org.polymap.core.model.security.AclPermission;
import org.polymap.core.operation.OperationSupport;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.Messages;
import org.polymap.core.project.ProjectPlugin;
import org.polymap.core.project.operations.RemoveLayerOperation;
import org.polymap.core.workbench.PolymapWorkbench;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @since 3.0
 */
public class RemoveLayerAction
        extends ActionDelegate
        implements IObjectActionDelegate {

    private static Log log = LogFactory.getLog( RemoveLayerAction.class );

    private List<ILayer>        selectedLayers = new ArrayList();
    
    
    public RemoveLayerAction() {
        super();
    }


    public void setActivePart( IAction action, IWorkbenchPart targetPart ) {
    }


    public void runWithEvent( IAction action, Event event ) {
        try {
            RemoveLayerOperation op = new RemoveLayerOperation();
            op.init( selectedLayers );
            OperationSupport.instance().execute( op, true, true );
        }
        catch (ExecutionException e) {
            PolymapWorkbench.handleError( ProjectPlugin.PLUGIN_ID, this, Messages.get( "operationFailed"), e);
        }
    }


    public void selectionChanged( IAction action, ISelection sel ) {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        
        if (sel instanceof IStructuredSelection) {
                
            selectedLayers.clear();
            action.setEnabled( false );

            for (Object elm : ((IStructuredSelection)sel).toArray()) {
                if (elm != null && elm instanceof ILayer) {
                    ILayer layer = (ILayer)elm;
                    // check ACL permission
                    boolean hasPermission = layer instanceof ACL
                            ? ACLUtils.checkPermission( layer, AclPermission.DELETE, false )
                            : true;
                    if (hasPermission) {
                        selectedLayers.add( layer );
                        action.setEnabled( true );
                        continue;
                    }
                }
                
                // if not ILayer or no permission -> disable
                action.setEnabled( false );
                selectedLayers.clear();
                break;
            }
        }
    }

}
