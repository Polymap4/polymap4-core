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
package org.polymap.service.geoserver;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.servlet.DispatcherServlet;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.lf5.util.StreamUtils;

import org.geoserver.logging.LoggingStartupContextListener;

import org.polymap.core.project.IMap;
import org.polymap.core.runtime.SessionContext;

import org.polymap.service.ServiceContext;
import org.polymap.service.http.WmsService;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class GeoServerWms
        extends WmsService {

    private static final Log log = LogFactory.getLog( GeoServerWms.class );

    /** First attemp to pass info to GeoServerLoader inside Spring. */
    public static ThreadLocal<GeoServerWms> servers = new ThreadLocal();
    
    private List<ServletContextListener>    loaders = new ArrayList();
    
    private DispatcherServlet               dispatcher;
    
    private PluginServletContext            context;
    
    private File                            dataDir;
    
    private String                          sessionKey;
    
    
    public GeoServerWms() {
        super();
        log.debug( "..." );
    }


    protected void init( String _pathSpec, IMap _map )
            throws MalformedURLException {
        super.init( _pathSpec, _map );
        try {
            sessionKey = SessionContext.current().getSessionKey();
            initGeoServer();
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }
    }


    public void destroy() {
        log.debug( "destroy(): ..." );
        if (dispatcher != null) {
            ClassLoader threadLoader = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader( context.cl );

            try {
                // listeners
                ServletContextEvent ev = new ServletContextEvent( context );
                for (ServletContextListener loader : loaders) {
                    loader.contextDestroyed( ev );
                }
                loaders = null;

                // dispatcher
                dispatcher.destroy();
                dispatcher = null;

                context.destroy();
                context = null;
            }
            finally {
                Thread.currentThread().setContextClassLoader( threadLoader );
            }
        }
    }


    protected void initGeoServer()
    throws Exception {

        context = new PluginServletContext( getServletContext() );
        log.debug( "initGeoServer(): contextPath=" + context.getContextPath() );
        
        ClassLoader threadLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader( context.cl );

        try {
            dataDir = new File( System.getProperty( "java.io.tmpdir" ), "polymap-geoserver-" + map.getLabel() );
            log.debug( "    dataDir=" + dataDir.getAbsolutePath() );
            dataDir.mkdirs();
            FileUtils.forceDeleteOnExit( dataDir );
            
            // web.xml
            context.setAttribute( "serviceStrategy", "SPEED" );
            context.setAttribute( "contextConfigLocation", "classpath*:/applicationContext.xml classpath*:/applicationSecurityContext.xml" );
            context.setAttribute( "enableVersioning", "false" );
            context.setAttribute( "GEOSERVER_DATA_DIR", dataDir.getAbsoluteFile() );

            try {
                servers.set( this );

                loaders.add( new LoggingStartupContextListener() );
                loaders.add( new ContextLoaderListener() );

                ServletContextEvent ev = new ServletContextEvent( context );
                for (Object loader : loaders) {
                    ((ServletContextListener)loader).contextInitialized( ev );
                }
            }
            finally {
                servers.set( null );
            }

            dispatcher = new DispatcherServlet();
            log.debug( "Dispatcher: " + dispatcher.getClass().getClassLoader() );
            (dispatcher).init( new ServletConfig() {

                public String getInitParameter( String name ) {
                    return GeoServerWms.this.getInitParameter( name );
                }

                public Enumeration getInitParameterNames() {
                    return GeoServerWms.this.getInitParameterNames();
                }

                public ServletContext getServletContext() {
                    return context;
                }

                public String getServletName() {
                    return "dispatcher";
                }
            });
        }
        finally {
            Thread.currentThread().setContextClassLoader( threadLoader );
        }
    }


    protected void service( final HttpServletRequest req, HttpServletResponse resp )
    throws ServletException, IOException {
        
        final String servletPath = req.getServletPath();
        String pathInfo = req.getPathInfo();
        log.debug( "Request: servletPath=" + servletPath + ", pathInfo=" + pathInfo );
        
//        if (req.getMethod().equals( "POST" )) {
//            ServletInputStream in = req.getInputStream();
//            System.out.println( "POST: " );
//            StreamUtils.copy( in, System.out );
//        }

        // schemas
        if (pathInfo != null && pathInfo.startsWith( "/schemas" )) {
            String resName = req.getPathInfo().substring( 1 );
            URL res = GeoServerPlugin.getDefault().getBundle().getResource( resName );
            if (res != null) {
                StreamUtils.copyThenClose( res.openStream(), resp.getOutputStream() );
            }
            else {
                log.warn( "No such resource found: " + resName );
            }
            return;
        }
        
        // services
        ClassLoader threadLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader( context.cl );
                
        try {
            // session context
            ServiceContext.mapContext( sessionKey /*req.getSession().getId()*/ );

//        HttpServletRequestWrapper wrapper = new HttpServletRequestWrapper( req ) {
//
//            public String getServletPath() {
//                log.info( "getServletPath(): ..." );
//                return "";
//            }
//
//            public String getRequestURI() {
//                HttpServletRequestWrapper delegate = (HttpServletRequestWrapper)getRequest();
//                String result = StringUtils.substringAfter(
//                        delegate.getRequestURI(), servletPath );
//                log.info( "getRequestURI(): " + result );
//                return result;
//            }
//
//            public StringBuffer getRequestURL() {
//                HttpServletRequestWrapper delegate = (HttpServletRequestWrapper)getRequest();
//                log.info( "getRequestURL(): " + delegate.getRequestURI() );
//                return delegate.getRequestURL();
//            }
//            
//        };

//            // FIXME huaahh... what a hack! :( remove this as soon as EPSG:3857 is
//            // supported
//            HttpServletRequestWrapper wrapper = new HttpServletRequestWrapper( req ) {
//
//                public Map getParameterMap() {
//                    Map result = new HashMap( 16 );
//                    for (Object entry : req.getParameterMap().entrySet()) {
//                        String[] values = (String[])((Map.Entry)entry).getValue();
//                        for (int i=0; i<values.length; i++) {
//                            values[i] = values[i].replace( "EPSG:3857", "EPSG:900913" ); 
//                        }
//                        result.put( ((Map.Entry)entry).getKey(), values );
//                    }
//                    log.debug( "getParameterMap(): " + result );
//                    return result;
//                }
//
//                public String getParameter( String name ) {
//                    String result = req.getParameter( name );
//                    log.debug( "getParameter(): " + name + " : " + result  );
//                    return result;
//                }
//            };
            
            (dispatcher).service( req, resp );
        }
        finally {
            Thread.currentThread().setContextClassLoader( threadLoader );

            ServiceContext.unmapContext();
        }
    }


    /**
     * A {@link ServletContext} facade to support GeoServer by given it
     * proper resources, real path, mime.
     */
    @SuppressWarnings("deprecation")
    class PluginServletContext
            implements ServletContext {
     
        private ServletContext          delegate;
        
        private GeoServerClassLoader    cl;

        
        protected PluginServletContext( ServletContext delegate ) {
            super();
            this.delegate = delegate;
            this.cl = new GeoServerClassLoader( getClass().getClassLoader() );
            log.debug( "ClassLoader: " + cl );
        }

        public void destroy() {
            cl.destroy();
            cl = null;
            delegate = null;
        }

        public Object getAttribute( String name ) {
            return delegate.getAttribute( name );
        }

        public Enumeration getAttributeNames() {
            return delegate.getAttributeNames();
        }

        public ServletContext getContext( String uripath ) {
            return delegate.getContext( uripath );
        }

        public String getContextPath() {
            log.debug( "getContextPath(): result=" + pathSpec );
            return pathSpec;
            //return delegate.getContextPath();
        }

        public String getInitParameter( String name ) {
            return delegate.getInitParameter( name );
        }

        public Enumeration getInitParameterNames() {
            return delegate.getInitParameterNames();
        }

        public int getMajorVersion() {
            return delegate.getMajorVersion();
        }

        public String getMimeType( String file ) {
            log.debug( "getMimeType(): file= " + file );
            String result = delegate.getMimeType( file );
            log.debug( "   result= " + result );
            return result;
        }

        public int getMinorVersion() {
            return delegate.getMinorVersion();
        }

        public RequestDispatcher getNamedDispatcher( String name ) {
            return delegate.getNamedDispatcher( name );
        }

        public String getRealPath( String path ) {
            path = path.startsWith( "/" ) ? path.substring( 1 ) : path;
            log.debug( "getRealPath(): path= " + path );
            File result = new File( dataDir, path );
//            String result = delegate.getRealPath( path );
            log.debug( "    result= " + result.getAbsolutePath() );
            if (!result.exists()) {
                result.mkdirs();
                log.debug( "    !exists() -> created." );
            }
            return result.getAbsolutePath();
        }

        public RequestDispatcher getRequestDispatcher( String path ) {
            return delegate.getRequestDispatcher( path );
        }

        public URL getResource( String path )
                throws MalformedURLException {
            path = path.startsWith( "/" ) ? path.substring( 1 ) : path;
            log.debug( "getResource(): path= " + path );
            return cl.getResource( path );
//            return delegate.getResource( path );
        }

        public InputStream getResourceAsStream( String path ) {
            path = path.startsWith( "/" ) ? path.substring( 1 ) : path;
            log.debug( "getResourceAsStream(): path= " + path );
            try {
                URL res = cl.getResource( path );
                return res != null ? res.openStream() : null;
            }
            catch (IOException e) {
                log.warn( e );
                return null;
            }
//            return delegate.getResourceAsStream( path );
        }

        public Set getResourcePaths( String path ) {
            return delegate.getResourcePaths( path );
        }

        public String getServerInfo() {
            return delegate.getServerInfo();
        }

        public Servlet getServlet( String name )
                throws ServletException {
            return delegate.getServlet( name );
        }

        public String getServletContextName() {
            return delegate.getServletContextName();
        }

        public Enumeration getServletNames() {
            return delegate.getServletNames();
        }

        public Enumeration getServlets() {
            return delegate.getServlets();
        }

        public void log( Exception exception, String msg ) {
            delegate.log( exception, msg );
        }


        public void log( String message, Throwable throwable ) {
            delegate.log( message, throwable );
        }

        public void log( String msg ) {
            delegate.log( msg );
        }

        public void removeAttribute( String name ) {
            delegate.removeAttribute( name );
        }

        public void setAttribute( String name, Object object ) {
            delegate.setAttribute( name, object );
        }
        
    }
}
