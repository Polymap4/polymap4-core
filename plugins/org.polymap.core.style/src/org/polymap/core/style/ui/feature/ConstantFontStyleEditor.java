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
package org.polymap.core.style.ui.feature;

import java.util.List;

import com.google.common.collect.Lists;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

import org.polymap.core.runtime.i18n.IMessages;
import org.polymap.core.style.Messages;
import org.polymap.core.style.model.feature.ConstantFontStyle;
import org.polymap.core.style.model.feature.FontStyle;
import org.polymap.core.style.ui.StylePropertyEditor;
import org.polymap.core.style.ui.StylePropertyFieldSite;

import org.polymap.model2.runtime.ValueInitializer;

/**
 * Editor that creates one {@link ConstantFontStyle}.
 *
 * @author Steffen Stundzig
 */
public class ConstantFontStyleEditor
        extends StylePropertyEditor<ConstantFontStyle> {

    private static final IMessages i18n = Messages.forPrefix( "ConstantFontStyleEditor", "ConstantEditor" );

    private final static List<FontStyle> content = Lists.newArrayList( FontStyle.values() );


    @Override
    public String label() {
        return i18n.get( "title" );
    }


    @Override
    public boolean init( StylePropertyFieldSite site ) {
        return FontStyle.class.isAssignableFrom( targetType( site ) ) ? super.init( site ) : false;
    }


    @Override
    public void updateProperty() {
        prop.createValue( new ValueInitializer<ConstantFontStyle>() {

            @Override
            public ConstantFontStyle initialize( ConstantFontStyle proto ) throws Exception {
                proto.value.set( FontStyle.normal );
                return proto;
            }
        } );
    }


    @Override
    public Composite createContents( Composite parent ) {
        Composite contents = super.createContents( parent );
        Combo combo = new Combo( contents, SWT.SINGLE | SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY );

        combo.setItems( content.stream().map( FontStyle::name ).map( n -> i18n.get( n ) ).toArray( String[]::new ) );
        combo.select( content.indexOf( prop.get().value.get() ) );

        combo.addSelectionListener( new SelectionAdapter() {

            @Override
            public void widgetSelected( SelectionEvent e ) {
                prop.get().value.set( content.get( combo.getSelectionIndex() ) );
            }
        } );
        return contents;
    }
}
