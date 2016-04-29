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

import org.polymap.core.style.model.ConstantStrokeJoinStyle;
import org.polymap.core.style.model.StrokeJoinStyle;

import org.polymap.model2.runtime.ValueInitializer;

/**
 * Editor that creates one {@link ConstantStrokeJoinStyle}.
 *
 * @author Steffen Stundzig
 */
class ConstantStrokeJoinStyleEditor
        extends StylePropertyEditor<ConstantStrokeJoinStyle> {

    private static Log                        log     = LogFactory.getLog( ConstantStrokeJoinStyleEditor.class );

    private final static List<StrokeJoinStyle> content = Lists.newArrayList( StrokeJoinStyle.values() );


    @Override
    public String label() {
        return "A join style";
    }


    @Override
    public boolean init( StylePropertyFieldSite site ) {
        return StrokeJoinStyle.class.isAssignableFrom( targetType( site ) ) ? super.init( site ) : false;
    }


    @Override
    public void updateProperty() {
        prop.createValue( new ValueInitializer<ConstantStrokeJoinStyle>() {

            @Override
            public ConstantStrokeJoinStyle initialize( ConstantStrokeJoinStyle proto ) throws Exception {
                proto.joinStyle.set( StrokeJoinStyle.round );
                return proto;
            }
        } );
    }


    @Override
    public Composite createContents( Composite parent ) {
        Composite contents = super.createContents( parent );
        Combo combo = new Combo( contents, SWT.SINGLE | SWT.BORDER | SWT.DROP_DOWN );

        combo.setItems( content.stream().map( StrokeJoinStyle::name ).toArray( String[]::new ) );
        combo.select( content.indexOf( prop.get().joinStyle.get() ) );

        combo.addSelectionListener( new SelectionAdapter() {

            @Override
            public void widgetSelected( SelectionEvent e ) {
                prop.get().joinStyle.set( content.get( combo.getSelectionIndex() ) );
            }
        } );
        return contents;
    }

}
