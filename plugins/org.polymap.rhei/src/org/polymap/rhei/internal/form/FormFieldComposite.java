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
package org.polymap.rhei.internal.form;

import org.opengis.feature.Property;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.core.runtime.ListenerList;

import org.polymap.rhei.field.FormFieldEvent;
import org.polymap.rhei.field.IFormField;
import org.polymap.rhei.field.IFormFieldDecorator;
import org.polymap.rhei.field.IFormFieldLabel;
import org.polymap.rhei.field.IFormFieldListener;
import org.polymap.rhei.field.IFormFieldSite;
import org.polymap.rhei.field.IFormFieldValidator;
import org.polymap.rhei.form.IFormEditorToolkit;

/**
 * The form specific parent Composite of a form field, consisting of an
 * {@link IFormField}, an {@link IFormFieldLabel} and an
 * {@link IFormFieldDecorator}. The FormFieldComposite provides them a context
 * via the {@link IFormFieldSite}.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @version ($Revision$)
 */
public class FormFieldComposite
        implements IFormFieldSite {

    private static Log log = LogFactory.getLog( FormFieldComposite.class );

    private Property                prop;
    
    private IFormEditorToolkit      toolkit;
    
    private IFormField              field;
    
    private IFormFieldDecorator     decorator;
    
    private IFormFieldLabel         labeler;
    
    private IFormFieldValidator     validator;
    
    private ListenerList            listeners = new ListenerList( ListenerList.IDENTITY );
    
    private boolean                 isDirty = false;
    
    /** The current error, externally set or returned by the validator. */
    private String                  errorMsg;
    
    /** Error message set by {@link #setErrorMessage(String)} */
    private String                  externalErrorMsg;
    

    public FormFieldComposite( IFormEditorToolkit toolkit,
            Property prop, IFormField field,
            IFormFieldLabel labeler, IFormFieldDecorator decorator, IFormFieldValidator validator ) {
        this.prop = prop;
        this.toolkit = toolkit;
        this.field = field;
        this.labeler = labeler;
        this.decorator = decorator;
        this.validator = validator;
    }
    
    
    public Composite createComposite( Composite parent, int style ) {
        Composite result = toolkit.createComposite( parent, style );
        result.setLayout( new FormLayout() );
        
        // label
        labeler.init( this );
        Control labelControl = labeler.createControl( result, toolkit );
        FormData layoutData = new FormData( labeler.getMaxWidth(), SWT.DEFAULT );
        layoutData.left = new FormAttachment( 0 );
        layoutData.top = new FormAttachment( 0, 3 );
        labelControl.setLayoutData( layoutData );
        
        // decorator
        decorator.init( this );
        Control decoControl = decorator.createControl( result, toolkit );
        layoutData = new FormData( 30, SWT.DEFAULT );
        layoutData.left = new FormAttachment( 100, -17 );
        layoutData.right = new FormAttachment( 100 );
        layoutData.top = new FormAttachment( 0, 0 );
        decoControl.setLayoutData( layoutData );
        
        // field
        field.init( this );
        Control fieldControl = field.createControl( result, toolkit );
        layoutData = fieldControl.getLayoutData() != null
                ? (FormData)fieldControl.getLayoutData()
                : new FormData( 100, SWT.DEFAULT );
        layoutData.left = new FormAttachment( labelControl, 5 );
        layoutData.right = new FormAttachment( decoControl, -3 );
        fieldControl.setLayoutData( layoutData );
        
        result.pack( true );
        return result;
    }


    public void dispose() {
        log.debug( "dispose(): ..." );
        if (field != null) {
            field.dispose();
            field = null;
        }
        if (labeler != null) {
            labeler.dispose();
            labeler = null;
        }
        if (decorator != null) {
            decorator.dispose();
            decorator = null;
        }
        listeners.clear();
    }

    public Property getProperty() {
        return prop;
    }

    public boolean isDirty() {
        return isDirty;
    }

    public boolean isValid() {
        return errorMsg == null;
    }
    
    public void load()
    throws Exception {
        field.load();
        //isDirty = false;
    }

    public Object store()
    throws Exception {
        field.store();
        
        // set isDirty and inform decorator
        fireEvent( this, IFormFieldListener.VALUE_CHANGE, getFieldValue() );
        
        return prop.getValue();
    }

    public void setFormFieldValue( Object value ) {
        field.setValue( value );
    }
    
    public void setEnabled( boolean enabled ) {
        field.setEnabled( enabled );
    }

    // IFormFieldSite *************************************
        
    public String getFieldName() {
        return prop.getName().getLocalPart();
    }

    public Object getFieldValue()
    throws Exception {
        return validator.transform2Field( prop.getValue() );
    }

    public void setFieldValue( Object value )
    throws Exception {
        prop.setValue( validator.transform2Model( value ) );
    }

    public IFormEditorToolkit getToolkit() {
        return toolkit;
    }

    public void addChangeListener( IFormFieldListener l ) {
        listeners.add( l );    
    }
    
    public void removeChangeListener( IFormFieldListener l ) {
        listeners.remove( l );
    }
    
    public void fireEvent( Object source, int eventCode, Object newValue ) {
        Object validatedNewValue = null;

        errorMsg = externalErrorMsg;
        
        // check isDirty / validator
        if (eventCode == IFormFieldListener.VALUE_CHANGE && errorMsg == null) {
            if (validator != null) {
                errorMsg = validator.validate( newValue );
            }
            if (errorMsg == null) {
                try {
                    Object value = getFieldValue();
                    if (value == null && newValue == null) {
                        isDirty = false;
                    }
                    else {
                        isDirty = value == null && newValue != null ||
                                value != null && newValue == null ||
                                !value.equals( newValue );
                    }
                    validatedNewValue = validator.transform2Model( newValue );
                }
                catch (Exception e) {
                    // XXX hmmm... what to do?
                    throw new RuntimeException( e );
                }
            }
        }
        // propagate
        FormFieldEvent ev = new FormFieldEvent( source, getFieldName(), field, eventCode, null, validatedNewValue );
        for (Object l : listeners.getListeners()) {
            ((IFormFieldListener)l).fieldChange( ev );
        }
    }

    public String getErrorMessage() {
        return errorMsg;
    }

    public void setErrorMessage( String msg ) {
        externalErrorMsg = msg;
    }

}
