/* 
 * polymap.org
 * Copyright 2012, Falko Br�utigam. All rights reserved.
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

import static com.google.common.collect.Iterables.filter;
import static org.polymap.core.mapeditor.tooling.EditorTools.hasStrictPrefix;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Lists;

import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.XMLMemento;

import org.eclipse.core.runtime.IPath;
import org.polymap.core.mapeditor.MapEditor;
import org.polymap.core.mapeditor.MapEditorPlugin;
import org.polymap.core.mapeditor.tooling.ToolingEvent.EventType;
import org.polymap.core.runtime.ListenerList;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.runtime.SessionSingleton;

/**
 * Provides the tree store, event handling and most of the other basic logic of
 * the looling engine. Viewers should be able to provide different UIs on top of that.
 * 
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
public class ToolingModel {

    private static Log log = LogFactory.getLog( ToolingModel.class );

    /**
     * All {@link ToolingModel}s of the session - associated to their {@link MapEditor}.
     */
    protected static class ToolingModels
            extends SessionSingleton
            implements IPartListener {
        
        protected Map<MapEditor,ToolingModel> map = new HashMap();
        
        /** Pages we have a {@link IPartListener} registered for. */
        protected Set<IWorkbenchPage>         pages = new HashSet();
        
        public ToolingModels() {
        }
        
        public void partClosed( IWorkbenchPart part ) {
            if (part instanceof MapEditor) {
                MapEditor mapEditor = (MapEditor)part;
                ToolingModel model = map.remove( mapEditor );
                model.dispose();
            }
        }
        public void partBroughtToTop( IWorkbenchPart part ) {
        }
        public void partDeactivated( IWorkbenchPart part ) {
        }
        public void partActivated( IWorkbenchPart part ) {
        }
        public void partOpened( IWorkbenchPart part ) {
        }
    }
    
    /**
     * Get ot create the {@link ToolingModel} for the given {@link MapEditor}.
     * <p/>
     * If the model is to be used to create UI elements then 
     */
    public static ToolingModel instance( MapEditor mapEditor ) {
        ToolingModels models = ToolingModels.instance( ToolingModels.class );
        ToolingModel model = models.map.get( mapEditor );
        if (model == null) {
            model = new ToolingModel( mapEditor );
            models.map.put( mapEditor, model );
            
            IWorkbenchPage page = mapEditor.getSite().getPage();
            if (! models.pages.contains( page )) {
                page.addPartListener( models );
                models.pages.add( page );
            }
        }
        return model;
    }
            
    
    // instance *******************************************

    private Map<IPath,IEditorTool>          tools = new HashMap();
    
    private Set<IPath>                      activeTools = new HashSet();
    
    private ListenerList<ToolingListener>   listeners = new ListenerList();

    private IMemento                        memento;

    /** The file backend of the {@link #memento}. */
    private File                            stateFile;
    
    private MapEditor                       editor;

    private IToolingToolkit                 toolkit;
    

    /**
     * 
     * 
     * @param editor This editor we are working with.
     */
    protected ToolingModel( MapEditor editor ) {
        this.editor = editor;

        // create then init tools
        for (EditorToolExtension ext : EditorToolExtension.all()) {
            IEditorTool tool = ext.createEditorTool();
            tools.put( tool.getToolPath(), tool );
        }
        for (IEditorTool tool : tools.values()) {
            if (!tool.init( new EditorToolSiteImpl( tool.getToolPath() ) )) {
                tools.remove( tool.getToolPath() );
            }
        }

        // init memento
        try {
           // ISettingStore settingStore = RWT.getSettingStore();

            String mapId = editor.getMap().id();
            IPath path = MapEditorPlugin.getDefault().getStateLocation();
            stateFile = new File( path.toFile(), 
                    "tooling_" + Polymap.instance().getUser().getName() + "_" + mapId + ".xml" );
            log.info( "State file: " +  stateFile.getAbsolutePath() );

            if (stateFile.exists()) {
                FileInputStream in = new FileInputStream( stateFile );
                memento = XMLMemento.createReadRoot( new InputStreamReader( in, "utf-8" ) );
            }
            else {
                memento = XMLMemento.createWriteRoot( "toolingModel" );
            }
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }
    }

    
    public void dispose() {
        tools = null;
        activeTools = null;
        listeners = null;
    }


    /**
     * Sets the toolkit for this model. Call this method before creating UI elements
     * of the tools.
     */
    public void setToolkit( IToolingToolkit toolkit ) {
        this.toolkit = toolkit;
    }


    public boolean triggerTool( IPath toolPath, boolean active ) {        
        // find node
        IEditorTool tool = tools.get( toolPath );
        assert tool != null : "No such tool: " + toolPath;
        boolean toolActive = activeTools.contains( toolPath );
        log.debug( "TRIGGER: " + toolPath + " -> " + (active?"ON":"OFF") + " (" + (toolActive?"ON":"OFF") + ")" );

        if (toolActive == active) {
            return false;
        }
        else {
            // trigger node state
            if (toolActive) {
                activeTools.remove( toolPath );
            } else {
                activeTools.add( toolPath );
            }

            // inform tools
            if (active) {
                tool.onActivate();
                fireEvent( tool, EventType.TOOL_ACTIVATED, null );
            } 
            else {
                tool.onDeactivate();
                // deactivate all children
                for (IEditorTool child : filter( tools.values(), hasStrictPrefix( toolPath ) )) {
                    triggerTool( child.getToolPath(), false );
                }
                fireEvent( tool, EventType.TOOL_DEACTIVATED, null );
            }
            return true;
        }
    }


    /**
     * Gets the tools for the given toolPath. Returns already initialized tool
     * instances or creates new tools if the given parentPath was not yet accessed in
     * the tool tree.
     * 
     * @param parentPath The tool path the search at in the tool tree.
     * @return List of tool instances, or an empty List if there are no children.
     */
    public List<IEditorTool> findTools( final IPath parentPath ) {
        Iterable<IEditorTool> result = filter( tools.values(), hasStrictPrefix( parentPath ) );
        // skip this copy?
        return Lists.newArrayList( result );
    }
    
    
    public boolean isActive( IPath toolPath ) {
        return activeTools.contains( toolPath );
    }
    
    
    public boolean addListener( ToolingListener l ) {
        return listeners.add( l );
    }

    public boolean removeListener( ToolingListener l ) {
        // ToolingModel and ToolsPanel both listen to part changes; so model might
        // have been disposed when ToolsPanel removes listener from its dispose()
        return listeners != null ? listeners.remove( l ) : false;        
    }
    
    public void fireEvent( IEditorTool src, EventType type, Object value ) {
        final ToolingEvent ev = new ToolingEvent( src, type, value );
        for (ToolingListener l : listeners) {
            l.toolingChanged( ev );
        }
        
//        listeners.forEach( new Callable<ToolingListener>() {
//            public void call( ToolingListener listener ) {
//                listener.toolingChanged( ev );
//            }
//        });
    }
    
    
    /**
     * 
     */
    protected class EditorToolSiteImpl
            implements IEditorToolSite {

        private IPath                   toolPath;
        
        private List<ToolingListener>   localListeners = new ArrayList();
        
        
        public EditorToolSiteImpl( IPath toolPath ) {
            this.toolPath = toolPath;
        }

        public void dispose() {
            for (ToolingListener l : localListeners) {
                ToolingModel.this.removeListener( l );
            }
        }
        
        @Override
        public boolean addListener( ToolingListener l ) {
            if (ToolingModel.this.addListener( l ) ) {
                localListeners.add( l );
                return true;
            }
            return false;
        }

        @Override
        public boolean removeListener( ToolingListener l ) {
            if (ToolingModel.this.removeListener( l ) ) {
                localListeners.remove( l );
                return true;
            }
            return false;
        }

        @Override
        public void fireEvent( IEditorTool src, EventType type, Object value ) {
            ToolingModel.this.fireEvent( src, type, value );
        }

        @Override
        public IToolingToolkit getToolkit() {
            assert toolkit != null : "Toolkit is not yet set for this model!";
            return toolkit;
        }

        @Override
        public IMemento getMemento() {
            // find existing
            for (IMemento m : memento.getChildren( "editorTool" )) {
                if (m.getID().equals( getToolPath().toString() )) {
                    return m;
                }
            }
            // create new
            return memento.createChild( "editorTool", getToolPath().toString() );
        }

        @Override
        public IPath getToolPath() {
            return toolPath;
        }

        @Override
        public MapEditor getEditor() {
            return editor;
        }

        @Override
        public Iterable<IEditorTool> filterTools( final Predicate<IEditorTool>... filters ) {
            return filter( tools.values(), Predicates.and( filters ) ); 
        }
        
        @Override
        public boolean triggerTool( IPath toTrigger, boolean active ) {
            return ToolingModel.this.triggerTool( toTrigger, active );
        }
    }
    
}
