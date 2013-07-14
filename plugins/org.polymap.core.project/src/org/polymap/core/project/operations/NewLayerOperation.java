/* 
 * polymap.org
 * Copyright 2009-2013, Polymap GmbH. All rights reserved.
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

import java.security.Principal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.opengis.geometry.BoundingBox;

import org.geotools.geometry.jts.ReferencedEnvelope;

import net.refractions.udig.catalog.IGeoResource;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.ui.PlatformUI;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.polymap.core.model.security.AclPermission;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.IMap;
import org.polymap.core.project.Messages;
import org.polymap.core.project.ProjectRepository;
import org.polymap.core.qi4j.event.AbstractModelChangeOperation;
import org.polymap.core.runtime.Polymap;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 * @since 3.0
 */
public class NewLayerOperation
        extends AbstractModelChangeOperation
        implements IUndoableOperation {

    private static Log log = LogFactory.getLog( NewLayerOperation.class );

    private IMap                map;

    private IGeoResource        geores;

    /** Newly created layer */
    private ILayer              layer;


    public NewLayerOperation() {
        super( "[undefined]" );
    }


    public void init( IMap _map, IGeoResource _geores ) {
        this.map = _map;
        this.geores = _geores;
        setLabel( Messages.get( "NewLayerOperation_title", geores.getTitle() ) );
    }


    public void dispose() {
        super.dispose();
    }

    
    public IGeoResource getGeores() {
        return geores;
    }


    public ILayer getNewLayer() {
        return layer;
    }


    public IStatus doExecute( final IProgressMonitor monitor, IAdaptable info ) throws ExecutionException {
        try {
            monitor.beginTask( getLabel(), 5 );
            ProjectRepository repo = ProjectRepository.instance();
            layer = repo.newEntity( ILayer.class, null );

            // default ACL
            for (Principal principal : Polymap.instance().getPrincipals()) {
                layer.addPermission( principal.getName(), AclPermission.ALL );
            }

            layer.setLabel( geores.getTitle() );
            layer.setOrderKey( 100 );
            layer.setOpacity( 100 );
            layer.setGeoResource( geores );
            layer.setVisible( true );

            map.addLayer( layer );

            // find highest order
            int highestOrder = 100;
            for (ILayer cursor : layer.getMap().getLayers()) {
                highestOrder = Math.max( highestOrder, cursor.getOrderKey() );
            }
            layer.setOrderKey( highestOrder + 1 );

            // Shapefile CRS in checked in ShapeCRSOperationConcern

            // transformed layerBBox
            ReferencedEnvelope layerBBox = SetLayerBoundsOperation.obtainBoundsFromResources( layer, map.getCRS(), monitor );
            if (layerBBox != null && !layerBBox.isNull() && layerBBox.getMaxX() < Double.POSITIVE_INFINITY) {
                monitor.subTask( Messages.get( "NewLayerOperation_transforming" ) );
                if (!layerBBox.getCoordinateReferenceSystem().equals( map.getCRS() )) {
                    try {
                        layerBBox = layerBBox.transform( map.getCRS(), true );
                    }
                    catch (Throwable e) {
                        log.warn( "", e );
                    }
                }
                log.debug( "transformed: " + layerBBox );
                monitor.worked( 1 );
            }

            // no max extent -> set 
            monitor.subTask( Messages.get( "NewLayerOperation_checkingMaxExtent" ) );
            if (map.getMaxExtent() == null) {
                if (layerBBox != null && !layerBBox.isNull() && !layerBBox.isEmpty()) {
                    log.info( "### Map: maxExtent= " + layerBBox );
                    map.setMaxExtent( layerBBox );
                    // XXX set map status
                }
                else {
                    Display display = (Display)info.getAdapter( Display.class );
                    display.syncExec( new Runnable() {
                        public void run() {
                            Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
                            MessageBox box = new MessageBox( shell, SWT.OK );
                            box.setText( "No layer bounds." );
                            box.setMessage( "Layer has no bounding box.\n Max extent of the map could not be set.\nThis may lead to unspecified map behaviour." );
                            box.open();
                        }
                    });
                }
            }
            // check if max extent contains layer
            else {
                try {
                    if (!layerBBox.isNull() && layerBBox.getMaxX() < Double.POSITIVE_INFINITY
                            && !map.getMaxExtent().contains( (BoundingBox)layerBBox )) {
                        ReferencedEnvelope bbox = new ReferencedEnvelope( layerBBox );
                        bbox.expandToInclude( map.getMaxExtent() );
                        final ReferencedEnvelope newMaxExtent = bbox;

                        Display display = (Display)info.getAdapter( Display.class );
                        display.syncExec( new Runnable() {
                            public void run() {
                                Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
                                MessageBox box = new MessageBox( shell, SWT.YES | SWT.NO );
                                box.setText( Messages.get( "NewLayerOperation_BBoxDialog_title" ) );
                                box.setMessage( Messages.get( "NewLayerOperation_BBoxDialog_msg" ) );
                                int answer = box.open();
                                if (answer == SWT.YES) {
                                    map.setMaxExtent( newMaxExtent );
                                }
                            }
                        });
                    }
                }
                catch (Exception e) {
                    log.warn( e.getLocalizedMessage(), e );
                }
            }
            monitor.worked( 1 );
        }
        catch (Throwable e) {
            throw new ExecutionException( e.getMessage(), e );
        }
        return Status.OK_STATUS;
    }


    @Override
    public boolean canUndo() {
        return false;
    }
    
    @Override
    public boolean canRedo() {
        return false;
    }


}
