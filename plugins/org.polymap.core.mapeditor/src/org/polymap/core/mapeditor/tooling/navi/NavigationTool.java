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
package org.polymap.core.mapeditor.tooling.navi;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import org.polymap.core.mapeditor.Messages;
import org.polymap.core.mapeditor.tooling.DefaultEditorTool;
import org.polymap.core.mapeditor.tooling.IEditorTool;
import org.polymap.core.mapeditor.tooling.IEditorToolSite;
import org.polymap.core.mapeditor.tooling.ToolingEvent;
import org.polymap.core.mapeditor.tooling.ToolingListener;
import org.polymap.core.mapeditor.tooling.ToolingEvent.EventType;
import org.polymap.core.mapeditor.tooling.edit.DigitizeTool;
import org.polymap.core.mapeditor.tooling.select.SelectionTool;
import org.polymap.core.runtime.Polymap;
import org.polymap.openlayers.rap.widget.controls.NavigationControl;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class NavigationTool
        extends DefaultEditorTool
        implements IEditorTool, ToolingListener {

    private static Log log = LogFactory.getLog( NavigationTool.class );

    private NavigationControl           control;
    
    private boolean                     active;

    
    @Override
    public boolean init( IEditorToolSite site ) {
        boolean result = super.init( site );
        
        // listen to other tools
        getSite().addListener( this );
        
        // deferred activation
        Polymap.getSessionDisplay().asyncExec( new Runnable() {
            public void run() {
                getSite().triggerTool( getSite().getToolPath(), true );
            }
        });
        return result;
    }


    @Override
    public void dispose() {
        log.debug( "dispose(): ..." );
        getSite().removeListener( this );
        onDeactivate();
        super.dispose();
    }


    @Override
    public void onActivate() {
        log.debug( "onActivate(): ..." );
        assert !active;
        active = true;

        control = new NavigationControl();
        getSite().getEditor().addControl( control );
        control.activate();
    }


    @Override
    public void createPanelControl( Composite parent ) {
        super.createPanelControl( parent );
        
        Button cb = getSite().getToolkit().createButton( parent, null, SWT.CHECK );
        cb.setSelection( true );
        cb.setEnabled( false );
        layoutControl( i18n( "keyboardLabel" ), cb );
    }


    @Override
    public void onDeactivate() {
        log.debug( "onDeactivate(): ..." );
        assert active;
        active = false;

        if (control != null) {
            control.deactivate();
            getSite().getEditor().removeControl( control );
            control.destroy();
            control.dispose();
            control = null;
        }
    }


    public boolean isActive() {
        return active;
    }


    @Override
    public void toolingChanged( ToolingEvent ev ) {
        // deactivate when SelectionTool or DigitizeTool
        if ((ev.getSource() instanceof SelectionTool 
                || ev.getSource() instanceof DigitizeTool)
                && ev.getType() == EventType.TOOL_ACTIVATED
                && isActive()) {
            
            getSite().triggerTool( getSite().getToolPath(), false );
        }
        // activate if no other is active
        if (ev.getSource() != this
                && ev.getType() == EventType.TOOL_DEACTIVATED
                && !isActive()
                // on the same hierarchy level
                && ev.getSource().getToolPath().segmentCount() == getToolPath().segmentCount()) {
            
            getSite().triggerTool( getSite().getToolPath(), true );
        }
    }
    
    
    public String i18n( String key, Object... args ) {
        return Messages.get( "NavigationTool_" + key, args );    
    }

}
