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
 * @since 3.0
 */
abstract class AbstractEditEditorAction
        extends MouseModeAction
        implements IEditorActionDelegate, IMapEditorSupportListener, PropertyChangeListener {

    private static Log log = LogFactory.getLog( AbstractEditEditorAction.class );

    /** The editor support interface that is currently handled by this action. */
    protected EditFeatureSupport            support;
    
    protected Class<? extends Control>      controlType;    
    

    public void setActiveEditor( IAction action, IEditorPart targetEditor ) {
        // disconnect old editor
        if (mapEditor != null) {
            mapEditor.removeSupportListener( this );
            if (support != null) {
                support.removeControlListener( this );
            }
        }
        
        // connect new editor
        super.setActiveEditor( action, targetEditor );
        
        action.setEnabled( false );
        action.setChecked( false );

        if (mapEditor != null) {
            support = (EditFeatureSupport)mapEditor.findSupport( EditFeatureSupport.class );
            if (support != null) {
                action.setEnabled( true );
                mapEditor.addSupportListener( this );
                support.addControlListener( this );

                action.setChecked( 
                        mapEditor.isActive( support ) &&
                        support.isControlActive( controlType ) );
            }
        }
    }

    public void supportStateChanged( MapEditor _editor, IMapEditorSupport _support, boolean _activated ) {
        if (this.support == _support) {
            log.debug( "supportStateChanged(): activated= " + _activated );
            
            action.setChecked( 
                    mapEditor.isActive( support ) && 
                    support.isControlActive( controlType ) );

            if (!_activated) {
                Control control = support.getControl( controlType );
                if (control != null) {
                    control.deactivate();
                }
            }
//            if (support.isControlActive( controlType )) {
//                support.setControlActive( controlType, false );
//            }
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
