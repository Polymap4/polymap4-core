/* 
 * polymap.org
 * Copyright 2009-2012, Polymap GmbH. All rights reserved.
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
package org.polymap.core.mapeditor.tooling.navi;

import java.util.List;

import java.beans.PropertyChangeEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.unitofwork.NoSuchEntityException;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;

import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;

import org.polymap.core.mapeditor.MapEditorPlugin;
import org.polymap.core.mapeditor.workbench.MapEditor;
import org.polymap.core.mapeditor.workbench.NavigationHistory;
import org.polymap.core.project.IMap;
import org.polymap.core.runtime.event.EventFilter;
import org.polymap.core.runtime.event.EventHandler;
import org.polymap.core.workbench.PolymapWorkbench;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @since 3.1
 */
public class ForwardEditorAction
        implements IEditorActionDelegate {

    private static Log log = LogFactory.getLog( ForwardEditorAction.class );

    private MapEditor           mapEditor;
    
    private IAction             action;
    

    public void dispose() {
        if (mapEditor != null) {
            mapEditor.getMap().removePropertyChangeListener( this );
        }
    }

    
    public void setActiveEditor( IAction _action, IEditorPart _editor ) {
        // disconnect old editor
        if (mapEditor != null) {
            try {
                mapEditor.getMap().removePropertyChangeListener( this );
            }
            catch (NoSuchEntityException e) {
            }
        }
        
        action = _action;
        mapEditor = _editor instanceof MapEditor ? (MapEditor)_editor : null;
        if (mapEditor != null) {
            NavigationHistory history = mapEditor.getNaviHistory();
            action.setEnabled( history.canRedo() );
            mapEditor.getMap().addPropertyChangeListener( this, new EventFilter<PropertyChangeEvent>() {
                public boolean apply( PropertyChangeEvent ev ) {
                    String name = ev.getPropertyName();
                    return action != null
                            && (IMap.PROP_EXTENT.equals( name ) || IMap.PROP_EXTENT_UPDATE.equals( name ));
                }
            });
        }
        else {
            action.setEnabled( false );
        }
    }


    @EventHandler(delay=1000,display=true)
    public void propertyChange( List<PropertyChangeEvent> ev ) {
        NavigationHistory history = mapEditor.getNaviHistory();
        action.setEnabled( history != null && history.canRedo() );
    }


    public void run( IAction _action ) {
        try {
            NavigationHistory history = mapEditor.getNaviHistory();
            history.redo();
        }
        catch (Exception e) {
            PolymapWorkbench.handleError( MapEditorPlugin.PLUGIN_ID, this, e.getLocalizedMessage(), e );
        }
    }


    public void selectionChanged( IAction _action, ISelection _sel ) {
    }

}
