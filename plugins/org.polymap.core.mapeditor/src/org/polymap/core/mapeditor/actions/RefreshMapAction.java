/* 
 * polymap.org
 * Copyright (C) 2013, Falko Bräutigam. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.core.mapeditor.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;

import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;

import org.polymap.core.mapeditor.workbench.MapEditor;
import org.polymap.core.mapeditor.workbench.RenderManager.RenderLayerDescriptor;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class RefreshMapAction
        implements IEditorActionDelegate {

    private MapEditor           mapEditor;

    
    @Override
    public void setActiveEditor( IAction action, IEditorPart targetEditor ) {
        mapEditor = targetEditor instanceof MapEditor 
                ? (MapEditor)targetEditor : null;
    }


    @Override
    public void run( IAction action ) {
        List<RenderLayerDescriptor> layers = new ArrayList( mapEditor.layers() );
        for (RenderLayerDescriptor layer : layers) {
            mapEditor.reloadLayer( layer );
        }
    }


    @Override
    public void selectionChanged( IAction action, ISelection selection ) {
    }

}
