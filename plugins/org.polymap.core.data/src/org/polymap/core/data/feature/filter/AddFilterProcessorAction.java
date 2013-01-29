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
package org.polymap.core.data.feature.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.opengis.filter.Filter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;

import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

import org.polymap.core.data.DataPlugin;
import org.polymap.core.data.ui.featureselection.FeatureSelectionView;
import org.polymap.core.operation.OperationSupport;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.PipelineProcessorConfiguration;
import org.polymap.core.project.operations.SetProcessorConfigurationsOperation;
import org.polymap.core.workbench.PolymapWorkbench;

/**
 * Adds a {@link FeatureFilterProcessor} to the pipeline of the layer
 * of the {@link FeatureSelectionView} we are working for.
 *  
 * @see FeatureFilterProcessor
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
public class AddFilterProcessorAction
        implements IViewActionDelegate {

    private static Log log = LogFactory.getLog( AddFilterProcessorAction.class );

    private FeatureSelectionView        selectionView;

    
    public void init( IViewPart view ) {
        selectionView = (FeatureSelectionView)view;
    }


    public void run( IAction action ) {
        Filter filter = selectionView.getFilter();
        log.info( "Filter: " + filter );
        
        ILayer layer = selectionView.getLayer();
        try {
            List<PipelineProcessorConfiguration> configs = new ArrayList(
                    Arrays.asList( layer.getProcessorConfigs() ) );

            // new config
            PipelineProcessorConfiguration newConfig = new PipelineProcessorConfiguration( "org.polymap.core.data.FilterProcessor", "Feature-Filter" );
            newConfig.getConfig().put( "filter", FeatureFilterProcessorConfig.encodeFilter( filter ) );
            configs.add( newConfig );

            // operation
            SetProcessorConfigurationsOperation op = new SetProcessorConfigurationsOperation();
            op.init( layer, configs.toArray(new PipelineProcessorConfiguration[configs.size()]) );
            OperationSupport.instance().execute( op, false, false );
        }
        catch (Exception e) {
            PolymapWorkbench.handleError( DataPlugin.PLUGIN_ID, this, e.getLocalizedMessage(), e );
        }
    }


    public void selectionChanged( IAction action, ISelection selection ) {
    }
    
}
