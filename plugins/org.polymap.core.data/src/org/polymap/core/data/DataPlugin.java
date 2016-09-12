/* 
 * polymap.org
 * Copyright (C) 2009-2013, Polymap GmbH. All rights reserved.
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
package org.polymap.core.data;

import org.geotools.factory.CommonFactoryFinder;
import org.opengis.filter.FilterFactory2;
import org.osgi.framework.BundleContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.graphics.Image;

import org.eclipse.ui.plugin.AbstractUIPlugin;

import org.polymap.core.ui.ImageRegistryHelper;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class DataPlugin
        extends AbstractUIPlugin {

    private static final Log log = LogFactory.getLog( DataPlugin.class );
    
    public static final String PLUGIN_ID = "org.polymap.core.data";

    /**
     * Central filter factory that can and should be used by all
     * code code depending on the data plugin.
     */
    public static final FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2( null );
    
    private static DataPlugin  plugin;


    public static DataPlugin getDefault() {
        return plugin;
    }
    
    
    /**
     * Use this to create frequently used images used by this plugin.
     */
    public static ImageRegistryHelper images() {
        return getDefault().images;
    }
    
    
    // instance *******************************************
    
    private ImageRegistryHelper         images = new ImageRegistryHelper( this );
    

    public void start( BundleContext context ) throws Exception {
        super.start( context );
        plugin = this;
        
        // does not seem to break things if not available
        log.info( "Using Marlin render engine if avalable." );
        System.setProperty( "sun.java2d.renderer", "org.marlin.pisces.PiscesRenderingEngine" );
    }


    public void stop( BundleContext context ) throws Exception {
        plugin = null;
        super.stop( context );
    }


    /**
     * @deprecated Use {@link #images()} instead.
     */
    public Image imageForName( String resName ) {
        return images.image( resName );
    }
    
}
