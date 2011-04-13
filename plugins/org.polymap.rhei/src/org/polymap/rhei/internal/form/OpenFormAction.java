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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.opengis.feature.Feature;
import org.opengis.feature.FeatureVisitor;

import org.geotools.filter.FidFilterImpl;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

import org.polymap.core.data.ui.featureTable.GeoSelectionView;
import org.polymap.core.workbench.PolymapWorkbench;
import org.polymap.rhei.RheiPlugin;
import org.polymap.rhei.form.FormEditor;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @version ($Revision$)
 */
public class OpenFormAction
        implements IViewActionDelegate {

    private static Log log = LogFactory.getLog( OpenFormAction.class );

    private GeoSelectionView    view;
    
    private String              selectedFid;
    
    
    public void init( IViewPart _view ) {
        if (_view instanceof GeoSelectionView) {
            log.debug( "init(): found GeoSelectionView..." );
            this.view = (GeoSelectionView)_view;
        }
    }


    public void run( IAction action ) {
        try {
            final List<Feature> features = new ArrayList();
            view.getFeatureCollection().accepts( new FeatureVisitor() {
                public void visit( Feature candidate ) {
                    if (candidate.getIdentifier().getID().equals( selectedFid )) {
                        features.add( candidate );
                    }
                }
            }, null );
            
            if (features.isEmpty()) {
                log.warn( "No feature found: " + selectedFid );
                return;
            } 
            if (features.size() > 1) {
                log.warn( "More than one feature for: " + selectedFid );
            }
            FormEditor.open( view.getFeatureStore(), features.get( 0 ) );
        }
        catch (IOException e) {
            PolymapWorkbench.handleError( RheiPlugin.PLUGIN_ID, this, e.getLocalizedMessage(), e );
        }
    }


    public void selectionChanged( IAction action, ISelection sel ) {
        log.debug( "selectionChanged(): sel= " + sel );

        selectedFid = null;
        
        if (!sel.isEmpty() && sel instanceof IStructuredSelection) {
            Object elm = ((IStructuredSelection)sel).getFirstElement();
            
            // called when the entity is clicked in GeoSellectionView
            if (elm instanceof FidFilterImpl) {
                Set fids = ((FidFilterImpl)elm).getIDs();
                selectedFid = fids.size() == 1 
                        ? (String)fids.iterator().next() : null;
            }
        }
        action.setEnabled( view != null && selectedFid != null );
    }

}
