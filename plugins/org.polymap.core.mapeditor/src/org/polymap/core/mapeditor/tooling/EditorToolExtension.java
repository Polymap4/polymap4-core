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
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Supplier;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;

import org.polymap.core.mapeditor.MapEditorPlugin;
import org.polymap.core.runtime.CachedLazyInit;
import org.polymap.core.runtime.LazyInit;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
class EditorToolExtension {

    private static Log log = LogFactory.getLog( EditorToolExtension.class );
    
    public static final String              POINT_ID = "editorTools";
    
    public static LazyInit<List<EditorToolExtension>> exts = new CachedLazyInit( 1024 );
    
    
    public static List<EditorToolExtension> all() {
        return exts.get( new Supplier<List<EditorToolExtension>>() {
            public List<EditorToolExtension> get() {
                IExtensionRegistry reg = Platform.getExtensionRegistry();
                IConfigurationElement[] elms = reg.getConfigurationElementsFor( 
                        MapEditorPlugin.PLUGIN_ID, POINT_ID );

                List<EditorToolExtension> result = new ArrayList( elms.length );
                for (int i=0; i<elms.length; i++) {
                    result.add( new EditorToolExtension( elms[i] ) );
                }
                return result;
            }
        });
    }

    
    // instance *******************************************
    
    private IConfigurationElement       config;
    
    
    public EditorToolExtension( IConfigurationElement config ) {
        this.config = config;
    }

    
    public IPath getToolPath() {
        return new Path( config.getAttribute( "toolPath" ) );
    }

    
    public IEditorTool createEditorTool() {
        try {
            DefaultEditorTool tool = (DefaultEditorTool)config.createExecutableExtension( "class" );
            tool.toolPath = getToolPath();
            tool.label = config.getAttribute( "label" );
            tool.tooltip = config.getAttribute( "tooltip" );
            tool.description = config.getAttribute( "description" );
            tool.icon = MapEditorPlugin.image( config.getAttribute( "icon" ) );
            return tool;
        }
        catch (CoreException e) {
            throw new RuntimeException( e );
        }
    }

}
