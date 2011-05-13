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
package org.polymap.rhei.internal.csv;

import java.util.ArrayList;
import java.util.List;

import java.io.IOException;

import org.opengis.feature.Feature;
import org.opengis.feature.FeatureVisitor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.rwt.widgets.ExternalBrowser;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;

import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

import org.polymap.core.data.ui.featureTable.GeoSelectionView;
import org.polymap.core.workbench.PolymapWorkbench;
import org.polymap.rhei.RheiPlugin;

/**
 *
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class DownloadCSVAction
        implements IViewActionDelegate {

    private static Log log = LogFactory.getLog( DownloadCSVAction.class );

    private GeoSelectionView    view;


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
                    features.add( candidate );
                }
            }, null );

            String id = String.valueOf( System.currentTimeMillis() );
            CsvServlet.map.put( id, features );
            log.info( "CSV: " + features.size() + " features given to servlet for id: " + id );

            String filename = view.getLayer() != null
                    ? view.getLayer().getLabel() + "_export.csv" : "polymap3_export.csv";
            String linkTarget = "../csv/" + id + "/" + filename;
            String htmlTarget = "../csv/download.html?id=" + id + "&filename=" + filename;

            ExternalBrowser.open( "csv_download_window", htmlTarget ,
                    ExternalBrowser.NAVIGATION_BAR | ExternalBrowser.STATUS);

//            JSExecutor.executeJS(
//                    "var newWindow = window.open('" + htmlTarget +  "', '_blank');" );

        }
        catch (IOException e) {
            PolymapWorkbench.handleError( RheiPlugin.PLUGIN_ID, this, e.getLocalizedMessage(), e );
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
