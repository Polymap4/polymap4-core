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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.widgets.Event;

import org.eclipse.jface.action.IAction;

import org.eclipse.ui.IEditorActionDelegate;

import org.polymap.openlayers.rap.widget.controls.SnappingControl;
import org.polymap.openlayers.rap.widget.layers.VectorLayer;

/**
 * Editor action for the {@link EditFeatureSupport}. This actions manipulates
 * the {@link SnappingControl}.
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class SnapEditorAction
        extends AbstractEditEditorAction
        implements IEditorActionDelegate {

    private static Log log = LogFactory.getLog( SnapEditorAction.class );

    
    public SnapEditorAction() {
        controlType = SnappingControl.class;
    }


    public void runWithEvent( IAction _action, Event ev ) {
        log.debug( "ev= " + ev );
        assert support != null;
        
        log.debug( "ev= " + ev );
        assert support != null;
        mapEditor.activateSupport( support, action.isChecked() );
        
        SnappingControl control = (SnappingControl)support.getControl( SnappingControl.class );
        if (control == null) {
            VectorLayer[] targetLayers = new VectorLayer[] {support.vectorLayer};
            control = new SnappingControl( support.vectorLayer, targetLayers, Boolean.FALSE );
            support.addControl( control );
        }

        // don't use EditFeatureSupport.setControlActive() to avoid event and deactivating other
        // editor actions
        if (action.isChecked()) {
            control.activate();
        }
        else {
            control.deactivate();
        }
    }


    public void propertyChange( PropertyChangeEvent ev ) {
        log.debug( "propertyChange(): ev= " + ev );
        // don't deactivate if another action was enabled
    }

}
