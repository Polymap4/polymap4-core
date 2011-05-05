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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.widgets.Event;

import org.eclipse.jface.action.IAction;

import org.eclipse.ui.IEditorPart;

import org.polymap.core.mapeditor.IMapEditorSupport;
import org.polymap.core.mapeditor.IMapEditorSupportListener;
import org.polymap.core.mapeditor.ISelectFeatureSupport;
import org.polymap.core.mapeditor.MapEditor;
import org.polymap.core.mapeditor.actions.MouseModeAction;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class SelectFeatureEditorAction
        extends MouseModeAction 
        implements IMapEditorSupportListener {

    private static Log log = LogFactory.getLog( SelectFeatureEditorAction.class );

    private SelectFeatureSupport        support;
    
    private PropertyChangeListener      propChangeListener = new PropChangeListener();

    
    public void init( IAction _action ) {
        super.init( _action );
//        ProjectRepository.instance().addPropertyChangeListener( propChangeListener );
    }

    public void dispose() {
        super.dispose();
//        ProjectRepository.instance().removePropertyChangeListener( propChangeListener );
    }


    public void setActiveEditor( IAction action, IEditorPart targetEditor ) {
        // disconnect old editor
        if (mapEditor != null && mapEditor != targetEditor) {
            mapEditor.removeSupportListener( this );
        }
        
        // connect new editor
        super.setActiveEditor( action, targetEditor );

        if (mapEditor != null) {
            support = (SelectFeatureSupport)mapEditor.findSupport( ISelectFeatureSupport.class );
            action.setEnabled( support != null );
            action.setChecked( mapEditor.isActive( support ) );
            mapEditor.addSupportListener( this );
        }
        else {
            action.setEnabled( false );
            action.setChecked( false );
        }
    }

    
    public void supportStateChanged( MapEditor _editor, IMapEditorSupport _support, boolean _activated ) {
        if (support == _support) {
            log.debug( "activated= " + _activated );
            action.setChecked( _activated );
        }
    }


    /**
     * 
     */
    class PropChangeListener
            implements PropertyChangeListener {

        public void propertyChange( PropertyChangeEvent ev ) {
//            if (editor != null 
//                    && ev.getSource() instanceof ILayer
//                    && ev.getPropertyName().equals( "edit" )
//                    && ev.getNewValue().equals( true )) {
//                
//                ILayer layer = (ILayer)ev.getSource();
//                if (layer.getMap() == editor.getMap()) {
//                    if (!support.isActive()) {
//                        
//                        // async to let the RenderManager produce the vectorLayer first
//                        Display display = Polymap.getSessionDisplay();
//                        display.asyncExec( new Runnable() {
//                            public void run() {
//                                selectFeature.setActive( true );
//                                action.setChecked( true );                        
//                            }
//                        });
//                    }
//                }
//            }
        }
    }

    
    public void runWithEvent( IAction _action, Event ev ) {
        log.debug( "ev= " + ev );
        assert support != null;
        mapEditor.activateSupport( support, _action.isChecked() );
    }

}
