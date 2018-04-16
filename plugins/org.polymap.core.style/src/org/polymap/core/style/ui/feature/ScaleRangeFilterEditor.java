/*
 * polymap.org 
 * Copyright (C) 2016-2018, the @authors. All rights reserved.
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

import org.opengis.filter.Filter;

import com.google.common.collect.Lists;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import org.polymap.core.runtime.i18n.IMessages;
import org.polymap.core.style.Messages;
import org.polymap.core.style.model.feature.ScaleRangeFilter;
import org.polymap.core.style.ui.StylePropertyEditor;
import org.polymap.core.style.ui.StylePropertyFieldSite;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;
import org.polymap.core.ui.UIUtils;

/**
 * Creates a {@link ScaleRangeFilter} for {@link Style#visibleIf} that matches
 * against the current map scale.
 *
 * @author Steffen Stundzig
 * @author Falko Bräutigam
 */
public class ScaleRangeFilterEditor
        extends StylePropertyEditor<ScaleRangeFilter> {

    private static final IMessages i18n = Messages.forPrefix( "ScaleRangeFilterEditor" );

    private final static List<Integer> SCALES = Lists.newArrayList( 
            1, 1000, 5000, 10000, 25000, 50000, 100000, 200000, 250000, 500000, 1000000, 80000000 );


    @Override
    public String label() {
        return i18n.get( "title" );
    }


    @Override
    public boolean init( StylePropertyFieldSite site ) {
        return super.init( site ) && Filter.class.isAssignableFrom( site.targetType() );
    }


    @Override
    public void updateProperty() {
        prop.createValue( ScaleRangeFilter.defaults() );
    }


    @Override
    public Composite createContents( Composite parent ) {
        final Composite contents = super.createContents( parent );
        contents.setLayout( FormLayoutFactory.defaults().spacing( 0 ).create() );

        final Combo maxScaleCombo = new Combo( contents, SWT.SINGLE | SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY );
        final Combo minScaleCombo = new Combo( contents, SWT.SINGLE | SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY );
        
        // min
        minScaleCombo.setVisibleItemCount( 10 );
        minScaleCombo.setItems( SCALES.stream().map( value -> "1:" + value ).toArray( String[]::new ) );
        minScaleCombo.select( SCALES.indexOf( prop.get().minScale.get() ) );
        minScaleCombo.addSelectionListener( UIUtils.selectionListener( ev -> {
            int index = minScaleCombo.getSelectionIndex();
            if (index < SCALES.size() && maxScaleCombo.getSelectionIndex() <= index) {
                maxScaleCombo.select( index + 1 );
            }
            prop.get().minScale.set( SCALES.get( index ) );
        }));

        final Label label = new Label( contents, SWT.NONE | SWT.CENTER );
        label.setText( i18n.get( "to" ) );

        // max
        maxScaleCombo.setVisibleItemCount( 10 );
        maxScaleCombo.setItems( SCALES.stream().map( value -> "1:" + value ).toArray( String[]::new ) );
        maxScaleCombo.select( SCALES.indexOf( prop.get().maxScale.get() ) );
        maxScaleCombo.addSelectionListener( UIUtils.selectionListener( ev -> {
            prop.get().maxScale.set( SCALES.get( maxScaleCombo.getSelectionIndex() ) );
        }));

        FormDataFactory.on( minScaleCombo ).left( 0 ).right( 50, -10 );
        FormDataFactory.on( label ).top( 12 ).left( minScaleCombo ).right( maxScaleCombo );
        FormDataFactory.on( maxScaleCombo ).right( 100 ).left( 50, 10 );
        return contents;
    }
    
}
