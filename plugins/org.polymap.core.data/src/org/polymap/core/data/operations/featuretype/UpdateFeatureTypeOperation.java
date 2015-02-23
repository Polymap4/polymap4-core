/*
 * polymap.org
 * Copyright (C) 2013, Falko Bräutigam. All rights reserved.
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

import java.util.ArrayList;
import java.util.List;

import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.catalog.IResolveFolder;
import net.refractions.udig.catalog.IService;

import org.geotools.data.DataAccess;
import org.geotools.data.FeatureSource;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.wizard.IWizardPage;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
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
import org.polymap.core.runtime.Timer;
import org.polymap.core.runtime.WeakListener;
import org.polymap.core.runtime.i18n.IMessages;

/**
 * Updates an existing {@link FeatureType}.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @since 3.1
 */
public class UpdateFeatureTypeOperation
        extends AbstractOperation
        implements IUndoableOperation {

    private static Log log = LogFactory.getLog( UpdateFeatureTypeOperation.class );

    private static final IMessages  i18n = Messages.forPrefix( "UpdateFeatureTypeOperation" );
    
    /** Usually this is an IService but for PostGIS this is an {@link IResolveFolder}. */
    private IGeoResource        geores;
    
    private SimpleFeatureType   schema;
    
    private SimpleFeatureType   newSchema;
    

    /**
     * Creates a new operation without the {@link FeatureType} set. This will open a dialog
     * when executing, which allows to edit the new type.
     *
     * @param service The service to create the new type in.
     */
    public UpdateFeatureTypeOperation( IGeoResource geores ) {
        super( i18n.get( "label" ) );
        this.geores = geores;
    }


    public IStatus execute( final IProgressMonitor monitor, IAdaptable info ) throws ExecutionException {
        monitor.beginTask( getLabel(), 2 );

        // create new schema within a wizard dialog
        OperationWizard wizard = new OperationWizard( this, info, monitor ) {
            public boolean doPerformFinish()
                    throws Exception {
                return ((FeatureEditorPage)getPage( FeatureEditorPage.ID )).performFinish();
            }
        };
        final FeatureEditorPage featureEditorPage = new FeatureEditorPage();
        wizard.addPage( featureEditorPage );

        // prepare source schema
        try {
            IService service = geores.service( monitor );
            DataAccess ds = service.resolve( DataAccess.class, monitor );
            if (ds == null) {
                throw new ExecutionException( "No DataStore found for service: " + service );
            }
            FeatureSource fs = geores.resolve( FeatureSource.class, monitor );
            if (fs == null) {
                throw new ExecutionException( "No FeatureSource found for geo resource: " + geores );
            }
            schema = (SimpleFeatureType)fs.getSchema();

            // open dialog
            if (!OperationWizard.openDialog( wizard )) {
                return Status.CANCEL_STATUS;
            }
            monitor.worked( 1 );

            // update type (newSchema set by the page)
            monitor.subTask( i18n.get( "subTaskUpdate" ) );
            ds.updateSchema( schema.getName(), newSchema );
                
            // reset catalog / wait for the new type to appear
            Timer timer = new Timer();
            while (!ds.getNames().contains( schema.getName() ) 
                    && timer.elapsedTime() < 5000) {
                Thread.sleep( 300 );
            }

            List<IService> resets = new ArrayList<IService>();
            if (service instanceof IService) {
                resets.add( service );                    
            }
            else if (service instanceof IResolveFolder) {
                resets.add( ((IResolveFolder)service).getService( monitor ) );                    
            }
            ResetServiceAction.reset( resets, new SubProgressMonitor( monitor, 2 ) );

            monitor.worked( 1 );
            return Status.OK_STATUS;
        }
        catch (ExecutionException e) {
            throw e;
        }
        catch (Exception e) {
            throw new ExecutionException( i18n.get( "executeError" ), e );
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
        

        protected FeatureEditorPage() {
            super( ID );
            setTitle( i18n.get( "FeatureEditorPage_title" ) );
            setDescription( i18n.get( "FeatureEditorPage_description" ) );
        }

        public boolean performFinish() {
            newSchema = editor.getFeatureType();
            return true;
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
                getContainer().getShell().setMinimumSize( 450, 450 );
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
                final Button createBtn = new Button( contents, SWT.PUSH /*| SWT.BORDER*/ );
                createBtn.setLayoutData( new SimpleFormData().top( nameText, 5 ).left( editor.getControl() ).right( 100 ).height( 28 ).create() );
                createBtn.setToolTipText( createAction.getToolTipText() );
                createBtn.setImage( DataPlugin.getDefault().imageForDescriptor( createAction.getImageDescriptor(), createAction.getId() ) );

                createBtn.addListener( SWT.Selection, new Listener() {
                    public void handleEvent( Event event ) {
                        createAction.runWithEvent( event );
                    }
                });

                // DeleteAttributeAction
                final DeleteAttributeAction delAction = new DeleteAttributeAction( editor );
                final Button delBtn = new Button( contents, SWT.PUSH /*| SWT.BORDER*/ );
                delBtn.setLayoutData( new SimpleFormData().top( createBtn ).left( editor.getControl() ).right( 100 ).height( 28 ).create() );
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
