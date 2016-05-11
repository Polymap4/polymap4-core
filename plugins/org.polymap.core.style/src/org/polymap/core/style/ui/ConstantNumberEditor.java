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
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import org.polymap.core.runtime.i18n.IMessages;
import org.polymap.core.style.Messages;
import org.polymap.core.style.model.ConstantNumber;
import org.polymap.core.style.model.DoubleRange;
import org.polymap.core.style.model.IntRange;

import org.polymap.model2.runtime.ValueInitializer;

/**
 * Editor that creates one {@link ConstantNumber}. 
 *
 * @author Falko Bräutigam
 * @author Steffen Stundzig
 */
class ConstantNumberEditor
        extends StylePropertyEditor<ConstantNumber> {

    private static final IMessages i18n = Messages.forPrefix( "ConstantNumber" );

    private static Log log = LogFactory.getLog( ConstantNumberEditor.class );
    
    @Override
    public String label() {
        return i18n.get( "title");
    }


    @Override
    public boolean init( StylePropertyFieldSite site ) {
        return Number.class.isAssignableFrom( targetType( site ) ) ? super.init( site ) : false;
    }


    @Override
    public void updateProperty() {
        prop.createValue( new ValueInitializer<ConstantNumber>() {
            @Override
            public ConstantNumber initialize( ConstantNumber proto ) throws Exception {
                IntRange ai = (IntRange)prop.info().getAnnotation( IntRange.class );
                DoubleRange ad = (DoubleRange)prop.info().getAnnotation( DoubleRange.class );
                if (ai != null) {
                    proto.value.set( ai.defaultValue() );
                }
                else if (ad != null) {
                    proto.value.set( ad.defaultValue() );
                }
                else {
                    throw new RuntimeException( "No Number value range defined for: " + prop );
                }
                return proto;
            }
        });
    }


    @Override
    public Composite createContents( Composite parent ) {
        Composite contents = super.createContents( parent );
        Text t = new Text( contents, SWT.BORDER );
        t.setText( prop.get().value.get().toString() );
        
        t.addModifyListener( new ModifyListener() {
            @Override
            public void modifyText( ModifyEvent ev ) {
                // XXX
                Double newValue = Double.valueOf( t.getText() );
                prop.get().value.set( newValue );
            }
        });
        return contents;
    }
    
}
