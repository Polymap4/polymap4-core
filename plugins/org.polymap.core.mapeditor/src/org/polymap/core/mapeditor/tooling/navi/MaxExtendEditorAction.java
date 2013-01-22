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
 * @since 3.1
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
    }

}
