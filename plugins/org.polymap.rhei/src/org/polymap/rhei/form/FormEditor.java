/* 
 * polymap.org
 * Copyright 2010, Falko Bräutigam, and other contributors as indicated
 * by the @authors tag.
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
 *
 * $Id: $
 */
package org.polymap.rhei.form;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.opengis.feature.Feature;
import org.opengis.feature.Property;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.Id;

import org.geotools.data.FeatureStore;
import org.geotools.factory.CommonFactoryFinder;

import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import org.polymap.core.data.operations.ModifyFeaturesOperation;
import org.polymap.core.operation.OperationSupport;
import org.polymap.core.workbench.PolymapWorkbench;
import org.polymap.rhei.Messages;
import org.polymap.rhei.RheiPlugin;
import org.polymap.rhei.field.FormFieldEvent;
import org.polymap.rhei.field.IFormFieldListener;
import org.polymap.rhei.internal.form.FormEditorPageContainer;
import org.polymap.rhei.internal.form.FormPageProviderExtension;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @version ($Revision$)
 */
public class FormEditor
        extends org.eclipse.ui.forms.editor.FormEditor 
        implements IFormFieldListener {

    private static Log log = LogFactory.getLog( FormEditor.class );

    public static final String          ID = "org.polymap.rhei.form.FormEditor";
    
    
    /**
     *
     * @param fs 
     * @param feature
     * @return The editor of the given feature, or null.
     */
    public static FormEditor open( FeatureStore fs, Feature feature ) {
        try {
            log.debug( "open(): feature= " + feature );
            FormEditorInput input = new FormEditorInput( fs, feature );

            // check current editors
            IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
            IEditorReference[] editors = page.getEditorReferences();
            for (IEditorReference reference : editors) {
                IEditorInput cursor = reference.getEditorInput();
                if (cursor instanceof FormEditorInput) {
                    log.debug( "        editor: feature= " + ((FormEditorInput)cursor).getFeature().getIdentifier().getID() );
                }
                if (cursor.equals( input )) {
                    Object previous = page.getActiveEditor();
                    page.activate( reference.getPart( true ) );
                    return (FormEditor)reference.getEditor( false );
                }
            }

            // not found -> open new editor
            IEditorPart part = page.openEditor( input, input.getEditorId(), true,
                    IWorkbenchPage.MATCH_NONE );
            log.debug( "editor= " + part );
            // can also be ErrorEditorPart
            return part instanceof FormEditor ? (FormEditor)part : null;
        }
        catch (PartInitException e) {
            PolymapWorkbench.handleError( RheiPlugin.PLUGIN_ID, null, e.getMessage(), e );
            return null;
        }
    }

    
    // instance *******************************************
    
    private List<FormEditorPageContainer> pages = new ArrayList();
    
    // FIXME visibility
    public List<Action>                 standardPageActions = new ArrayList();

    private boolean                     isDirty;
    
    private boolean                     isValid;
    
    private boolean                     actionsEnabled;
    
    
    public FormEditor() {
    }


    public void init( IEditorSite site, IEditorInput input )
            throws PartInitException {
        super.init( site, input );
        
        // submit action
        Action submitAction = new Action( Messages.get( "FormEditor_submit" ) ) {
            public void run() {
                try {
                    log.debug( "submitAction.run(): ..." );
                    doSave( new NullProgressMonitor() );
                    OperationSupport.instance().saveChanges();
                }
                catch (Exception e) {
                    PolymapWorkbench.handleError( RheiPlugin.PLUGIN_ID, this, e.getLocalizedMessage(), e );
                }
            }
        };
        submitAction.setImageDescriptor( ImageDescriptor.createFromURL( 
                RheiPlugin.getDefault().getBundle().getResource( "icons/etool16/validate.gif" ) ) );
        submitAction.setToolTipText( Messages.get( "FormEditor_submitTip" ) );
        submitAction.setEnabled( false );
        standardPageActions.add( submitAction );

        // revert action
        Action revertAction = new Action( Messages.get( "FormEditor_revert" ) ) {
            public void run() {
                log.debug( "revertAction.run(): ..." );
                doLoad( new NullProgressMonitor() );
            }
        };
        revertAction.setImageDescriptor( ImageDescriptor.createFromURL( 
                RheiPlugin.getDefault().getBundle().getResource( "icons/etool16/revert.gif" ) ) );
        revertAction.setToolTipText( Messages.get( "FormEditor_revertTip" ) );
        revertAction.setEnabled( false );
        standardPageActions.add( revertAction );
    }

    
    public void fieldChange( FormFieldEvent ev ) {
        isDirty = false;
        for (FormEditorPageContainer page : pages) {
            if (page.isDirty()) {
                isDirty = true;
                break;
            }
        }
        isValid = true;
        for (FormEditorPageContainer page : pages) {
            if (!page.isValid()) {
                isValid = false;
                break;
            }
        }

        log.debug( "fieldChange(): dirty=" + isDirty + ", isValid=" + isValid );
        boolean old = actionsEnabled;
        actionsEnabled = isValid && isDirty;
        if (actionsEnabled != old) {
            for (Action action : standardPageActions) {
                action.setEnabled( actionsEnabled );
            }
            editorDirtyStateChanged();
        }
    }


    public void dispose() {
        super.dispose();
        pages.clear();
    }


    public Composite getContainer() {
        return super.getContainer();
    }

    
    /**
     * This called by the Workbench so that we can add our pages. The implementation
     * searches for extensions that provide a {@link IFormPageProvider}. All providers
     * are called.
     */
    protected void addPages() {
        for (FormPageProviderExtension ext : FormPageProviderExtension.allExtensions()) {
            try {
                IFormPageProvider provider = ext.newPageProvider();
                List<IFormEditorPage> _pages = provider.addPages( this, getFeature() );

                if (_pages != null) {
                    for (IFormEditorPage page : _pages) {
                        FormEditorPageContainer wrapper = new FormEditorPageContainer( page, this, page.getId(), page.getTitle() );
                        addPage( wrapper );
                        pages.add( wrapper );
                        
                        if (ext.isStandard()) {
                            setActivePage( page.getId() );
                            setPartName( page.getTitle() );
                        }
                        
                        wrapper.addFieldListener( this );
                    }
                }
            }
            catch (CoreException e) {
                log.warn( "Exception while initializing pages of FormEditor.", e );
            }
        }
    }


    public Feature getFeature() {
        return ((FormEditorInput)getEditorInput()).getFeature();    
    }
    
    public FeatureStore getFeatureStore() {
        return ((FormEditorInput)getEditorInput()).getFeatureStore();    
    }
    
    
    public boolean isDirty() {
        return isDirty;
    }


    public void doSave( IProgressMonitor monitor ) {
        log.debug( "doSave(): ..." );
        
        Map<Property,Object> changes = new HashMap();
        
        try {
            // submit all pages and get their changes
            for (FormEditorPageContainer page : pages) {
                changes.putAll( page.doSubmit( monitor ) );
            }

            // find featury properties
            List<AttributeDescriptor> attrs = new ArrayList();
            List values = new ArrayList();
            for (Property prop : changes.keySet()) {
                AttributeDescriptor descriptor = (AttributeDescriptor)prop.getDescriptor();
                // this check allows to subclassed Properties to work with
                // complex attribute types; saving has to be done elsewhere
                if (descriptor != null) {
                    attrs.add( descriptor );
                    values.add( changes.get( prop ) );
                }
                else {
                    log.debug( "Property has no descriptor -> ommitting!" );
                }
            }

            // execute operation
            FilterFactory ff = CommonFactoryFinder.getFilterFactory( null );
            Id filter = ff.id( Collections.singleton( getFeature().getIdentifier() ) );
            
            ModifyFeaturesOperation op = new ModifyFeaturesOperation( 
                    getFeatureStore(),
                    filter,
                    attrs.toArray( new AttributeDescriptor[attrs.size()]), 
                    values.toArray() ); 
            OperationSupport.instance().execute( op, false, false );
        }
        catch (Exception e) {
            PolymapWorkbench.handleError( RheiPlugin.PLUGIN_ID, this, "Objekt konnte nicht gespeichert werden.", e );
        }
    }


    public void doLoad( IProgressMonitor monitor ) {
        log.debug( "doLoad(): ..." );
        try {
            for (FormEditorPageContainer page : pages) {
                page.doLoad( monitor );
            }
        }
        catch (Exception e) {
            PolymapWorkbench.handleError( RheiPlugin.PLUGIN_ID, this, "Objekt konnte nicht gespeichert werden.", e );
        }
    }


    public void doSaveAs() {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    public boolean isSaveAsAllowed() {
        return false;
    }


    public void setFocus() {
    }

}
