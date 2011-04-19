/* 
 * polymap.org
 * Copyright 2011, Falko Bräutigam, and other contributors as indicated
 * by the @authors tag.
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
package org.polymap.rhei.filter;

import org.opengis.filter.Filter;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;

import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.FormToolkit;

import org.polymap.rhei.Messages;
import org.polymap.rhei.field.FormFieldEvent;
import org.polymap.rhei.field.IFormFieldListener;
import org.polymap.rhei.internal.form.FormEditorToolkit;

/**
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class FilterDialog
        extends TitleAreaDialog
        implements IFormFieldListener {

    public static final int         RESET_BUTTON = 10;
    
    private static Image            titleImage;


//    /**
//     * Opens a modal dialog with the given {@link FilterEditor}.
//     * 
//     * @return {@link Window#OK} or {@link Window#OK}. 
//     */
//    public static int openDialog( IFilter filter ) {
//        try {
//            FilterDialog dialog = new FilterDialog( filter );
//
//            dialog.setBlockOnOpen( true );
//            return dialog.open();
//        }
//        catch (Exception e) {
//            PolymapWorkbench.handleError( RheiPlugin.PLUGIN_ID, null, "Fehler beim Suchen und Öffnen der Ergebnisliste.", e );
//            return Window.CANCEL;
//        }
//    }
    

    // instance *******************************************
    
    private IFilter                 filter;
    
    private FilterEditor            filterEditor;

    private Composite               contents;
    

    public FilterDialog( IFilter filter ) {
        super( PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell() );
        setShellStyle( getShellStyle() | SWT.RESIZE );
// bit to flashy
//        if (titleImage == null) {
//            titleImage = ImageDescriptor.createFromURL( 
//                    RheiPlugin.getDefault().getBundle().getResource( "icons/eview16/newsearch_wiz.gif" ) ).createImage();
//        }
//        setTitleImage( titleImage );
        
        // filterEditor
        this.filter = filter;
        this.filterEditor = new FilterEditor() {
            
            private Composite       layoutLast;

            public Composite createStandardLayout( Composite parent ) {
                Composite result = new Composite( parent, SWT.NONE );
                result.setBackground( Display.getCurrent().getSystemColor( SWT.COLOR_WHITE ) );
                
                GridData gridData = new GridData( GridData.FILL_BOTH );
                gridData.grabExcessHorizontalSpace = true;
                result.setLayoutData( gridData );

                layoutLast = null;

                FormLayout layout = new FormLayout();
                layout.marginWidth = 10;
                layout.marginHeight = 10;
                result.setLayout( layout );
                return result;
            }

            public void addStandardLayout( Composite composite ) {
                FormData data = new FormData();
                data.left = new FormAttachment( 0, 0 );
                data.right = new FormAttachment( 100, 0 );
                if (layoutLast != null) {
                    data.top = new FormAttachment( layoutLast, 3 );
                }
                composite.setLayoutData( data );
                layoutLast = composite;
            }
  
        };
        this.filterEditor.setToolkit( new FormEditorToolkit( new FormToolkit( getParentShell().getDisplay() ) ) );
        this.filterEditor.addFieldListener( this );
    }


    public void dispose() {
        if (filterEditor != null) {
            filterEditor.dispose();
            filterEditor = null;
        }
    }


    public void fieldChange( FormFieldEvent ev ) {
        Button okButton = getButton( IDialogConstants.OK_ID );
        if (okButton != null) {
            okButton.setEnabled( filterEditor.isDirty() && filterEditor.isValid() );
        }
    }

    
    public Shell getParentShell() {
        return super.getParentShell();
    }


    // TitleAreaDialog ********************************

    protected Image getImage() {
        return getShell().getDisplay().getSystemImage( SWT.ICON_QUESTION );
    }

    
    protected Point getInitialSize() {
        //            return new Point( 350, 260 );
        return super.getInitialSize();
    }

    
    protected Control createDialogArea( Composite parent ) {
        Composite area = (Composite)super.createDialogArea( parent );

        setTitle( Messages.get( "FilterEditor_title" ) );
        setMessage( Messages.get( "FilterEditor_description" ) );

        contents = filter.createControl( area, filterEditor );

        // load default values
        filterEditor.doLoad();

        area.pack();
        return area;
    }

    
    protected void createButtonsForButtonBar( Composite parent ) {
        super.createButtonsForButtonBar( parent );
        createButton( parent, RESET_BUTTON, "Zurücksetzen", false );

        getButton( IDialogConstants.OK_ID ).setEnabled( false );
    }

    
    protected void buttonPressed( int buttonId ) {
        switch (buttonId) {
            case RESET_BUTTON : {
                filterEditor.doReset();
                break;
            }
            case IDialogConstants.OK_ID: {
                filterEditor.doSubmit();
                if (filterEditor.isDirty()) {
                    super.okPressed();
                }
                break;
            }
            default: {
                super.buttonPressed( buttonId );
                break;
            }
        }
    }


    public Filter createFilter() {
        return filter.createFilter( filterEditor );
    }

}
