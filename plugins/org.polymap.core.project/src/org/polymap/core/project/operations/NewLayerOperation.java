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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.polymap.core.project.ILayer;
import org.polymap.core.project.IMap;
import org.polymap.core.runtime.config.Config2;
import org.polymap.core.runtime.config.ConfigurationFactory;
import org.polymap.core.runtime.config.Immutable;
import org.polymap.core.runtime.config.Mandatory;

import org.polymap.model2.runtime.UnitOfWork;

/**
 * Creates a new {@link ILayer} inside the given {@link #uow UnitOfWork}.
 * <p/>
 * This might open dialogs and must not be executed with progress dialog.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class NewLayerOperation
        extends AbstractOperation
        implements IUndoableOperation {

    private static Log log = LogFactory.getLog( NewLayerOperation.class );

    /**
     * The UnitOfWork to work with. This UnitOfWork is committed/rolled back and
     * closed by this operation.
     */
    @Mandatory
    @Immutable
    public Config2<NewLayerOperation,UnitOfWork>    uow;
    
    @Mandatory
    @Immutable
    public Config2<NewLayerOperation,IMap>          map;
    
    @Mandatory
    @Immutable
    public Config2<NewLayerOperation,String>        label;
    
    @Mandatory
    @Immutable
    public Config2<NewLayerOperation,String>        resourceIdentifier;
    
    @Immutable
    public Config2<NewLayerOperation,String>        styleIdentifier;
    
    /** Newly created layer */
    @Immutable
    public Config2<NewLayerOperation,ILayer>        layer;


    public NewLayerOperation() {
        super( "New layer" );
        ConfigurationFactory.inject( this );
    }


    @Override
    public IStatus execute( IProgressMonitor monitor, IAdaptable info ) throws ExecutionException {
        try {
            monitor.beginTask( getLabel(), 5 );
            
            IMap localMap = uow.get().entity( map.get() );
            // create entity
            layer.set( uow.get().createEntity( ILayer.class, null, (ILayer proto) -> {
                proto.parentMap.set( localMap );
                proto.orderKey.set( proto.maxOrderKey() + 1  );
                proto.label.set( label.get() );
                proto.resourceIdentifier.set( resourceIdentifier.get() );
                styleIdentifier.ifPresent( id -> proto.styleIdentifier.set( id ) );
                return proto;
            }));

            localMap.layers.add( layer.get() );
            // force commit (https://github.com/Polymap4/polymap4-model/issues/6)
            localMap.label.set( localMap.label.get() );
                        
            uow.get().commit();
        }
        catch (Throwable e) {
            uow.get().rollback();
            throw new ExecutionException( e.getMessage(), e );
        }
        finally {
            uow.get().close();
        }
        
//        try {
//            monitor.beginTask( getLabel(), 5 );
//            ProjectRepository repo = ProjectRepository.instance();
//            layer = repo.newEntity( ILayer.class, null );
//
//            // default ACL
//            for (Principal principal : Polymap.instance().getPrincipals()) {
//                layer.addPermission( principal.getName(), AclPermission.ALL );
//            }
//
//            layer.setLabel( geores.getTitle() );
//            layer.setOrderKey( 100 );
//            layer.setOpacity( 100 );
//            layer.setGeoResource( geores );
//            layer.setVisible( true );
//
//            map.addLayer( layer );
//
//            // find highest order
//            int highestOrder = 100;
//            for (ILayer cursor : layer.getMap().getLayers()) {
//                highestOrder = Math.max( highestOrder, cursor.getOrderKey() );
//            }
//            layer.setOrderKey( highestOrder + 1 );
//
//            // Shapefile CRS in checked in ShapeCRSOperationConcern
//
//            // transformed layerBBox
//            ReferencedEnvelope layerBBox = SetLayerBoundsOperation.obtainBoundsFromResources( layer, map.getCRS(), monitor );
//            if (layerBBox != null && !layerBBox.isNull() && layerBBox.getMaxX() < Double.POSITIVE_INFINITY) {
//                monitor.subTask( Messages.get( "NewLayerOperation_transforming" ) );
//                if (!layerBBox.getCoordinateReferenceSystem().equals( map.getCRS() )) {
//                    try {
//                        layerBBox = layerBBox.transform( map.getCRS(), true );
//                    }
//                    catch (Throwable e) {
//                        log.warn( "", e );
//                    }
//                }
//                log.debug( "transformed: " + layerBBox );
//                monitor.worked( 1 );
//            }
//
//            // no max extent -> set 
//            monitor.subTask( Messages.get( "NewLayerOperation_checkingMaxExtent" ) );
//            if (map.get().maxExtent.get() == null) {
//                if (layerBBox != null && !layerBBox.isNull() && !layerBBox.isEmpty()) {
//                    log.info( "### Map: maxExtent= " + layerBBox );
//                    map.get().maxExtent.set( layerBBox );
//                    // XXX set map status
//                }
//                else {
//                    Display display = (Display)info.getAdapter( Display.class );
//                    display.syncExec( new Runnable() {
//                        public void run() {
//                            Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
//                            MessageBox box = new MessageBox( shell, SWT.OK );
//                            box.setText( "No layer bounds." );
//                            box.setMessage( "Layer has no bounding box.\n Max extent of the map could not be set.\nThis may lead to unspecified map behaviour." );
//                            box.open();
//                        }
//                    });
//                }
//            }
//            // check if max extent contains layer
//            else {
//                try {
//                    if (!layerBBox.isNull() && layerBBox.getMaxX() < Double.POSITIVE_INFINITY
//                            && !map.get().maxExtent.get().contains( (BoundingBox)layerBBox )) {
//                        ReferencedEnvelope bbox = new ReferencedEnvelope( layerBBox );
//                        bbox.expandToInclude( map.get().maxExtent.get() );
//                        final ReferencedEnvelope newMaxExtent = bbox;
//
//                        Display display = (Display)info.getAdapter( Display.class );
//                        display.syncExec( new Runnable() {
//                            public void run() {
//                                Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
//                                MessageBox box = new MessageBox( shell, SWT.YES | SWT.NO );
//                                box.setText( Messages.get( "NewLayerOperation_BBoxDialog_title" ) );
//                                box.setMessage( Messages.get( "NewLayerOperation_BBoxDialog_msg" ) );
//                                int answer = box.open();
//                                if (answer == SWT.YES) {
//                                    map.setMaxExtent( newMaxExtent );
//                                }
//                            }
//                        });
//                    }
//                }
//                catch (Exception e) {
//                    log.warn( e.getLocalizedMessage(), e );
//                }
//            }
//            monitor.worked( 1 );
//        }
//        catch (Throwable e) {
//            throw new ExecutionException( e.getMessage(), e );
//        }
        return Status.OK_STATUS;
    }


    @Override
    public IStatus redo( IProgressMonitor monitor, IAdaptable info ) throws ExecutionException {
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public IStatus undo( IProgressMonitor monitor, IAdaptable info ) throws ExecutionException {
        throw new RuntimeException( "not yet implemented." );
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
