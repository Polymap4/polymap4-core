/* 
 * polymap.org
 * Copyright 2009-2013, Polymap GmbH. All rights reserved.
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
package org.polymap.core.project;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.rwt.SessionSingletonBase;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.ListenerList;

import org.polymap.core.project.ui.PartListenerAdapter;

/**
 * The session dependent part of the {@link MapEditorPlugin} API and
 * implementation.
 * 
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a> 
 * @since 3.0
 */
public class ProjectPluginSession
        extends SessionSingletonBase {

    private static Log log = LogFactory.getLog( ProjectPluginSession.class );

    // static factory *************************************
    
    /**
     * Gets or creates the Platform instance for the application session of the
     * current thread.
     */
    public static ProjectPluginSession instance() {
        return (ProjectPluginSession)getInstance( ProjectPluginSession.class );
    }
    
    
    // instance *******************************************
    
    /** The map that is currently selected in the UI, or null. */
    private IMap                    selectedMap;
    
    private ListenerList            mapSelectionListeners = new ListenerList( ListenerList.IDENTITY );


    protected ProjectPluginSession() {
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

        // selection listener
        page.addSelectionListener( new ISelectionListener() {
            public void selectionChanged( IWorkbenchPart part, ISelection sel ) {
                log.debug( "selection: " + sel );
                if (sel instanceof IStructuredSelection) {
                    //
                    Object elm = ((IStructuredSelection)sel).getFirstElement();
                    if (elm != null && elm instanceof IMap) {
                        selectedMap = (IMap)elm;
                    }
                    fireEvent( part );
                }
            }
        });
        
        // part listener
        page.addPartListener( new PartListenerAdapter() {
            public void partActivated( IWorkbenchPart part ) {
                if (part instanceof IEditorPart) {
                    IEditorInput input = ((IEditorPart)part).getEditorInput();
                    if (input instanceof IAdaptable) {
                        selectedMap = (IMap)((IAdaptable)input).getAdapter( IMap.class );
                        fireEvent( part );
                    }
                }
            }
            public void partOpened( IWorkbenchPart part ) {
                partActivated( part );
            }
        });
    }

    
    protected void fireEvent( IWorkbenchPart part) {
        // fire event
        PropertyChangeEvent ev = new PropertyChangeEvent( part, "selectedMap", null, selectedMap );
        for (Object listener : mapSelectionListeners.getListeners()) {
            ((PropertyChangeListener)listener).propertyChange( ev );
        }
    }
    
    
    public IMap getSelectedMap() {
        return selectedMap;
    }

    
    public void addMapSelectionListener( PropertyChangeListener listener ) {
        mapSelectionListeners.add( listener );
    }

    
    public void removeMapSelectionListener( PropertyChangeListener listener ) {
        mapSelectionListeners.remove( listener );
    }
    
}
