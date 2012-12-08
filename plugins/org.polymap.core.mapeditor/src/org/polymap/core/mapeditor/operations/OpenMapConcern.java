/* 
 * polymap.org
 * Copyright 2009-2012, Polymap GmbH. All rights reserved.
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.widgets.Display;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.polymap.core.mapeditor.MapEditor;
import org.polymap.core.mapeditor.MapEditorInput;
import org.polymap.core.mapeditor.MapEditorPlugin;
import org.polymap.core.operation.OperationConcernAdapter;
import org.polymap.core.operation.OperationInfo;
import org.polymap.core.project.operations.OpenMapOperation;
import org.polymap.core.workbench.PolymapWorkbench;

/**
 * Put the given layer in edit mode.
 * 
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @since 3.0
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
                        monitor.subTask( getLabel() );
                        MapEditorInput input = new MapEditorInput( op.getMap() );
                        openMap( input, op.getPage(), monitor );
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


    private static void openMap( final MapEditorInput input, IWorkbenchPage page, IProgressMonitor monitor )
            throws PartInitException {
        log.debug( "        new editor: map= " + (input).getMap().id() ); //$NON-NLS-1$

        // check current editors
        IEditorReference[] editors = page.getEditorReferences();
        for (IEditorReference reference : editors) {
            IEditorInput cursor = reference.getEditorInput();
            if (cursor instanceof MapEditorInput) {
                log.debug( "        editor: map= " + ((MapEditorInput)cursor).getMap().id() ); //$NON-NLS-1$
            }
            if (cursor.equals( input )) {
                Object previous = page.getActiveEditor();
                page.activate( reference.getPart( true ) );
                return;
            }
        }

        // not found -> open new editor
        IEditorPart part = page.openEditor( input, input.getEditorId(), true,
                IWorkbenchPage.MATCH_NONE );
        log.debug( "editor= " + part ); //$NON-NLS-1$
    }

}
