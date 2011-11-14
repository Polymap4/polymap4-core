/* 
 * polymap.org
 * Copyright 2009, Polymap GmbH, and individual contributors as indicated
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
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 * $Id$
 */
package org.polymap.service.http;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

import org.polymap.core.CorePlugin;
import org.polymap.core.http.HttpServiceRegistry;
import org.polymap.core.project.IMap;

/**
 * Provides the factory and registry for {@link HttpService}s.
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class HttpServiceFactory {

    private static final Log log = LogFactory.getLog( HttpServiceFactory.class );

    private static final String             WMS_EXTENSION_POINT_ID = "org.polymap.service.wms";

    private static IConfigurationElement    wmsExt, wfsExt;
    
//    private static Map<String,HttpService>  servers = new HashMap();

    
    static {
        // wms extension
        IConfigurationElement[] exts = Platform.getExtensionRegistry().getConfigurationElementsFor( 
                WMS_EXTENSION_POINT_ID ); 
        log.info( "WMS extensions found: " + exts.length ); //$NON-NLS-1$

        if (exts.length == 0) {
            log.warn( "No WMS extension." );
        }
        else if (exts.length > 1) {
            // FIXME hack to priorize the SimpleWmsServer
            for (IConfigurationElement ext : exts) {
                if (ext.getAttribute( "class" ).equals( "SimpleWmsServer" )) {
                    wmsExt = ext;
                }
                wmsExt = wmsExt != null ? wmsExt : exts[0];
                log.info( "    ==> using: " + wmsExt.getAttribute( "class" ) ); //$NON-NLS-1$
            }
        }
        else {
            wmsExt = exts[0];
            log.info( "    ==> using: " + wmsExt.getAttribute( "class" ) ); //$NON-NLS-1$
        }
    }


    /**
     * 
     * @see #registerServer(HttpService, String, boolean)
     * @return The newly created server.
     */
    public static WmsService createWMS( IMap map, String pathSpec, boolean forceException )
            throws Exception {
        if (wmsExt == null) {
            throw new IllegalStateException( "No WMS service extension found." );
        }
        try {
            WmsService wmsServer = (WmsService)wmsExt.createExecutableExtension( "class" ); //$NON-NLS-1$
            log.debug( "service: " + wmsServer.getClass().getName() ); //$NON-NLS-1$

            String url = registerServer( wmsServer, pathSpec, forceException );
            wmsServer.init( url, map );
            
            return wmsServer;
        }
        catch (Exception e) {
            log.error( "Error while starting HttpServer: " + wmsExt.getName() );
            CorePlugin.logError( "Error while starting HttpServer: " + wmsExt.getName() );
            throw e;
        }
    }
    
    
    /**
     * 
     * @param pathSpec The pathSpec must begin with slash ('/') and must not end
     *        with slash ('/'), with the exception that an alias of the form
     *        &quot;/&quot; is used to denote the root alias. See the
     *        specification text for details on how HTTP requests are mapped to
     *        servlet and resource registrations.
     * @param forceException
     * @return The URL of the server.
     * @throws Exception
     */
    public static String registerServer( HttpService server, String pathSpec, boolean forceException )
            throws Exception {
        // check pathSpec
        pathSpec = trimPathSpec( pathSpec );

        // give relative URL to the server (without leading slash)
        String url = pathSpec.substring( 1 );

//        HttpService old = servers.put( url, server );
//        if (old != null) {
//            if (!forceException) {
//                unregisterServer( old, false );
//            }
//            else {
//                servers.put( url, old );
//                throw new IllegalStateException( "Service already registered for patSpec: " + pathSpec );
//            }
//        }

        HttpServiceRegistry.registerServlet( pathSpec, server, null, null );
        return url;
    }
    
    
    public static void unregisterServer( HttpService server, boolean forceException ) {
        try {
//            HttpService old = servers.remove( server.getPathSpec() );
//            if (old != null) {
                HttpServiceRegistry.unregisterServlet( server.getPathSpec() );
//            }
//            else {
//                throw new IllegalStateException( "No service for pathSpec: " + server.getPathSpec() );
//            }
        }
        catch (RuntimeException e) {
            if (forceException) {
                throw e;
            }
        }
    }
    
    
    /**
     * The OwsServer for the given path.
     * 
     * @param pathSpec
     * @return The OwsServer for the given path, or null if no such service is registered.
     */
    public HttpService findService( String pathSpec ) {
        throw new RuntimeException( "not yet implemented." );
    }
    

    /**
     * Ensures that the given pathSpec The pathSpec must begin with slash ('/')
     * and must not end with slash ('/'), with the exception that an alias of
     * the form &quot;/&quot; is used to denote the root alias. See the
     * specification text for details on how HTTP requests are mapped to servlet
     * and resource registrations.
     * 
     * @return The modified pathSpec.
     */
    public static String trimPathSpec( String pathSpec ) {
        String result = pathSpec;
        // check pathSpec
        if (!result.startsWith( "/" )) {
            log.warn( "pathSpec does not start with slash: " + pathSpec );
            result = "/" + pathSpec;
        }
        if (result.endsWith( "/" )) {
            log.warn( "pathSpec ends with slash: " + pathSpec );
            result = StringUtils.substring( pathSpec, 0, pathSpec.length()-1 );
        }
        return result;
    }
    
}
