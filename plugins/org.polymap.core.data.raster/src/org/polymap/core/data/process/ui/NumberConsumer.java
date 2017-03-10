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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import org.polymap.core.runtime.Polymap;

import org.polymap.rhei.field.IFormFieldValidator;
import org.polymap.rhei.field.NumberValidator;

/**
 * 
 *
 * @author Falko Bräutigam
 */
public class NumberConsumer
        extends OutputFieldConsumer {

    private static final Log log = LogFactory.getLog( NumberConsumer.class );

    private IFormFieldValidator<String,? extends Number> validator;

    
    @Override
    public String label() {
        return "Number";
    }
    
    
    @Override
    public boolean init( @SuppressWarnings( "hiding" ) FieldViewerSite site ) {
        if (super.init( site )) {
            Class<?> fieldType = site.fieldInfo.get().type.get();
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
        Text text = new Text( parent, SWT.BORDER );
        try {
            text.setText( validator.transform2Field( site.getFieldValue() ) );
        }
        catch (Exception e) {
            log.warn( "", e );
            text.setText( "Error: " + e.getMessage() );
        }
    }
    
}
