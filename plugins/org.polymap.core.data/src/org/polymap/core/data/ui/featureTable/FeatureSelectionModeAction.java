/* 
 * polymap.org
 * Copyright 2011, Polymap GmbH. All rights reserved.
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
package org.polymap.core.data.ui.featureTable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;

import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

import org.polymap.core.geohub.LayerFeatureSelectionManager;
import org.polymap.core.geohub.LayerFeatureSelectionManager.MODE;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class FeatureSelectionModeAction
        implements IViewActionDelegate {

    private static Log log = LogFactory.getLog( FeatureSelectionModeAction.class );

    protected GeoSelectionView      view;
    

    public void init( IViewPart _view ) {
        this.view = (GeoSelectionView)_view;
    }

    protected void setMode( MODE mode ) {
        LayerFeatureSelectionManager fsm = LayerFeatureSelectionManager.forLayer( view.getLayer() );
        fsm.setMode( mode );
    }
    
    public void selectionChanged( IAction action, ISelection selection ) {
    }
    
}
