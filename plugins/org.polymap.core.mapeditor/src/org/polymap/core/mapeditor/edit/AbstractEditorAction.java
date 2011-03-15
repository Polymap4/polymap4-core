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
package org.polymap.core.mapeditor.edit;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.jface.action.IAction;

import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;

import org.polymap.core.mapeditor.IMapEditorSupport;
import org.polymap.core.mapeditor.IMapEditorSupportListener;
import org.polymap.core.mapeditor.MapEditor;
import org.polymap.core.mapeditor.actions.MouseModeAction;
import org.polymap.openlayers.rap.widget.controls.Control;

/**
 * Provides base handling of editor activation, support change and control
 * activation.
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
abstract class AbstractEditorAction
        extends MouseModeAction
        implements IEditorActionDelegate, IMapEditorSupportListener, PropertyChangeListener {

    private static Log log = LogFactory.getLog( AbstractEditorAction.class );

    /** The editor support interface that is currently handled by this action. */
    protected EditFeatureSupport            support;
    
    protected Class<? extends Control>      controlType;    
    

    public void setActiveEditor( IAction action, IEditorPart targetEditor ) {
        //log.debug( "### active editor: editor=" + mapEditor + "; new: " + targetEditor  );
        // disconnect old editor
        if (mapEditor != null && mapEditor != targetEditor) {
            mapEditor.removeSupportListener( this );
            if (support != null) {
                support.removeControlListener( this );
            }
        }
        
        // connect new editor
        super.setActiveEditor( action, targetEditor );
        
        if (mapEditor != null) {
            support = (EditFeatureSupport)mapEditor.findSupport( EditFeatureSupport.class );
            if (support == null) {
                action.setEnabled( false );
            }
            else {
                action.setEnabled( true );
                mapEditor.addSupportListener( this );
                support.addControlListener( this );

                action.setChecked( 
                        mapEditor.isActive( support ) &&
                        support.isControlActive( controlType ) );
            }
        }
        else {
            action.setEnabled( false );
            //action.setChecked( false );
        }
    }

    public void supportStateChanged( MapEditor _editor, IMapEditorSupport _support, boolean _activated ) {
        if (this.support == _support) {
            log.debug( "supportStateChanged(): activated= " + _activated );
            
            action.setChecked( _activated && 
                    support.isControlActive( controlType ) );

            if (support.isControlActive( controlType )) {
                support.setControlActive( controlType, false );
            }
        }
    }

    public void propertyChange( PropertyChangeEvent ev ) {
        log.debug( "propertyChange(): ev= " + ev );
        
        // deactivate if another action was enabled
        if (!ev.getSource().getClass().equals( controlType ) &&
                ((Boolean)ev.getNewValue()).booleanValue() ) {
            action.setChecked( false );
            
            if (support.isControlActive( controlType )) {
                support.setControlActive( controlType, false );
            }
        }
    }

}
