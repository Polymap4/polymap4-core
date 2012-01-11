/*
 * polymap.org
 * Copyright 2011, Falko Bräutigam, and other contributors as
 * indicated by the @authors tag.
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
package org.polymap.core.data.operations.feature;

import org.geotools.data.DefaultQuery;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;

import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

import org.polymap.core.data.DataPlugin;
import org.polymap.core.data.PipelineFeatureSource;
import org.polymap.core.data.ui.featureselection.FeatureSelectionView;
import org.polymap.core.operation.OperationSupport;
import org.polymap.core.workbench.PolymapWorkbench;

/**
 *
 * @deprecated As of {@link CopyFeaturesOperation2}
 * @see CopyFeaturesOperation
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class CopyGeoSelectionFeaturesAction
        implements IViewActionDelegate {

    private static Log log = LogFactory.getLog( CopyGeoSelectionFeaturesAction.class );

    private FeatureSelectionView        view;


    public void init( IViewPart _view ) {
        this.view = (FeatureSelectionView)_view;
    }


    public void run( IAction action ) {
        try {
            PipelineFeatureSource fs = view.getFeatureStore();
            DefaultQuery query = new DefaultQuery( fs.getSchema().getName().getLocalPart(),
                    view.getFilter() );

            CopyFeaturesOperation op = new CopyFeaturesOperation( fs, query );
            OperationSupport.instance().execute( op, true, true );
        }
        catch (Exception e) {
            PolymapWorkbench.handleError( DataPlugin.PLUGIN_ID, this, e.getLocalizedMessage(), e );
        }
    }


    public void selectionChanged( IAction action, ISelection sel ) {
//        log.debug( "selectionChanged(): sel= " + sel );
//
//        selectedFid = null;
//
//        if (!sel.isEmpty() && sel instanceof IStructuredSelection) {
//            Object elm = ((IStructuredSelection)sel).getFirstElement();
//
//            // called when the entity is clicked in GeoSellectionView
//            if (elm instanceof FidFilterImpl) {
//                Set fids = ((FidFilterImpl)elm).getIDs();
//                selectedFid = fids.size() == 1
//                        ? (String)fids.iterator().next() : null;
//            }
//        }
//        action.setEnabled( view != null && selectedFid != null );
    }

}
