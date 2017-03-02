/* 
 * polymap.org
 * Copyright (C) 2017, the @authors. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.core.data.raster;

import org.osgi.framework.BundleContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.ui.plugin.AbstractUIPlugin;

import org.polymap.core.data.DataPlugin;
import org.polymap.core.ui.ImageRegistryHelper;

/**
 * 
 * 
 * @author Falko Bräutigam
 */
public class RasterDataPlugin
        extends AbstractUIPlugin {

    private static final Log log = LogFactory.getLog( DataPlugin.class );

    public static final String PLUGIN_ID = "org.polymap.core.data.raster";

    private static RasterDataPlugin instance;


    public static RasterDataPlugin instance() {
        return instance;
    }

    /**
     * Use this to create frequently used images used by this plugin.
     */
    public static ImageRegistryHelper images() {
        return instance().images;
    }

    // instance *******************************************

    private ImageRegistryHelper images = new ImageRegistryHelper( this );


    public void start( BundleContext context ) throws Exception {
        super.start( context );
        instance = this;
    }


    public void stop( BundleContext context ) throws Exception {
        instance = null;
        super.stop( context );
    }

}
