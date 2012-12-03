/* 
 * polymap.org
 * Copyright 2012, Falko Bräutigam. All rights reserved.
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
package org.polymap.core.mapeditor;

import java.beans.PropertyChangeEvent;
import org.geotools.geometry.jts.ReferencedEnvelope;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.commands.operations.DefaultOperationHistory;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.commands.operations.ObjectUndoContext;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;

import org.polymap.core.mapeditor.MapEditorPlugin;
import org.polymap.core.project.IMap;
import org.polymap.core.runtime.event.EventFilter;
import org.polymap.core.runtime.event.EventHandler;
import org.polymap.core.workbench.PolymapWorkbench;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class NavigationHistory {

    private static Log log = LogFactory.getLog( NavigationHistory.class );

    private MapEditor                   mapEditor;

    private ObjectUndoContext           context;

    private DefaultOperationHistory     history;

    private ReferencedEnvelope          lastMapExtent;

    private boolean                     ignoreEvents = false;

    
    public NavigationHistory( MapEditor mapEditor ) {
        this.mapEditor = mapEditor;
        this.context = new ObjectUndoContext( this, "Navigation Context" );
        this.history = new DefaultOperationHistory();
//        approver = new AdvancedValidationUserApprover( context );
//        history.addOperationApprover( approver );
        this.history.setLimit( context, 10 );
        
        mapEditor.getMap().addPropertyChangeListener( this, new EventFilter<PropertyChangeEvent>() {
            public boolean apply( PropertyChangeEvent ev ) {
                String name = ev.getPropertyName();
                return !ignoreEvents
                        && NavigationHistory.this.mapEditor.getMap().equals( ev.getSource() )
                        
                        // XXX the extent events are often followed by an update
                        // event with different extent (diskrete scales); processing just
                        // the update event solves this but we are potentially loosing
                        // map extent chnages
                        && (IMap.PROP_EXTENT.equals( name ) || IMap.PROP_EXTENT_UPDATE.equals( name ));
            }
        });
    }
    
    
    public void dispose() {
        mapEditor.getMap().removePropertyChangeListener( this );
        mapEditor = null;
        
        history.dispose( context, true, true, true );
        history = null;
    }
    
    
    @EventHandler
    public void propertyChange( PropertyChangeEvent ev ) {
        try {
            if (ev.getNewValue() instanceof ReferencedEnvelope) {
                ReferencedEnvelope newExtent = (ReferencedEnvelope)ev.getNewValue();
                if (lastMapExtent != null && !newExtent.equals( lastMapExtent )) {
                    NavigationOperation op = new NavigationOperation( lastMapExtent, newExtent );
                    op.addContext( context );
                    history.execute( op, new NullProgressMonitor(), null );
                    log.debug( "propertyChange(): " + newExtent + ", history= " + history.getUndoHistory( context ).length );
                }
                lastMapExtent = newExtent;
            }
        }
        catch (ExecutionException e) {
            PolymapWorkbench.handleError( MapEditorPlugin.PLUGIN_ID, this, e.getLocalizedMessage(), e );
        }
    }


    public boolean canUndo() {
        return history.canUndo( context );
    }

    
    public IStatus undo() {
        try {
            ignoreEvents = true;
            return history.undo( context, new NullProgressMonitor(), null );
        }
        catch (ExecutionException e) {
            return new Status( Status.ERROR, MapEditorPlugin.PLUGIN_ID, e.getMessage() );
        }
        finally {
            ignoreEvents = false;
        }
    }

    
    public boolean canRedo() {
        return history.canRedo( context );
    }

    
    public IStatus redo() {
        try {
            ignoreEvents = true;
            return history.redo( context, new NullProgressMonitor(), null );
        }
        catch (ExecutionException e) {
            return new Status( Status.ERROR, MapEditorPlugin.PLUGIN_ID, e.getMessage() );
        }
        finally {
            ignoreEvents = false;
        }
    }

    
    /**
     * 
     */
    class NavigationOperation
            extends AbstractOperation
            implements IUndoableOperation {

        private ReferencedEnvelope      undoMapExtent, redoMapExtent;
        
        
        protected NavigationOperation( ReferencedEnvelope undoMapExtent, ReferencedEnvelope redoMapExtent ) {
            super( "Navigation" );
            this.undoMapExtent = undoMapExtent;
            this.redoMapExtent = redoMapExtent;
        }

        public void dispose() {
            log.debug( "dispose(): " + undoMapExtent );
        }

        public IStatus execute( IProgressMonitor monitor, IAdaptable info )
        throws ExecutionException {
            // do nothing, the map has already changed it extent/zoom
            return Status.OK_STATUS;
        }

        public IStatus undo( IProgressMonitor monitor, IAdaptable info )
        throws ExecutionException {
            log.debug( "undo(): " + undoMapExtent );
            mapEditor.getMap().setExtent( undoMapExtent );
            return Status.OK_STATUS;
        }

        public IStatus redo( IProgressMonitor monitor, IAdaptable info )
        throws ExecutionException {
            mapEditor.getMap().setExtent( redoMapExtent );
            return Status.OK_STATUS;
        }

    }

}
