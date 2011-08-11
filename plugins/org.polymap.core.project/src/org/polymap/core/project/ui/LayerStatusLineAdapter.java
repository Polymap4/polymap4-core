/* 
 * polymap.org
 * Copyright 2009, 2011 Polymap GmbH. All rights reserved.
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
package org.polymap.core.project.ui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;

import org.eclipse.ui.part.ViewPart;

import org.eclipse.core.runtime.Status;

import org.polymap.core.project.ILayer;

/**
 * This selction change listener updates the views status line according to the
 * status of the {@link ILayer} selected.
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @since 3.0
 */
public class LayerStatusLineAdapter
        implements ISelectionChangedListener {
    
    private static Log log = LogFactory.getLog( LayerStatusLineAdapter.class );

    private ViewPart            part;

    private IStatusLineManager  statusLine;
    
    
    public LayerStatusLineAdapter( ViewPart part ) {
        super();
        this.part = part;
    }

    
    /**
     * New ILayer was selected -> update status line for this view.
     */
    public void selectionChanged( final SelectionChangedEvent ev ) {
        if (ev.getSelection().isEmpty()
                || !(ev.getSelection() instanceof IStructuredSelection)) {
            return;
        }
        
        if (statusLine == null) {
            statusLine = part.getViewSite().getActionBars().getStatusLineManager();
        }
        
        // do it after other listeners have produced their errors
        part.getViewSite().getShell().getDisplay().asyncExec( new Runnable() {
            public void run() {
                // clear status line
                statusLine.setErrorMessage( null );
                statusLine.setMessage( null );

                IStructuredSelection sel = (IStructuredSelection) ev.getSelection();
                Object firstElement = sel.getFirstElement();
                log.debug( "selection: " + firstElement );
                if (firstElement instanceof ILayer) {
                    ILayer layer = (ILayer)firstElement;

                    displayStatus( layer.getRenderStatus() );
                    displayStatus( layer.getLayerStatus() );            
                }
            }
        });
    }
    
    
    private void displayStatus( Status status ) {
        log.debug( "    severity: " + status.getSeverity() );
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
