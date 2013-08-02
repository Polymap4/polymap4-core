/* 
 * polymap.org
 * Copyright (C) 2009-2013 Polymap GmbH. All rights reserved.
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
package org.polymap.core.data.operations;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.geotools.feature.FeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import org.eclipse.swt.SWT;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.polymap.core.project.IMap;
import org.polymap.core.project.operations.SetLayerBoundsOperation;

/**
 * Calculates the bounds of the given features and sets the extent of the given map.
 * The bounds will be reprojected into the crs provided.
 * <p/>
 * This works on the given {@link FeatureCollection} and uses its
 * {@link FeatureCollection#getBounds()} method.
 * 
 * @see SetLayerBoundsOperation
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @since 3.1
 */
public class ZoomFeatureBoundsOperation
        extends AbstractOperation
        implements IUndoableOperation {
    
    private static Log log = LogFactory.getLog( ZoomFeatureBoundsOperation.class );

    private FeatureCollection           features;
    
    private IMap                        map;
    
    private CoordinateReferenceSystem   crs;

    public ReferencedEnvelope           result;
    
    private ReferencedEnvelope          oldExtent;


    /**
     * 
     * @param crs the desired CRS for the returned envelope.
     */
    public ZoomFeatureBoundsOperation( FeatureCollection features, IMap map, CoordinateReferenceSystem crs ) {
        super( "Hüllrechteck für Objekte" );
        this.features = features;
        this.map = map;
        this.crs = crs;
    }


    public IStatus execute( IProgressMonitor monitor, IAdaptable info )
    throws ExecutionException {
        monitor.beginTask( getLabel(), features.size() );

        // calculate bounds
        result = features.getBounds();

        // transform
        if (result != null && !result.isNull()) {
            if (crs != null) {
                try {
                    monitor.subTask( "Transforming bounds" );
                    result = result.transform( crs, true );
                    monitor.worked( 1 );
                } 
                catch (Exception fe) {
                    throw new ExecutionException( "failure to transform layer bounds", fe );
                }
            }
        } 
        
        // set map extent
        monitor.subTask( "Setting map extent" );
        
        final AtomicInteger dialogResult = new AtomicInteger( SWT.YES );
        // FIXME Check result and ask the user if suspicious
//        Polymap.getSessionDisplay().syncExec( new Runnable() {
//            public void run() {
//                MessageBox mbox = new MessageBox( 
//                        PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
//                        SWT.YES | SWT.NO | SWT.ICON_INFORMATION | SWT.APPLICATION_MODAL );
//                mbox.setMessage( "X : " + result.getMinX() + " - " + result.getMaxX() +
//                        "\nY : " + result.getMinY() + " - " + result.getMaxY() );
//                mbox.setText( getLabel() );
//                dialogResult.set( mbox.open() );
//            }
//        });
        
        if (dialogResult.get() == SWT.YES) {
            oldExtent = map.getExtent();
            
            map.setExtent( result );
            monitor.done();
            return Status.OK_STATUS;
        }
        else {
            return Status.CANCEL_STATUS;
        }
    }


    public IStatus undo( IProgressMonitor monitor, IAdaptable info ) {
        if (oldExtent != null) {
            map.setExtent( oldExtent );
        }
        return Status.OK_STATUS;
    }


    public IStatus redo( IProgressMonitor monitor, IAdaptable info )
    throws ExecutionException {
        map.setExtent( result );
        return Status.OK_STATUS;
    }

}
