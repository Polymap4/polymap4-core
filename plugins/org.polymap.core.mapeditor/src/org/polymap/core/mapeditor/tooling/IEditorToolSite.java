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

import com.google.common.base.Predicate;

import org.eclipse.ui.IMemento;

import org.eclipse.core.runtime.IPath;

import org.polymap.core.mapeditor.tooling.ToolingEvent.EventType;
import org.polymap.core.mapeditor.workbench.MapEditor;

/**
 * The interface a {@link IEditorTool} uses to access the tooling system. 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface IEditorToolSite {
    
    public MapEditor getEditor();
    
    /**
     * The path of this tool within the hierarchy of tools.
     */
    public IPath getToolPath();
    
    public IMemento getMemento();
    
    public IToolingToolkit getToolkit();

    public boolean addListener( ToolingListener l );

    public boolean removeListener( ToolingListener l );

    public void fireEvent( IEditorTool src, EventType type, Object value );


//    /**
//     * Changes the activation state of this tool.
//     * 
//     * @param active The new activation state.
//     * @return True indicates that the state has changed actually as a result of this
//     *         call.
//     */
//    public boolean setActive( boolean active );
//    
//    public boolean setEnabled( boolean enabled );

    /**
     * Applies the given predicate to all tools and returns a filtered set of tools.
     * 
     * @see EditorTools
     * @param filters
     * @return A filtered set of tools.
     */
    public Iterable<IEditorTool> filterTools( Predicate<IEditorTool>... filters );

    /**
     * Changes the activation state of this tool.
     * 
     * @param active The new activation state.
     * @return True indicates that the state has changed actually as a result of this
     *         call.
     */
    public boolean triggerTool( IPath toolPath, boolean active );
    
}
