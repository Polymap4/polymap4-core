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

package org.polymap.core.data.ui.featureTable;

import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;

import org.geotools.factory.CommonFactoryFinder;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionDelegate;

import org.polymap.core.data.DataPlugin;
import org.polymap.core.geohub.GeoHub;
import org.polymap.core.geohub.event.GeoEvent;
import org.polymap.core.project.ILayer;
import org.polymap.core.workbench.PolymapWorkbench;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class FeatureTableAction
        extends ActionDelegate
        implements IObjectActionDelegate {

    private IWorkbenchPart      activePart;
    
    private ILayer              selectedLayer;
    

    public void setActivePart( IAction action, IWorkbenchPart targetPart ) {
        this.activePart = targetPart;
    }


    public void runWithEvent( IAction action, Event ev ) {
        Display.getCurrent().asyncExec( new Runnable() {
            public void run() {
                try {
                    // ensure that the view is shown
                    GeoSelectionView view = GeoSelectionView.open( selectedLayer, true );
                    
                    FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2( null );
                    
                    // emulate a selection event so that the view can
                    GeoEvent event = new GeoEvent( GeoEvent.Type.FEATURE_SELECTED, 
                            selectedLayer.getMap().getLabel(), 
                            selectedLayer.getGeoResource().getIdentifier().toURI() );
                    event.setFilter( Filter.INCLUDE );
                    GeoHub.instance().send( event );
                }
                catch (Exception e) {
                    PolymapWorkbench.handleError( DataPlugin.PLUGIN_ID, this, "Fehler beim Öffnen der Attributtabelle.", e );
                }
            }
        });
    }


    public void selectionChanged( IAction action, ISelection sel ) {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (sel instanceof IStructuredSelection) {
            Object elm = ((IStructuredSelection)sel).getFirstElement();
            selectedLayer = (elm != null && elm instanceof ILayer)
                    ? (ILayer)elm : null;
        }
        else {
            selectedLayer = null;
        }
    }

}
