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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

import org.polymap.core.CorePlugin;
import org.polymap.core.project.IMap;

import org.polymap.service.ServicesPlugin;

/**
 * Provides the factory for HTPP servers registered via the
 * {@value #WMS_EXTENSION_POINT_ID} extension point.
 * 
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @since 3.0
 */
public class MapHttpServletFactory {

    private static final Log log = LogFactory.getLog( MapHttpServletFactory.class );

    private static final String             WMS_EXTENSION_POINT_ID = "org.polymap.service.wms";

    private static IConfigurationElement    wmsExt, wfsExt;
    
//    private static Map<String,MapHttpServer>  servers = new HashMap();

    
    static {
        // wms extension
        IConfigurationElement[] exts = 
                Platform.getExtensionRegistry().getConfigurationElementsFor( WMS_EXTENSION_POINT_ID ); 
        log.info( "WMS extensions found: " + exts.length );

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
     * @see pathSpec The alias of the servlet, without leading /services path.
     * @return The newly created server.
     */
    public static MapHttpServer createWMS( IMap map, String pathSpec, boolean forceException )
            throws Exception {
        if (wmsExt == null) {
            throw new IllegalStateException( "No WMS service extension found." );
        }
        try {
            MapHttpServer wmsServer = (MapHttpServer)wmsExt.createExecutableExtension( "class" );
            log.debug( "service: " + wmsServer.getClass().getName() );

            String servicePath = null;
            if (pathSpec.startsWith( ServicesPlugin.SERVICES_PATHSPEC )) {
                log.warn( "", new IllegalStateException( "" ) );
                servicePath = pathSpec;
            }
            else {
                servicePath = ServicesPlugin.createServicePath( pathSpec );
            }
            CorePlugin.registerServlet( servicePath, wmsServer, null );
            wmsServer.init( map );
            
            return wmsServer;
        }
        catch (Exception e) {
            log.error( "Error while starting HttpServer: " + wmsExt.getName() );
            CorePlugin.logError( "Error while starting HttpServer: " + wmsExt.getName() );
            throw e;
        }
    }
    
    
    public static void destroyServer( MapHttpServer server ) {
        assert server != null;
        CorePlugin.unregister( server );    
    }
    
}
