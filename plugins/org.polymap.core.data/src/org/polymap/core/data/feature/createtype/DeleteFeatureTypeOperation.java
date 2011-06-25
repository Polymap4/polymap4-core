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
package org.polymap.core.data.feature.createtype;

import java.net.URL;

import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.catalog.IService;

import org.geotools.data.DataStore;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.opengis.feature.type.FeatureType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.widgets.Display;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
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

    private IGeoResource        geores;


    /**
     * Creates a new operation without the {@link FeatureType} set. This will open a dialog
     * when executing, which allows to edit the new type.
     *
     * @param service The service to create the new type in.
     */
    public DeleteFeatureTypeOperation( IGeoResource geores ) {
        super( Messages.get( "DeleteFeatureTypeOperation_label" ) );
        this.geores = geores;
    }


    public IStatus execute( IProgressMonitor monitor, IAdaptable info )
    throws ExecutionException {
        monitor.beginTask( getLabel(), 1 );

        try {
            IService service = geores.service( monitor );
            final DataStore ds = service.resolve( DataStore.class, monitor );
            if (ds == null) {
                throw new ExecutionException( "No DataStore found for service: " + service );
            }
            // shapefile
            else if (ds instanceof ShapefileDataStore) {
                Display display = (Display)info.getAdapter( Display.class );
                deleteShapefile( display, monitor, service.getIdentifier() );
            }
            // all other
            else {
                throw new ExecutionException( Messages.get( "DeleteFeatureTypeOperation_unsupportedDataStore" ) + ds.getClass().getSimpleName() );

//                List<IService> resets = new ArrayList<IService>();
//                resets.add( service );
//                ResetServiceAction.reset( resets, new SubProgressMonitor( monitor, 2 ) );
            }

            monitor.worked( 1 );
            return Status.OK_STATUS;
        }
        catch (ExecutionException e) {
            throw e;
        }
        catch (Exception e) {
            throw new ExecutionException( Messages.get( "DeleteFeatureTypeOperation_executeError" ), e );
        }
    }

    
    private void deleteShapefile( final Display display, IProgressMonitor monitor, URL oldID ) 
    throws Exception {
        throw new ExecutionException( Messages.get( "DeleteFeatureTypeOperation_unsupportedDataStore" ) + "Shapefile" );

        //        File file = null;
//        if (!oldID.getProtocol().equals( "file" )) { //$NON-NLS-1$
//            try {
//                String workingDir = FileLocator.toFileURL( Platform.getInstanceLocation().getURL() )
//                        .getFile();
//                file = new File( workingDir, type.getName().getLocalPart() + ".shp" ); //$NON-NLS-1$
//            }
//            catch (IOException e) {
//                file = new File(
//                        System.getProperty( "java.user" ) + type.getName().getLocalPart() + ".shp" ); //$NON-NLS-1$ //$NON-NLS-2$
//            }
//            final File f = file;
////            if (!testing) {
////                display.asyncExec( new Runnable() {
////
////                    public void run() {
////                        MessageDialog.openInformation( display.getActiveShell(),
////                                Messages.NewFeatureTypeOp_shpTitle,
////                                Messages.NewFeatureTypeOp_shpMessage + f.toString() );
////                    }
////                } );
////            }
//        }
//        else {
//            String s = new File( oldID.getFile() ).toString();
//            int lastIndexOf = s.lastIndexOf( ".shp" ); //$NON-NLS-1$
//            s = s.substring( 0, lastIndexOf == -1 ? s.length() : lastIndexOf + 1 );
//            lastIndexOf = s.lastIndexOf( File.separator );
//            s = s.substring( 0, lastIndexOf == -1 ? s.length() : lastIndexOf + 1 );
//            file = new File( s + type.getName().getLocalPart() + ".shp" ); //$NON-NLS-1$
//        }
//        ShapefileDataStoreFactory factory = new ShapefileDataStoreFactory();
//        Map<String, Serializable> params = new HashMap<String, Serializable>();
//        params.put( ShapefileDataStoreFactory.URLP.key, file.toURI().toURL() );
//        params.put( ShapefileDataStoreFactory.CREATE_SPATIAL_INDEX.key, true );
//
//        DataStore ds = factory.createNewDataStore( params );
//        ds.createSchema( type );
//        List<IService> services = CatalogPlugin.getDefault().getServiceFactory().createService(
//                file.toURI().toURL() );
//        for (IService service2 : services) {
//            try {
//                DataStore ds2 = service2.resolve( DataStore.class, monitor );
//                if (ds2 instanceof ShapefileDataStore) {
//                    CatalogPlugin.getDefault().getLocalCatalog().add( service2 );
//                }
//            }
//            catch (Exception e) {
//                continue;
//            }
//        }
    }


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
