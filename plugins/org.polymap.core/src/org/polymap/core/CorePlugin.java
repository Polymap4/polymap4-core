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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

import org.osgi.framework.BundleContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.graphics.Image;

import org.eclipse.jface.resource.ImageDescriptor;

import org.eclipse.ui.plugin.AbstractUIPlugin;

import org.polymap.core.http.HttpServiceTracker;
import org.polymap.core.runtime.RapSessionContextProvider;
import org.polymap.core.runtime.SessionContext;
import org.polymap.core.ui.ImageRegistryHelper;

/**
 * The activator class controls the plug-in life cycle
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @since 3.0
 */
public class CorePlugin
        extends AbstractUIPlugin {

	private static Log log = LogFactory.getLog( CorePlugin.class );

	// The plug-in ID
	public static final String PLUGIN_ID = "org.polymap.core";

	// The shared instance
	private static CorePlugin  plugin;


    /**
     * A url stream handler that delegates to the default one but if it doesn't work
     * then it returns null as the stream.
     */
    public final static URLStreamHandler RELAXED_HANDLER = new URLStreamHandler(){
        @Override
        protected URLConnection openConnection( URL u ) throws IOException {
            try{
                URL url=new URL(u.toString());
                return url.openConnection();
            }catch (MalformedURLException e){
                return null;
            }
        }
    };

    
    public static CorePlugin instance() {
    	return plugin;
    }


	static {
//	    try {
//            Logging.GEOTOOLS.setLoggerFactory( "org.geotools.util.logging.CommonsLoggerFactory" );
//            System.out.print( "GEOTOOLS logger set to: " + "CommonsLogger" );
//        }
//        catch (Exception e) {
//            System.out.println( "No GEOTOOLS logger: " + e );
//        }

        // horrible log configuration system...
	    System.setProperty( "org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog" );
        System.setProperty( "org.apache.commons.logging.simplelog.defaultlog", "info" );
        System.setProperty( "org.apache.commons.logging.simplelog.showdatetime", "false" );
        System.setProperty( "org.apache.commons.logging.simplelog.dateTimeFormat", "HH:mm:ss" );

        //System.setProperty( "org.apache.commons.logging.simplelog.log.org.polymap.core.help", "debug" );
        //System.setProperty( "org.apache.commons.logging.simplelog.log.org.polymap.core.runtime.event", "debug" );

        //System.setProperty( "org.apache.commons.logging.simplelog.log.org.geotools.jdbc", "trace" );
        //System.setProperty( "org.apache.commons.logging.simplelog.log.org.geotools.data", "trace" );
        System.setProperty( "org.apache.commons.logging.simplelog.log.org.geotools.data.wfs", "trace" );
        System.setProperty( "org.apache.commons.logging.simplelog.log.org.geotools.data.communication", "trace" );

        //System.setProperty( "org.apache.commons.logging.simplelog.log.org.polymap.core.runtime.cache", "debug" );
        //System.setProperty( "org.apache.commons.logging.simplelog.log.org.polymap.core.runtime.recordstore", "debug" );
        System.setProperty( "org.apache.commons.logging.simplelog.log.org.polymap.core.runtime.recordstore.lucene", "debug" );
        //System.setProperty( "org.apache.commons.logging.simplelog.log.org.polymap.core.model2", "debug" );
        //System.setProperty( "org.apache.commons.logging.simplelog.log.org.polymap.core.workbench.dnd", "debug" );

        System.setProperty( "org.apache.commons.logging.simplelog.log.org.polymap.core.service.geoserver", "debug" );

        //System.setProperty( "org.apache.commons.logging.simplelog.log.org.polymap.core.mapeditor.RenderManager", "debug" );
        //System.setProperty( "org.apache.commons.logging.simplelog.log.org.polymap.core.mapeditor.services.SimpleWmsServer", "debug" );
        //System.setProperty( "org.apache.commons.logging.simplelog.log.org.polymap.core.mapeditor.tooling", "trace" );

        //System.setProperty( "org.apache.commons.logging.simplelog.log.org.polymap.core.qi4j", "debug" );
        
        //System.setProperty( "org.apache.commons.logging.simplelog.log.org.polymap.rhei.data", "debug" );
        //System.setProperty( "org.apache.commons.logging.simplelog.log.org.polymap.rhei.data.entityfeature.EntitySourceProcessor", "info" );
        //System.setProperty( "org.apache.commons.logging.simplelog.log.org.polymap.rhei.navigator", "debug" );

        System.setProperty( "org.apache.commons.logging.simplelog.log.org.polymap.biotop", "debug" );
        
        //System.setProperty( "org.apache.commons.logging.simplelog.log.org.polymap.core.data.pipeline", "debug" );
        System.setProperty( "org.apache.commons.logging.simplelog.log.org.polymap.core.data.image.cache304.ImageCacheProcessor", "debug" );
        //System.setProperty( "org.apache.commons.logging.simplelog.log.org.polymap.core.data.ui.csvimport", "debug" );
        //System.setProperty( "org.apache.commons.logging.simplelog.log.org.polymap.core.data.feature.recordstore", "debug" );

        System.setProperty( "org.apache.commons.logging.simplelog.log.com.ettrema.http", "info" );
        System.setProperty( "org.apache.commons.logging.simplelog.log.com.bradmcevoy", "info" );
        //System.setProperty( "org.apache.commons.logging.simplelog.log.org.polymap.service.fs", "debug" );

        //System.setProperty( "org.apache.commons.logging.simplelog.log.org.polymap.lka.osmtilecache", "debug" );        
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
        
        System.setProperty( "http.agent", "Polymap3 (http://polymap.org/polymap3/)" );
        System.setProperty( "https.agent", "Polymap3 (http://polymap.org/polymap3/)" );

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

	
    public Image imageForDescriptor( ImageDescriptor descriptor, String key ) {
        return images.image( descriptor, key );
    }

    
    public ImageDescriptor imageDescriptor( String path ) {
        return images.imageDescriptor( path );
    }

    
    public Image image( String path ) {
        return images.image( path );
    }

}
