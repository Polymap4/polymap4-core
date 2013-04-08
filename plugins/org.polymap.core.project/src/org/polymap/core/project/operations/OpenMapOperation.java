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

import java.util.Collection;

import net.refractions.udig.catalog.IGeoResource;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;

import org.eclipse.ui.IWorkbenchPage;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.polymap.core.operation.IOperationConcernFactory;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.IMap;
import org.polymap.core.project.LayerStatus;
import org.polymap.core.project.Messages;

/**
 * Opens the given {@link IMap} in the Workbench. Other plugins may hook up via
 * {@link IOperationConcernFactory} in order to perform their specific tasks to
 * "open" the map.
 * 
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @since 3.1
 */
public class OpenMapOperation
        extends AbstractOperation
        implements IUndoableOperation {
    
    private static Log log = LogFactory.getLog( OpenMapOperation.class );

    private IMap                map;
    
    private IWorkbenchPage      page;
    

    public OpenMapOperation( IMap map, IWorkbenchPage page ) {
        super( Messages.get( "OpenMapOperation_titlePrefix" ) + map.getLabel() );
        this.map = map;
        this.page = page;
    }
    
    public IMap getMap() {
        return map;
    }
    
    public IWorkbenchPage getPage() {
        return page;
    }


    public IStatus execute( final IProgressMonitor monitor, IAdaptable info )
            throws ExecutionException {
        Display display = (Display)info.getAdapter( Display.class );
        
        // set map extent
        try {
            monitor.subTask( Messages.get( "OpenMapOperation_calcLayersBounds" ) );
            final ReferencedEnvelope bbox = map.getMaxExtent() == null
                    ? calcLayersBounds( map.getLayers(), map.getCRS(), monitor )
                    : map.getMaxExtent();
                
            if (map.getMaxExtent() == null && bbox != null) {
                log.info( "### No map max extent -> using calculated values: " + bbox );
                map.setMaxExtent( bbox );
            }
            if (bbox == null && !map.getLayers().isEmpty()) {
                display.syncExec( new Runnable() {
                    public void run() {
                        MessageBox box = new MessageBox( page.getWorkbenchWindow().getShell() );
                        box.setText( Messages.get( "OpenMapOperation_bboxErrorText" ) );
                        box.setMessage( Messages.get( "OpenMapOperation_bboxErrorMsg" ) );
                        box.open();
                    }
                });
            }
            else {
                map.setVisible( true );
            }
            return Status.OK_STATUS;
        }
        catch (Exception e) {
            throw new ExecutionException( e.getLocalizedMessage(), e );
        }
    }


    public boolean canUndo() {
        return false;
    }

    
    public IStatus undo( IProgressMonitor monitor, IAdaptable info ) {
        throw new RuntimeException( "not yet implemented." ); //$NON-NLS-1$
    }

    
    public boolean canRedo() {
        return false;
    }

    
    public IStatus redo( IProgressMonitor monitor, IAdaptable info )
            throws ExecutionException {
        throw new RuntimeException( "not yet implemented." ); //$NON-NLS-1$
    }


    private ReferencedEnvelope calcLayersBounds( Collection<ILayer> layers, CoordinateReferenceSystem crs,
            IProgressMonitor monitor )
            throws Exception {
        log.debug( "### mapCRS: " + crs ); //$NON-NLS-1$

        ReferencedEnvelope result = null; // new ReferencedEnvelope( crs );
        for (ILayer layer : layers) {
            try {
                IGeoResource res = layer.getGeoResource();
                if (res == null) {
                    continue;
                }
                ReferencedEnvelope bbox = SetLayerBoundsOperation
                        .obtainBoundsFromResources( layer, crs, monitor );
                if (!bbox.getCoordinateReferenceSystem().equals( crs )) {
                    bbox = bbox.transform( crs, true );
                }
                log.debug( "layer: " + layer + ", bbox= " + bbox ); //$NON-NLS-1$ //$NON-NLS-2$

                if (result == null) {
                    result = bbox;
                } else {
                    result.expandToInclude( bbox );
                }
                log.debug( "result: bbox=  " + result ); //$NON-NLS-1$
            }
            catch (Exception e) {
                // XXX mark layers!?
                log.debug( "", e ); //$NON-NLS-1$
                log.warn( "skipping layer: " + layer.getLabel() + " (" + e.toString(), e ); //$NON-NLS-1$ //$NON-NLS-2$
                layer.setLayerStatus( new LayerStatus( Status.WARNING, LayerStatus.UNSPECIFIED, 
                        Messages.get( "LayerStatus_noCrs" ), e ) ); //$NON-NLS-1$
            }
        }
        return result != null ? result : ReferencedEnvelope.EVERYTHING.transform( crs, true );
    }

}
