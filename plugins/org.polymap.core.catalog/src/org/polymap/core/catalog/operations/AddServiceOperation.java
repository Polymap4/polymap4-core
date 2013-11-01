/* 
 * polymap.org
 * Copyright (C) 2013, Falko Bräutigam. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.core.catalog.operations;

import java.io.File;

import net.refractions.udig.catalog.ICatalog;
import net.refractions.udig.catalog.IService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.polymap.core.catalog.model.CatalogRepository;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class AddServiceOperation
        extends AbstractOperation
        implements IUndoableOperation {

    private static Log log = LogFactory.getLog( AddServiceOperation.class );

    private ICatalog            catalog;
    
    private IService            service;

    private boolean             canUndo;
    
    
    public AddServiceOperation( ICatalog catalog, IService service ) {
        super( "Catalog" );
        this.catalog = catalog;
        this.service = service;
    }

    @Override
    public IStatus execute( IProgressMonitor monitor, IAdaptable info ) throws ExecutionException {
        catalog.add( service );
        canUndo = true;

        // for file based services: Shape, DXF, TIFF: 
        // commit changes so that no dangling files are left if not saved
        if (service.canResolve( File.class )) {
            try {
                log.info( "Committing file based service: " + service );
                canUndo = false;
                CatalogRepository.instance().commitChanges();
            }
            catch (Exception e) {
                throw new ExecutionException( e.getLocalizedMessage(), e );
            }
        }
        return Status.OK_STATUS;
    }

    @Override
    public boolean canUndo() {
        return canUndo;
    }

    @Override
    public IStatus undo( IProgressMonitor monitor, IAdaptable info ) throws ExecutionException {
        catalog.remove( service );
        return Status.OK_STATUS;
    }

    @Override
    public IStatus redo( IProgressMonitor monitor, IAdaptable info ) throws ExecutionException {
        return execute( monitor, info );
    }
    
}
