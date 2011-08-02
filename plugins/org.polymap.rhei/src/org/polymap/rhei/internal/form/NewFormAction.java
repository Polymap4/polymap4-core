/* 
 * polymap.org
 * Copyright 2010, Falko Bräutigam, and other contributors as indicated
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
 * $Id: $
 */
package org.polymap.rhei.internal.form;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

import org.polymap.core.data.operations.NewFeatureOperation;
import org.polymap.core.data.ui.featureselection.GeoSelectionView;
import org.polymap.core.operation.OperationSupport;
import org.polymap.core.project.ILayer;
import org.polymap.core.workbench.PolymapWorkbench;
import org.polymap.rhei.RheiPlugin;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @version ($Revision$)
 */
public class NewFormAction
        implements IViewActionDelegate {

    private static Log log = LogFactory.getLog( NewFormAction.class );

    private GeoSelectionView    view;
    
    /** The layer we are associated with. Might be null. */
    private ILayer              layer;

    
    public void init( IViewPart _view ) {
        if (view instanceof GeoSelectionView) {
            log.debug( "init(): found GeoSelectionView..." );
            this.view = (GeoSelectionView)_view;
            this.layer = (view).getLayer();
            assert layer != null : "Layer must not be null.";
        }
    }


    public void run( IAction action ) {
        try {
            NewFeatureOperation op = new NewFeatureOperation( layer, null );
            OperationSupport.instance().execute( op, false, false );
//            FeatureId fid = op.getCreatedFid();
//
//            FeatureStore fs = view != null
//                    ? view.getFeatureStore()
//                    // FIXME do blocking operation inside a job?
//                    : PipelineFeatureSource.forLayer( layer, false );
//
//            Id fidFilter = CommonFactoryFinder.getFilterFactory( null ).id( Collections.singleton( fid ) );
//            FeatureCollection coll = fs.getFeatures( fidFilter );
//            Feature feature = (Feature)coll.toArray( new Feature[1] )[0];
//            FormEditor.open( fs, feature );
        }
        catch (Exception e) {
            PolymapWorkbench.handleError( RheiPlugin.PLUGIN_ID, this, e.getLocalizedMessage(), e );
        }
    }


    public void selectionChanged( IAction action, ISelection sel ) {
        log.debug( "selectionChanged(): sel= " + sel );
        
        // called when popup menu is opened
        if (!sel.isEmpty() && sel instanceof IStructuredSelection) {
            Object elm = ((IStructuredSelection)sel).getFirstElement();
            
            if (elm instanceof ILayer) {
                layer = (ILayer)elm;
            }
        }
    }

}
