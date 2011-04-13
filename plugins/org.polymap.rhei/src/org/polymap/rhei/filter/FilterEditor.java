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
 *
 * $Id: $
 */
package org.polymap.rhei.filter;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.window.Window;

import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.FormToolkit;

import org.polymap.core.data.DataPlugin;
import org.polymap.core.workbench.PolymapWorkbench;
import org.polymap.rhei.Messages;
import org.polymap.rhei.RheiPlugin;
import org.polymap.rhei.field.FormFieldEvent;
import org.polymap.rhei.field.IFormField;
import org.polymap.rhei.field.IFormFieldListener;
import org.polymap.rhei.field.IFormFieldValidator;
import org.polymap.rhei.form.IFormEditorToolkit;
import org.polymap.rhei.internal.DefaultFormFieldDecorator;
import org.polymap.rhei.internal.DefaultFormFieldLabeler;
import org.polymap.rhei.internal.filter.FilterFieldComposite;
import org.polymap.rhei.internal.form.FormEditorToolkit;

/**
 * Provides a standard UI for filters. This class can be used by subclasses if
 * {@link IFilter} to provide a standard filter dialog.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @version ($Revision$)
 */
public abstract class FilterEditor {

    private static Log log = LogFactory.getLog( FilterEditor.class );

    private IFormEditorToolkit          toolkit;
    
    private FilterEditorDialog          dialog;
    
    private Map<String,FilterFieldComposite>  fields = new HashMap();
    
    private Map<String,Object>          fieldValues = new HashMap();
    
    private Composite                   layoutLast;
    
    
    public FilterEditor() {
    }

    
    /**
     *
     * @return
     */
    protected abstract Composite createControl( Composite parent, IFilterEditorSite site );
    

    /**
     * Opens a modal dialog.
     * 
     * @return {@link Window#OK} or {@link Window#OK}. 
     */
    public int openDialog() {
        try {
            dialog = new FilterEditorDialog();

            toolkit = new FormEditorToolkit( new FormToolkit( dialog.getParentShell().getDisplay() ) );
            dialog.setBlockOnOpen( true );
            return dialog.open();
        }
        catch (Exception e) {
            PolymapWorkbench.handleError( DataPlugin.PLUGIN_ID, this, "Fehler beim Öffnen der Attributtabelle.", e );
            return Window.CANCEL;
        }
    }
    

    public Object getFieldValue( String propertyName ) {
        return fieldValues.get( propertyName );
    }
    

    
    /**
     * 
     */
    class FilterEditorDialog
            extends TitleAreaDialog
            implements IFilterEditorSite {

        private Composite           contents;
        

        public FilterEditorDialog() {
            super( PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell() );
            setShellStyle( getShellStyle() | SWT.RESIZE );
        }

        public Shell getParentShell() {
            return super.getParentShell();
        }

        public Composite getPageBody() {
            throw new RuntimeException( "not yet implemented." );
        }

        public IFormEditorToolkit getToolkit() {
            return toolkit;
        }

        public Composite newFormField( Composite parent, String propName, Class propType, 
                IFormField field, IFormFieldValidator validator ) {
            return newFormField( parent, propName, propType, field, validator, null );
        }
        
        public Composite newFormField( Composite parent, String propName, Class propType, 
                IFormField field, IFormFieldValidator validator, String label ) {
            
            final FilterFieldComposite fieldComposite = new FilterFieldComposite( 
                    toolkit, propName, propType, field, 
                    new DefaultFormFieldLabeler( label ), new DefaultFormFieldDecorator(), 
                    validator );
            
            fieldComposite.addChangeListener( new IFormFieldListener() {
                public void fieldChange( FormFieldEvent ev ) {
                    // record value
                    if (ev.getEventCode() == VALUE_CHANGE) {
                        fieldValues.put( fieldComposite.getFieldName(), ev.getNewValue() );
                    }
                    // check validity
                    boolean valid = true;
                    for (FilterFieldComposite fc : fields.values()) {
                        if (!fc.isValid()) {
                            valid = false;
                            break;
                        }
                    }
                    getButton( IDialogConstants.OK_ID ).setEnabled( valid );

                    // FIXME propagate
//                    for (FilterFieldComposite elm : fields.values()) {
//                        if (elm != fieldComposite) {
//                            elm.fireEvent( ev.getEventCode(), ev.getNewValue() );
//                        }
//                    }
                }
            });
            
            fields.put( fieldComposite.getFieldName(), fieldComposite );
            
            return fieldComposite.createComposite( parent, SWT.NONE );
        }
        
        public Composite createStandardLayout( Composite parent ) {
            Composite result = new Composite( parent, SWT.NONE );
            GridData gridData = new GridData( GridData.FILL_BOTH );
            gridData.grabExcessHorizontalSpace = true;
            result.setLayoutData( gridData );

            layoutLast = null;
            
            FormLayout layout = new FormLayout();
            layout.marginWidth = 10;
            layout.marginHeight = 10;
            result.setLayout( layout );
            return result;
        }

        public void addStandardLayout( Composite composite ) {
            FormData data = new FormData();
            data.left = new FormAttachment( 0, 0 );
            data.right = new FormAttachment( 100, 0 );
            if (layoutLast != null) {
                data.top = new FormAttachment( layoutLast, 3 );
            }
            composite.setLayoutData( data );
            layoutLast = composite;
        }
        
        
        // TitleAreaDialog ********************************
        
        protected Image getImage() {
            return getShell().getDisplay().getSystemImage( SWT.ICON_QUESTION );
        }

        protected Point getInitialSize() {
//            return new Point( 350, 260 );
            return super.getInitialSize();
        }

        protected Control createDialogArea( Composite parent ) {
            Composite area = (Composite)super.createDialogArea( parent );

            setTitle( Messages.get( "FilterEditor_title" ) );
            setMessage( Messages.get( "FilterEditor_description" ) );
            
            contents = createControl( area, this );

            area.pack();
            return area;
        }


        protected Control createButtonBar( Composite parent ) {
            Control result = super.createButtonBar( parent );
            getButton( IDialogConstants.OK_ID ).setEnabled( false );
            return result;
        }

        protected void okPressed() {
            try {
                // fieldValues are already filled in the event handler; we call store() method
                // of the field anyway in order to keep the contract. Some form fields, for
                // example BetweenFormField, provide a different value via the store value
                for (FilterFieldComposite field : fields.values()) {
                    if (field.isDirty()) {
                        field.getFormField().store();
                        fieldValues.put( field.getFieldName(), field.getFieldValue() );
                    }
                }

                if (!fieldValues.isEmpty()) {
                    super.okPressed();
                }
            }
            catch (Exception e) {
                PolymapWorkbench.handleError( RheiPlugin.PLUGIN_ID, this, Messages.get( "FilterEditor_okError" ), e );
            }
        }

    }
    
}
