/* 
 * polymap.org
 * Copyright 2011, Falko Bräutigam, and other contributors as
 * indicated by the @authors tag. All rights reserved.
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
package org.polymap.core.data.ui.featureTable;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.widgets.Display;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

import org.polymap.core.data.DataPlugin;
import org.polymap.core.operation.IOperationConcernFactory;
import org.polymap.core.operation.OperationConcernAdapter;
import org.polymap.core.operation.OperationInfo;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.ui.layer.LayerEditableOperation;
import org.polymap.core.project.ui.layer.LayerSelectableOperation;
import org.polymap.core.workbench.PolymapWorkbench;

/**
 * Hooks on the {@link LayerSelectableOperation} and opens/closes
 * {@link GeoSelectionView} when the operation is executed.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class LayerSelectableOperationConcern
        extends IOperationConcernFactory {

    private static Log log = LogFactory.getLog( LayerSelectableOperationConcern.class );


    public IUndoableOperation newInstance( final IUndoableOperation op, final OperationInfo info ) {
        final List<ILayer> layers = 
                op instanceof LayerSelectableOperation ? ((LayerSelectableOperation)op).getLayers() :
                op instanceof LayerEditableOperation ? ((LayerEditableOperation)op).getLayers() : null;
        
        if (layers != null) {
            return new OperationConcernAdapter() {
                
                public IStatus execute( IProgressMonitor monitor, IAdaptable _info )
                throws ExecutionException {
                    IStatus result = info.next().execute( monitor, _info );
                    
                    // open GeoSelectionView
                    Display display = (Display)_info.getAdapter( Display.class );
                    display.asyncExec( new Runnable() {
                        public void run() {
                            try {
                                for (ILayer layer : layers) {                                
                                    if (op instanceof LayerSelectableOperation && layer.isSelectable()
                                            || op instanceof LayerEditableOperation && layer.isEditable()) {
                                        GeoSelectionView.open( layer, true );
                                    }
                                    else {
                                        GeoSelectionView.close( layer );
                                    }
                                }
                            }
                            catch (Exception e) {
                                PolymapWorkbench.handleError( DataPlugin.PLUGIN_ID, this, e.getMessage(), e );
                            }
                        }
                    });
                    return result;
                }

                public IStatus redo( IProgressMonitor monitor, IAdaptable _info )
                        throws ExecutionException {
                    log.info( "Operation : " + op.getClass().getName() );
                    return info.next().redo( monitor, info );
                }

                public IStatus undo( IProgressMonitor monitor, IAdaptable _info )
                        throws ExecutionException {
                    log.info( "Operation : " + op.getClass().getName() );
                    return info.next().undo( monitor, info );
                }

                protected OperationInfo getInfo() {
                    return info;
                }
                
            };
        }
        return null;
    }
    
}
