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

import static org.apache.commons.lang3.StringUtils.defaultString;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import org.polymap.core.ui.StatusDispatcher;

/**
 * 
 *
 * @author Falko Bräutigam
 */
public class StringSupplier
        extends InputFieldSupplier {

    private static final Log log = LogFactory.getLog( StringSupplier.class );
    
    private Text                text;
    
    
    @Override
    public String label() {
        return "Text";
    }
    
    
    @Override
    public boolean init( @SuppressWarnings( "hiding" ) FieldViewerSite site ) {
        return super.init( site ) && String.class.isAssignableFrom( site.fieldInfo.get().type() );
    }


    @Override
    public void createContents( Composite parent ) {
        text = new Text( parent, SWT.BORDER );
        text.setFont( parent.getFont() );

        text.setText( defaultString( site.getFieldValue(), "" ) );
        
        text.addModifyListener( ev -> {
            try {
                site.setFieldValue( text.getText() );
            }
            catch (Exception e) {
                StatusDispatcher.handleError( "Value was not set properly.", e );
            }
        });
    }

}
