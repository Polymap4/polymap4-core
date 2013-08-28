/*
 * polymap.org 
 * Copyright (C) 2009-2013, Polymap GmbH. All rights reserved.
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */
package org.polymap.core.mapeditor;

import org.osgi.framework.BundleContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.graphics.Image;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import org.polymap.core.ImageRegistryHelper;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @since 3.0
 */
public class MapEditorPlugin
        extends AbstractUIPlugin {

    private static Log log = LogFactory.getLog( MapEditorPlugin.class );

    // The plug-in ID
    public static final String      PLUGIN_ID = "org.polymap.core.mapeditor";

    // The shared instance
    private static MapEditorPlugin  plugin;


    public static MapEditorPlugin getDefault() {
        return plugin;
    }

    /**
     * Creates a {@link ImageDescriptor} from the given resource path.
     *
     * @param resource The path the the resource inside the bundle.
     * @return Newly created or cached instance.
     */
    public static ImageDescriptor imageDescriptor( String resource ) {
        return getDefault().images.imageDescriptor( resource );
    }

    
    public static Image image( String resource ) {
        return getDefault().images.image( resource );
    }

    
    // instance *******************************************
    
    private ImageRegistryHelper     images = new ImageRegistryHelper( this );
    
    
    public void start( BundleContext context ) throws Exception {
        super.start( context );
        plugin = this;
    }


    public void stop( BundleContext context ) throws Exception {
        plugin = null;
        super.stop( context );
    }


}
