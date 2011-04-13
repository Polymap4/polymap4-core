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
package org.polymap.rhei.internal.filter;

import org.opengis.filter.Filter;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;

import org.polymap.core.data.DataPlugin;
import org.polymap.core.data.ui.featureTable.GeoSelectionView;
import org.polymap.core.geohub.GeoHub;
import org.polymap.core.geohub.event.GeoEvent;
import org.polymap.core.workbench.PolymapWorkbench;
import org.polymap.rhei.Messages;
import org.polymap.rhei.filter.IFilter;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @version ($Revision$)
 */
public class OpenFilterAction
        extends Action
        implements IAction {

    private IFilter             filter;
    
    
    OpenFilterAction( IFilter filter ) {
        super( Messages.get( "OpenFilterAction_name" ) );
        setToolTipText( Messages.get( "OpenFilterAction_tip" ) );
        this.filter = filter;
    }

    public void run() {
        try {
            Filter filterFilter = filter.createFilter();

            if (filterFilter != null) {
                // ensure that the view is shown
                // XXX allow search when incremental search is there
                GeoSelectionView view = GeoSelectionView.open( filter.getLayer(), false );
                
                // emulate a selection event so that the view can handle it
                GeoEvent event = new GeoEvent( GeoEvent.Type.FEATURE_SELECTED, 
                        filter.getLayer().getMap().getLabel(), 
                        filter.getLayer().getGeoResource().getIdentifier().toURI() );
                event.setFilter( filterFilter );
                GeoHub.instance().send( event );
            }
        }
        catch (Exception e) {
            PolymapWorkbench.handleError( DataPlugin.PLUGIN_ID, this, "Fehler beim Öffnen der Attributtabelle.", e );
        }
    }

}
