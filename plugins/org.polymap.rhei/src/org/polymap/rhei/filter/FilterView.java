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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.part.ViewPart;

import org.polymap.core.runtime.Polymap;
import org.polymap.core.workbench.PolymapWorkbench;
import org.polymap.rhei.RheiPlugin;
import org.polymap.rhei.field.FormFieldEvent;
import org.polymap.rhei.field.IFormFieldListener;
import org.polymap.rhei.internal.form.FormEditorToolkit;
import org.polymap.rhei.navigator.filter.OpenFilterAction;


/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class FilterView
        extends ViewPart 
        implements IFormFieldListener {

    private static Log log = LogFactory.getLog( FilterView.class );

    public static final String              ID = "org.polymap.rhei.FilterView";
    
    
    /**
     * Makes sure that the view for the layer is open. If the view is already
     * open, then it is activated.
     * 
     * @param layer
     * @param allowSearch XXX
     * @return The view for the given layer.
     */
    public static FilterView open( final IFilter filter ) {
        final FilterView[] result = new FilterView[1];
        
        Polymap.getSessionDisplay().syncExec( new Runnable() {
            public void run() {
                try {
                    IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                    
                    result[0] = (FilterView)page.showView( 
                            FilterView.ID, String.valueOf( filter.hashCode() ), IWorkbenchPage.VIEW_ACTIVATE );
                    result[0].showFilter( filter );
                }
                catch (Exception e) {
                    PolymapWorkbench.handleError( RheiPlugin.PLUGIN_ID, null, e.getMessage(), e );
                }
            }
        });
        return result[0];
    }

    
//    public static void close( final ILayer layer ) {
//        Polymap.getSessionDisplay().asyncExec( new Runnable() {
//            public void run() {
//                try {
//                    IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
//
//                    GeoSelectionView view = (GeoSelectionView)page.findView( GeoSelectionView.ID );
//                    if (view != null) {
//                        page.hideView( view );
//                        view.disconnectLayer();
//                    }
//                }
//                catch (Exception e) {
//                    PolymapWorkbench.handleError( DataPlugin.PLUGIN_ID, this, e.getMessage(), e );
//                }
//            }
//        });
//    }

    
    // instance *******************************************
    
    private IFilter                     filter;
    
    private FilterEditor                filterEditor;

    private ScrolledComposite           content;

    private Button resetBtn;

    private Button submitBtn;
    
    
    public FilterView() {
    }


    public void init( IViewSite site )
            throws PartInitException {
        super.init( site );
    }


    public void showFilter( IFilter _filter ) {
        assert _filter != null && _filter.hasControl();
        filter = _filter;
        
        // FilterEditor
        assert filterEditor == null;
        filterEditor = new FilterEditor() {

            private Composite       layoutLast;
            
            public Composite createStandardLayout( Composite parent ) {
                Composite result = new Composite( parent, SWT.NONE );
                result.setBackground( Display.getCurrent().getSystemColor( SWT.COLOR_WHITE ) );
                
                GridData gridData = new GridData( GridData.FILL_BOTH );
                gridData.grabExcessHorizontalSpace = true;
                result.setLayoutData( gridData );

                layoutLast = null;

                FormLayout layout = new FormLayout();
                layout.marginWidth = 7;
                layout.marginHeight = 1;
                result.setLayout( layout );
                return result;
            }

            public void addStandardLayout( Composite composite ) {
                FormData data = new FormData();
                data.left = new FormAttachment( 0, 0 );
                data.right = new FormAttachment( 100, 0 );
                if (layoutLast != null) {
                    data.top = new FormAttachment( layoutLast, 1 );
                }
                composite.setLayoutData( data );
                layoutLast = composite;
            }
        };
        
        final FormEditorToolkit toolkit = new FormEditorToolkit( 
                new FormToolkit( getSite().getShell().getDisplay() ) );
        filterEditor.setToolkit( toolkit );
        filterEditor.addFieldListener( this );

        getSite().getShell().getDisplay().asyncExec( new Runnable() {
            public void run() {
                for (Control child : content.getChildren()) {
                    child.dispose();
                }
                Composite filterControl = filter.createControl( content, filterEditor );
                content.setContent( filterControl );
                content.setMinSize( filterControl.computeSize( SWT.DEFAULT, SWT.DEFAULT ) );

                content.changed( new Control[] { filterControl } );
                //content.pack( true );
            }
        });
    }

    
    public void fieldChange( FormFieldEvent ev ) {
        if (ev.getEventCode() == IFormFieldListener.VALUE_CHANGE) {
            if (submitBtn != null) {
                submitBtn.setEnabled( filterEditor.isDirty() && filterEditor.isValid() );
            }
            if (resetBtn != null) {
                resetBtn.setEnabled( filterEditor.isDirty() );
            }
        }
    }


    public void createPartControl( Composite parent ) {
        Composite composite = new Composite( parent, SWT.NONE );
        GridLayout layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.verticalSpacing = 0;
        layout.horizontalSpacing = 0;
        composite.setLayout( layout );

        // button row
        Composite btnRow = new Composite( composite, SWT.NONE );
        btnRow.setLayout( new RowLayout() );

        // reset
        resetBtn = new Button( btnRow, SWT.PUSH );
        resetBtn.setText( "Zurücksetzen" );
        resetBtn.setSize( SWT.DEFAULT, 18 );
        resetBtn.addSelectionListener( new SelectionAdapter() {
            public void widgetSelected( SelectionEvent ev ) {
                try {
                    filterEditor.doReset();
                }
                catch (Exception e) {
                    PolymapWorkbench.handleError( RheiPlugin.PLUGIN_ID, this, "", e );
                }
            }
        });
            
        // submit
        submitBtn = new Button( btnRow, SWT.PUSH );
        submitBtn.setText( "Suchen" );
        submitBtn.setSize( SWT.DEFAULT, 18 );
        submitBtn.addSelectionListener( new SelectionAdapter() {
            public void widgetSelected( SelectionEvent ev ) {
                try {
                    filterEditor.doSubmit();
                    
                    Filter filterFilter = filter.createFilter( filterEditor );
                    OpenFilterAction.showResults( filter.getLayer(), filterFilter );
                }
                catch (Exception e) {
                    PolymapWorkbench.handleError( RheiPlugin.PLUGIN_ID, this, "", e );
                }
            }
        });
        btnRow.pack();
            
        // separator line
        Label titleBarSeparator = new Label( composite, SWT.HORIZONTAL | SWT.SEPARATOR );
        titleBarSeparator.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );

        // filter content area
        content = new ScrolledComposite( composite, SWT.V_SCROLL );
        layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.verticalSpacing = 0;
        layout.horizontalSpacing = 0;
        content.setLayout( layout );
        content.setLayoutData( new GridData( GridData.FILL_BOTH ) );
        content.setFont( parent.getFont() );
        content.setBackground( parent.getBackground() );
        content.setExpandHorizontal( true );
        content.setExpandVertical( true );
        content.setShowFocusedControl( true );
    }


    public void setFocus() {
    }
    
}
