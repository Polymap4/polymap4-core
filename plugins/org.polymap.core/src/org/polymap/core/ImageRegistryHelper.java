/* 
 * polymap.org
 * Copyright (C) 2013, Falko Bräutigam. All rights reserved.
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
package org.polymap.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.graphics.Image;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;

import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * Provides helper methods to work with the {@link ImageRegistry} of an
 * {@link AbstractUIPlugin}.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ImageRegistryHelper {

    private static Log log = LogFactory.getLog( ImageRegistryHelper.class );

    private AbstractUIPlugin        plugin;

    
    public ImageRegistryHelper( AbstractUIPlugin plugin ) {
        assert plugin != null;
        this.plugin = plugin;
    }


    public ImageRegistry getImageRegistry() {
        return plugin.getImageRegistry();
    }
    

    public Image image( ImageDescriptor imageDescriptor, String key ) {
        ImageRegistry images = getImageRegistry();
        Image image = images.get( key );
        if (image == null || image.isDisposed()) {
            images.put( key, imageDescriptor );
            image = images.get( key );
        }
        return image;
    }


    public Image image( String path ) {
        return image( imageDescriptor( path ), path );
    }

    
    public ImageDescriptor imageDescriptor( String path ) {
        assert path != null;
        ImageRegistry images = getImageRegistry();
        ImageDescriptor image = images.getDescriptor( path );
        if (image == null) {
            String pluginId = plugin.getBundle().getSymbolicName();
            image = AbstractUIPlugin.imageDescriptorFromPlugin( pluginId, path );
            images.put( path, image );
        }
        return image;
    }

}
