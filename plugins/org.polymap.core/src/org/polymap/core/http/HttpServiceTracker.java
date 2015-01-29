/* 
 * polymap.org
 * Copyright 2009-2012, Polymap GmbH, and individual contributors as indicated
 * by the @authors tag.
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
package org.polymap.core.http;

import java.util.Dictionary;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.osgi.util.tracker.ServiceTracker;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

import org.polymap.core.CorePlugin;

/**
 * Tracks the {@link HttpService}. When a HttpService is available then it
 * initializes servlets registered via the {@link #SERVLETS_EXTENSION_POINT_ID}.
 * <p/>
 * This also provides a facade to the {@link HttpService} that might be used for
 * better argument checking and error handling.
 * 
 * @author <a href="http://www.polymap.de">Marcus -LiGi- Bueschleb</a>
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @since 3.0
 */
public class HttpServiceTracker
        extends ServiceTracker {
    
    private static final Log log = LogFactory.getLog( HttpServiceTracker.class );

    private static final String         SERVLETS_EXTENSION_POINT_ID = CorePlugin.PLUGIN_ID + ".http.servlets";

    private HttpService                 httpService;
    
    
    public HttpServiceTracker( BundleContext context ) {
        super( context, HttpService.class.getName(), null );
    }

    
    public HttpService getHttpService() {
        return httpService;
    }


    public Object addingService( ServiceReference reference ) {
        HttpService _httpService = (HttpService)super.addingService( reference );                
        if (httpService == null && _httpService != null) {
            httpService = _httpService;
            init();
        }
        return httpService;
    }
    

    protected void init() {
        assert httpService != null;
        
        // servlet extensions ******
        IConfigurationElement[] exts = Platform.getExtensionRegistry().getConfigurationElementsFor( 
                SERVLETS_EXTENSION_POINT_ID ); 
        log.debug( "servlet extensions found: " + exts.length );
        
        for (IConfigurationElement ext : exts) {
            try {
                String path = ext.getAttribute( "path" );
                String contextPath = ext.getAttribute( "contextPath" );
                if (contextPath != null && !contextPath.startsWith( "/" )) {
                    contextPath = "/" + contextPath;
                }
                                
                HttpServlet servlet = (HttpServlet)ext.createExecutableExtension( "class" );
                httpService.registerServlet( path, servlet, null, null );
                log.debug( "    context: " + contextPath + " :" + servlet.getClass().getName() );
            }
            catch (Exception e) {
                throw new RuntimeException( "Error while starting servlet extension: ", e );
            }
        }
    }


    public HttpContext createDefaultHttpContext() {
        return httpService.createDefaultHttpContext();
    }


    public void registerResources( String alias, String name, 
            @SuppressWarnings("hiding") HttpContext context )
            throws NamespaceException {
        httpService.registerResources( alias, name, context );
    }


    public void registerServlet( String alias, Servlet servlet, Dictionary initparams,
            @SuppressWarnings("hiding") HttpContext context )
            throws ServletException, NamespaceException {
        httpService.registerServlet( alias, servlet, initparams, context );
    }


    public void unregister( String alias ) {
        httpService.unregister( alias );
    }
    
}
