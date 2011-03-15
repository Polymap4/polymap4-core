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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.HandlerEvent;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandler2;

import org.polymap.core.mapeditor.operations.OpenMapOperation;
import org.polymap.core.operation.OperationSupport;
import org.polymap.core.project.ProjectPlugin;
import org.polymap.core.project.ProjectPluginSession;

/**
 * This is handler for the <em>org.polymap.core.project.command.openMap</em> command.
 * This handler ...
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class OpenMapHandler
        extends AbstractHandler
        implements IHandler, IHandler2 {

    private static Log log = LogFactory.getLog( OpenMapHandler.class );

    private PropertyChangeListener      mapSelectionListener;
    
    
    public OpenMapHandler() {
        super();
        
    }

    
    public void dispose() {
        super.dispose();
        if (mapSelectionListener != null) {
            ProjectPluginSession.instance().removeMapSelectionListener( mapSelectionListener );
            mapSelectionListener = null;
        }
    }


    public boolean isEnabled() {
        // listen to changes
        if (mapSelectionListener != null) {
            mapSelectionListener = new PropertyChangeListener() {            
                public void propertyChange( PropertyChangeEvent ev ) {
                    //log.info( "currentMap= " + ProjectPlugin.getSelectedMap() );
                    fireHandlerChanged( new HandlerEvent( OpenMapHandler.this, true, true) );
                }
            };
            ProjectPluginSession.instance().addMapSelectionListener( mapSelectionListener );
        }
        return ProjectPlugin.getSelectedMap() != null
                && !ProjectPlugin.getSelectedMap().getLayers().isEmpty();
    }


    public boolean isHandled() {
        log.info( "currentMap= " + ProjectPlugin.getSelectedMap() );
        return true;  //currentMap != null;
    }


    public Object execute( ExecutionEvent ev )
            throws ExecutionException {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();

        OpenMapOperation op = new OpenMapOperation( ProjectPlugin.getSelectedMap(), page );
        OperationSupport.instance().execute( op, true, false );
        return null;
    }
    
}
