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
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import org.polymap.core.ui.UIUtils;

/**
 * 
 *
 * @author Falko Bräutigam
 */
public class BooleanSupplier
        extends InputFieldSupplier {

    private static final Log log = LogFactory.getLog( BooleanSupplier.class );
    
    private Button              checkbox;

    @Override
    public String label() {
        return "Switch";
    }
    
    
    @Override
    public boolean init( @SuppressWarnings( "hiding" ) FieldViewerSite site ) {
        if (super.init( site )) {
            Class<?> fieldType = site.fieldInfo.get().type();
            return Boolean.class.isAssignableFrom( fieldType ) || Boolean.TYPE.equals( fieldType );
        }
        return false;
    }


    @Override
    public void createContents( Composite parent ) {
        assert checkbox == null;
        checkbox = new Button( parent, SWT.CHECK );
        checkbox.setSelection( site.getFieldValue() );
        checkbox.addSelectionListener( UIUtils.selectionListener( ev -> {
            supply();
        }));
    }


    public void supply() {
        site.setFieldValue( checkbox.getSelection() );
    }

}
