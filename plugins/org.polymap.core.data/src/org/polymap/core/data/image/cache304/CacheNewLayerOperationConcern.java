/* 
 * polymap.org
 * Copyright 2011, Polymap GmbH. All rights reserved.
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
package org.polymap.core.data.image.cache304;

import org.apache.commons.lang.ArrayUtils;
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
import org.polymap.core.data.Messages;
import org.polymap.core.operation.IOperationConcernFactory;
import org.polymap.core.operation.OperationConcernAdapter;
import org.polymap.core.operation.OperationInfo;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.PipelineProcessorConfiguration;
import org.polymap.core.project.operations.NewLayerOperation;
import org.polymap.core.workbench.PolymapWorkbench;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class CacheNewLayerOperationConcern
        extends IOperationConcernFactory {

    private static Log log = LogFactory.getLog( CacheNewLayerOperationConcern.class );


    public CacheNewLayerOperationConcern() {
    }


    public IUndoableOperation newInstance( final IUndoableOperation op, final OperationInfo info ) {
        if (op instanceof NewLayerOperation) {
            
            return new OperationConcernAdapter() {
                
                public IStatus execute( IProgressMonitor monitor, IAdaptable _info )
                throws ExecutionException {
                    IStatus result = info.next().execute( monitor, _info );
                    
                    Display display = (Display)_info.getAdapter( Display.class );
                    display.syncExec( new Runnable() {
                        public void run() {
                            boolean yes = MessageDialog.openQuestion( 
                                    PolymapWorkbench.getShellToParentOn(),
                                    Messages.get( "CacheNewLayerConcern_title" ),
                                    Messages.get( "CacheNewLayerConcern_msg" ) );
                            if (yes) {
                                ILayer layer = ((NewLayerOperation)op).getNewLayer();
                                try {
                                    PipelineProcessorConfiguration[] configs = layer.getProcessorConfigs();

                                    // new config
                                    PipelineProcessorConfiguration newConfig = new PipelineProcessorConfiguration( 
                                            ImageCacheProcessor.class.getName(), "Image-TileCache" );
                                    configs = (PipelineProcessorConfiguration[])ArrayUtils.add( configs, newConfig );

                                    layer.setProcessorConfigs( configs );
                                }
                                catch (Exception e) {
                                    PolymapWorkbench.handleError( DataPlugin.PLUGIN_ID, this, e.getLocalizedMessage(), e );
                                }
                            }
                        }
                    });
                    return result;
                }
                
                protected OperationInfo getInfo() {
                    return info;
                }
            };
        }
        return null;
    }
    
}
