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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.widgets.Event;

import org.eclipse.jface.action.IAction;

import org.eclipse.ui.IEditorActionDelegate;

import org.polymap.openlayers.rap.widget.controls.DeleteFeatureControl;
import org.polymap.openlayers.rap.widget.controls.DrawFeatureControl;

/**
 * Editor action for the {@link EditFeatureSupport}. This actions controls
 * the {@link DrawFeatureControl}.
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class DeleteFeatureEditorAction
        extends AbstractEditorAction
        implements IEditorActionDelegate {

    private static Log log = LogFactory.getLog( DeleteFeatureEditorAction.class );

    
    public DeleteFeatureEditorAction() {
        controlType = DeleteFeatureControl.class;
    }


    public void runWithEvent( IAction _action, Event ev ) {
        log.debug( "ev= " + ev );
        assert support != null;
        mapEditor.activateSupport( support, action.isChecked() );
        
        if (action.isChecked()) {
            DeleteFeatureControl control = (DeleteFeatureControl)support.getControl( DeleteFeatureControl.class );
            if (control == null) {
                control = new DeleteFeatureControl( support.vectorLayer );
                support.addControl( control );
            }
            support.setControlActive( DeleteFeatureControl.class, true );
        }
    }

}
