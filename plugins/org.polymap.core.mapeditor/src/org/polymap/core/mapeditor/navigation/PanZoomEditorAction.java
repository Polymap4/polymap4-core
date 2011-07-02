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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.widgets.Event;

import org.eclipse.jface.action.IAction;

import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;

import org.polymap.core.mapeditor.IMapEditorSupport;
import org.polymap.core.mapeditor.IMapEditorSupportListener;
import org.polymap.core.mapeditor.INavigationSupport;
import org.polymap.core.mapeditor.MapEditor;
import org.polymap.core.mapeditor.actions.MouseModeAction;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class PanZoomEditorAction
        extends MouseModeAction
        implements IEditorActionDelegate, IMapEditorSupportListener {

    private static Log log = LogFactory.getLog( PanZoomEditorAction.class );

    /** The editor support interface that is currently handled by this action. */
    private NavigationSupport         navigation;
    
    
    public void dispose() {
    }


    public void setActiveEditor( IAction action, IEditorPart targetEditor ) {
        // disconnect old editor
        if (mapEditor != null) {
            mapEditor.removeSupportListener( this );
        }
        
        // connect new editor
        super.setActiveEditor( action, targetEditor );
        
        if (mapEditor != null) {
            navigation = (NavigationSupport)mapEditor.findSupport( INavigationSupport.class );
            if (navigation == null) {
                navigation = new NavigationSupport( mapEditor );
                mapEditor.addSupport( navigation );
                mapEditor.activateSupport( navigation, true );
            }
            action.setEnabled( true );
            action.setChecked( mapEditor.isActive( navigation ) );
            mapEditor.addSupportListener( this );
        }
        else {
            action.setEnabled( false );
            action.setChecked( false );
        }
    }


    public void supportStateChanged( MapEditor _editor, IMapEditorSupport support, boolean activated ) {
        if (support != navigation && !activated) {
            mapEditor.activateSupport( navigation, true );
        }
        if (support == navigation) {
            action.setChecked( mapEditor.isActive( navigation ) );
        }
    }


    public void runWithEvent( IAction _action, Event ev ) {
        log.debug( "ev= " + ev );
        assert navigation != null;
        mapEditor.activateSupport( navigation, action.isChecked() );
    }

}
