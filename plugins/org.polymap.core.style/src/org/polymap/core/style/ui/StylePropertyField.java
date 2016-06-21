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
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;

import org.eclipse.rap.rwt.RWT;

import org.polymap.core.runtime.config.Config;
import org.polymap.core.runtime.config.Configurable;
import org.polymap.core.runtime.i18n.IMessages;
import org.polymap.core.style.Messages;
import org.polymap.core.style.model.StylePropertyValue;
import org.polymap.core.ui.FormLayoutFactory;
import org.polymap.core.ui.SelectionAdapter;
import org.polymap.core.ui.UIUtils;

import org.polymap.model2.Property;

/**
 * The viewer of an {@link StylePropertyValue}. 
 *
 * @author Falko Bräutigam
 */
public class StylePropertyField
        extends Configurable {

    private final static IMessages i18n = Messages.forPrefix( "Field" );

    private static Log log = LogFactory.getLog( StylePropertyField.class );
    
    public Config<String>                       title;
    
    public Config<String>                       tooltip;
    
    private Property<StylePropertyValue>        prop;

    private Composite                           contents;

    private ComboViewer                         combo;
    
    private Composite                           valueContainer;

    /**
     * The available editors for our {@link #propInfo} property type. Model of
     * {@link #combo}.
     */
    private StylePropertyEditor[]               editors;

    private StylePropertyEditor                 selected;

    private final StylePropertyFieldSite fieldSite;

    
    public StylePropertyField( StylePropertyFieldSite fieldSite ) {
        this.fieldSite = fieldSite;
        this.prop = fieldSite.prop.get();
        this.title.set( prop.info().getDescription().isPresent()
                ? i18n.get( (String)prop.info().getDescription().get() ) : "" );
        this.tooltip.set( prop.info().getDescription().isPresent()
                ? i18n.get( (String)prop.info().getDescription().get() + "Tooltip" ) : "" );
        this.editors = StylePropertyEditor.forValue( fieldSite );
    }


    public Control createContents( Composite parent ) {
        assert contents == null : "StylePropertyField can be created only once.";
        
        contents = new Composite( parent, SWT.NONE );
        tooltip.ifPresent( txt -> contents.setToolTipText( txt ) );
        
        // label
        Label t = new Label( contents, SWT.NONE );
        t.setData( RWT.MARKUP_ENABLED, Boolean.TRUE );
        t.setText( "<b>" + title.get() + "</b>" );

        // value container
        valueContainer = new Composite( contents, SWT.NONE );
        updateEditor();
        
        // combo
        combo = new ComboViewer( new Combo( contents, SWT.READ_ONLY ) );
        combo.getCombo().setVisibleItemCount( 5 );
        combo.setContentProvider( new ArrayContentProvider() );
        combo.setLabelProvider( new LabelProvider() {
            @Override
            public String getText( Object elm ) {
                return ((StylePropertyEditor)elm).label();
            }
        });
        combo.setInput( editors );
        for (StylePropertyEditor editor : editors) {
            if (editor.canHandleCurrentValue()) {
                selected = editor;
                combo.setSelection( new StructuredSelection( editor ) );
                updateEditor();
                break;
            }
        }
        combo.addSelectionChangedListener( ev -> {
            selected = SelectionAdapter.on( ev.getSelection() ).first( StylePropertyEditor.class ).get();
            selected.updatePropertyWithHint();
            updateEditor();
        });
        
        // layout
        contents.setLayout( FormLayoutFactory.defaults().create() );
        on( t ).fill().left( 0, 5 ).noBottom();
        on( combo.getCombo() ).top( t ).left( 0 ).right( 30 );
        on( valueContainer ).top( t ).bottom( 100 ).left( combo.getControl(), 5 ).right( 100 );
        return contents;
    }

    
    public void updateEditor() {
        UIUtils.disposeChildren( valueContainer );
        if (selected == null) {
            valueContainer.setLayout( new FillLayout() );
            Label msg = new Label( valueContainer, SWT.NONE );
            msg.setText( "" );
            //msg.setToolTipText( "No value" );
        }
        else {
            selected.createContents( valueContainer );
        }
        valueContainer.layout( true );
    }

}
