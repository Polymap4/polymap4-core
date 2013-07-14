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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import org.eclipse.rwt.graphics.Graphics;
import org.eclipse.rwt.lifecycle.WidgetUtil;

import org.eclipse.jface.resource.JFaceResources;

import org.eclipse.ui.forms.widgets.ColumnLayoutData;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import org.polymap.core.mapeditor.MapEditor;
import org.polymap.core.mapeditor.tooling.ToolingEvent.EventType;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.ui.ColumnLayoutFactory;

/**
 * The default viewer for the editor tools and panels based on a {@link ToolingModel}.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ToolingViewer {

    private static Log log = LogFactory.getLog( ToolingViewer.class );

    private ToolingModel        model;

    private Composite           content;
    
    private ToolingToolkit      toolkit;
    
    /** Maintained by {@link ToolsPanel} itself. */
    private Set<ToolsPanel>     panels = new HashSet();
    
    
    public ToolingViewer( MapEditor editor ) {
        model = ToolingModel.instance( editor );

        toolkit = new ToolingToolkit();
        model.setToolkit( toolkit );
    }


    public void dispose() {
        for (ToolsPanel panel : new ArrayList<ToolsPanel>( panels )) {
            // removing itself from panels
            panel.dispose();
        }
        panels.clear();
        if (content != null) {
            content.dispose();
            content = null;
        }
        if (toolkit != null) {
            toolkit.dispose();
            toolkit = null;
        }
        // dispose is done by model itself when part is closed
        model = null;
    }


    public Composite createControl( Composite parent ) {
        content = new Composite( parent, SWT.NONE );
        content.setLayout( ColumnLayoutFactory.defaults().create() );
        
        List<IEditorTool> tools = model.findTools( Path.EMPTY );
        ToolsPanel rootPanel = new ToolsPanel( Path.EMPTY, tools );
        Composite control = rootPanel.createControl( content );
//        control.setLayoutData( new RowData( parent.getSize().x, SWT.DEFAULT ) );
//        control.setLayoutData( SimpleFormData.filled().bottom( -1 ).create() );
        
        // reflect model state (XXX first hierarchy level only)
        for (IEditorTool tool : model.findTools( Path.EMPTY )) {
            if (model.isActive( tool.getToolPath() )) {
                rootPanel.onToolActivated( tool );
            }
        }
        
        return content;
    }


    public Composite getControl() {
        assert content != null;
        return content;
    }


    /**
     * Provides the UI of one hierarchy level of tools.
     * <p/>
     * Once created the ToolsPanel listens to {@link ToolingEvent}s and reflects
     * de/activation of child tools in the UI. It also disposes itself when its {@link #toolPath}
     */
    public class ToolsPanel
            implements ToolingListener {

        private IPath                   toolPath;
        
        @SuppressWarnings("hiding")
        private Composite               content;
        
        private List<IEditorTool>       tools;

        private Map<IEditorTool,Composite> toolAreas = new HashMap();

        private ToolBar                 tb;
        
        
        public ToolsPanel( IPath toolPath, List<IEditorTool> tools ) {
            this.toolPath = toolPath;
            this.tools = tools;
            panels.add( this );
        }
        
        
        public void dispose() {
            if (content != null) {
                content.dispose();
                content = null;
            }
            model.removeListener( this );
            tools = null;
            toolAreas = null;
            tb = null;
            panels.remove( this );
        }


        public void toolingChanged( ToolingEvent ev ) {
            if (toolPath.equals( ev.getSource().getToolPath() )
                    && ev.getType() == EventType.TOOL_DEACTIVATED) {
                dispose();
            }
            else if (tools != null && tools.contains( ev.getSource() )) {
                if (ev.getType() == EventType.TOOL_ACTIVATED) {
                    onToolActivated( ev.getSource() );
                }
                else if (ev.getType() == EventType.TOOL_DEACTIVATED) {
                    onToolDeactivated( ev.getSource() );
                }
            }
        }


        public Composite createControl( Composite parent ) {
            // panel
            content = new Composite( parent, SWT.NONE );
            content.setLayout( ColumnLayoutFactory.defaults().create() );
            
            // toolbar
            tb = new ToolBar( content, SWT.BORDER | SWT.FLAT );
            toolkit.adapt( tb );
            
            for (final IEditorTool tool : tools) {
                final ToolItem item = new ToolItem( tb, SWT.CHECK );
                item.setData( "editorTool", tool );
                item.setData( WidgetUtil.CUSTOM_VARIANT, ToolingToolkit.CUSTOM_VARIANT_VALUE );

                //item.setText( tool.getLabel() );
                item.setToolTipText( tool.getTooltip() );
                item.setImage( tool.getIcon() );
                item.addSelectionListener( new SelectionAdapter() {
                    public void widgetSelected( SelectionEvent ev ) {
                        model.triggerTool( tool.getToolPath(), item.getSelection() );
                    }
                });
            }
            
            // add lister (after toolbar is initialized)
            model.addListener( this );
            
            return content;
        }

        
        protected void onToolActivated( IEditorTool tool ) {
            assert !toolAreas.containsKey( tool ) : "toolAreay has entry for tool: " + tool;
            
            // toolbar icon (in case triggered via API)
            for (ToolItem item : tb.getItems()) {
                if (item.getData( "editorTool" ) == tool) {
                    item.setSelection( true );
                }
            }
            
            // tool area
            final Composite toolArea = new Composite( content, SWT.NONE );
            toolAreas.put( tool, toolArea );
            //toolArea.setBackground( Graphics.getColor( 0xff, 0xff, 0xff ) );
            toolArea.setLayout( ColumnLayoutFactory.defaults().margins( 3, 3 ).spacing( 3 ).create() );

            // title
            CLabel title = new CLabel( toolArea, SWT.NONE );
            title.setFont( JFaceResources.getFontRegistry().getBold( JFaceResources.DEFAULT_FONT ) );
            title.setMargins( 0, 0, 0, 0 );
            title.setImage( tool.getIcon() );
            title.setText( tool.getLabel() );
            
            // description
            Label description = new Label( toolArea, SWT.WRAP );
            // this width defines the minimum width of the view
            description.setLayoutData( new ColumnLayoutData( 230, SWT.DEFAULT ) );
            description.setForeground( Graphics.getColor( 0x90, 0x90, 0x90 ) );
            if (tool.getDescription() != null) {
                description.setText( tool.getDescription() );
            } 
            else if (tool.getTooltip() != null) {
                description.setText( tool.getTooltip() );            
            }
            else if (tool.getLabel() != null) {
                description.setText( tool.getLabel() );            
            }

            // tool panel
            Composite panelArea = new Composite( toolArea, SWT.NONE );
            panelArea.setLayout( ColumnLayoutFactory.defaults().spacing( 3 ).create() );
            tool.createPanelControl( panelArea );
            panelArea.pack( true );

            // children
            List<IEditorTool> children = model.findTools( tool.getToolPath() );
            if (!children.isEmpty()) {
                ToolsPanel childPanel = new ToolsPanel( tool.getToolPath(), children );
                Composite control = childPanel.createControl( toolArea );
            }

            new Label( toolArea, SWT.SEPARATOR | SWT.HORIZONTAL );
            
            content.pack();
            ToolingViewer.this.content.layout( true );

            Polymap.getSessionDisplay().asyncExec( new Runnable() {
                public void run() {
                    content.getParent().layout( true );
                }
            });
        }
        
        
        protected void onToolDeactivated( IEditorTool tool ) {
            // toolbar icon (in case triggered via API)
            if (!tb.isDisposed()) {
                for (ToolItem item : tb.getItems()) {
                    if (item.getData( "editorTool" ) == tool) {
                        item.setSelection( false );
                    }
                }
            }
            else {
                log.warn( "onToolDeactivated(): " + tool.getToolPath() );
            }

            Composite toolArea = toolAreas.remove( tool );
            assert toolArea != null;
            toolArea.dispose();

            content.pack();
            ToolingViewer.this.content.layout( true );

            Polymap.getSessionDisplay().asyncExec( new Runnable() {
                public void run() {
                    if (content != null) {
                        content.getParent().layout( true );
                    }
                }
            });
        }
        
    }

}
