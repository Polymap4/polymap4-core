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
package org.polymap.core.data.ui.featureselection;

import org.geotools.feature.FeatureCollection;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;

import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

import org.polymap.core.data.DataPlugin;
import org.polymap.core.data.PipelineFeatureSource;
import org.polymap.core.data.operations.ZoomFeatureBoundsOperation;
import org.polymap.core.operation.OperationSupport;
import org.polymap.core.project.IMap;
import org.polymap.core.workbench.PolymapWorkbench;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ZoomFeatureSelectionAction
        implements IViewActionDelegate {

    private static Log log = LogFactory.getLog( ZoomFeatureSelectionAction.class );

    private FeatureSelectionView        view;


    public void init( IViewPart _view ) {
        view = (FeatureSelectionView)_view;
    }


    public void run( IAction action ) {
        try {
            IMap map = view.getLayer().getMap();
            CoordinateReferenceSystem crs = map.getCRS();
            PipelineFeatureSource fs = view.getFeatureStore();
            FeatureCollection features = fs.getFeatures( view.getFilter() );
            ZoomFeatureBoundsOperation op = new ZoomFeatureBoundsOperation( features, map, crs );

            OperationSupport.instance().execute( op, true, true );
        }
        catch (Exception e) {
            PolymapWorkbench.handleError( DataPlugin.PLUGIN_ID, this, "", e );
        }
    }


    public void selectionChanged( IAction action, ISelection selection ) {
    }
    
}
