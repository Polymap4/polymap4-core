/* 
 * polymap.org
 * Copyright 2011, Falko Bräutigam, and other contributors as indicated
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
 */
package org.polymap.rhei.form;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.jface.dialogs.TitleAreaDialog;

import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.FormToolkit;

import org.eclipse.core.runtime.NullProgressMonitor;

import org.polymap.core.workbench.PolymapWorkbench;
import org.polymap.rhei.RheiPlugin;
import org.polymap.rhei.internal.form.AbstractFormEditorPageContainer;
import org.polymap.rhei.internal.form.FormEditorToolkit;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @since 1.0
 */
public class FormEditorDialog
        extends TitleAreaDialog {

    private static Log log = LogFactory.getLog( FormEditorDialog.class );
    
    private Composite                   pageBody;

    private PageContainer               pageContainer;
    
    private IFormEditorToolkit          toolkit;
    
    
    public FormEditorDialog( IFormEditorPage page ) {
        super( PlatformUI.  getWorkbench().getActiveWorkbenchWindow().getShell() );
        setShellStyle( getShellStyle() | SWT.RESIZE );
        
        this.pageContainer = new PageContainer( page );
    }

    
    protected void okPressed() {
        log.debug( "okPressed() ..." );
        try {
            pageContainer.doSubmit( new NullProgressMonitor() );
            pageContainer.dispose();

            super.okPressed();
        }
        catch (Exception e) {
            PolymapWorkbench.handleError( RheiPlugin.PLUGIN_ID, this, "Werte konnten nicht gespeichert werden.", e );
        }
    }


    protected void cancelPressed() {
        pageContainer.dispose();
    }


    protected Control createDialogArea( Composite parent ) {
        Composite result = (Composite)super.createDialogArea( parent );
        toolkit = new FormEditorToolkit( new FormToolkit( getParentShell().getDisplay() ) );
        
        // make margins
        Composite container = new Composite( parent, SWT.NONE );
        container.setLayoutData( new GridData( GridData.FILL_BOTH ) );
        GridLayout gl = new GridLayout();
        gl.marginWidth = 5;
        gl.marginHeight = 0;
        container.setLayout( gl );

        pageBody = new Composite( container, SWT.NONE );
        pageBody.setLayoutData( new GridData( GridData.FILL_BOTH ) );
        
        pageContainer.createContent();
        try {
            pageContainer.doLoad( new NullProgressMonitor() );
        }
        catch (Exception e) {
            PolymapWorkbench.handleError( RheiPlugin.PLUGIN_ID, this, e.getLocalizedMessage(), e );        
        }
        
        // form.getToolkit().decorateFormHeading( form.getForm().getForm() );

//        // add page editor actions
//        Action[] pageActions = page.getEditorActions();
//        if (pageActions != null && pageActions.length > 0) {
//            form.getForm().getToolBarManager().add( new GroupMarker( "__pageActions__" ) );
//
//            for (Action action : pageActions) {
//                form.getForm().getToolBarManager().appendToGroup( "__pageActions__", action );
//            }
//            form.getForm().getToolBarManager().appendToGroup( "__pageActions__", new Separator() );
//        }
//
//        // add actions
//        form.getForm().getToolBarManager().add( new GroupMarker( "__standardPageActions__" ) );
//        for (Action action : ((FormEditor)getEditor()).standardPageActions) {
//            form.getForm().getToolBarManager().appendToGroup( "__standardPageActions__", action );
//        }
//        form.getForm().getToolBarManager().update( true );
        
        result.pack();
        return result;
    }



    /**
     * 
     */
    class PageContainer
            extends AbstractFormEditorPageContainer {

        public PageContainer( IFormEditorPage page ) {
            super( page, "_id_", "_title_" );
        }

        public void createContent() {
            page.createFormContent( this );
        }

        public Composite getPageBody() {
            return pageBody;
        }

        
        public IFormEditorToolkit getToolkit() {
            return toolkit;
        }

        public void setFormTitle( String title ) {
            setTitle( title );
        }
        
    }

}
