/* 
 * polymap.org
 * Copyright 2009, 2011 Polymap GmbH. All rights reserved.
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
package org.polymap.core.project.ui.dnd;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.polymap.core.operation.OperationSupport;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.ProjectPlugin;
import org.polymap.core.project.Messages;
import org.polymap.core.qi4j.event.AbstractModelChangeOperation;
import org.polymap.core.workbench.PolymapWorkbench;

import net.refractions.udig.ui.IDropAction;
import net.refractions.udig.ui.ViewerDropLocation;

/**
 * This drop action handles {@link ILayer} objects dropped into an
 * view in order to change the Z-priority of the dropped layer.
 * 
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @since 3.1
 */
public class LayerOrderDropAction
        extends IDropAction {

    private static Log log = LogFactory.getLog( LayerOrderDropAction.class );


    public boolean accept() {
        Object data = getData();
        Object dest = getDestination();
        ViewerDropLocation location = getViewerLocation();
        log.debug( "Drop accept(): data=" + data + ", dest=" + dest + ", location=" + location );
        
        // check dest
        if (! (dest instanceof ILayer)) {
            return false;
        }
        
        // check data
        if (data.getClass().isArray()) {
            Object[] objects = ((Object[])data);
            for (Object object : objects) {
                if (accept( object )) {
                    return true;
                }
            }
            return false;
        }
        else {
            return accept( data );
        }
    }

    
    protected boolean accept( Object data ) {
        if (data instanceof ILayer) {
            ILayer layer = (ILayer)data;
            return true;
        }
        else {
            return false;
        }
    }


    public void perform( IProgressMonitor monitor ) {
        Object data = getData();
        Object dest = getDestination();
        ViewerDropLocation location = getViewerLocation();
        log.debug( "Drop perform(): data=" + data + ", dest=" + dest + ", location=" + location );
        
        ILayer destLayer = (ILayer)dest;
        
        int order = -1;
        switch (location) {
            case AFTER: {
                order = destLayer.getOrderKey() - 1;
                break;
            }
            case ON:
            case BEFORE: {
                order = destLayer.getOrderKey() + 1;
                break;
            }
            case NONE: {
                log.info( "no drop location, skipping..." );
                return;
            }
        }

        if (data.getClass().isArray()) {
            Object[] array = (Object[])data;
            for (Object object : array) {
                if (accept( object )) {
                    perform( (ILayer)object, order, monitor );
                }
            }
        }
        else {
            perform( (ILayer)data, order, monitor );
        }

    }
    
    
    protected void perform( final ILayer layer, final int order, IProgressMonitor _monitor ) {
        
        IUndoableOperation op = new AbstractModelChangeOperation( 
                Messages.get( "LayerOrderDropAction_operation", layer.getLabel() ) ) {
            
            protected IStatus doExecute( IProgressMonitor monitor, IAdaptable info )
            throws Exception {
                // shift up layers
                for (ILayer cursor : layer.getMap().getLayers()) {
                    if (cursor.getOrderKey() == order) {
                        cursor.setOrderKey( cursor.getOrderKey() + 1 );
                    }
                }
                //
                layer.setOrderKey( order );
                return Status.OK_STATUS;
            }

            public boolean canUndo() {
                return true;
            }
            
        };
            
        try {
            OperationSupport.instance().execute( op, true, false );
        }
        catch (ExecutionException e) {
            PolymapWorkbench.handleError( ProjectPlugin.PLUGIN_ID, this, e.getLocalizedMessage(), e );
        }
    }

}
