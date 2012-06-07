/* 
 * polymap.org
 * Copyright 2009-2012, Polymap GmbH. All rigths reserved.
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
package org.polymap.core.project.ui.project;

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

import org.polymap.core.operation.OperationSupport;
import org.polymap.core.project.IMap;
import org.polymap.core.project.Messages;
import org.polymap.core.project.ProjectPlugin;
import org.polymap.core.project.operations.OpenMapOperation;
import org.polymap.core.workbench.PolymapWorkbench;

/**
 * Triggers the {@link OpenMapOperation}.
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @since 3.1
 */
public class OpenMapAction
        extends ActionDelegate
        implements IObjectActionDelegate {

    private static Log log = LogFactory.getLog( OpenMapAction.class );

    public static final String      ID = "org.polymap.core.project.openMapAction";
    
    private IMap                    selectedMap;
    
    
    public void runWithEvent( IAction action, Event ev ) {
        try {
            IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
            OpenMapOperation op = new OpenMapOperation( selectedMap, window.getActivePage() );
            OperationSupport.instance().execute( op, true, true );
        }
        catch (ExecutionException e) {
            PolymapWorkbench.handleError( ProjectPlugin.PLUGIN_ID, this, Messages.get( "operationFailed" ), e );
        }
    }


    public void selectionChanged( IAction action, ISelection sel ) {
        if (sel instanceof IStructuredSelection) {
            Object elm = ((IStructuredSelection)sel).getFirstElement();
            selectedMap = (elm != null && elm instanceof IMap) ? (IMap)elm : null;
                    
            action.setEnabled( selectedMap != null && !selectedMap.getLayers().isEmpty() );
        }
        else {
            selectedMap = null;
        }
    }


    public void setActivePart( IAction action, IWorkbenchPart targetPart ) {
    }

}
