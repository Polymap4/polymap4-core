/* 
 * polymap.org
 * Copyright 2012-2013, Falko Bräutigam. All rights reserved.
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

import com.google.common.collect.Iterables;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.core.runtime.IPath;

import org.polymap.core.mapeditor.Messages;
import org.polymap.core.mapeditor.tooling.DefaultEditorTool;
import org.polymap.core.mapeditor.tooling.EditorTools;
import org.polymap.core.mapeditor.tooling.IEditorTool;
import org.polymap.core.mapeditor.tooling.IEditorToolSite;
import org.polymap.core.mapeditor.tooling.ToolingEvent;
import org.polymap.core.mapeditor.tooling.ToolingEvent.EventType;
import org.polymap.core.runtime.Polymap;

import org.polymap.openlayers.rap.widget.controls.KeyboardDefaultsControl;
import org.polymap.openlayers.rap.widget.controls.NavigationControl;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class NavigationTool
        extends DefaultEditorTool
        implements IEditorTool {

    private static Log log = LogFactory.getLog( NavigationTool.class );

    private NavigationControl           naviControl;

    private KeyboardDefaultsControl     keyboardControl;
    
    
    @Override
    public boolean init( IEditorToolSite site ) {
        boolean result = super.init( site );
        
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
        super.onActivate();
//        // keyboardControl (catches each and every keyboard event)
//        keyboardControl = new KeyboardDefaultsControl();
//        getSite().getEditor().addControl( keyboardControl );
//        keyboardControl.activate();
        
        // navi naviControl
        naviControl = new NavigationControl();
        getSite().getEditor().addControl( naviControl );
        naviControl.activate();
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
        super.onDeactivate();
        if (keyboardControl != null) {
            getSite().getEditor().removeControl( keyboardControl );
            keyboardControl.deactivate();
            keyboardControl.dispose();
            keyboardControl = null;
        }
        if (naviControl != null) {
            naviControl.deactivate();
            getSite().getEditor().removeControl( naviControl );
            naviControl.destroy();
            naviControl.dispose();
            naviControl = null;
        }
    }


    @Override
    public void toolingChanged( ToolingEvent ev ) {
        super.toolingChanged( ev );
        
        // activate if no other is active
        if (ev.getSource() != this
                && ev.getType() == EventType.TOOL_DEACTIVATED
                && !isActive()
                // on the same hierarchy level
                && ev.getSource().getToolPath().segmentCount() == getToolPath().segmentCount()) {
            
            // check if there is any tool active - after all events have been processed
            Polymap.getSessionDisplay().asyncExec( new Runnable() {
                public void run() {
                    IPath levelToolPath = getToolPath().removeLastSegments( 1 );
                    if (Iterables.isEmpty( getSite().filterTools( 
                            EditorTools.hasStrictPrefix( levelToolPath ),
                            EditorTools.isActive() ) )) {

                        getSite().triggerTool( getSite().getToolPath(), true );
                    }
                }
            });
        }
    }
    
    
    public String i18n( String key, Object... args ) {
        return Messages.get( "NavigationTool_" + key, args );    
    }

}
