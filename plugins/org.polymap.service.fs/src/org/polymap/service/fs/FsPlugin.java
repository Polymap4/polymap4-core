/* 
 * polymap.org
 * Copyright 2011, Polymap GmbH. All rights reserved.
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
package org.polymap.service.fs;

import java.io.File;

import org.osgi.framework.BundleContext;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.ui.plugin.AbstractUIPlugin;

import org.polymap.core.runtime.Polymap;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class FsPlugin
        extends AbstractUIPlugin {

    private static Log log = LogFactory.getLog( FsPlugin.class );
    
    // The plug-in ID
    public static final String      PLUGIN_ID = "org.polymap.service.fs";
    
    private static FsPlugin         plugin;
    
    private File                    cacheDir;

    
    public void start( BundleContext context )
    throws Exception {
        super.start( context );
        plugin = this;
        
        cacheDir = new File( Polymap.getCacheDir(), PLUGIN_ID );
        log.info( "Cleaning cache dir: " + cacheDir.getAbsolutePath() + " ..." );
        FileUtils.deleteDirectory( cacheDir );
        cacheDir.mkdirs();
    }

    public void stop( BundleContext context )
    throws Exception {
        super.stop( context );
        plugin = null;
    }
    
    public static FsPlugin getDefault() {
        return plugin;
    }

    public File getCacheDir() {
        return cacheDir;
    }
    
}
