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

import org.eclipse.core.runtime.Plugin;

import org.polymap.core.runtime.DefaultSessionContextProvider;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.runtime.SessionContext;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class FsPlugin
        extends Plugin {

    private static Log log = LogFactory.getLog( FsPlugin.class );
    
    // The plug-in ID
    public static final String      PLUGIN_ID = "org.polymap.service.fs";
    
    private static FsPlugin         plugin;
    

    public static FsPlugin getDefault() {
        return plugin;
    }

    
    // instance *******************************************
    
    private File                    cacheDir;

    public DefaultSessionContextProvider sessionContextProvider;

    
    public void start( BundleContext context ) throws Exception {
        super.start( context );
        plugin = this;
        
        // init cacheDir
        cacheDir = new File( Polymap.getCacheDir(), PLUGIN_ID );
        log.info( "Cleaning cache dir: " + cacheDir.getAbsolutePath() + " ..." );
        FileUtils.deleteDirectory( cacheDir );
        cacheDir.mkdirs();
        
        // register session context provider
        this.sessionContextProvider = new DefaultSessionContextProvider();
        SessionContext.addProvider( sessionContextProvider );
        
//        // start WorkbenchState listener
//        log.info( "Starting " + WorkbenchState.class.getSimpleName() + " listener ..." );
//        WorkbenchState.startup();
    }

    
    public void stop( BundleContext context ) throws Exception {
        super.stop( context );
        plugin = null;

        SessionContext.removeProvider( sessionContextProvider );
        sessionContextProvider = null;
    }
    
    
    public void invalidateSession( SessionContext sessionContext ) {
        assert sessionContext != null;
        try {
            sessionContextProvider.destroyContext( sessionContext.getSessionKey() );
        }
        catch (Exception e) {
            log.warn( "Error during invalidateSession(): " + e );
            log.debug( "", e );
        } 
    }
    
    
    public File getCacheDir() {
        return cacheDir;
    }
    
}
