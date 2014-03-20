/* 
 * polymap.org
 * Copyright 2012, Polymap GmbH. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.core.project.ui.properties;

import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.catalog.ui.CatalogTreeViewer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;

import org.eclipse.ui.views.properties.PropertyDescriptor;

import org.polymap.core.project.ILayer;
import org.polymap.core.project.Messages;
import org.polymap.core.runtime.IMessages;
import org.polymap.core.runtime.WeakListener;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class GeoresPropertyDescriptor
        extends PropertyDescriptor {
    
    public static final IMessages   i18n = Messages.forPrefix( "LayerProperty_Geores" ); //$NON-NLS-1$
    
    private ILayer          layer;

    
    public GeoresPropertyDescriptor( Object id, String displayName, ILayer layer ) {
        super( id, displayName );
        this.layer = layer;
        setLabelProvider( new LabelProvider() {
            public String getText( Object elm ) {
                return (String)elm;
            }
        } );
    }

    
    @Override
    public CellEditor createPropertyEditor( Composite parent ) {
        return new GeoresDialogCellEditor( parent );
    }


    /**
     * 
     */
    public class GeoresDialogCellEditor 
            extends DialogCellEditor {
        
        public GeoresDialogCellEditor( Composite parent ) {
            super( parent );
        }


//        protected void updateContents( Object value ) {
//            throw new RuntimeException();
//            CoordinateReferenceSystem crs = (CoordinateReferenceSystem)value;
//            if (crs != null) {
//                String srs = CRS.toSRS( crs );
//                super.updateContents( srs /*crs.getName()*/ );
//            }
//        }


        @Override
        protected Object openDialogBox( Control cellEditorWindow ) {
            ChooseGeoresDialog dialog = new ChooseGeoresDialog( cellEditorWindow.getShell() );
            dialog.setBlockOnOpen( true );
            dialog.open();
            if (dialog.getResult() == null || dialog.getResult().equals( getValue() )) {
                return null;
            }
            else {
                return dialog.getResult();
            }
        }
    }

    
    public class ChooseGeoresDialog
            extends TitleAreaDialog
            implements ISelectionChangedListener {

        public static final String          ID = "ChooseLayerPage";

        private CatalogTreeViewer           viewer;

        private IGeoResource                result;


        public ChooseGeoresDialog( Shell shell ) {
            super( shell );
        }

        public void preset( IGeoResource geores ) {
            result = geores;
        }

        public IGeoResource getResult() {
            return result;
        }

        @Override
        protected Point getInitialSize() {
            return new Point( 350, 600 );
        }

        @Override
        protected boolean isResizable() {
            return true;
        }


        @Override
        protected Control createDialogArea( Composite parent ) {
            Composite contents = new Composite( (Composite)super.createDialogArea( parent ), SWT.NONE );
            contents.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
            setTitle( i18n.get( "dialogTitle" ) );
            setMessage( i18n.get( "dialogDescription", layer.getLabel() ) );
            
            contents.setLayout( FormLayoutFactory.defaults().margins( 5 ).create() );

            viewer = new CatalogTreeViewer( contents, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER, true );
            viewer.getTree().setLayoutData( FormDataFactory.filled().create() );

            viewer.addDoubleClickListener( new IDoubleClickListener() {
                public void doubleClick( DoubleClickEvent ev ) {
                    okPressed();
                }
            });
            if (result != null) {
                viewer.setSelection( new StructuredSelection( result ), true );
            }

            viewer.addSelectionChangedListener( WeakListener.forListener( this ) );
            return contents;
        }

        
        @Override
        protected Control createButtonBar( Composite parent ) {
            Control btnBar = super.createButtonBar( parent );
            updateEnabled();
            return btnBar;
        }


        protected void updateEnabled() {
            Button okBtn = getButton( IDialogConstants.OK_ID );
            okBtn.setEnabled( result != null );
        }

        
        @Override
        public void selectionChanged( SelectionChangedEvent ev ) {
            result = null;
            ISelection sel = ev.getSelection();
            if (sel != null && sel instanceof IStructuredSelection) {
                Object elm = ((IStructuredSelection)sel).getFirstElement();
                if (elm != null && elm instanceof IGeoResource) {
                    result = (IGeoResource)elm;
                }
            }
            updateEnabled();
        }

    }
}
