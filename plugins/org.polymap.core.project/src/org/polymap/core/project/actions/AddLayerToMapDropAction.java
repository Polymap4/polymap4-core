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
package org.polymap.core.project.actions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.ui.IDropAction;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.operation.OperationSupport;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.IMap;
import org.polymap.core.project.ProjectPlugin;
import org.polymap.core.project.ProjectRepository;
import org.polymap.core.project.model.operations.NewLayerOperation;
import org.polymap.core.project.ui.MapLayersView;
import org.polymap.core.workbench.PolymapWorkbench;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class AddLayerToMapDropAction
        extends IDropAction {

    private static Log log = LogFactory.getLog( AddLayerToMapDropAction.class );

    
    public AddLayerToMapDropAction() {
        log.info( "..." );
    }


    public boolean accept() {
        Object data = getData();
        Object dest = getDestination();
        log.info( "Drop accept(): data=" + data + ", dest=" + dest );
        
//        if (dest instanceof ProjectView) {
//            Display display = RWTLifeCycle.getSessionDisplay();
//            display.asyncExec( new Runnable() {
//                public void run() {
//                    //Status status = new Status( IStatus.ERROR, pluginId, msg, e );
//                    ErrorDialog dialog = new ErrorDialog( 
//                            PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
//                            "Einen Moment bitte",
//                            "Das direkte Anlagen eines Projektes per Drag&Drop wird noch nicht unterstützt.\nZiehen Sie direkt auf eine Projekt, um dort einen neuen Layer zu erzeugen.",
//                            status,
//                            SWT.OK | SWT.ICON_ERROR | SWT.APPLICATION_MODAL );
//                    dialog.open();
//                }
//            });
//            return false;
//        }
        
        // check dest
        if (!(dest instanceof IMap) 
                && !(dest instanceof ILayer)
                && !(dest instanceof MapLayersView)) {
            return false;
        }
        
        // check data
        if (data.getClass().isArray()) {
            Object[] objects = ((Object[])data);
            for (Object object : objects) {
                if (checkData( object )) {
                    return true;
                }
            }
            return false;
        }
        else {
            return checkData( data );
        }
    }

    
    protected boolean checkData( Object data) {
        if (data instanceof IGeoResource) {
            return true;
        }
        else {
            return false;
        }
    }


    public void perform( IProgressMonitor monitor ) {
        Object data = getData();
        Object dest = getDestination();
        log.info( "Drop perform(): data=" + data + ", dest=" + dest );
        
        if (data.getClass().isArray()) {
            Object[] array = (Object[])data;
            for (Object object : array) {
                if (checkData( object )) {
                    perform( (IGeoResource)object, dest, monitor );
                }
            }
        }
        else {
            perform( (IGeoResource)data, dest, monitor );
        }
    }

    
    protected void perform( IGeoResource geores, Object dest, IProgressMonitor monitor ) {
        // determine destination map
        IMap map = null;
        if (dest instanceof IMap) {
            map = (IMap)dest;
        }
        else if (dest instanceof ILayer) {
            map = ((ILayer)dest).getMap();
        }
        else if (dest instanceof MapLayersView) {
            map = ((MapLayersView)dest).getRootMap();
            if (map == null) {
                return;
            }
        }
        else {
            throw new IllegalArgumentException( "Unhandled drop destination type: " + dest );
        }
        
        // execute operation
        try {
            NewLayerOperation op = ProjectRepository.instance().newOperation( NewLayerOperation.class ); 
            op.init( map, geores ); 
            // XXX JobMonitors?
            OperationSupport.instance().execute( op, false, false );
        }
        catch (ExecutionException e) {
            PolymapWorkbench.handleError( ProjectPlugin.PLUGIN_ID, this, e.getLocalizedMessage(), e );
        }
    }
    
}
