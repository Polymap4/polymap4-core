/* 
 * polymap.org
 * Copyright 2009, Polymap GmbH, and individual contributors as indicated
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
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 * $Id$
 */
package org.polymap.core.model.security;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.beans.PropertyChangeListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;

import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ui.internal.dialogs.AdaptableForwarder;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;

import org.polymap.core.CorePlugin;
import org.polymap.core.model.Messages;
import org.polymap.core.operation.OperationSupport;
import org.polymap.core.qi4j.event.AbstractModelChangeOperation;
import org.polymap.core.qi4j.event.PropertyChangeSupport;
import org.polymap.core.runtime.WeakListener;
import org.polymap.core.workbench.PolymapWorkbench;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @since 3.0
 */
@SuppressWarnings("restriction")
public class ACLPropertiesPage
        extends PropertyPage
        implements IWorkbenchPropertyPage {

    private static final Log log = LogFactory.getLog( ACLPropertiesPage.class );

    private TableViewer             tableViewer;

    private ACLContentProvider      model;
    
    private ACLOperation            op;

    
    public ACLPropertiesPage() {
        model = new ACLContentProvider();
    }


    protected Control createContents( Composite composite ) {
        // XXX see PropertyDialogAction for more detail
        final ACL acl = getElement() instanceof AdaptableForwarder
                ? (ACL)((AdaptableForwarder)getElement()).getAdapter( ACL.class )
                : (ACL)getElement();
        
        // check permission
        if (!ACLUtils.checkPermission( acl, AclPermission.ACL, false )) {
            Label l = new Label( composite, SWT.NONE );
            l.setText( "Keine Zugriffsberechtigung." );
            return l;
        }

        // start operation
        if (acl instanceof PropertyChangeSupport) {
            op = new ACLOperation();
            ((PropertyChangeSupport)acl).addPropertyChangeListener( 
                    WeakListener.forListener( (PropertyChangeListener)op ) );
        }
  
        Composite parent = new Composite( composite, SWT.NONE );
        
        // create the table
        Composite tableParent = new Composite( parent, SWT.NONE );
        tableViewer = new TableViewer( tableParent, SWT.V_SCROLL | SWT.BORDER | SWT.FULL_SELECTION );
        Table table = tableViewer.getTable();
        table.setHeaderVisible( true );
        //table.setLinesVisible( true );

        // columns / layout
        TableColumnLayout tableLayout = new TableColumnLayout();
        TableColumn name = new TableColumn( table, SWT.NONE );
        name.setText( "Nutzer/Gruppe" );
        tableLayout.setColumnData( name, new ColumnWeightData( 35, true ) );

        TableColumn permissions = new TableColumn( table, SWT.NONE );
        permissions.setText( "Rechte" );
        tableLayout.setColumnData( permissions, new ColumnWeightData( 68, true ) );
        tableParent.setLayout( tableLayout );

        tableViewer.setColumnProperties( new String[]{"name", "permissions"} );

        // LabelProvider
        tableViewer.setLabelProvider( new ACLLabelProvider() );
        
        // ContentProvider
        tableViewer.setContentProvider( model );
        tableViewer.setInput( acl );

        // cell editor / modifier
        tableViewer.setCellModifier( new ICellModifier() {

            public boolean canModify( Object element, String property ) {
                return property.equals( "permissions" );
            }

            public Object getValue( Object element, String property ) {
                ACL.Entry entry = (ACL.Entry)element;
                if (property.equals( "name" )) {
                    return entry.getPrincipal().getName();
                }
                if (property.equals( "permissions")) {
                    return entry;
                }
                return "?";
            }

            public void modify( Object element, String property, Object value ) {
                TableItem tabItem = (TableItem) element;
                ACL.Entry entry = (ACL.Entry)tabItem.getData();
                log.debug( "modify(): value=" + value );
                if (property.equals( "permissions" )) {
                    // all the work has been done by the PermissionCellEditor already
                }
                tableViewer.refresh();
//                checkFinish();
            }

        });

        tableViewer.setCellEditors( new CellEditor[] {
                new TextCellEditor( table ),
                new PermissionCellEditor( table )
        });

        // new button
        Button newBtn = new Button( parent, SWT.NONE );
        newBtn.setText( Messages.get( "ACLPropertiesPage_newBtn_title" ) );
        newBtn.setToolTipText( Messages.get( "ACLPropertiesPage_newBtn_tip" ) );
        newBtn.addListener( SWT.Selection, new Listener() {
            public void handleEvent( Event ev ) {
                InputDialog dialog = new InputDialog( getShell(), 
                        Messages.get( "ACLPropertiesPage_newEntry_title" ), 
                        Messages.get( "ACLPropertiesPage_newEntry_msg" ), 
                        "", null );
                dialog.setBlockOnOpen( true );
                dialog.open();
                
                if (dialog.getReturnCode() == InputDialog.OK) {
                    model.addPermission( dialog.getValue(), AclPermission.READ );
                }
            }
        });
        
        // delete button
        Button deleteBtn = new Button( parent, SWT.NONE );
        deleteBtn.setText( Messages.get( "ACLPropertiesPage_deleteBtn_title" ) );
        deleteBtn.setToolTipText( Messages.get( "ACLPropertiesPage_deleteBtn_tip" ) );
        deleteBtn.addListener( SWT.Selection, new Listener() {
            public void handleEvent( Event ev ) {
                IStructuredSelection sel = (IStructuredSelection)tableViewer.getSelection();
                if (sel != null && sel.getFirstElement() != null) {
                    ACL.Entry aclEntry = (ACL.Entry)sel.getFirstElement();
                    for (AclPermission permission : aclEntry.permissions()) {
                        model.removePermission( aclEntry.getPrincipal().getName(), permission );
                    }
                }
            }
        });
        
        
        // Layout
        FormLayout layout = new FormLayout();
        layout.marginWidth = 3;
        layout.marginHeight = 3;
        parent.setLayout( layout );
        
        FormData newBtnData = new FormData( 80, SWT.DEFAULT );
        newBtnData.top = new FormAttachment( 0, 0 );
        newBtnData.right = new FormAttachment( 100, 0);
        newBtn.setLayoutData( newBtnData );

        FormData delBtnData = new FormData( 80, SWT.DEFAULT );
        delBtnData.top = new FormAttachment( newBtn, 5 );
        delBtnData.right = new FormAttachment( 100, 0);
        deleteBtn.setLayoutData( delBtnData );

        FormData viewerData = new FormData();
        viewerData.top = new FormAttachment( 0, 0 );
        viewerData.bottom = new FormAttachment( 100, 0 );
        viewerData.left = new FormAttachment( 0, 0 );
        viewerData.right = new FormAttachment( newBtn, -5 );
        tableParent.setLayoutData( viewerData );

        return parent;
    }
    

    public boolean okToLeave() {
        return true;
//        if (changes != null) {
//            int result = changes.entities().size(); 
//            log.debug( "okToLeave(): changes entities: " + result );
//            return result == 0;
//        }
//        return true;
    }


    public boolean performCancel() {
        log.debug( "..." );
        if (op != null) {
            try {
                ((PropertyChangeSupport)model.acl).removePropertyChangeListener( op );
                op.undo( new NullProgressMonitor(), null );
                return true;
            }
            catch (ExecutionException e) {
                throw new RuntimeException( "", e );
            }
        }
        else {
            throw new RuntimeException( "Not yet implemented." );
        }
    }


    public boolean performOk() {
        log.debug( "..." );
        try {
            if (op != null) {
                ((PropertyChangeSupport)model.acl).removePropertyChangeListener( op );
                OperationSupport.instance().execute( op, false, false );
            }
            return true;
        }
        catch (ExecutionException e) {
            PolymapWorkbench.handleError( CorePlugin.PLUGIN_ID, this, e.getLocalizedMessage(), e );
            return false;
        }
    }

    
    /**
     * This is a "fake" operation that does nothing but helps us to take
     * part of the operation undo/redo system.
     */
    static class ACLOperation
            extends AbstractModelChangeOperation
            implements IUndoableOperation {


        public ACLOperation( ) {
            super( Messages.get( "ACLOperation_label" ) );
        }

        public IStatus doExecute( IProgressMonitor monitor, IAdaptable info )
                throws ExecutionException {
            // the work has been done by the properties page
            return Status.OK_STATUS;
        }

    }
    
    
    /** 
     * The content provider and update interface.
     */
    class ACLContentProvider
            implements IStructuredContentProvider {
        
        private Viewer              viewer;
        
        private ACL                 acl;
        
        
        public void inputChanged( Viewer _viewer, Object oldInput, Object newInput ) {
            this.viewer = _viewer;
            this.acl = (ACL)newInput;
            log.debug( "inputChanged(): acl=" + acl );
        }

        public Object[] getElements( Object input ) {
            log.debug( "getElements(): acl=" );
            List<ACL.Entry> result = new ArrayList();
            for (ACL.Entry entry : acl.entries()) {
                result.add( entry );
            }
            return result.toArray();
        }
        
        public void addPermission( String principalName, AclPermission permission ) {
            acl.addPermission( principalName, permission );
            viewer.refresh();
        }

        public void removePermission( String principalName, AclPermission permission ) {
            acl.removePermission( principalName, permission );
            viewer.refresh();
        }

        public void dispose() {
        }
        
    }

    
    /**
     * 
     */
    class ACLLabelProvider
            implements ITableLabelProvider {

        public Image getColumnImage( Object element, int columnIndex ) {
            return null;
        }

        public String getColumnText( Object element, int columnIndex ) {
            log.debug( "getColumnText(): elm=" + element + ", columnIndex=" + columnIndex );
            ACL.Entry entry = (ACL.Entry)element;
            switch (columnIndex) {
                case 0: {
                    return (String)entry.getPrincipal().getName();
                }
                case 1: {
                    StringBuffer result = new StringBuffer();
                    for (AclPermission permission : entry.permissions()) {
                        result.append( result.length() > 0 ? ", " : "" )
                              .append( permission.getName() );
                    }
                    return result.toString();
                }
                default:
                    break;
            }
            return "";
        }

        public void addListener( ILabelProviderListener listener ) {
        }

        public void dispose() {
        }

        public boolean isLabelProperty( Object element, String property ) {
            return false;
        }

        public void removeListener( ILabelProviderListener listener ) {
        }
    }
    
    
    /**
     * 
     */
    class PermissionCellEditor
            extends CellEditor {

        private Composite                   editor;
        
        private Map<String,Button>          checkboxes;
        
        private ACL.Entry                   value;
        

        protected PermissionCellEditor( Composite parent ) {
            super( parent );
        }

        protected Control createControl( Composite parent ) {
            log.debug( "createControl(): ..." );
            Font font = parent.getFont();
            Color bg = parent.getBackground();

            editor = new Composite( parent, getStyle() );
            editor.setFont( font );
            editor.setBackground( bg );
            
            checkboxes = new HashMap();
            for (final AclPermission permission : AclPermission.ALL) {
                final Button check = new Button( editor, SWT.CHECK );
                check.setFont( font );
                check.setBackground( bg );
                check.setText( permission.getName() );
                checkboxes.put( permission.getName(), check );
                
                check.addSelectionListener( new SelectionAdapter() {
                    public void widgetSelected( SelectionEvent ev ) {
                        log.debug( "widgetSelected(): ev=" + ev );
                        try {
                            if (check.getSelection()) {
                                model.addPermission( value.getPrincipal().getName(), permission );
                            }
                            else {
                                model.removePermission( value.getPrincipal().getName(), permission );
                            }
                        }
                        // SecurityException
                        catch (Exception e) {
                            PolymapWorkbench.handleError( CorePlugin.PLUGIN_ID, this, e.getLocalizedMessage(), e );
                        }
                    }
                });
            }
            
            // layout
            RowLayout layout = new RowLayout();
            layout.spacing = 0;
            layout.marginTop = 0;
            layout.marginBottom = 0;
            layout.wrap = false;
            editor.setLayout( layout );
            
            setValueValid( true );
            
            return editor;
        }

        protected Object doGetValue() {
            log.debug( "doGetValue(): ..." );

            List<AclPermission> result = new ArrayList();
            for (Map.Entry<String,Button> entry : checkboxes.entrySet()) {
                if (entry.getValue().getSelection()) {
                    result.add( AclPermission.forName( entry.getKey() ) );
                }
            }
            return result;
        }

        protected void doSetValue( Object _value ) {
            log.debug( "doSetValue(): value=" + _value.getClass().getName() );
            
            if (_value instanceof ACL.Entry) {
                this.value = (ACL.Entry)_value;
                for (AclPermission permission : value.permissions()) {
                    log.debug( "    permission found: " + permission.getName() );
                    Button check = checkboxes.get( permission.getName() );
                    if (check != null) {
                        check.setSelection( true );
                    }
                }
            }
        }
        
        protected void doSetFocus() {
        }

    }
    
}
