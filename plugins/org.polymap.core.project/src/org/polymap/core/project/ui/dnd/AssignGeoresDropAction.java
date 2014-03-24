/* 
 * polymap.org
 * Copyright (C) 2014, Falko Bräutigam. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.core.project.ui.dnd;

import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.ui.IDropAction;
import net.refractions.udig.ui.ViewerDropLocation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.operation.OperationSupport;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.ProjectPlugin;
import org.polymap.core.project.operations.SetPropertyOperation;
import org.polymap.core.workbench.PolymapWorkbench;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class AssignGeoresDropAction
        extends IDropAction {

    private static Log log = LogFactory.getLog( AssignGeoresDropAction.class );


    @Override
    public boolean accept() {
        ViewerDropLocation location = getViewerLocation();
        return location == ViewerDropLocation.ON;
    }


    @Override
    public void perform( IProgressMonitor monitor ) {
        try {
            IGeoResource geores = (IGeoResource)getData();
            ILayer layer = (ILayer)getDestination();

            SetPropertyOperation op = new SetPropertyOperation();
            op.init( ILayer.class, layer, ILayer.PROP_GEORESID, geores );
            OperationSupport.instance().execute( op, true, false );
        }
        catch (ExecutionException e) {
            PolymapWorkbench.handleError( ProjectPlugin.PLUGIN_ID, this, "", e );
        }
    }
    
}
