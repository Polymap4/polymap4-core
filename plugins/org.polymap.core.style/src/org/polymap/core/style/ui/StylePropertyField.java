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

import static org.polymap.core.ui.FormDataFactory.on;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import org.polymap.core.runtime.config.Config;
import org.polymap.core.runtime.config.Configurable;
import org.polymap.core.style.model.StylePropertyValue;
import org.polymap.core.ui.FormLayoutFactory;

import org.polymap.model2.runtime.PropertyInfo;

/**
 * The viewer of an {@link StylePropertyValue}. 
 *
 * @author Falko Bräutigam
 */
public class StylePropertyField
        extends Configurable {

    private static Log log = LogFactory.getLog( StylePropertyField.class );
    
    public Config<String>                       title;
    
    public Config<String>                       tooltip;
    
    private PropertyInfo<StylePropertyValue>    propInfo;

    private Composite                           contents;

    private Combo                               combo;

    
    public StylePropertyField( PropertyInfo<StylePropertyValue> propInfo ) {
        this.propInfo = propInfo;
        this.title.set( propInfo.getDescription().orElse( "" ) );
    }


    public Control createContents( Composite parent ) {
        assert contents == null : "StylePropertyField can be created only once.";
        
        contents = new Composite( parent, SWT.NONE );
        contents.setLayout( FormLayoutFactory.defaults().create() );
        tooltip.ifPresent( txt -> contents.setToolTipText( txt ) );
        
        Label t = new Label( contents, SWT.NONE );
        t.setText( title.get() );

        combo = new Combo( contents, SWT.READ_ONLY );
        combo.setVisibleItemCount( 5 );
        
        // layout
        on( t ).fill().noBottom();
        on( combo ).top( t ).left( 0 ).right( 50 );
        return contents;
    }
    
    
//    protected List<StylePropertyEditor> availableEditors() {
//        
//    }
    
}
