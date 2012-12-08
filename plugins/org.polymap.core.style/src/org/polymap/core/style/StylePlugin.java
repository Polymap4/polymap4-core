/* 
 * polymap.org
 * Copyright 2009-2012, Polymap GmbH. All rights reserved.
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
package org.polymap.core.style;

import java.util.HashMap;

import net.refractions.udig.catalog.ID;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;

import org.eclipse.ui.plugin.AbstractUIPlugin;

import org.eclipse.core.runtime.IPath;

import org.polymap.core.runtime.Polymap;
import org.polymap.core.style.geotools.GtLocalCatalog;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @since 3.0
 */
public class StylePlugin 
        extends AbstractUIPlugin {

    private static Log log = LogFactory.getLog( StylePlugin.class );
    
	public static final String PLUGIN_ID = "org.polymap.core.style";

	private static StylePlugin             plugin;
	
	/** The style catalog that holds all styles of this instance. */
	private static IStyleCatalog           localCatalog;
	
    private static boolean                 started = false;

    private HashMap<String,ID> servlet_path_mapping;
	
    private final static String sld_servlet_path="/sld";
    
	public StylePlugin() {
		servlet_path_mapping=new HashMap<String,ID>();
	}

	
	public void start( final BundleContext context ) throws Exception {
		super.start( context );
		plugin = this;
        
		IPath ws = Polymap.getWorkspacePath();
		log.info( "Starting StyleCatalog Plugin. Workspace: " + ws );
        localCatalog = new GtLocalCatalog( ws );
        
        // start HttpServiceRegistry
        context.addBundleListener( new BundleListener() {

            public void bundleChanged( BundleEvent ev ) {

                if (!started && (HttpService.class != null)) {

                    // log.info("bundle event" + ev.getType() + " " +
                    // ev.getBundle() );
                    // if (ev.getType() == BundleEvent.STARTED &&
                    // ev.getBundle().equals( getBundle() )) {

                    HttpService httpService;
                    // BundleContext context=
                    // CorePlugin.getDefault().getBundle().getBundleContext();
                    ServiceReference[] httpReferences = null;
                    try {
                        httpReferences = context.getServiceReferences( HttpService.class.getName(), null );
                    }
                    catch (InvalidSyntaxException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    if (httpReferences != null) {
                        String port = context.getProperty( "org.osgi.service.http.port" );
                        String hostname = context.getProperty( "org.osgi.service.http.hostname" );

                        log.info( "found http service on hostname:" + hostname + "/ port:" + port );

                        httpService = (HttpService)context.getService( httpReferences[0] );

                        try {
                            SLDDownloadServlet s = new SLDDownloadServlet();

                            httpService.registerServlet( sld_servlet_path, s, null, null );
                            started = true;
                        }
                        catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                    else {
                        log.debug( "No http service yet available - waiting for next BundleEvent" );
                    }
                }
                // stop
                else if (ev.getType() == BundleEvent.STOPPED && ev.getBundle().equals( getBundle() )) {

                }
            }
        } );
    }

	

    public String getServletPathForId(ID id) {
    	servlet_path_mapping.put(id.labelResource(), id);
    	return StringUtils.removeStart( sld_servlet_path, "/" ) + "/" + id.labelResource();
    }
    	
    public ID getIDbyServletPath(String path) {
    	return servlet_path_mapping.get(path);
    }

	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	public static StylePlugin getDefault() {
		return plugin;
	}

	
    /** 
     * The style catalog that holds all styles of this instance. 
     */
	public static IStyleCatalog getStyleCatalog() {
	    return localCatalog;
	}
	
}
