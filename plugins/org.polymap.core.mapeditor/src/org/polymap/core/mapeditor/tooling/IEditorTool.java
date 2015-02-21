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

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.core.runtime.IPath;

import org.polymap.core.mapeditor.workbench.MapEditor;

/**
 * An IEditorTool implementation provides the logic to edit the content of a
 * {@link MapEditor}. It has a tool area where it may display options, setting or
 * action buttons. EditorTools are organized in a tree like hierarchy. Every
 * IEditorTool has a parent and may have children.
 * <p/>
 * The {@link ToolingViewer} provides the UI to display and use the tools
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface IEditorTool {
    
    public boolean init( IEditorToolSite site );

    public boolean isActive();

    /**
     * Informs this tool that is has been activated.
     */
    public void onActivate();


    /**
     * Informs this tool that is has been deactivated.
     * <p/>
     * This method is responsible of disposing UI controls, listeners and/or any
     * other resource that might have been created by
     * {@link #createPanelControl(Composite)}.
     */
    public void onDeactivate();


    /**
     * Creates the controls of this tool. The controls should reflect the current
     * state of the tools.
     * <p/>
     * This method is called at any time (but at most once) between
     * {@link #onActivate()} and {@link #onDeactivate()}.
     * 
     * @param parent The panel Composite in which to create the controls of this
     *        tool. The Composite has a {@link FormLayout}.
     */
    public void createPanelControl( Composite parent );
    
    public void dispose();

    public String getLabel();
    
    public String getTooltip();
    
    public Image getIcon();
    
    public String getDescription();

    public IPath getToolPath();

}
