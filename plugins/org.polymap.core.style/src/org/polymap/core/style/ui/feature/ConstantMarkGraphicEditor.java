/*
 * polymap.org 
 * Copyright (C) 2016-2018, the @authors. All rights reserved.
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
package org.polymap.core.style.ui.feature;

import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

import org.polymap.core.runtime.i18n.IMessages;
import org.polymap.core.style.Messages;
import org.polymap.core.style.model.feature.ConstantGraphic;
import org.polymap.core.style.model.feature.Graphic;
import org.polymap.core.style.model.feature.Graphic.WellKnownMark;
import org.polymap.core.style.ui.StylePropertyEditor;
import org.polymap.core.style.ui.StylePropertyFieldSite;
import org.polymap.core.ui.UIUtils;

import org.polymap.model2.runtime.NotNullableException;

/**
 * 
 * @author Falko Bräutigam
 */
public class ConstantMarkGraphicEditor
        extends StylePropertyEditor<ConstantGraphic> {

    private static final IMessages i18n = Messages.forPrefix( "ConstantMarkGraphicEditor" );

    private final static List<WellKnownMark> VALUES = Lists.newArrayList( WellKnownMark.values() );


    @Override
    public String label() {
        return i18n.get( "title" );
    }


    @Override
    public boolean init( StylePropertyFieldSite site ) {
        return Graphic.class.isAssignableFrom( targetType( site ) ) ? super.init( site ) : false;
    }


    @Override
    public void updateProperty() {
        prop.createValue( ConstantGraphic.defaults( WellKnownMark.Circle ) );
    }


    @Override
    public Composite createContents( Composite parent ) {
        Composite contents = super.createContents( parent );
        Combo combo = new Combo( contents, SWT.SINGLE | SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY );
        combo.setVisibleItemCount( 6 );

        List<String> items = VALUES.stream().map( v -> /*i18n.get(*/ v.name() ).collect( Collectors.toList() );
        combo.setItems( items.toArray( new String[items.size()]) );
        try {
            combo.select( items.indexOf( prop.get().markOrName.get() ) );
        }
        catch (NotNullableException e) {
            // previous code without .markOrName
            combo.select( 0 );
        }

        combo.addSelectionListener( UIUtils.selectionListener( e -> {
            prop.get().markOrName.set( VALUES.get( combo.getSelectionIndex() ).name() );
        }));
        return contents;
    }
    
}
