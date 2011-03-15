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
package org.polymap.core.mapeditor.navigation;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;

import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;

import org.polymap.core.geohub.event.GeoEvent;
import org.polymap.core.mapeditor.INavigationSupport;
import org.polymap.core.mapeditor.MapEditor;
import org.polymap.core.mapeditor.MapEditorPlugin;
import org.polymap.core.project.IMap;
import org.polymap.core.project.ProjectRepository;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.workbench.PolymapWorkbench;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class BackwardEditorAction
        implements IEditorActionDelegate, PropertyChangeListener {

    private static Log log = LogFactory.getLog( BackwardEditorAction.class );

    private MapEditor           mapEditor;
    
    private INavigationSupport  support;
    
    private IAction             action;
    

    public void dispose() {
        ProjectRepository.instance().removePropertyChangeListener( this );
    }

    
    public void propertyChange( final PropertyChangeEvent ev ) {
        String name = ev.getPropertyName();
        if (action != null
                && mapEditor.getMap().equals( ev.getSource() )
                && (IMap.PROP_EXTENT.equals( name ) || IMap.PROP_EXTENT_UPDATE.equals( name )) ) {

            Polymap.getSessionDisplay().asyncExec( new Runnable() {
                public void run() {
                    log.debug( "propertyChange(): canUndo= " + support.canUndo() );
                    action.setEnabled( support != null && support.canUndo() );
                }
            });
        }
    }

    
    public void onEvent( final GeoEvent ev ) {
        if (action != null) {
            Polymap.getSessionDisplay().asyncExec( new Runnable() {
                public void run() {
                    log.debug( "onEvent(): ev= " + ev + ", canUndo= " + support.canUndo() );
                    action.setEnabled( support != null && support.canUndo() );
                }
            });
        }
    }

    
    public void setActiveEditor( IAction _action, IEditorPart _targetEditor ) {
        // disconnect old editor
        if (mapEditor != null) {
            ProjectRepository.instance().removePropertyChangeListener( this );
            //GeoHub.instance().unsubscribe( this ); 
        }
        
        action = _action;
        mapEditor = _targetEditor instanceof MapEditor 
                ? (MapEditor)_targetEditor : null;
                
        if (mapEditor != null && mapEditor.findSupport( INavigationSupport.class ) != null) {
            support = (INavigationSupport)mapEditor.findSupport( INavigationSupport.class );
            action.setEnabled( support.canUndo() );

            ProjectRepository.instance().addPropertyChangeListener( this );
//            GeoHub.instance().subscribe( this, new GeoEventSelector( 
//                    new GeoEventSelector.TypeFilter( GeoEvent.Type.NAVIGATION ) ) );
        }
        else {
            action.setEnabled( false );
        }
    }


    public void run( IAction _action ) {
        try {
            support.undo();
        }
        catch (Exception e) {
            PolymapWorkbench.handleError( MapEditorPlugin.PLUGIN_ID, this, e.getLocalizedMessage(), e );
        }
    }


    public void selectionChanged( IAction _action, ISelection _sel ) {
        log.debug( "selection: " + _sel );
    }

}
