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
package org.polymap.core.mapeditor.tooling;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;

import org.polymap.core.mapeditor.workbench.MapEditor;
import org.polymap.core.runtime.Polymap;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ToolingView
        extends ViewPart
        implements IPartListener {

    private static Log log = LogFactory.getLog( ToolingView.class );
    
    private Composite                   parent;
    
    /** Maps IMap.id() into viewer. */
    private Map<String,ToolingViewer>   viewers = new HashMap();
    
    private ToolingViewer               activeViewer;
    
    private StackLayout                 layout;


    public ToolingView() {
        log.info( "TOOLINGVIEW *************************" );
    }

    
    @Override
    public void createPartControl( Composite _parent ) {
        parent = _parent;
    
        layout = new StackLayout();
        parent.setLayout( layout );

        IEditorPart editor = getSite().getPage().getActiveEditor();
        if (editor instanceof MapEditor) {
            partActivated( editor );
        }
        
        // listen to MapEditors in the Workbench
        getSite().getPage().addPartListener( this );
    }


    @Override
    public void dispose() {
        getSite().getPage().removePartListener( this );
    }


    @Override
    public void partActivated( IWorkbenchPart part ) {
        if (part instanceof MapEditor) {
            MapEditor editor = (MapEditor)part;
            
            // deactive current viewer
            if (activeViewer != null) {
                // FIXME save state
            }

            // activate/create
            activeViewer = viewers.get( editor.getMap().id() );
            if (activeViewer == null) {
                activeViewer = new ToolingViewer( editor );
                activeViewer.createControl( parent );
                viewers.put( editor.getMap().id(), activeViewer );
            }
            layout.topControl = activeViewer.getControl();

            //activeViewer.getControl().pack( true );
            parent.pack( true );
            Polymap.getSessionDisplay().asyncExec( new Runnable() {
                public void run() {
                    if (!parent.isDisposed() && !parent.getParent().isDisposed()) {
                        parent.getParent().layout( true );
                    }
                }
            });
        }    
    }


    @Override
    public void partClosed( IWorkbenchPart part ) {
        if (part instanceof MapEditor) {
            MapEditor editor = (MapEditor)part;

            ToolingViewer viewer = viewers.remove( editor.getMap().id() );
            viewer.dispose();

            if (viewer == activeViewer) {
                activeViewer = null;
            }
            
            parent.pack( true );
            Polymap.getSessionDisplay().asyncExec( new Runnable() {
                public void run() {
                    parent.getParent().layout( true );
                }
            });
        }
    }


    @Override
    public void partBroughtToTop( IWorkbenchPart part ) {
    }


    @Override
    public void partDeactivated( IWorkbenchPart part ) {
    }


    @Override
    public void partOpened( IWorkbenchPart part ) {
    }


    @Override
    public void setFocus() {
    }
    
}
