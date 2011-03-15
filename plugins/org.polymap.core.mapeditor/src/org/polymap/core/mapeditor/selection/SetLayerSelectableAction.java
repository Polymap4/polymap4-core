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

package org.polymap.core.mapeditor.selection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;

import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import org.polymap.core.mapeditor.ISelectFeatureSupport;
import org.polymap.core.mapeditor.MapEditor;
import org.polymap.core.mapeditor.MapEditorInput;
import org.polymap.core.mapeditor.MapEditorPlugin;
import org.polymap.core.mapeditor.Messages;
import org.polymap.core.operation.OperationSupport;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.ui.MapLayersView;
import org.polymap.core.workbench.PolymapWorkbench;

/**
 * This action is used in the {@link MapLayersView} and the popup of
 * {@link ILayer}. It puts the given layer in "features selectable" mode.
 * The actual operation is done by {@link SetLayerSelectableOperation}.
 * 
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class SetLayerSelectableAction
        implements IViewActionDelegate {

    private static Log log = LogFactory.getLog( SetLayerSelectableAction.class );
    
    private MapLayersView           view;
    
    private ILayer                  layer;
    
    private String                  origTooltip;
    
    
    public void init( IViewPart view0 ) {
        this.view = (MapLayersView)view0;
    }


    public void run( IAction action ) {
        try {
            log.info( "isChecked()= " + action.isChecked() );
            SetLayerSelectableOperation op = new SetLayerSelectableOperation( layer, action.isChecked() );
            OperationSupport.instance().execute( op, true, false );
        }
        catch (Exception e) {
            log.error( e.getMessage(), e );
            PolymapWorkbench.handleError( MapEditorPlugin.PLUGIN_ID, this,
                    Messages.get( getClass(), "errorDialogMsg" ), e );
            
//            MessageBox mbox = new MessageBox( 
//                    PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
//                    SWT.OK | SWT.ICON_ERROR | SWT.APPLICATION_MODAL );
//            mbox.setMessage( "Fehler: " + e.toString() );
//            mbox.setText( "Fehler bei der Operation." );
//            mbox.open();
        }
    }


    public void selectionChanged( IAction action, ISelection sel ) {
        log.debug( "sel= " + sel );
        layer = null;
        if (sel instanceof StructuredSelection) {
            Object elm = ((StructuredSelection)sel).getFirstElement();
            if (elm instanceof ILayer) {
                layer = (ILayer)elm;
            }
        }

        // check editors
        MapEditor editor = null;
        if (layer != null) {
            IWorkbench workbench = PlatformUI.getWorkbench();
            final IWorkbenchPage page = workbench.getActiveWorkbenchWindow().getActivePage();
            
            editor = (MapEditor)page.findEditor( new MapEditorInput( layer.getMap() ) );
            log.debug( "editor: " + editor );
            if (editor == null) {
                layer = null;
            }
        }

        // enable/disable
        if (layer != null 
                && editor != null
                && layer.isVisible()
                && !layer.isEditable()
                // no other layer is currently in select mode
                && (editor.findSupport( ISelectFeatureSupport.class) == null || layer.isSelectable())) {
            
            action.setEnabled( true );
            action.setChecked( layer.isSelectable() );
            if (origTooltip == null) {
                origTooltip = action.getToolTipText();
            }
//            action.setToolTipText( origTooltip + ": aus" );
        }
        else {
            action.setEnabled( false );
            action.setChecked( false );
//            action.setToolTipText( origTooltip );
        }
    }

}
