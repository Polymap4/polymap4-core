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
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.polymap.core.style.model.StylePropertyValue;

/**
 * 
 *
 * @author Falko Bräutigam
 */
abstract class StylePropertyEditor<SPV extends StylePropertyValue> {

    private static Log log = LogFactory.getLog( StylePropertyEditor.class );
    
    protected SPV               spv;
    
    
    /**
     * The human readable name of this editor. Usually displayed in the UI to
     * select/indentify this editor.
     */
    public abstract String label();
    
    
    public Control createContents( Composite parent ) {
        Composite contents = new Composite( parent, SWT.BORDER );
        return contents;
    }

    
    /**
     * Creates a control that displays the current value of this editor.
     */
    public Composite createValueContents( Composite parent ) {
        Composite contents = new Composite( parent, SWT.BORDER );
        contents.setLayout( new FillLayout( SWT.HORIZONTAL ) );
        return contents;
    }
    
}
