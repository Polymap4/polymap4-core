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
package org.polymap.rhei.internal.form;

import org.geotools.data.FeatureStore;
import org.opengis.feature.Feature;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.widgets.Display;

import org.eclipse.jface.dialogs.MessageDialog;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

import org.polymap.core.data.DataPlugin;
import org.polymap.core.data.operations.NewFeatureOperation;
import org.polymap.core.data.ui.featureselection.LayerSelectableOperationConcern;
import org.polymap.core.operation.IOperationConcernFactory;
import org.polymap.core.operation.OperationConcernAdapter;
import org.polymap.core.operation.OperationInfo;
import org.polymap.core.workbench.PolymapWorkbench;

import org.polymap.rhei.form.FormEditor;


/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class NewFeatureOperationConcern
        extends IOperationConcernFactory {

    private static Log log = LogFactory.getLog( LayerSelectableOperationConcern.class );


    public IUndoableOperation newInstance( final IUndoableOperation op, final OperationInfo info ) {
        if (op instanceof NewFeatureOperation) {

            return new OperationConcernAdapter() {

                public IStatus execute( IProgressMonitor monitor, IAdaptable _info )
                throws ExecutionException {
                    IStatus result = info.next().execute( monitor, _info );

                    Display display = (Display)_info.getAdapter( Display.class );
                    display.asyncExec( new Runnable() {
                        public void run() {
                            try {
                                if (MessageDialog.openConfirm( 
                                        PolymapWorkbench.getShellToParentOn(), "Neues Objekt",
                                        "Wollen Sie das Formular zum Bearbeiten des neuen Objektes öffnen?" )) {
                                    
                                    Feature feature = ((NewFeatureOperation)op).getCreatedFeature();
                                    FeatureStore fs = ((NewFeatureOperation)op).getFeatureStore();
                                    FormEditor.open( fs, feature, null );
                                }
                            }
                            catch (Exception e) {
                                PolymapWorkbench.handleError( DataPlugin.PLUGIN_ID, this, "Das Formular zum Bearbeiten des neuen Objektes konnte nicht geöffnet werden.", e );
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
