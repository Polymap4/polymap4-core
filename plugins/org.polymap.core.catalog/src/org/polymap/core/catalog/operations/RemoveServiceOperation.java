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

import java.util.ArrayList;
import java.util.List;

import java.io.File;

import net.refractions.udig.catalog.ICatalog;
import net.refractions.udig.catalog.IDeletingSchemaService;
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
public class RemoveServiceOperation
        extends AbstractOperation
        implements IUndoableOperation {

    private static Log log = LogFactory.getLog( RemoveServiceOperation.class );

    private ICatalog            catalog;
    
    private IService            service;
    
    private boolean             canUndo;
    
    private List<File>          undoFiles = new ArrayList();
    
    
    public RemoveServiceOperation( ICatalog catalog, IService service ) {
        super( "Catalog" );
        this.catalog = catalog;
        this.service = service;
    }

    @Override
    public IStatus execute( IProgressMonitor monitor, IAdaptable info ) throws ExecutionException {
        catalog.remove( service );
        canUndo = true;
        
        // all file based services (shapefile, DXF, TIFF)
        if (service instanceof IDeletingSchemaService
                && service.canResolve( File.class )) {
            try {
//                // copy to /tmp
//                File file = service.resolve( File.class, monitor );
//                String baseName = FilenameUtils.getBaseName( file.getName() );
//                for (File f : file.getParentFile().listFiles()) {
//                    if (FilenameUtils.getBaseName( f.getName() ).equals( baseName )) {
//                        File undoFile = File.createTempFile( f.getName(), String.valueOf( hashCode() ) );
//                        log.info( "UNDO file: " + undoFile.getAbsolutePath() );
//                        FileUtils.copyFile( f, undoFile );
//                        undoFiles.add( undoFile );
//                    }
//                }

                // delete files and commit service
                log.info( "Deleting files and committing service: " + service );
                ((IDeletingSchemaService)service).deleteSchema( null, monitor );
                canUndo = false;
                CatalogRepository.instance().commitChanges();
            }
            catch (Exception e) {
                throw new ExecutionException( e.getLocalizedMessage(), e );
            }
        }
        
//        ResetServiceAction.reset( Collections.singletonList( service ), monitor );
        return Status.OK_STATUS;
    }

        
    @Override
    public boolean canUndo() {
        return canUndo;
    }

    
    @Override
    public IStatus undo( IProgressMonitor monitor, IAdaptable info ) throws ExecutionException {
        catalog.add( service );
        assert undoFiles.isEmpty();
        return Status.OK_STATUS;
    }

    
    @Override
    public IStatus redo( IProgressMonitor monitor, IAdaptable info ) throws ExecutionException {
        return execute( monitor, info );
    }
    
}
