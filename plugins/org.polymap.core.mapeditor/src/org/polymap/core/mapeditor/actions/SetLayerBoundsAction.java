/* 
 * polymap.org
 * Copyright 2009, 2011 Polymap GmbH. All rights reserved.
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
package org.polymap.core.mapeditor.actions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;

import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.polymap.core.mapeditor.MapEditorPlugin;
import org.polymap.core.mapeditor.operations.SetLayerBoundsOperation;
import org.polymap.core.operation.OperationSupport;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.ui.layer.LayerNavigator;
import org.polymap.core.workbench.PolymapWorkbench;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @since 3.0
 */
public class SetLayerBoundsAction
        implements IObjectActionDelegate, IEditorActionDelegate, IViewActionDelegate, ISelectionListener {

    private static Log log = LogFactory.getLog( SetLayerBoundsAction.class );

    private LayerNavigator          view;
    
    private ILayer                  layer;

    /* The selection service if used as an editor action. */
    private ISelectionService       selectionService;
    
    private IAction                 action;
    
    private String                  origTooltip;
    
    
    public void init( IViewPart view0 ) {
        this.view = (LayerNavigator)view0;
    }


    public void run( IAction action ) {
        try {
            CoordinateReferenceSystem crs = layer.getMap().getCRS();
            SetLayerBoundsOperation op = new SetLayerBoundsOperation( layer, crs );

            OperationSupport.instance().execute( op, true, false );
        }
        catch (Exception e) {
            PolymapWorkbench.handleError( MapEditorPlugin.PLUGIN_ID, this, "", e );
        }
    }


    public void selectionChanged( IAction action, ISelection sel ) {
        log.info( "sel: " + sel );
        if (sel instanceof StructuredSelection) {
            Object elm = ((StructuredSelection)sel).getFirstElement();
            if (elm instanceof ILayer) {
                layer = (ILayer)elm;
                origTooltip = origTooltip == null ? action.getToolTipText() : origTooltip;
                action.setToolTipText( origTooltip + " : " + layer.getLabel() );
                action.setEnabled( true );
                return;
            }
        }
        layer = null;
        action.setEnabled( false );
    }


    public void setActivePart( IAction action, IWorkbenchPart targetPart ) {
        this.action = action;
    }


    public void setActiveEditor( IAction action, IEditorPart targetEditor ) {
        if (selectionService != null) {
            selectionService.removeSelectionListener( this );
        }
        
        if (targetEditor != null) {
            IEditorSite editorSite = targetEditor.getEditorSite();
            selectionService = editorSite.getWorkbenchWindow().getSelectionService();
            selectionService.addSelectionListener( this );
            this.action = action;
        }
        else {
            action.setEnabled( false );
        }
    }


    public void selectionChanged( IWorkbenchPart part, ISelection selection ) {
        selectionChanged( action, selection );
    }

}
