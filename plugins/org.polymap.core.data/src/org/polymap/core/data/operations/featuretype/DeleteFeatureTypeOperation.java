/*
 * polymap.org
 * Copyright 2011, Falko Bräutigam. All rights reserved.
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
package org.polymap.core.data.operations.featuretype;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

import net.refractions.udig.catalog.CatalogPlugin;
import net.refractions.udig.catalog.ICatalog;
import net.refractions.udig.catalog.IDeletingSchemaService;
import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.catalog.IService;
import net.refractions.udig.catalog.internal.shp.ShpServiceImpl;

import org.opengis.feature.type.FeatureType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.widgets.Display;

import org.eclipse.jface.dialogs.MessageDialog;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;

import org.polymap.core.catalog.actions.ResetServiceAction;
import org.polymap.core.catalog.model.CatalogRepository;
import org.polymap.core.data.Messages;

/**
 * Creates a new {@link FeatureType}.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @since 3.1
 */
public class DeleteFeatureTypeOperation
        extends AbstractOperation
        implements IUndoableOperation {

    private static Log log = LogFactory.getLog( DeleteFeatureTypeOperation.class );

    public static String i18n( String key, Object... args) {
        return Messages.get( "DeleteFeatureTypeOperation_" + key, args );
    }

    
    // instance *******************************************
    
    private IGeoResource        geores;


    /**
     * Creates a new operation without the {@link FeatureType} set. This will open a dialog
     * when executing, which allows to edit the new type.
     *
     * @param service The service to create the new type in.
     */
    public DeleteFeatureTypeOperation( IGeoResource geores ) {
        super( i18n( "label" ) );
        this.geores = geores;
    }


    public IStatus execute( IProgressMonitor monitor, IAdaptable info )
    throws ExecutionException {
        monitor.beginTask( getLabel(), 2 );

        try {
            IService service = geores.service( monitor );
            final String title = service.getInfo( monitor ).getTitle();

            if (!(service instanceof IDeletingSchemaService)) {
                throw new ExecutionException( i18n( "unsupportedDataStore", title ) );
            }

            // confirm
            final AtomicBoolean confirmed = new AtomicBoolean();
            final Display display = (Display)info.getAdapter( Display.class );
            display.syncExec( new Runnable() {
                public void run() {
                    confirmed.set( MessageDialog.openConfirm( display.getActiveShell(),
                            i18n( "confirmTitle", title ), i18n( "confirmMessage", title ) ) );
                }
            });
            
            if (confirmed.get()) {
                // delete geores
                ((IDeletingSchemaService)service).deleteSchema( geores, 
                        new SubProgressMonitor( monitor, 1 ) );
                
                // shapefile? -> remove service altogether
                if (service instanceof ShpServiceImpl) {
                    ICatalog catalog = CatalogPlugin.getDefault().getLocalCatalog();
                    catalog.remove( service );
                    CatalogRepository.instance().commitChanges();
                }
                // other -> reset service
                else {
                    ResetServiceAction.reset( Collections.singletonList( service ), monitor );
                }
            }
            return Status.OK_STATUS;
        }
        catch (ExecutionException e) {
            throw e;
        }
        catch (Exception e) {
            throw new ExecutionException( e.getLocalizedMessage(), e );
        }
    }

    
//    private void deleteShapefile( ShapefileDataStore ds, final File file, final Display display, IProgressMonitor monitor ) 
//    throws Exception {
////        throw new ExecutionException( i18n( "unsupportedDataStore", "Shapefile" ) );
//
//        
//        if (yes.get()) {
//            String baseName = FilenameUtils.getBaseName( file.getName() );
//            for (File f : file.getParentFile().listFiles()) {
//                if (FilenameUtils.getBaseName( f.getName() ).equals( baseName )) {
//                    log.info( "deleting: " + f.getAbsolutePath() );
//                }
//            }
//        }
//    }


    public boolean canUndo() {
        return false;
    }

    
    public IStatus undo( IProgressMonitor monitor, IAdaptable info ) {
        throw new RuntimeException( "not yet implemented." );
    }

    
    public boolean canRedo() {
        return false;
    }


    public IStatus redo( IProgressMonitor monitor, IAdaptable info )
            throws ExecutionException {
        throw new RuntimeException( "not yet implemented." );
    }
    
}
