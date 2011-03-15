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

package org.polymap.core.mapeditor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.rwt.SessionSingletonBase;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

import org.eclipse.core.runtime.ListenerList;

import org.polymap.core.project.IMap;

/**
 * The session dependent part of the {@link MapEditorPlugin} API and
 * implementation.
 * 
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a> 
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class MapEditorPluginSession
        extends SessionSingletonBase {

    private static Log log = LogFactory.getLog( MapEditorPluginSession.class );

    // static factory *************************************
    
    /**
     * Gets or creates the Platform instance for the application session of the
     * current thread.
     */
    public static MapEditorPluginSession instance() {
        return (MapEditorPluginSession)getInstance( MapEditorPluginSession.class );
    }
    
    
    // instance *******************************************
    
    /** The map that is currently selected in the UI, or null. */
    private IMap                    selectedMap;
    
    private ListenerList            mapSelectionListeners = new ListenerList( ListenerList.IDENTITY );


    protected MapEditorPluginSession() {
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        page.addSelectionListener( new ISelectionListener() {
            public void selectionChanged( IWorkbenchPart part, ISelection sel ) {
                log.info( "selection: " + sel );
                if (sel instanceof IStructuredSelection) {
                    //
                    Object elm = ((IStructuredSelection)sel).getFirstElement();
                    if (elm != null && elm instanceof IMap) {
                        selectedMap = (IMap)elm;
                    } else {
                        selectedMap = null;
                    }
                    // fire event
                    PropertyChangeEvent ev = new PropertyChangeEvent( part, "selectedMap", null, selectedMap );
                    for (Object listener : mapSelectionListeners.getListeners()) {
                        ((PropertyChangeListener)listener).propertyChange( ev );
                    }
                }
            }
        });
    }

    
//    public IMap getSelectedMap() {
//        return selectedMap;
//    }
//
//    
//    public void addMapSelectionListener( PropertyChangeListener listener ) {
//        mapSelectionListeners.add( listener );
//    }
//
//    
//    public void removeMapSelectionListener( PropertyChangeListener listener ) {
//        mapSelectionListeners.remove( listener );
//    }
    
}
