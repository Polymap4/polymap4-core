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
package org.polymap.core.mapeditor.operations;

import java.beans.PropertyChangeEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.widgets.Display;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.polymap.core.mapeditor.MapEditor;
import org.polymap.core.mapeditor.MapEditorPlugin;
import org.polymap.core.operation.OperationConcernAdapter;
import org.polymap.core.operation.OperationInfo;
import org.polymap.core.project.IMap;
import org.polymap.core.project.Visible;
import org.polymap.core.project.operations.OpenMapOperation;
import org.polymap.core.project.ui.PartListenerAdapter;
import org.polymap.core.runtime.event.EventFilter;
import org.polymap.core.runtime.event.EventHandler;
import org.polymap.core.workbench.PolymapWorkbench;

/**
 * Decorates {@link OpenMapOperation} in order to open editor for the {@link IMap}.
 * <p/>
 * Installs a {@link MapVisibilityListener} for each opened editor in order to
 * track changes of the editor state or the visibility of the {@link IMap}.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @since 3.1
 */
public class OpenMapConcern
        extends OperationConcernAdapter {
    
    private static Log log = LogFactory.getLog( OpenMapConcern.class );

    private OpenMapOperation    op;
    
    private OperationInfo       info;
    
    private MapEditor           openedEditor;


    protected OpenMapConcern( OpenMapOperation op, OperationInfo info ) {
        this.op = op;
        this.info = info;
    }


    protected OperationInfo getInfo() {
        return info;
    }


    public IStatus execute( final IProgressMonitor monitor, IAdaptable _info )
            throws ExecutionException {
        // execute next
        IStatus status = info.next().execute( monitor, _info );
        
        if (status.isOK()) {
            // open editor
            Display display = (Display)info.getAdapter( Display.class );
            display.syncExec( new Runnable() {
                public void run() {
                    try {
                        monitor.subTask( op.getLabel() );
                        MapEditor.openMap( op.getMap(), true );
                        monitor.worked( 1 );
                    }
                    catch (PartInitException e) {
                        PolymapWorkbench.handleError( MapEditorPlugin.PLUGIN_ID, OpenMapConcern.this, e.getLocalizedMessage(), e );
                    }
                }
            });
        }
        return status;
    }


    public boolean canUndo() {
        return false;
    }

    
    public IStatus undo( IProgressMonitor monitor, IAdaptable info ) {
        throw new RuntimeException( "not yet implemented." ); //$NON-NLS-1$
    }

    
    public boolean canRedo() {
        return false;
    }

    
    public IStatus redo( IProgressMonitor monitor, IAdaptable info )
            throws ExecutionException {
        throw new RuntimeException( "not yet implemented." ); //$NON-NLS-1$
    }


//    protected static void openMap( final MapEditorInput input, IWorkbenchPage page, IProgressMonitor monitor )
//            throws PartInitException {
//        log.debug( "        new editor: map= " + (input).getMap().id() ); //$NON-NLS-1$
//
//        // check current editors
//        IEditorReference[] editors = page.getEditorReferences();
//        for (IEditorReference reference : editors) {
//            IEditorInput cursor = reference.getEditorInput();
//            if (cursor instanceof MapEditorInput) {
//                log.debug( "        editor: map= " + ((MapEditorInput)cursor).getMap().id() ); //$NON-NLS-1$
//            }
//            if (cursor.equals( input )) {
//                Object previous = page.getActiveEditor();
//                page.activate( reference.getPart( true ) );
//                return;
//            }
//        }
//
//        // not found -> open new editor
//        MapEditor editor = (MapEditor)page.openEditor( input, input.getEditorId(), true, 
//                IWorkbenchPage.MATCH_NONE );
//        
//        // install listener
//        new MapVisibilityListener( page, editor, input.getMap() );
//    }
    
    
    /**
     * Tracks the state of the editor and the visible prop of the IMap
     * and keeps both in synch if one is changing.
     */
    public static class MapVisibilityListener
            extends PartListenerAdapter {
        
        private IWorkbenchPage          page;
        
        private MapEditor               editor;

        private IMap                    map;
        
        public MapVisibilityListener( IWorkbenchPage page, MapEditor editor, IMap map ) {
            this.page = page;
            this.editor = editor;
            this.map = map;
            assert map != null;
            
            page.addPartListener( this );
            
            // the partListener prevents the object from being reclaimed
            // (as the EventManager holds only weak references)
            editor.getMap().addPropertyChangeListener( this, new EventFilter<PropertyChangeEvent>() {
                public boolean apply( PropertyChangeEvent ev ) {
                    return ev.getPropertyName().equals( Visible.PROP_VISIBLE );
                }
            });
        }

        protected void dispose() {
            // don't set to null as we need variables afterwards
            if (map != null) {
                map.removePropertyChangeListener( this );
            }
            if (page != null) {
                page.removePartListener( this );
            }
        }
        
        @EventHandler(display=true)
        public void propertyChange( PropertyChangeEvent ev ) {
            if (!editor.getMap().isVisible()) {
                dispose();
                page.closeEditor( editor, false );
            }
        }

        @Override
        public void partClosed( IWorkbenchPart part ) {
            if (part.equals( editor )) {
                dispose();
                // XXX use an operation?
                map.setVisible( false );
            }
        }
        
    }

}
