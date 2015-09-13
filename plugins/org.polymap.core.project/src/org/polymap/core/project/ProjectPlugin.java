/* 
 * polymap.org
 * Copyright (C) 2009-2015, Polymap GmbH. All rights reserved.
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
package org.polymap.core.project;

import org.osgi.framework.BundleContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.graphics.Image;

import org.eclipse.jface.resource.ImageDescriptor;

import org.eclipse.ui.plugin.AbstractUIPlugin;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.polymap.core.ui.ImageRegistryHelper;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ProjectPlugin
        extends AbstractUIPlugin {

    private static Log log = LogFactory.getLog( ProjectPlugin.class );
    
    public static final String              PLUGIN_ID = "org.polymap.core.project";

    private static ProjectPlugin            instance;
    

    public static ProjectPlugin instance() {
        return instance;
    }


    public static void logInfo( String msg ) {
        instance().getLog().log( new Status( IStatus.INFO, PLUGIN_ID, msg ) );    
    }


    public static void logError( String msg ) {
        try {
            instance().getLog().log( new Status( IStatus.ERROR, PLUGIN_ID, msg ) );
        }
        catch (Exception e) {
            // ignore
        }    
    }


    /**
     * Use this to create frequently used images used by this plugin.
     */
    public static ImageRegistryHelper images() {
        return instance().images;
    }

    
    // instance *******************************************
    
    private ImageRegistryHelper     images = new ImageRegistryHelper( this );

    
    public void start( BundleContext context ) throws Exception {
        super.start( context );
        instance = this;
    }
    
    
    @Override
    public void stop( BundleContext context ) throws Exception {
        super.stop( context );
        instance = null;
    }


    /**
     * @deprecated Use {@link #images()} instead.
     */
    public Image imageForName( String resName ) {
        return images.image( resName );
    }

    
    /**
     * @deprecated Use {@link #images()} instead.
     */
    public ImageDescriptor imageDescriptor( String path ) {
        return images.imageDescriptor( path );
    }    

}
