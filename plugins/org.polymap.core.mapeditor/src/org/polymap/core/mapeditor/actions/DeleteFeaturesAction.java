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
package org.polymap.core.mapeditor.actions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.opengis.filter.Filter;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

import org.polymap.core.data.PipelineFeatureSource;
import org.polymap.core.data.ui.featureTable.GeoSelectionView;
import org.polymap.core.mapeditor.MapEditorPlugin;
import org.polymap.core.workbench.PolymapWorkbench;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class DeleteFeaturesAction
        implements IViewActionDelegate {

    private static Log log = LogFactory.getLog( DeleteFeaturesAction.class );
    
    private GeoSelectionView            view;
    
    private Filter                      selectionFilter;
    

    public void init( IViewPart _view ) {
        this.view = (GeoSelectionView)_view;
    }


    public void run( IAction action ) {
        if (selectionFilter != null) {
            try {
                // XXX needs an operation
                PipelineFeatureSource fs = PipelineFeatureSource.forLayer( view.getLayer(), true );

//                FilterFactory ff = CommonFactoryFinder.getFilterFactory(null);
//                Id fidFilter = ff.id( Collections.singleton( 
//                        ff.featureId( selection.getIdentifier().getID() ) ) );
                
                fs.removeFeatures( selectionFilter );
            }
            catch (Exception e) {
                PolymapWorkbench.handleError( MapEditorPlugin.PLUGIN_ID, this, e.getLocalizedMessage(), e );
            }
        }
    }


    public void selectionChanged( IAction action, ISelection sel ) {
        log.info( "Selection: " + sel );
        if (sel instanceof IStructuredSelection) {
            Object obj = ((IStructuredSelection)sel).getFirstElement();
            if (obj instanceof Filter) {
                selectionFilter = (Filter)obj;
            }
        }
    }

}
