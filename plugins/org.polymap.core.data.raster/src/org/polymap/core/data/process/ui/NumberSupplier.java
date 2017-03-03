/* 
 * polymap.org
 * Copyright (C) 2017, the @authors. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.core.data.process.ui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import org.polymap.core.runtime.Polymap;
import org.polymap.core.ui.StatusDispatcher;

import org.polymap.rhei.field.IFormFieldValidator;
import org.polymap.rhei.field.NumberValidator;

/**
 * 
 *
 * @author Falko Bräutigam
 */
public class NumberSupplier
        extends InputFieldSupplier {

    private static final Log log = LogFactory.getLog( NumberSupplier.class );
    
    private Text                text;
    
    private IFormFieldValidator<String,? extends Number> validator;

    private Color               defaultForeground;

    
    @Override
    public String label() {
        return "Number";
    }
    
    
    @Override
    public boolean init( @SuppressWarnings( "hiding" ) FieldViewerSite site ) {
        if (super.init( site )) {
            Class<?> fieldType = site.fieldInfo.get().type.get();
            log.info( "Type: " + fieldType );
            if (Integer.class.isAssignableFrom( fieldType ) || Integer.TYPE.equals( fieldType )) {
                validator = new NumberValidator( Integer.class, Polymap.getSessionLocale(), 10, 0, 1, 0 );
                return true;                
            }
            else if (Long.class.isAssignableFrom( fieldType ) || Long.TYPE.equals( fieldType )) {
                validator = new NumberValidator( Integer.class, Polymap.getSessionLocale(), 10, 0, 1, 0 );
                return true;                
            }
            else if (Double.class.isAssignableFrom( fieldType ) || Double.TYPE.equals( fieldType )) {
                validator = new NumberValidator( Double.class, Polymap.getSessionLocale(), 10, 5, 1, 1 );
                return true;                
            }
            else if (Float.class.isAssignableFrom( fieldType ) || Float.TYPE.equals( fieldType )) {
                validator = new NumberValidator( Double.class, Polymap.getSessionLocale(), 10, 5, 1, 1 );
                return true;                
            }
        }
        return false;
    }


    @Override
    public void createContents( Composite parent ) {
        text = new Text( parent, SWT.BORDER );
        text.setFont( parent.getFont() );
        defaultForeground = text.getForeground();

        // init text value
        try {
            String textValue = validator.transform2Field( site.getFieldValue() );
            text.setText( textValue != null ? textValue : "" );
        }
        catch (Exception e) {
            log.warn( "", e );
        }
        
        text.addModifyListener( ev -> {
            // validate
            String msg = validator.validate( text.getText() );
            if (msg != null) {
                text.setForeground( FieldViewer.errorColor() );
                text.setToolTipText( msg );
            }
            else {
                text.setForeground( defaultForeground );
                text.setToolTipText( null );
                try {
                    // set value
                    Number value = validator.transform2Model( text.getText() );
                    site.setFieldValue( value );
                }
                catch (Exception e) {
                    StatusDispatcher.handleError( "Value was not set properly.", e );
                }
            }
        });
    }

}
