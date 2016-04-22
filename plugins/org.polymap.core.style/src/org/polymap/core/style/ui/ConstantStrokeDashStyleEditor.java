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

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Lists;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

import org.polymap.core.style.model.ConstantStrokeDashStyle;
import org.polymap.core.style.model.StrokeDashStyle;

import org.polymap.model2.Property;
import org.polymap.model2.runtime.ValueInitializer;

/**
 * Editor that creates one {@link ConstantStrokeDashStyle}.
 *
 * @author Steffen Stundzig
 */
class ConstantStrokeDashStyleEditor
        extends StylePropertyEditor<ConstantStrokeDashStyle> {

    private static Log                         log     = LogFactory.getLog( ConstantStrokeDashStyleEditor.class );

    private final static List<StrokeDashStyle> content = Lists.newArrayList( StrokeDashStyle.values() );


    @Override
    public String label() {
        return "Constant dash style";
    }


    @Override
    public boolean init( Property<ConstantStrokeDashStyle> _prop ) {
        return StrokeDashStyle.class.isAssignableFrom( targetType( _prop ) ) ? super.init( _prop ) : false;
    }


    @Override
    public void updateProperty() {
        prop.createValue( new ValueInitializer<ConstantStrokeDashStyle>() {

            @Override
            public ConstantStrokeDashStyle initialize( ConstantStrokeDashStyle proto ) throws Exception {
                proto.dashStyle.set( StrokeDashStyle.solid );
                return proto;
            }
        } );
    }


    @Override
    public Composite createContents( Composite parent ) {
        Composite contents = super.createContents( parent );
        Combo combo = new Combo( contents, SWT.SINGLE | SWT.BORDER | SWT.DROP_DOWN );

        combo.setItems( content.stream().map( StrokeDashStyle::name ).toArray( String[]::new ) );
        combo.select( content.indexOf( prop.get().dashStyle.get() ) );

        combo.addSelectionListener( new SelectionAdapter() {

            @Override
            public void widgetSelected( SelectionEvent e ) {
                prop.get().dashStyle.set( content.get( combo.getSelectionIndex() ) );
            }
        } );
        return contents;
    }

}
