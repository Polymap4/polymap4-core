/* 
 * polymap.org
 * Copyright (C) 2013, Falko Bräutigam. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.core.data.image.cache304;

import java.util.ArrayList;
import java.util.List;

import net.refractions.udig.catalog.IService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;

import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import org.polymap.core.data.DataPlugin;
import org.polymap.core.data.pipeline.DefaultPipelineIncubator;
import org.polymap.core.data.pipeline.Pipeline;
import org.polymap.core.data.pipeline.PipelineProcessor;
import org.polymap.core.model.security.ACLUtils;
import org.polymap.core.model.security.AclPermission;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.LayerUseCase;
import org.polymap.core.project.ui.util.SelectionAdapter;
import org.polymap.core.workbench.PolymapWorkbench;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ClearCacheAction
        implements IObjectActionDelegate {

    private static Log log = LogFactory.getLog( ClearCacheAction.class );

    private List<ILayer>        selected = new ArrayList();
    
    
    @Override
    public void run( IAction action ) {
        for (ILayer layer : selected) {
            try {
                Cache304.instance().updateLayer( layer, null );
                layer.setRerender( true );
            }
            catch (Exception e) {
                PolymapWorkbench.handleError( DataPlugin.PLUGIN_ID, this, e.getLocalizedMessage(), e );
            }
        }
    }


    @Override
    public void setActivePart( IAction action, IWorkbenchPart part ) {
        log.info( "Part: " + part );
    }
    

    @Override
    public void selectionChanged( IAction action, ISelection selection ) {
        selected.clear();
        action.setEnabled( false );
        for (ILayer layer : new SelectionAdapter( selection ).elementsOfType( ILayer.class )) {
            // check permissions
            if (!ACLUtils.checkPermission( layer, AclPermission.WRITE, false )) {
                return;
            }
            // check cache processor in pipeline
            try {
                IService service = layer.getGeoResource().service( null );
                Pipeline pipeline = new DefaultPipelineIncubator().newPipeline( LayerUseCase.ENCODED_IMAGE, layer.getMap(), layer, service );
                PipelineProcessor found = Iterables.find( pipeline, Predicates.instanceOf( ImageCacheProcessor.class ), null );
                if (found == null) {
                    return;
                }
            }
            catch (Exception e) {
                log.warn( "", e );
                return;
            }

            selected.add( layer );
        }
        action.setEnabled( true );
    }

}
