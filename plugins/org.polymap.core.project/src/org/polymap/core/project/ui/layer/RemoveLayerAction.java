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

package org.polymap.core.project.ui.layer;

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
import org.polymap.core.project.ProjectRepository;
import org.polymap.core.project.operations.RemoveLayerOperation;
import org.polymap.core.workbench.PolymapWorkbench;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class RemoveLayerAction
        extends ActionDelegate
        implements IObjectActionDelegate {

    private static Log log = LogFactory.getLog( RemoveLayerAction.class );

    private ILayer                  selectedLayer;
    
    
    public RemoveLayerAction() {
        super();
    }


    public void setActivePart( IAction action, IWorkbenchPart targetPart ) {
    }


    public void runWithEvent( IAction action, Event event ) {
        try {
            RemoveLayerOperation op = ProjectRepository.instance().newOperation( RemoveLayerOperation.class );
            op.init( selectedLayer );
            OperationSupport.instance().execute( op, true, true );
        }
        catch (ExecutionException e) {
            PolymapWorkbench.handleError( ProjectPlugin.PLUGIN_ID, this, Messages.get( "operationFailed"), e);
        }
    }


    public void selectionChanged( IAction action, ISelection sel ) {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (sel instanceof IStructuredSelection) {
            Object elm = ((IStructuredSelection)sel).getFirstElement();
            selectedLayer = (elm != null && elm instanceof ILayer)
                    ? (ILayer)elm : null;

            if (selectedLayer != null) {
                // check ACL permission
                boolean hasPermission = selectedLayer instanceof ACL
                        ? ACLUtils.checkPermission( (ACL)selectedLayer, AclPermission.DELETE, false )
                        : true;
                action.setEnabled( hasPermission );
            }
            else {
                action.setEnabled( false );
            }
        }
        else {
            selectedLayer = null;
        }
    }

}
