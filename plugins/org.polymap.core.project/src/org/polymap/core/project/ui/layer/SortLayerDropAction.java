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
package org.polymap.core.project.ui.layer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.operation.OperationSupport;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.ProjectPlugin;
import org.polymap.core.project.ProjectRepository;
import org.polymap.core.project.operations.SetPropertyOperation;
import org.polymap.core.workbench.PolymapWorkbench;

import net.refractions.udig.ui.IDropAction;
import net.refractions.udig.ui.ViewerDropLocation;

/**
 * This drop action handles {@link ILayer} objects dropped into an
 * {@link MapLayersView} in order to change the Z-priority of the dropped layer.
 * 
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class SortLayerDropAction
        extends IDropAction {

    private static Log log = LogFactory.getLog( SortLayerDropAction.class );


    public boolean accept() {
        Object data = getData();
        Object dest = getDestination();
        ViewerDropLocation location = getViewerLocation();
        log.info( "Drop accept(): data=" + data + ", dest=" + dest + ", location=" + location );
        
        // check dest
        if (! (dest instanceof ILayer)) {
            return false;
        }
        
        // check data
        if (data.getClass().isArray()) {
            Object[] objects = ((Object[])data);
            for (Object object : objects) {
                if (accept( object )) {
                    return true;
                }
            }
            return false;
        }
        else {
            return accept( data );
        }
    }

    
    protected boolean accept( Object data ) {
        if (data instanceof ILayer) {
            ILayer layer = (ILayer)data;
            return true;
        }
        else {
            return false;
        }
    }


    public void perform( IProgressMonitor monitor ) {
        Object data = getData();
        Object dest = getDestination();
        ViewerDropLocation location = getViewerLocation();
        log.info( "Drop perform(): data=" + data + ", dest=" + dest + ", location=" + location );
        
        ILayer destLayer = (ILayer)dest;
        
        int order = -1;
        switch (location) {
            case AFTER: {
                order = destLayer.getOrderKey() - 1;
                break;
            }
            case ON:
            case BEFORE: {
                order = destLayer.getOrderKey() + 1;
                break;
            }
            case NONE: {
                log.info( "no drop location, skipping..." );
                return;
            }
        }

        if (data.getClass().isArray()) {
            Object[] array = (Object[])data;
            for (Object object : array) {
                if (accept( object )) {
                    perform( (ILayer)object, order, monitor );
                }
            }
        }
        else {
            perform( (ILayer)data, order, monitor );
        }

    }
    
    
    protected void perform( ILayer layer, int order, IProgressMonitor monitor ) {
        // shift up layers
        for (ILayer cursor : layer.getMap().getLayers()) {
            if (cursor.getOrderKey() == order) {
                perform( cursor, cursor.getOrderKey()+1, monitor );
            }
        }
        
        try {
            SetPropertyOperation op = ProjectRepository.instance().newOperation( SetPropertyOperation.class ); 
            op.init( ILayer.class, layer, ILayer.PROP_ORDERKEY, Integer.valueOf( order ) );
            // XXX JobMonitors
            OperationSupport.instance().execute( op, false, false );
        }
        catch (ExecutionException e) {
            PolymapWorkbench.handleError( ProjectPlugin.PLUGIN_ID, this, e.getLocalizedMessage(), e );
        }
    }

}
