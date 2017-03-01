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

/**
 * 
 *
 * @author Falko Bräutigam
 */
public class NumberSupplier
        extends InputFieldSupplier {

    private static final Log log = LogFactory.getLog( NumberSupplier.class );
    
    private Text            text;

    
    @Override
    public String label() {
        return "Number";
    }
    
    
    @Override
    public boolean init( @SuppressWarnings( "hiding" ) FieldViewerSite site ) {
        if (super.init( site )) {
            Class<?> fieldType = site.fieldInfo.get().type.get();
            log.info( "Type: " + fieldType );
            return Number.class.isAssignableFrom( fieldType ) 
                    || Integer.TYPE.equals( fieldType )
                    || Double.TYPE.equals( fieldType );
        }
        return false;
    }


    @Override
    public void createContents( Composite parent ) {
        text = new Text( parent, SWT.BORDER );
        text.setFont( parent.getFont() );
        
        text.setText( site.getFieldValue().toString() );
        text.addModifyListener( ev -> {
            supply();
        });
    }


    public void supply() {
        throw new RuntimeException( "not yet implemented" );
        //site.setFieldValue( text.getText() );
    }

}
