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
import org.geotools.geometry.jts.ReferencedEnvelope;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;

import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;

import org.polymap.core.mapeditor.MapEditor;
import org.polymap.core.mapeditor.MapEditorPlugin;
import org.polymap.core.workbench.PolymapWorkbench;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class MaxExtendEditorAction
        implements IEditorActionDelegate {

    private static Log log = LogFactory.getLog( MaxExtendEditorAction.class );

    private MapEditor           mapEditor;
    

    public void init( IViewPart _view ) {
    }


    public void setActiveEditor( IAction action, IEditorPart targetEditor ) {
        this.mapEditor = targetEditor instanceof MapEditor 
                ? (MapEditor)targetEditor : null;
    }


    public void run( IAction action ) {
            try {
                ReferencedEnvelope bbox = mapEditor.getMap().getMaxExtent();
                log.info( "Feature bbox: " + bbox );
                mapEditor.getMap().setExtent( bbox );
            }
            catch (Exception e) {
                PolymapWorkbench.handleError( MapEditorPlugin.PLUGIN_ID, this, e.getLocalizedMessage(), e );
            }
    }


    public void selectionChanged( IAction action, ISelection sel ) {
        log.info( "Selection: " + sel );
    }

}
