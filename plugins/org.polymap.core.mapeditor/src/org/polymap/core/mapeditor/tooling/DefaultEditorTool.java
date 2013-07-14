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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import org.eclipse.core.runtime.IPath;

import org.polymap.core.mapeditor.tooling.ToolingEvent.EventType;

/**
 * Default implementation of the {@link IEditorTool} to be used for extensions of
 * extension point {@link EditorToolExtension#POINT_ID}. Extension property members
 * are directly set by {@link EditorToolExtension}. Provides handling of the
 * {@link IEditorToolSite} and tools to layout the panel control.
 * <p/>
 * Maintains its {@link #isActive()} state. This default implementation disables
 * if any other tool becomes active.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class DefaultEditorTool
        implements IEditorTool, ToolingListener {

    private static Log log = LogFactory.getLog( DefaultEditorTool.class );
    
    public static final String  CONTROL_INTENT_DATA = "toolingControlIntent";

    /** Initialized by {@link EditorToolExtension}. */
    Image                       icon;
    String                      label;
    String                      tooltip;
    String                      description;
    IPath                       toolPath;
    
    private IEditorToolSite     site;
    
    private Composite           parent;
   
    protected Control           lastControl;

    private Label               lastLabel;
    
    private boolean             active;
    
    @Override
    public boolean init( @SuppressWarnings("hiding") IEditorToolSite site ) {
        this.site = site;
        
        // listen to other tools
        getSite().addListener( this );
        
        return true;
    }

    protected IEditorToolSite getSite() {
        return site;
    }
    
    protected Composite getParent() {
        return parent;
    }

    @Override
    public boolean isActive() {
        return active;
    }
    
    /**
     * Sub-classes should call this method if overriden.
     */
    @Override
    public void onActivate() {
        log.trace( getClass().getSimpleName() + ".onActivate()" );
        assert !active;
        active = true;
    }

    /**
     * Sub-classes should call this method if overriden.
     */
    @Override
    public void onDeactivate() {
        log.trace( getClass().getSimpleName() + ".onDeactivate()" );
        assert active;
        active = false;
    }

    /**
     * Sub-classes should call this method if overriden.
     */
    @Override
    public void toolingChanged( ToolingEvent ev ) {
        // deactivate if any other on same level becomes active
        if (ev.getType() == EventType.TOOL_ACTIVATING
                && isActive()
                && !this.equals( ev.getSource() )
                && toolPath.removeLastSegments( 1 ).equals( ev.getSource().getToolPath().removeLastSegments( 1 ) )) {
            getSite().triggerTool( getSite().getToolPath(), false );
        }
    }

    /**
     * 
     */
    @Override
    public void createPanelControl( Composite _parent ) {
        this.parent = _parent;
        FormLayout layout = new FormLayout();
        layout.spacing = 3;
        parent.setLayout( layout );
    }

    protected void layoutControl( @SuppressWarnings("hiding") String label, Control control ) {
        Label l = new Label( control.getParent(), SWT.NONE );
        l.setText( label );
        
        l.setData( CONTROL_INTENT_DATA, control.getData( CONTROL_INTENT_DATA ) );
        
        FormData labelData = new FormData( 80, SWT.DEFAULT );
        labelData.left = new FormAttachment( 0 );
        labelData.top = lastLabel != null
                ? new FormAttachment( lastControl, 3 )
                : new FormAttachment( 0 );
        l.setLayoutData( labelData );
        
        FormData controlData = new FormData();
        controlData.top = lastControl != null
                ? new FormAttachment( lastControl )
                : new FormAttachment( 0 );
        controlData.right = new FormAttachment( 100 );
        controlData.left = new FormAttachment( l );
        control.setLayoutData( controlData );
    
        lastLabel = l;
        lastControl = control;
    }
    
    @Override
    public void dispose() {
    }

    @Override
    public Image getIcon() {
        return icon;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public String getTooltip() {
        return tooltip;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public IPath getToolPath() {
        return toolPath;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [toolPath=" + getToolPath() + "]";
    }
    
}
