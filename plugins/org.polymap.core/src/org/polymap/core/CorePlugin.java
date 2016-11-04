/*
 * polymap.org
 * Copyright (C) 2009-2015 Polymap GmbH. All rights reserved.
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
package org.polymap.core;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.ui.plugin.AbstractUIPlugin;

import org.eclipse.core.runtime.Plugin;
import org.polymap.core.http.HttpServiceTracker;
import org.polymap.core.runtime.session.RapSessionContextProvider;
import org.polymap.core.runtime.session.SessionContext;
import org.polymap.core.ui.ImageRegistryHelper;

/**
 * The activator class controls the plug-in life cycle
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class CorePlugin
        extends AbstractUIPlugin {

	private static Log log = LogFactory.getLog( CorePlugin.class );

    public static final String      PLUGIN_ID = "org.polymap.core";

    public static final String      DATA_DIR = "data";

    private static CorePlugin       plugin;


//    static {
//        ThemeManager.STANDARD_RESOURCE_LOADER = new ResourceLoader() {
//            @Override
//            public InputStream getResourceAsStream( String resName ) throws IOException {
//                log.info( "Loading: " + resName );
//                InputStream result = ThemeManager.class.getClassLoader().getResourceAsStream( resName );
//                if (result == null) {
//                    log.info( "   not found!!!" );                    
//                }
//                return result;
//            }
//        };
//    }

    public static CorePlugin instance() {
        return plugin;
    }


    /**
     * @see #getDataLocation(Bundle) 
     */
    public static File getDataLocation( @SuppressWarnings("hiding") Plugin plugin ) {
       return getDataLocation( plugin.getBundle() );
    }
    
    
    /**
     * Returns the location in the local file system of the plug-in data area for
     * this plug-in. If the plug-in data area did not exist prior to this call, it
     * is created.
     * <p/>
     * The plug-in data area is a file directory within the platform's data area
     * where a plug-in is free to create files. The content and structure of this
     * area is defined by the plug-in, and the particular plug-in is solely
     * responsible for any files it puts there.
     * 
     * @throws IllegalStateException If the system is running with no data area
     *         (-data @none), or when a data area has not been set yet.
     */
    public static File getDataLocation( Bundle bundle ) {
        File workspace = instance().getStateLocation().toFile().getParentFile().getParentFile().getParentFile();
        File result = new File( new File( workspace, DATA_DIR ), bundle.getSymbolicName() );
        result.mkdirs();
        return result;
    }
    
    
    /**
     * A url stream handler that delegates to the default one but if it doesn't work
     * then it returns null as the stream.
     */
    public final static URLStreamHandler RELAXED_HANDLER = new URLStreamHandler() {
        @Override
        protected URLConnection openConnection( URL url ) throws IOException {
            try {
                URL url2 = new URL( url.toString() );
                return url2.openConnection();
            }
            catch (MalformedURLException e) {
                return null;
            }
        }
    };

    
    /**
     * Use this to create frequently used images used by this plugin.
     */
    public static ImageRegistryHelper images() {
        return instance().images;
    }
    
    
	// instance *******************************************
	
    private RapSessionContextProvider   rapSessionContextProvider;

    private HttpServiceTracker          httpServiceTracker;
    
    private ImageRegistryHelper         images = new ImageRegistryHelper( this );
    

    public CorePlugin() {
        log.info( "Log system initialized" );
    }
    
    
    public void start( final BundleContext context ) throws Exception {
        super.start( context );
        log.debug( "start..." );
        plugin = this;
        
        System.setProperty( "http.agent", "Polymap4 (https://github.com/Polymap4)" );
        System.setProperty( "https.agent", "Polymap4 (https://github.com/Polymap4)" );

        // RAP session context
        this.rapSessionContextProvider = new RapSessionContextProvider();
        SessionContext.addProvider( rapSessionContextProvider );

        // init HttpServiceTracker
        httpServiceTracker = new HttpServiceTracker( context );
        httpServiceTracker.open();
    }


    public void stop( BundleContext context ) throws Exception {
        log.debug( "stop..." );
        
        httpServiceTracker.close();
        httpServiceTracker = null;
        
        plugin = null;
        super.stop( context );
        
        SessionContext.removeProvider( rapSessionContextProvider );
        rapSessionContextProvider = null;
    }
	
}
