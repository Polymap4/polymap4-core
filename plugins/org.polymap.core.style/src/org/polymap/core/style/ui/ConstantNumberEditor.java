/* 
 * polymap.org
 * Copyright (C) 2016, the @authors. All rights reserved.
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
package org.polymap.core.style.ui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import org.polymap.core.style.model.ConstantNumber;

import org.polymap.model2.Property;

/**
 * Editor that creates one {@link ConstantNumber}. 
 *
 * @author Falko Bräutigam
 */
class ConstantNumberEditor
        extends StylePropertyEditor<ConstantNumber> {

    private static Log log = LogFactory.getLog( ConstantNumberEditor.class );
    
    @Override
    public String label() {
        return "Constant number";
    }


    @Override
    public boolean init( Property<ConstantNumber> _prop ) {
        return Number.class.isAssignableFrom( targetType( _prop ) ) ? super.init( _prop ) : false;
    }


    @Override
    public Composite createContents( Composite parent ) {
        Composite contents = super.createContents( parent );
        Text t = new Text( contents, SWT.BORDER );
        t.setText( "" + prop.get().value.get() );
        return contents;
    }
    
}
