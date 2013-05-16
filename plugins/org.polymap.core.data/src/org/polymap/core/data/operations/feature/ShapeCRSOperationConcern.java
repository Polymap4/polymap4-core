/* 
 * polymap.org
 * Copyright 2013, Falko Bräutigam. All rights reserved.
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
package org.polymap.core.data.operations.feature;

import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.catalog.internal.shp.ShpGeoResourceImpl;
import net.refractions.udig.catalog.shp.op.SetProjection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.widgets.Display;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

import org.polymap.core.operation.IOperationConcernFactory;
import org.polymap.core.operation.OperationConcernAdapter;
import org.polymap.core.operation.OperationInfo;
import org.polymap.core.project.operations.NewLayerOperation;

/**
 * Checks if the the geores of a new layer is ShpGeoResourceImpl and has CRS set. If
 * not then create *.prj file for it.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
@SuppressWarnings("restriction")
public class ShapeCRSOperationConcern
        extends IOperationConcernFactory {

    private static Log log = LogFactory.getLog( ShapeCRSOperationConcern.class );

    public ShapeCRSOperationConcern() {
    }

    @Override
    public IUndoableOperation newInstance( final IUndoableOperation op, final OperationInfo info ) {
        if (op instanceof NewLayerOperation) {
            return new OperationConcernAdapter() {

                @Override
                public IStatus execute( IProgressMonitor monitor, IAdaptable _info ) throws ExecutionException {
                    try {
                        Display display = (Display)_info.getAdapter( Display.class );
                        IGeoResource geores = ((NewLayerOperation)op).getGeores();
                        if (geores instanceof ShpGeoResourceImpl && geores.getInfo( monitor).getCRS() == null) {
                            new SetProjection().op( display, new Object[] {geores}, monitor );
                            ((ShpGeoResourceImpl)geores).service().resetDataSource();
                            ((ShpGeoResourceImpl)geores).resetInfo();
                        }
                        
                        // now call next
                        IStatus result = info.next().execute( monitor, _info );
                        return result;
                    }
                    catch (Exception e) {
                        throw new ExecutionException( "", e );
                    }
                }

                @Override
                protected OperationInfo getInfo() {
                    return info;
                }
            };
        }
        return null;
    }
    
}
