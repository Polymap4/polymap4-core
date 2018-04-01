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

import java.io.InputStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerSorter;

import org.eclipse.rap.rwt.client.ClientFile;

import org.polymap.core.runtime.UIThreadExecutor;
import org.polymap.core.runtime.i18n.IMessages;
import org.polymap.core.style.Messages;
import org.polymap.core.style.model.feature.ConstantGraphic;
import org.polymap.core.style.model.feature.Graphic;
import org.polymap.core.style.ui.StylePropertyEditor;
import org.polymap.core.style.ui.StylePropertyFieldSite;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;
import org.polymap.core.ui.StatusDispatcher;
import org.polymap.core.ui.UIUtils;

import org.polymap.rap.updownload.upload.Upload;

/**
 * 
 * @author Falko Bräutigam
 */
public class ConstantExternalGraphicEditor
        extends StylePropertyEditor<ConstantGraphic> { 

    private static final IMessages i18n = Messages.forPrefix( "ConstantExternalGraphicEditor" );

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
        prop.createValue( ConstantGraphic.defaults( Graphic.defaultGraphic() ) );
    }


    @Override
    public Composite createContents( Composite parent ) {
        Composite container = super.createContents( parent );
        
        ComboViewer viewer = new ComboViewer( container, SWT.SINGLE | SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY );
        viewer.getCombo().setVisibleItemCount( 12 );
        viewer.setSorter( new ViewerSorter() );
        viewer.setContentProvider( ArrayContentProvider.getInstance() );
        viewer.setInput( Graphic.allGraphics() );
        
        viewer.setSelection( new StructuredSelection( prop.get().markOrName.get() ), true );
        viewer.addSelectionChangedListener( ev -> {
            String selected = UIUtils.selection( ev.getSelection() ).first( String.class ).get();            
            prop.get().markOrName.set( selected );
        });
        
        Upload upload = new Upload( container, SWT.BORDER );
        upload.setText( "Upload..." );
        upload.setToolTipText( "Upload an image file (*.png, *.svg, *.gif, *.jpg)" );
        upload.setHandler( (clientFile, in) -> {
            String name = upload( clientFile, in );
            UIThreadExecutor.async( () -> {
                viewer.setInput( Graphic.allGraphics() );
                viewer.setSelection( new StructuredSelection( name ) );
            });
        });
        
        // layout
        container.setLayout( FormLayoutFactory.defaults().spacing( 5 ).margins( 3, 0 ).create() );
        FormDataFactory.on( viewer.getControl() ).fill().right( 65 );
        FormDataFactory.on( upload ).fill().left( viewer.getControl() );
        return container;
    }

    
    public String upload( ClientFile clientFile, InputStream in ) throws Exception {
        try {
            return Graphic.uploadGraphic( clientFile.getName(), in );
        }
        catch (Exception e) {
            StatusDispatcher.handleError( "Unable to upload graphic.", e );
            throw e;
        }
    }
    
}
