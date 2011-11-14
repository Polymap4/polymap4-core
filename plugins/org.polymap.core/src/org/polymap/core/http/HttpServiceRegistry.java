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

package org.polymap.core.http;

import java.util.Dictionary;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

import org.polymap.core.CorePlugin;

/**
 * Provides a HTTP service registry based on OSGi {@link HttpService}. Allows to
 * register and unregister {@link HttpServlet}s.
 * 
 * @author <a href="http://www.polymap.de">Marcus -LiGi- Bueschleb</a>
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a> 
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class HttpServiceRegistry {
    
    private static final Log log = LogFactory.getLog( HttpServiceRegistry.class );

    private static final String                 SERVLETS_EXTENSION_POINT_ID = CorePlugin.PLUGIN_ID + ".http.servlets";

    private static HttpService                  httpService;
    
    /** The registered servlets. Maps pathSpec into servlet. */
    //private static Map<String,HttpServlet>      servlets = new HashMap();


    /**
     * This method can be used during startup to check in bundly change handlers
     * if the {@link HttpServiceRegistry} is available. This is necessary because
     * the availability of a particula service depends on the startup sequence
     * of the bundles.
     */
    public static boolean isInitialized() {
        return httpService != null;
    }
    
    
    /**
     * Initialization.
     */
    public static void init() {
        // HttpService ******
        BundleContext context = CorePlugin.getDefault().getBundle().getBundleContext();
        ServiceReference[] httpReferences = null;
        try {
//            ServiceReference[] services = context.getServiceReferences( null, null );
//            for (ServiceReference service : services) {
//                log.info( "Service: " + service );
//            }            
            httpReferences = context.getServiceReferences( HttpService.class.getName(), null );
        }
        catch (InvalidSyntaxException e) {
            throw new RuntimeException( e.getMessage(), e );
        }
        if (httpReferences != null) {
            String port = context.getProperty( "org.osgi.service.http.port" );
            String hostname = context.getProperty( "org.osgi.service.http.hostname" );
            log.info( "HTTP service found on hostname:" + hostname + "/ port:" + port );

            httpService = (HttpService)context.getService( httpReferences[0] );
        }
        else {
            throw new RuntimeException( "No HTTP service available" );
        }
        
        // servlet extensions ******
        IConfigurationElement[] exts = Platform.getExtensionRegistry().getConfigurationElementsFor( 
                SERVLETS_EXTENSION_POINT_ID ); 
        log.info( "servlet extensions found: " + exts.length ); //$NON-NLS-1$
        
        for (IConfigurationElement ext : exts) {
            try {
                String path = ext.getAttribute( "path" ); //$NON-NLS-1$
                String contextPath = ext.getAttribute( "contextPath" ); //$NON-NLS-1$
                if (contextPath != null && !contextPath.startsWith( "/" )) { //$NON-NLS-1$
                    contextPath = "/" + contextPath; //$NON-NLS-1$
                }
                HttpServlet servlet = (HttpServlet)ext.createExecutableExtension( "class" ); //$NON-NLS-1$
                registerServlet( path, servlet, null, null );
                log.info( "    context: " + contextPath + " :" + servlet.getClass().getName() ); //$NON-NLS-1$
            }
            catch (Exception e) {
                CorePlugin.logError( "Error while starting servlet extension: " + ext.getName(), log, e );
            }
        }
    }

    
    /**
     * 
     */
    public static synchronized void dispose() {
//        for (String pathSpec : servlets.keySet()) {
//            unregisterServlet( pathSpec );
//        }
//        servlets.clear();
        httpService = null;
    }


    /**
     * Registers a servlet into the URI namespace.
     * <p>
     * The pathSpec is the name in the URI namespace of the Http Service at
     * which the registration will be mapped.
     * <p>
     * An pathSpec must begin with slash ('/') and must not end with slash
     * ('/'), with the exception that an alias of the form &quot;/&quot; is used
     * to denote the root alias. See the specification text for details on how
     * HTTP requests are mapped to servlet and resource registrations.
     * <p>
     * The Http Service will call the servlet's <code>init</code> method before
     * returning.
     * 
     * <pre>
     * httpService.registerServlet( &quot;/myservlet&quot;, servlet, initparams, context );
     * </pre>
     * 
     * <p>
     * Servlets registered with the same <code>HttpContext</code> object will
     * share the same <code>ServletContext</code>. The Http Service will call
     * the <code>context</code> argument to support the
     * <code>ServletContext</code> methods <code>getResource</code>,
     * <code>getResourceAsStream</code> and <code>getMimeType</code>, and to
     * handle security for requests. If the <code>context</code> argument is
     * <code>null</code>, a default <code>HttpContext</code> object is used (see
     * {@link #createDefaultHttpContext}).
     * 
     * @param pathSpec name in the URI namespace at which the servlet is
     *        registered
     * @param servlet the servlet object to register
     * @param initparams initialization arguments for the servlet or
     *        <code>null</code> if there are none. This argument is used by the
     *        servlet's <code>ServletConfig</code> object.
     * @param context the <code>HttpContext</code> object for the registered
     *        servlet, or <code>null</code> if a default
     *        <code>HttpContext</code> is to be created and used.
     * @throws NamespaceException if the registration fails because the alias is
     *         already in use.
     * @throws ServletException if the servlet's <code>init</code> method throws
     *         an exception, or the given servlet object has already been
     *         registered at a different alias.
     * @throws IllegalArgumentException if any of the arguments are invalid
     */
    public static synchronized void registerServlet( String pathSpec, HttpServlet servlet,
            Dictionary initparams, HttpContext http_context ) 
            throws ServletException, NamespaceException {
        //assert !servlets.containsKey( pathSpec );
        
        httpService.registerServlet( pathSpec, servlet, initparams, http_context );
        //servlets.put( pathSpec, servlet );
    }

    
    public static synchronized void unregisterServlet( String pathSpec ) {
//        assert servlets.containsKey( pathSpec );
        httpService.unregister( pathSpec.startsWith( "/" ) ? pathSpec : "/"+pathSpec );
//        servlets.remove( pathSpec );
    }

    
//    public static synchronized void unregisterServlet( HttpServlet servlet ) {
//        String path = servlet.getUgetServletContext().getContextPath();
//        unregisterServlet( path );
//        
////        for (Map.Entry entry : servlets.entrySet()) {
////            if (entry.getValue() == servlet) {
////                unregisterServlet( (String)entry.getKey() );
////                return;
////            }
////        }
////        throw new IllegalStateException( "No such servlet: " + servlet );
//    }

}
