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
package org.polymap.rhei.field;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

import org.polymap.rhei.form.IFormEditorToolkit;
import org.polymap.rhei.internal.form.FormEditorToolkit;

/**
 *
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @version ($Revision$)
 */
public class StringFormField
        implements IFormField {

    private static Log log = LogFactory.getLog( StringFormField.class );

    private IFormFieldSite          site;

    private Text                    text;

    // XXX use (proper) validator to make the translation to String
    private Object                  loadedValue;
    
    private boolean                 deferredEnabled = true;


    public void init( IFormFieldSite _site ) {
        this.site = _site;
    }

    public void dispose() {
    }

    public Control createControl( Composite parent, IFormEditorToolkit toolkit ) {
        return createControl( parent, toolkit, SWT.NONE );
    }

    protected Control createControl( Composite parent, IFormEditorToolkit toolkit, int style ) {
        text = toolkit.createText( parent, "", style );

        // modify listener
        text.addModifyListener( new ModifyListener() {
            public void modifyText( ModifyEvent ev ) {
                log.debug( "modifyEvent(): test= " + text.getText() );
                site.fireEvent( StringFormField.this, IFormFieldListener.VALUE_CHANGE,
                        loadedValue == null && text.getText().equals( "" ) ? null : text.getText() );
            }
        });
        // focus listener
        text.addFocusListener( new FocusListener() {
            public void focusLost( FocusEvent event ) {
                site.fireEvent( StringFormField.this, IFormFieldListener.FOCUS_LOST, text.getText() );
            }
            public void focusGained( FocusEvent event ) {
                site.fireEvent( StringFormField.this, IFormFieldListener.FOCUS_GAINED, text.getText() );
            }
        });
        text.setEnabled( deferredEnabled );
        text.setBackground( deferredEnabled ? FormEditorToolkit.textBackground : FormEditorToolkit.textBackgroundDisabled );
        return text;
    }

    public IFormField setEnabled( boolean enabled ) {
        if (text != null) {
            text.setEnabled( enabled );
            text.setBackground( enabled ? FormEditorToolkit.textBackground : FormEditorToolkit.textBackgroundDisabled );
        }
        else {
            deferredEnabled = enabled;
        }
        return this;
    }

    /**
     * Explicitly set the value of the text field. This causes events to be
     * fired just like the value was typed in.
     */
    public IFormField setValue( Object value ) {
        text.setText( value != null ? (String)value : "" );
        return this;
    }

    public void load() throws Exception {
        assert text != null : "Control is null, call createControl() first.";

        loadedValue = site.getFieldValue();
        text.setText( loadedValue != null ? loadedValue.toString() : "" );
    }

    public void store() throws Exception {
        // XXX what is the semantics?
//        if (text.getEnabled() && (text.getStyle() | SWT.READ_ONLY) == 0) {
            site.setFieldValue( text.getText() );
//        }
    }

}
