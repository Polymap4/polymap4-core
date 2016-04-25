/*
 * polymap.org Copyright (C) 2016, the @authors. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 3.0 of the License, or (at your option) any later
 * version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */
package org.polymap.core.style.ui;

import java.awt.Color;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.polymap.core.runtime.i18n.IMessages;
import org.polymap.core.style.Messages;
import org.polymap.core.style.model.ConstantColor;

import org.polymap.model2.Property;
import org.polymap.model2.runtime.ValueInitializer;

/**
 * Editor that creates one {@link ConstantColor}.
 *
 * @author Steffen Stundzig
 */
class ConstantColorEditor
        extends StylePropertyEditor<ConstantColor> {
    
    private static final IMessages i18n = Messages.forPrefix( "ColorEditor" );

    private static Log log = LogFactory.getLog( ConstantColorEditor.class );


    @Override
    public String label() {
        return i18n.get( "title" );
    }


    @Override
    public boolean init( Property<ConstantColor> _prop ) {
        return Color.class.isAssignableFrom( targetType( _prop ) ) ? super.init( _prop ) : false;
    }


    @Override
    public void updateProperty() {
        prop.createValue( new ValueInitializer<ConstantColor>() {
            @Override
            public ConstantColor initialize( ConstantColor proto ) throws Exception {
                // TODO default value here
                proto.r.set( 255 );
                proto.g.set( 0 );
                proto.b.set( 0 );
                return proto;
            }
        } );
    }


    @Override
    public Composite createContents( Composite parent ) {
        Composite contents = super.createContents( parent );
        final Button button = new Button( parent, SWT.PUSH );
        button.setText( i18n.get( "choose" ) );
        button.addSelectionListener( new SelectionAdapter() {
            @Override
            public void widgetSelected( SelectionEvent e ) {
                ColorChooser cc = new ColorChooser( new RGB( prop.get().r.get(), prop.get().g.get(), prop.get().b.get() ) );
                UIService.instance().openDialog( cc.title(), dialogParent -> {
                    cc.createContents( dialogParent );
                }, () -> {
                    RGB rgb = cc.getRGB();
                    prop.get().r.set( rgb.red );
                    prop.get().b.set( rgb.blue );
                    prop.get().g.set( rgb.green );
                    button.setBackground( new org.eclipse.swt.graphics.Color( button.getDisplay(), rgb ) );
                    return true;
                } );
            }

        } );
        button.setBackground( new org.eclipse.swt.graphics.Color( button.getDisplay(), new RGB( prop.get().r.get(), prop.get().g.get(), prop.get().b.get() ) ) );
        return contents;
    }
}
