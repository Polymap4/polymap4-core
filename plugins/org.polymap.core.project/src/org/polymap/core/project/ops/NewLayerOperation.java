/* 
 * polymap.org
 * Copyright (C) 2009-2013, Polymap GmbH. All rights reserved.
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
package org.polymap.core.project.ops;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.polymap.core.project.ILayer;
import org.polymap.core.project.IMap;
import org.polymap.core.runtime.config.Config2;
import org.polymap.core.runtime.config.Immutable;
import org.polymap.core.runtime.config.Mandatory;

import org.polymap.model2.runtime.UnitOfWork;
import org.polymap.model2.runtime.ValueInitializer;

/**
 * Creates a new {@link ILayer} inside the given {@link #uow UnitOfWork}.
 * <p/>
 * This might open dialogs and must not be executed with progress dialog.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class NewLayerOperation
        extends TwoPhaseCommitOperation {

    private static Log log = LogFactory.getLog( NewLayerOperation.class );

    /**
     * Inbound: The UnitOfWork to work with.
     */
    @Mandatory
    @Immutable
    public Config2<NewLayerOperation,UnitOfWork>    uow;
    
    @Mandatory
    @Immutable
    public Config2<NewLayerOperation,IMap>          map;
    
    @Mandatory
    @Immutable
    public Config2<NewLayerOperation,ValueInitializer<ILayer>>  initializer;
    
    /** Outbound: Newly created layer */
    @Immutable
    public Config2<NewLayerOperation,ILayer>        layer;


    public NewLayerOperation() {
        super( "New layer" );
    }


    @Override
    public IStatus doWithCommit( IProgressMonitor monitor, IAdaptable info ) throws Exception {
        assert map.get().belongsTo( uow.get() );
        
        monitor.beginTask( getLabel(), 5 );
        register( uow.get() );

        // create entity
        ILayer newLayer = uow.get().createEntity( ILayer.class, null, (ILayer proto) -> {
            initializer.get().initialize( proto );
            
            assert proto.label.get() != null;
            assert proto.resourceIdentifier.get() != null;

            proto.parentMap.set( map.get() );
            proto.orderKey.set( proto.maxOrderKey() + 1  );
            return proto;
        });
        layer.set( newLayer );
        
        map.get().layers.add( layer.get() );
        // force commit (https://github.com/Polymap4/polymap4-model/issues/6)
        map.get().label.set( map.get().label.get() );

            
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

}
