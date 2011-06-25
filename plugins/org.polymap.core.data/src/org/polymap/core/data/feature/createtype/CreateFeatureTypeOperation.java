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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;

import net.refractions.udig.catalog.CatalogPlugin;
import net.refractions.udig.catalog.IResolve;
import net.refractions.udig.catalog.IResolveFolder;
import net.refractions.udig.catalog.IService;

import org.geotools.data.DataStore;
import org.geotools.data.DataUtilities;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.wizard.IWizardPage;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;

import org.polymap.core.catalog.actions.ResetServiceAction;
import org.polymap.core.data.DataPlugin;
import org.polymap.core.data.Messages;
import org.polymap.core.data.ui.featuretypeeditor.CreateAttributeAction;
import org.polymap.core.data.ui.featuretypeeditor.DeleteAttributeAction;
import org.polymap.core.data.ui.featuretypeeditor.FeatureTypeEditor;
import org.polymap.core.operation.OperationWizard;
import org.polymap.core.operation.OperationWizardPage;
import org.polymap.core.project.ui.util.SimpleFormData;
import org.polymap.core.runtime.WeakListener;

/**
 * Creates a new {@link FeatureType}.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @author jones (uDig - NewFeatureTypeOp)
 * @since 3.1
 */
public class CreateFeatureTypeOperation
        extends AbstractOperation
        implements IUndoableOperation {

    private static Log log = LogFactory.getLog( CreateFeatureTypeOperation.class );

    /** Usually this is an IService but for PostGIS this is an {@link IResolveFolder}. */
    private IResolve            service;

    private SimpleFeatureType   schema;
    

    /**
     * Creates a new operation without the {@link FeatureType} set. This will open a dialog
     * when executing, which allows to edit the new type.
     *
     * @param service The service to create the new type in.
     */
    public CreateFeatureTypeOperation( IResolve service ) {
        super( Messages.get( "CreateFeatureTypeOperation_label" ) );
        this.service = service;
    }


    public IStatus execute( IProgressMonitor monitor, IAdaptable info )
    throws ExecutionException {
        monitor.beginTask( getLabel(), 2 );

        // create new schema within a wizard dialog
        if (schema == null) {
            monitor.subTask( Messages.get( "CreateFeatureTypeOperation_subTaskWizard" ) );
            try {
                schema = DataUtilities.createType( Messages.get( "CreateFeatureTypeOperation_newSchemaName" ), "geom:MultiLineString" );
            }
            catch (SchemaException e) {
                throw new ExecutionException( Messages.get( "CreateFeatureTypeOperation_defaultTypeError" ), e );
            }
            
            OperationWizard wizard = new OperationWizard( this, info, monitor ) {
                public boolean doPerformFinish()
                throws Exception {
                    return ((FeatureEditorPage)getPage( FeatureEditorPage.ID )).performFinish();
                }
            };
            FeatureEditorPage featureEditorPage = new FeatureEditorPage();
            wizard.addPage( featureEditorPage );

            if (OperationWizard.openDialog( wizard )) {
                schema = featureEditorPage.getNewSchema();
            }
            else {
                return Status.CANCEL_STATUS;
            }
            monitor.worked( 1 );
        }

        // create type
        try {
            monitor.subTask( Messages.get( "CreateFeatureTypeOperation_subTaskCreate" ) );

            final DataStore ds = service.resolve( DataStore.class, monitor );
            if (ds == null) {
                throw new ExecutionException( "No DataStore found for service: " + service );
            }
            // shapefile
            else if (ds instanceof ShapefileDataStore) {
                Display display = (Display)info.getAdapter( Display.class );
                createShapefile( display, monitor, schema, service.getIdentifier() );
            }
            // all other
            else {
                ds.createSchema( schema );
                
                // wait for the new type to appear
                long start=System.currentTimeMillis();
                while (!ArrayUtils.contains( ds.getTypeNames(), schema.getName().getLocalPart() ) 
                        && start+5000 > System.currentTimeMillis()) {
                    Thread.sleep( 300 );
                }

                List<IService> resets = new ArrayList<IService>();
                if (service instanceof IService) {
                    resets.add( (IService)service );                    
                }
                else if (service instanceof IResolveFolder) {
                    resets.add( ((IResolveFolder)service).getService( monitor ) );                    
                }
                ResetServiceAction.reset( resets, new SubProgressMonitor( monitor, 2 ) );
            }

            monitor.worked( 1 );
            return Status.OK_STATUS;
        }
        catch (Exception e) {
            throw new ExecutionException( Messages.get( "CreateFeatureTypeOperation_executeError" ), e );
        }
    }

    
    private void createShapefile( final Display display, IProgressMonitor monitor,
            SimpleFeatureType type, URL oldID ) throws MalformedURLException, IOException {
        File file = null;
        if (!oldID.getProtocol().equals( "file" )) { //$NON-NLS-1$
            try {
                String workingDir = FileLocator.toFileURL( Platform.getInstanceLocation().getURL() )
                        .getFile();
                file = new File( workingDir, type.getName().getLocalPart() + ".shp" ); //$NON-NLS-1$
            }
            catch (IOException e) {
                file = new File(
                        System.getProperty( "java.user" ) + type.getName().getLocalPart() + ".shp" ); //$NON-NLS-1$ //$NON-NLS-2$
            }
            final File f = file;
//            if (!testing) {
//                display.asyncExec( new Runnable() {
//
//                    public void run() {
//                        MessageDialog.openInformation( display.getActiveShell(),
//                                Messages.NewFeatureTypeOp_shpTitle,
//                                Messages.NewFeatureTypeOp_shpMessage + f.toString() );
//                    }
//                } );
//            }
        }
        else {
            String s = new File( oldID.getFile() ).toString();
            int lastIndexOf = s.lastIndexOf( ".shp" ); //$NON-NLS-1$
            s = s.substring( 0, lastIndexOf == -1 ? s.length() : lastIndexOf + 1 );
            lastIndexOf = s.lastIndexOf( File.separator );
            s = s.substring( 0, lastIndexOf == -1 ? s.length() : lastIndexOf + 1 );
            file = new File( s + type.getName().getLocalPart() + ".shp" ); //$NON-NLS-1$
        }
        ShapefileDataStoreFactory factory = new ShapefileDataStoreFactory();
        Map<String, Serializable> params = new HashMap<String, Serializable>();
        params.put( ShapefileDataStoreFactory.URLP.key, file.toURI().toURL() );
        params.put( ShapefileDataStoreFactory.CREATE_SPATIAL_INDEX.key, true );

        DataStore ds = factory.createNewDataStore( params );
        ds.createSchema( type );
        List<IService> services = CatalogPlugin.getDefault().getServiceFactory().createService(
                file.toURI().toURL() );
        for (IService service2 : services) {
            try {
                DataStore ds2 = service2.resolve( DataStore.class, monitor );
                if (ds2 instanceof ShapefileDataStore) {
                    CatalogPlugin.getDefault().getLocalCatalog().add( service2 );
                }
            }
            catch (Exception e) {
                continue;
            }
        }
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


    /**
     *
     */
    class FeatureEditorPage
            extends OperationWizardPage
            implements IWizardPage, IPageChangedListener {

        public static final String          ID = "FeatureEditorPage";

        private FeatureTypeEditor           editor;

        private Composite                   contents;
        
        private SimpleFeatureType           newSchema;


        protected FeatureEditorPage() {
            super( ID );
            setTitle( Messages.get( "CreateFeatureTypeOperation_FeatureEditorPage_title" ) );
            setDescription( Messages.get( "CreateFeatureTypeOperation_FeatureEditorPage_description" ) );
        }

        public boolean performFinish() {
            newSchema = editor.getFeatureType();
            return true;
        }

        public SimpleFeatureType getNewSchema() {
            return newSchema;
        }
        
        public void createControl( Composite parent ) {
            this.contents = new Composite( parent, SWT.NONE );
            FormLayout layout = new FormLayout();
            layout.spacing = 5;
            contents.setLayout( layout );
            setControl( contents );
            getWizard().addPageChangedListener( WeakListener.forListener( this ) );
        }

        public void pageChanged( PageChangedEvent ev ) {
            log.info( "pageChanged(): ev= " + ev.getSelectedPage() );
            if (ev.getSelectedPage() == this && editor == null) {
                getContainer().getShell().setMinimumSize( 500, 400 );
                getContainer().getShell().layout( true );

//                try {
                editor = new FeatureTypeEditor();

                // nameText
                final Text nameText = new Text( contents, SWT.BORDER );
                nameText.setLayoutData( new SimpleFormData().top( 0 ).left( 0 ).right( 100 ).create() );
                nameText.setText( schema.getTypeName() );
                nameText.addModifyListener( new ModifyListener() {
                    public void modifyText( ModifyEvent mev ) {
                        log.debug( "modifyText(): =" + nameText.getText() );
                        SimpleFeatureTypeBuilder builder = editor.getFeatureTypeBuilder();
                        builder.setName( nameText.getText() );
                        editor.setFeatureType( builder.buildFeatureType() );
                    }
                });
                
                editor.createTable( contents, 
                        new SimpleFormData().fill().top( nameText ).right( 100, -40 ).create(), 
                        schema, true );

                // CreateAttributeAction
                final CreateAttributeAction createAction = new CreateAttributeAction( editor );
                final Button createBtn = new Button( contents, SWT.PUSH | SWT.BORDER );
                createBtn.setLayoutData( new SimpleFormData().top( nameText, 5 ).left( editor.getControl() ).right( 100 ).create() );
                createBtn.setToolTipText( createAction.getToolTipText() );
                createBtn.setImage( DataPlugin.getDefault().imageForDescriptor( createAction.getImageDescriptor(), createAction.getId() ) );

                createBtn.addListener( SWT.Selection, new Listener() {
                    public void handleEvent( Event event ) {
                        createAction.runWithEvent( event );
                    }
                });

                // DeleteAttributeAction
                final DeleteAttributeAction delAction = new DeleteAttributeAction( editor );
                final Button delBtn = new Button( contents, SWT.PUSH | SWT.BORDER );
                delBtn.setLayoutData( new SimpleFormData().top( createBtn ).left( editor.getControl() ).right( 100 ).create() );
                delBtn.setToolTipText( delAction.getToolTipText() );
                delBtn.setImage( DataPlugin.getDefault().imageForDescriptor( delAction.getImageDescriptor(), delAction.getId() ) );

                delBtn.addListener( SWT.Selection, new Listener() {
                    public void handleEvent( Event event ) {
                        delAction.runWithEvent( event );
                    }
                });

                contents.layout( true );
//                }
//                catch (Exception e) {
//                    throw new RuntimeException( Messages.get( "CopyFeaturesOperation_FeatureEditorPage_errorLayerSchema" ), e );
//                }
            }
        }

        public boolean isPageComplete() {
            return true;
        }

    }

}
