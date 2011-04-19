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
package org.polymap.rhei.filter;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import java.security.Principal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.core.runtime.ListenerList;

import org.polymap.core.runtime.Polymap;
import org.polymap.core.workbench.PolymapWorkbench;
import org.polymap.rhei.Messages;
import org.polymap.rhei.RheiPlugin;
import org.polymap.rhei.field.FormFieldEvent;
import org.polymap.rhei.field.IFormField;
import org.polymap.rhei.field.IFormFieldListener;
import org.polymap.rhei.field.IFormFieldValidator;
import org.polymap.rhei.field.NullValidator;
import org.polymap.rhei.form.IFormEditorToolkit;
import org.polymap.rhei.internal.DefaultFormFieldDecorator;
import org.polymap.rhei.internal.DefaultFormFieldLabeler;
import org.polymap.rhei.internal.filter.FilterFieldComposite;

/**
 * Provides a standard UI for filters. This class can be used by subclasses of
 * {@link IFilter} to provide a standard {@link FilterDialog} or {@link FilterView}.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class FilterEditor
        implements IFilterEditorSite, IFormFieldListener {

    private static Log log = LogFactory.getLog( FilterEditor.class );

    /** Saved values from last dialog run for each user. */
    static Map<Principal,Map<String,Object>> defaultValues = new WeakHashMap();
    
    private IFormEditorToolkit              toolkit;
    
    private Map<String,FilterFieldComposite> fields = new HashMap();
    
    private Map<String,Object>              fieldValues = new HashMap();
    
    private boolean                         isValid = true;
    
    private boolean                         isDirty = false;
    
    /** Listeners of type {@link IFormFieldListener}. */
    private ListenerList                    listeners = new ListenerList( ListenerList.IDENTITY );

    
    public FilterEditor() {
    }

    
//    /**
//     *
//     * @return
//     */
//    protected abstract Composite createControl( Composite parent );
    
    public synchronized void dispose() {
        for (FilterFieldComposite field : fields.values()) {
            field.dispose();
        }
        fields.clear();
        fieldValues.clear();
        listeners.clear();
    }

    public boolean isDirty() {
        return isDirty;
    }

    public boolean isValid() {
        return isValid;
    }

    public IFormEditorToolkit getToolkit() {
        return toolkit;
    }
    
    void setToolkit( IFormEditorToolkit toolkit ) {
        this.toolkit = toolkit;
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
                validator != null ? validator : new NullValidator() );

        fieldComposite.addChangeListener( this );

        fields.put( fieldComposite.getFieldName(), fieldComposite );

        return fieldComposite.createComposite( parent, SWT.NONE );
    }

    
    public abstract Composite createStandardLayout( Composite parent );
    
    public abstract void addStandardLayout( Composite composite );
    
//    public Composite createStandardLayout( Composite parent ) {
//        Composite result = new Composite( parent, SWT.NONE );
//        GridData gridData = new GridData( GridData.FILL_BOTH );
//        gridData.grabExcessHorizontalSpace = true;
//        result.setLayoutData( gridData );
//
//        layoutLast = null;
//
//        FormLayout layout = new FormLayout();
//        layout.marginWidth = 10;
//        layout.marginHeight = 10;
//        result.setLayout( layout );
//        return result;
//    }
//
//    
//    public void addStandardLayout( Composite composite ) {
//        FormData data = new FormData();
//        data.left = new FormAttachment( 0, 0 );
//        data.right = new FormAttachment( 100, 0 );
//        if (layoutLast != null) {
//            data.top = new FormAttachment( layoutLast, 3 );
//        }
//        composite.setLayoutData( data );
//        layoutLast = composite;
//    }

    
    public void addFieldListener( IFormFieldListener listener ) {
        listeners.add( listener );
    }

    
    public void removeFieldListener( IFormFieldListener listener ) {
        listeners.remove( listener );
    }

    
    public void fieldChange( FormFieldEvent ev ) {
        // record value
        if (ev.getEventCode() == VALUE_CHANGE) {
            fieldValues.put( ev.getFieldName(), ev.getNewValue() );
            isDirty = true;
        }
        // check validity
        isValid = true;
        for (FilterFieldComposite fc : fields.values()) {
            if (!fc.isValid()) {
                isValid = false;
                break;
            }
        }

        // XXX a event scope is needed when registering for listener for field to distinguish
        // between local event within that field or changes from other fields in the page or whole form

        //                 // propagate event to all fields
        //                 for (FormFieldComposite field : fields) {
        //                     if (field.getFormField() != ev.getFormField()) {
        //                         field.fireEvent( ev.getEventCode(), ev.getNewValue() );
        //                     }
        //                 }

        for (Object l : listeners.getListeners()) {
            ((IFormFieldListener)l).fieldChange( ev );
        }
    }

    
    public Object getFieldValue( String propertyName ) {
        return fieldValues.get( propertyName );
    }

    
    protected void doSubmit() {
        try {
            // reset default values
            Map<String,Object> userDefaults = new HashMap();
            FilterEditor.defaultValues.put( Polymap.instance().getUser(), userDefaults );

            // fieldValues are already filled in the event handler; we call store() method
            // of the field anyway in order to keep the contract. Some form fields, for
            // example BetweenFormField, provide a different value via the store value
            for (FilterFieldComposite field : fields.values()) {
                if (field.isDirty()) {
                    field.getFormField().store();

                    String name = field.getFieldName();
                    Object value = field.getValue();
                    fieldValues.put( name, value );

                    userDefaults.put( name, value );
                }
            }
        }
        catch (Exception e) {
            PolymapWorkbench.handleError( RheiPlugin.PLUGIN_ID, this, Messages.get( "FilterEditor_okError" ), e );
        }
    }

    
    protected void doLoad() {
        Map<String, Object> userDefaults = FilterEditor.defaultValues.get( Polymap.instance().getUser() );
        if (userDefaults != null) {

            for (FilterFieldComposite fieldComposite : fields.values()) {
                try {
                    String propName = fieldComposite.getFieldName();
                    Object defaultValue = userDefaults.get( propName );

                    if (defaultValue != null) {
                        FilterEditor.log.debug( "   " + propName + ": " + defaultValue );
                        fieldComposite.loadDefaultValue( defaultValue );

                        fieldValues.put( propName, defaultValue );
                    }
                }
                catch (Exception e) {
                    FilterEditor.log.warn( e, e );
                }
            }
        }
        isDirty = false;
    }

    
    protected void doReset() {
        FilterEditor.defaultValues.remove( Polymap.instance().getUser() );

        for (FilterFieldComposite fieldComposite : fields.values()) {
            try {
                String propName = fieldComposite.getFieldName();
                fieldComposite.loadDefaultValue( null );

                fieldValues.put( propName, null );
            }
            catch (Exception e) {
                FilterEditor.log.warn( e, e );
            }
        }
        isDirty = false;
        isValid = true;
    }
    

    public Composite getPageBody() {
        throw new RuntimeException( "not implemented." );
    }

}
