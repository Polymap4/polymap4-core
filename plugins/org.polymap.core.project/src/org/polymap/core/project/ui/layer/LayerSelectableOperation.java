/* 
 * polymap.org
 * Copyright 2011, Falo Bräutigam. All rights reserved.
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

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.polymap.core.operation.JobMonitors;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.Messages;

/**
 * This operation is triggered by {@link LayerSelectableStatusAction} it puts the
 * given layer in "features selectable" mode.
 * <p>
 * This operation changes the layer state, which in turn triggers domain
 * listeners.
 * 
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @since 3.1
 */
public class LayerSelectableOperation
        extends AbstractOperation
        implements IUndoableOperation {
    
    private static Log log = LogFactory.getLog( LayerSelectableOperation.class );

    private List<ILayer>                layers;
    
    private boolean                     selectable;
    

    /**
     * 
     */
    public LayerSelectableOperation( List<ILayer> layers, boolean selectable ) {
        super( Messages.get( "LayerSelectableOperation_labelPrefix" ) + layers.iterator().next().getLabel() );
        this.layers = layers;
        this.selectable = selectable;
    }


    public List<ILayer> getLayers() {
        return layers;
    }


    public IStatus execute( IProgressMonitor monitor, IAdaptable info )
    throws ExecutionException {
        try {
//            Display display = Polymap.getSessionDisplay();
//            log.debug( "### Display: " + display );
//            OffThreadProgressMonitor monitor = new OffThreadProgressMonitor( _monitor );
//            JobMonitors.set( monitor );
            monitor.subTask( getLabel() );
            
            // do all work in the domain listeners
            for (ILayer layer : layers) {
                layer.setSelectable( selectable );
            }
            monitor.worked( 1 );
        }
        catch (Exception e) {
            throw new ExecutionException( "Failure...", e );
        }
        finally {
            JobMonitors.remove();
        }
        return Status.OK_STATUS;
    }


    public boolean canUndo() {
        return true;
    }

    public IStatus undo( IProgressMonitor monitor, IAdaptable info ) {
        for (ILayer layer : layers) {
            layer.setSelectable( !selectable );
        }
        return Status.OK_STATUS;
    }

    public boolean canRedo() {
        return true;
    }

    public IStatus redo( IProgressMonitor monitor, IAdaptable info )
    throws ExecutionException {
        for (ILayer layer : layers) {
            layer.setSelectable( selectable );
        }
        return Status.OK_STATUS;
    }

}
