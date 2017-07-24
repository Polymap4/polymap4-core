/* 
 * polymap.org
 * Copyright (C) 2017, the @authors. All rights reserved.
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
package org.polymap.core.data.process.ui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;

import org.polymap.core.data.process.FieldInfo;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;
import org.polymap.core.ui.SelectionAdapter;
import org.polymap.core.ui.UIUtils;

/**
 * The viewer of a {@link FieldInfo}.
 *
 * @author Falko Bräutigam
 */
public class FieldViewer {

    private static final Log log = LogFactory.getLog( FieldViewer.class );
    
    public static Color errorColor() {
        return UIUtils.getColor( 240, 20, 20 );
    }

    private FieldViewerSite     site;
    
    private ComboViewer         combo;

    private Composite           contents;

    private Composite           editorContainer;
    
    private FieldIO             selected;
    
    
    public FieldViewer( FieldViewerSite site ) {
        this.site = site;
    }


    public Composite createContents( Composite parent ) {
        assert contents == null : "StylePropertyField can be created only once.";
        contents = new Composite( parent, SWT.NONE );
        //tooltip.ifPresent( txt -> contents.setToolTipText( txt ) );
        
        // label
        Label t = new Label( contents, SWT.WRAP );
        t.setText( site.fieldInfo.get().description().orElse( "?" ) );
        t.setEnabled( false );
        //t.setFont( UIUtils.bold( t.getFont() ) );

        // value container
        editorContainer = new Composite( contents, SWT.NONE );
        editorContainer.setFont( UIUtils.bold( editorContainer.getFont() ) );
        
        // combo
        combo = new ComboViewer( new Combo( contents, SWT.READ_ONLY ) );
        combo.getCombo().setFont( UIUtils.bold( combo.getCombo().getFont() ) );
        combo.getCombo().setVisibleItemCount( 5 );
        combo.setContentProvider( new ArrayContentProvider() );
        combo.setLabelProvider( new LabelProvider() {
            @Override
            public String getText( Object elm ) {
                return ((FieldIO)elm).label();
            }
        });
        FieldIO[] editors = FieldIO.forField( site );
        combo.setInput( editors );
        combo.addSelectionChangedListener( ev -> {
            selected = SelectionAdapter.on( ev.getSelection() ).first( FieldIO.class ).get();
            updateEditor();
        });
        combo.setSelection( new StructuredSelection( editors[0] ) );
        
        // layout
        contents.setLayout( FormLayoutFactory.defaults().create() );
        FormDataFactory.on( t ).fill().left( 0, 5 ).noBottom().width( 300 );
        FormDataFactory.on( combo.getCombo() ).top( t ).left( 0 ).right( 30 );
        FormDataFactory.on( editorContainer ).top( t ).bottom( 100 ).left( combo.getControl(), 5 ).right( 100 );
        return contents;
    }


    protected void updateEditor() {
        try {
            UIUtils.disposeChildren( editorContainer );
            editorContainer.setLayout( new FillLayout() );
            selected.createContents( editorContainer );
            editorContainer.layout( true, true );
        }
        catch (Exception e) {
            log.warn( "", e );
        }
    }
    
}
