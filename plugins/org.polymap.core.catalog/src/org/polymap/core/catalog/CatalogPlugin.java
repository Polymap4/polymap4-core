/* 
 * polymap.org
 * Copyright (C) 2015, Falko Bräutigam. All rights reserved.
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
package org.polymap.core.catalog;

import java.net.URL;

import org.osgi.framework.BundleContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.graphics.Image;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;

import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class CatalogPlugin
        extends AbstractUIPlugin {

    private static Log log = LogFactory.getLog( CatalogPlugin.class );

    public static final String      ID = "org.polymap.core.catalog";

    private static CatalogPlugin    instance;
    
    
    public static CatalogPlugin instance() {
        return instance;
    }


    // instance *******************************************
    
    public void start( final BundleContext context ) throws Exception {
        super.start( context );
        instance = this;
    }


    public void stop( BundleContext context ) throws Exception {
        instance = null;
        super.stop( context );
    }


    public Image imageForName( String resName ) {
        ImageRegistry images = getImageRegistry();
        Image image = images.get( resName );
        if (image == null || image.isDisposed()) {
            URL res = getBundle().getResource( resName );
            assert res != null : "Image resource not found: " + resName;
            images.put( resName, ImageDescriptor.createFromURL( res ) );
            image = images.get( resName );
        }
        return image;
    }


    public Image imageForDescriptor( ImageDescriptor imageDescriptor, String key ) {
        ImageRegistry images = getImageRegistry();
        Image image = images.get( key );
        if (image == null || image.isDisposed()) {
            images.put( key, imageDescriptor );
            image = images.get( key );
        }
        return image;
    }

}
