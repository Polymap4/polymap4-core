/* 
 * polymap.org
 * Copyright 2013, Polymap GmbH. All rights reserved.
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
package org.polymap.core.project.ui.project;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;

import org.eclipse.ui.part.ViewPart;

import org.eclipse.core.runtime.Status;

import org.polymap.core.project.ILayer;
import org.polymap.core.project.IMap;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.ui.SelectionAdapter;

/**
 * This selection change listener updates the views status line according to the
 * status of the {@link ILayer} selected.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @since 3.0
 */
public class ProjectStatusLineAdapter
        implements ISelectionChangedListener {
    
    private static Log log = LogFactory.getLog( ProjectStatusLineAdapter.class );

    private ViewPart            part;

    private IStatusLineManager  statusLine;
    
    
    public ProjectStatusLineAdapter( ViewPart part ) {
        this.part = part;
        this.statusLine = part.getViewSite().getActionBars().getStatusLineManager();
    }

    
    /**
     * {@link IMap} selected -> update status line for this view.
     */
    public void selectionChanged( final SelectionChangedEvent ev ) {
        // do it after other listeners have produced their errors
        Polymap.getSessionDisplay().asyncExec( new Runnable() {
            public void run() {
                statusLine.setErrorMessage( null );
                statusLine.setMessage( null );

                SelectionAdapter sel = new SelectionAdapter( ev.getSelection() );
                // display nothing for multiple selections
                if (sel.size( IMap.class ) == 1) {
                    IMap map  = sel.first( IMap.class );
                    displayStatus( map.getMapStatus() );
                }
            }
        });
    }
    
    
    private void displayStatus( Status status ) {
        if (status.matches( Status.ERROR | Status.WARNING )) {
            statusLine.setErrorMessage( status.getMessage() );
        }
        else if (status.matches( Status.CANCEL | Status.INFO )) {
            statusLine.setMessage( status.getMessage() );
        }
        else if (status.isOK()) {
            // do nothing
        }
        else {
            assert false : "No such severity: " + status.getSeverity();
        }        
    }
    
}
