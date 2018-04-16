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

import static org.polymap.core.style.serialize.sld2.ShadowStyleSerializer.SUPPORTED;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;

import org.polymap.core.runtime.i18n.IMessages;
import org.polymap.core.style.Messages;
import org.polymap.core.style.model.Style;
import org.polymap.core.style.model.feature.ConstantNumber;
import org.polymap.core.style.model.feature.ConstantStyleId;
import org.polymap.core.style.model.feature.ShadowStyle.StyleId;
import org.polymap.core.style.ui.StylePropertyEditor;
import org.polymap.core.style.ui.StylePropertyFieldSite;
import org.polymap.core.ui.UIUtils;

/**
 * Editor that creates one {@link ConstantNumber}.
 *
 * @author Falko Bräutigam
 * @author Steffen Stundzig
 */
public class ConstantStyleIdEditor
        extends StylePropertyEditor<ConstantStyleId> {

    private static final IMessages i18n = Messages.forPrefix( "ConstantStyleIdEditor", "ConstantEditor" );
    
    private Map<String,Style>           allowed;
    

    @Override
    public String label() {
        return i18n.get( "title" );
    }

    @Override
    public boolean init( StylePropertyFieldSite site ) {
        if (StyleId.class.isAssignableFrom( targetType( site ) ) && super.init( site )) {
            allowed = site().mapStyle.get().members().stream()
                    .filter( s -> SUPPORTED.contains( s.getClass() ) )
                    .peek( s -> s.id.set( s.id.opt().orElse( UUID.randomUUID().toString() ) ) )  // support old styles
                    .collect( Collectors.toMap( s -> s.id.get(), s -> s ) );
            return true;
        }
        return false;
    }


    @Override
    public void updateProperty() {
        if (!allowed.isEmpty()) {
            prop.createValue( ConstantStyleId.defaults( allowed.values().stream().findAny().orElse( null ) ) );
        }
    }


    @Override
    public Composite createContents( Composite parent ) {
        Composite contents = super.createContents( parent );
        //
        if (!allowed.isEmpty()) {
            ComboViewer combo = new ComboViewer( contents, SWT.SINGLE | SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY );
            combo.getCombo().setVisibleItemCount( 8 );
            combo.setLabelProvider( new LabelProvider() {
                @Override public String getText( Object elm ) {
                    return ((Style)elm).title.get();
                }
            });
            combo.setContentProvider( ArrayContentProvider.getInstance() );
            combo.setInput( allowed.values() );

            Style current = allowed.get( prop.get().styleId.get() );
            if (current != null) {
                combo.setSelection( new StructuredSelection( current) );
            }
            combo.addSelectionChangedListener( ev -> {
                Style selected = UIUtils.selection( ev.getSelection() ).first( Style.class ).get();
                prop.createValue( ConstantStyleId.defaults( selected ) );
            });
        }
        //
        else {
            new Label( contents, SWT.NONE ).setText( "Add an other style first." );
        }

        return contents;
    }
    
}
