/* 
 * polymap.org
 * Copyright 2012, Polymap GmbH. All rights reserved.
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
package org.polymap.core.project.operations;

import org.geotools.referencing.CRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import net.refractions.udig.catalog.IService;
import net.refractions.udig.catalog.IServiceInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.widgets.Display;

import org.eclipse.jface.dialogs.MessageDialog;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

import org.polymap.core.operation.IOperationConcernFactory;
import org.polymap.core.operation.OperationConcernAdapter;
import org.polymap.core.operation.OperationInfo;
import org.polymap.core.project.Messages;
import org.polymap.core.project.ProjectRepository;
import org.polymap.core.workbench.PolymapWorkbench;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class CatalogOperationConcern
        extends IOperationConcernFactory {

    private static Log log = LogFactory.getLog( CatalogOperationConcern.class );

    public IUndoableOperation newInstance( IUndoableOperation op, final OperationInfo info ) {
        return null;
    }

    /**
     * XXX Not yet used. The CatalogComposite does use operation to add
     * new entries - but this wrongly is used during startup.
     *
     * @param op
     * @param info
     * @return
     */
    public IUndoableOperation newInstance2( IUndoableOperation op, final OperationInfo info ) {
        log.info( "Operation: " + op.getClass() + ", info: " + info );
        
        // XXX this check is bad, revise
        if (op.getClass().getName().endsWith( "CatalogAddOperation" )) {
            
            return new OperationConcernAdapter() {

                public IStatus execute( final IProgressMonitor monitor, IAdaptable _info )
                throws ExecutionException {
                    IStatus result = info.next().execute( monitor, _info );
                    
                    Display display = (Display)_info.getAdapter( Display.class );
                    display.syncExec( new Runnable() {
                        public void run() {
                            boolean yes = MessageDialog.openQuestion( 
                                    PolymapWorkbench.getShellToParentOn(),
                                    Messages.get( "CatalogOperationConcern_title" ),
                                    Messages.get( "CatalogOperationConcern_msg" ) );
                            if (yes) {
                                try {
                                    ProjectRepository repo = ProjectRepository.instance();
                                    NewMapOperation newMap = repo.newOperation( NewMapOperation.class );
                                    
                                    IService service = (IService)((IAdaptable)info.next()).getAdapter( IService.class );
                                    IServiceInfo serviceInfo = service.getInfo( monitor );
                                    String mapName = service.getTitle();
                                    //DataStore ds = service.resolve( DataStore.class, monitor );
                                    CoordinateReferenceSystem crs = CRS.decode( "EPSG:4326" );
                                    newMap.init( repo.getRootMap(), mapName, crs );
                                    newMap.execute( monitor, info );
                                }
                                catch (Exception e) {
                                    throw new RuntimeException( e );
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
