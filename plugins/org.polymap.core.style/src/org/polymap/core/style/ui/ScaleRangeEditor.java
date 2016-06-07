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

import org.opengis.filter.Filter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Lists;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import org.polymap.core.runtime.i18n.IMessages;
import org.polymap.core.style.Messages;
import org.polymap.core.style.model.ScaleRangeFilter;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;

/**
 * Matches a range of scales.
 *
 * @author Steffen Stundzig
 */
public class ScaleRangeEditor
        extends StylePropertyEditor<ScaleRangeFilter> {

    private static final IMessages i18n = Messages.forPrefix( "ScaleRange" );

    private static Log log = LogFactory.getLog( ScaleRangeEditor.class );

    private final static List<Integer> scales = Lists.newArrayList( 1, 1000, 5000, 10000, 25000, 50000, 100000, 200000,
            250000, 500000, 1000000, 80000000 );


    @Override
    public String label() {
        return i18n.get( "title" );
    }


    @Override
    public boolean init( StylePropertyFieldSite site ) {
        return Filter.class.isAssignableFrom( targetType( site ) ) ? super.init( site ) : false;
    }


    @Override
    public void updateProperty() {
        prop.createValue( ScaleRangeFilter.defaults() );
    }


    @Override
    public Composite createContents( Composite parent ) {
        final Composite contents = super.createContents( parent );
        contents.setLayout( FormLayoutFactory.defaults().create() );

        final Combo maxScaleCombo = new Combo( contents, SWT.SINGLE | SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY );

        final Combo minScaleCombo = new Combo( contents, SWT.SINGLE | SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY );
        minScaleCombo.setItems( scales.stream().map( value -> "1:" + value ).toArray( String[]::new ) );
        minScaleCombo.select( scales.indexOf( prop.get().minScale.get() ) );
        minScaleCombo.addSelectionListener( new SelectionAdapter() {

            @Override
            public void widgetSelected( SelectionEvent e ) {
                int index = minScaleCombo.getSelectionIndex();
                if (index < scales.size() && maxScaleCombo.getSelectionIndex() <= index) {
                    maxScaleCombo.select( index + 1 );
                }
                prop.get().minScale.set( scales.get( index ) );
            }
        } );

        final Label label = new Label( contents, SWT.NONE | SWT.CENTER );
        label.setText( i18n.get( "to" ) );

        maxScaleCombo.setItems( scales.stream().map( value -> "1:" + value ).toArray( String[]::new ) );
        maxScaleCombo.select( scales.indexOf( prop.get().maxScale.get() ) );
        maxScaleCombo.addSelectionListener( new SelectionAdapter() {

            @Override
            public void widgetSelected( SelectionEvent e ) {
                prop.get().maxScale.set( scales.get( maxScaleCombo.getSelectionIndex() ) );
            }
        } );

        FormDataFactory.on( minScaleCombo ).left( 0 ).right( 43 );
        FormDataFactory.on( label ).top( 12 ).left( minScaleCombo, 2 ).right( 57 );
        FormDataFactory.on( maxScaleCombo ).left( label, 2 ).right( 100 );
        return contents;
    }
}
