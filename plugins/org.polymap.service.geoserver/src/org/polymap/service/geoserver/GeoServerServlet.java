/* 
 * polymap.org
 * Copyright (C) 2009-2015 Polymap GmbH. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.service.geoserver;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.List;
import java.util.Map;
import java.util.Set;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.ServletRegistration.Dynamic;
import javax.servlet.SessionCookieConfig;
import javax.servlet.SessionTrackingMode;
import javax.servlet.descriptor.JspConfigDescriptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.servlet.DispatcherServlet;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.geoserver.logging.LoggingStartupContextListener;

import org.polymap.core.runtime.Stringer;
import javax.servlet.http.*;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class GeoServerServlet
        extends HttpServlet {

    private static final Log log = LogFactory.getLog( GeoServerServlet.class );

    /** First attemp to pass info to GeoServerLoader inside Spring. */
    public static ThreadLocal<GeoServerServlet> servers = new ThreadLocal();
    
    /**
     * XXX Bad hack. I just don't find the right way through GeoServer code
     * to get HTTP response in a {@link PipelineMapProducer}.
     */
    public static ThreadLocal<HttpServletResponse> response = new ThreadLocal();
    
    private List<ServletContextListener>    loaders = new ArrayList();
    
    private DispatcherServlet               dispatcher;
    
    private PluginServletContext            context;
    
    private File                            dataDir;
    
//    private String                          sessionKey;
    
    
    @Override
    public void init( ServletConfig config ) throws ServletException {
        super.init( config );

//        sessionKey = SessionContext.current().getSessionKey();
//        assert sessionKey != null;

        context = new PluginServletContext( getServletContext() );
        log.debug( "initGeoServer(): contextPath=" + context.getContextPath() );

        ClassLoader threadLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader( context.cl );
        try {
//            ServiceContext.mapContext( sessionKey );
            initGeoServer();
        }
        catch (RuntimeException e) {
            throw e;
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }
        finally {
            Thread.currentThread().setContextClassLoader( threadLoader );
//            ServiceContext.unmapContext( false );
        }
    }


    public void destroy() {
        log.debug( "destroy(): ..." );
        super.destroy();
        if (dispatcher != null) {
            ClassLoader threadLoader = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader( context.cl );

            try {
                // listeners
                ServletContextEvent ev = new ServletContextEvent( context );
                for (ServletContextListener loader : loaders) {
                    loader.contextDestroyed( ev );
                }
                loaders.clear();

                // dispatcher
                dispatcher.destroy();
                dispatcher = null;

                context.destroy();
                context = null;
            }
            catch (IOException e) {
                log.warn( "", e );
            }
            finally {
                Thread.currentThread().setContextClassLoader( threadLoader );
            }
        }
    }


    protected void initGeoServer() throws Exception {        
        File cacheDir = GeoServerPlugin.instance().getCacheDir();
        dataDir = new File( cacheDir, Stringer.of( getMapLabel() ).toFilename( "_" ).toString() );
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
        dispatcher.init( new ServletConfig() {

            public String getInitParameter( String name ) {
                return GeoServerServlet.this.getInitParameter( name );
            }

            public Enumeration getInitParameterNames() {
                return GeoServerServlet.this.getInitParameterNames();
            }

            public ServletContext getServletContext() {
                return context;
            }

            public String getServletName() {
                return "dispatcher";
            }
        });
    }
    
    
    protected String getMapLabel() {
//    	return map.getLabel();
    	return "dummy_map";
    }


    protected void service( final HttpServletRequest req, HttpServletResponse resp )
            throws ServletException, IOException {
        
        final String servletPath = req.getServletPath();
        String pathInfo = req.getPathInfo();
        log.debug( "Request: servletPath=" + servletPath + ", pathInfo=" + pathInfo );
        
        // schemas
        if (pathInfo != null && pathInfo.startsWith( "/schemas" )) {
            String resName = req.getPathInfo().substring( 1 );
            URL res = GeoServerPlugin.instance().getBundle().getResource( resName );
            if (res != null) {
                IOUtils.copy( res.openStream(), resp.getOutputStream() );
                IOUtils.closeQuietly( res.openStream() );
                resp.getOutputStream().flush();
                resp.flushBuffer();
            }
            else {
                log.warn( "No such resource found: " + resName );
            }
            return;
        }
        
        // service
        ClassLoader threadLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader( context.cl );
        try {
            // session context
//            ServiceContext.mapContext( sessionKey );
            response.set( resp );
            dispatcher.service( req, resp );
        }
        finally {
            Thread.currentThread().setContextClassLoader( threadLoader );
//            ServiceContext.unmapContext( false );
            response.set( null );
        }
    }
    
    // JRE: copied from /org.polymap.service/src/org/polymap/service/http/MapHttpServer.java
    public String getPathSpec() {
//        return CorePlugin.servletAlias( this );
    	return "/wms";
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

        public void destroy() throws IOException {
//            LogFactory.release( cl );
            cl.close();
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
            log.debug( "getContextPath(): result=" + getPathSpec() );
            return getPathSpec();
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

        // Servlet API version 2.3 ?
                
        public int getEffectiveMajorVersion() {
            return delegate.getEffectiveMajorVersion();
        }

        public int getEffectiveMinorVersion() {
            return delegate.getEffectiveMinorVersion();
        }

        public boolean setInitParameter( String name, String value ) {
            return delegate.setInitParameter( name, value );
        }

        public Dynamic addServlet( String servletName, String className ) {
            return delegate.addServlet( servletName, className );
        }

        public Dynamic addServlet( String servletName, Servlet servlet ) {
            return delegate.addServlet( servletName, servlet );
        }

        public Dynamic addServlet( String servletName, Class<? extends Servlet> servletClass ) {
            return delegate.addServlet( servletName, servletClass );
        }

        public <T extends Servlet> T createServlet( Class<T> clazz ) throws ServletException {
            return delegate.createServlet( clazz );
        }

        public ServletRegistration getServletRegistration( String servletName ) {
            return delegate.getServletRegistration( servletName );
        }

        public Map<String, ? extends ServletRegistration> getServletRegistrations() {
            return delegate.getServletRegistrations();
        }

        public javax.servlet.FilterRegistration.Dynamic addFilter( String filterName, String className ) {
            return delegate.addFilter( filterName, className );
        }

        public javax.servlet.FilterRegistration.Dynamic addFilter( String filterName, Filter filter ) {
            return delegate.addFilter( filterName, filter );
        }

        public javax.servlet.FilterRegistration.Dynamic addFilter( String filterName,
                Class<? extends Filter> filterClass ) {
            return delegate.addFilter( filterName, filterClass );
        }

        public <T extends Filter> T createFilter( Class<T> clazz ) throws ServletException {
            return delegate.createFilter( clazz );
        }

        public FilterRegistration getFilterRegistration( String filterName ) {
            return delegate.getFilterRegistration( filterName );
        }

        public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
            return delegate.getFilterRegistrations();
        }

        public SessionCookieConfig getSessionCookieConfig() {
            return delegate.getSessionCookieConfig();
        }

        public void setSessionTrackingModes( Set<SessionTrackingMode> sessionTrackingModes ) {
            delegate.setSessionTrackingModes( sessionTrackingModes );
        }

        public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
            return delegate.getDefaultSessionTrackingModes();
        }

        public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
            return delegate.getEffectiveSessionTrackingModes();
        }

        public void addListener( String className ) {
            delegate.addListener( className );
        }

        public <T extends EventListener> void addListener( T t ) {
            delegate.addListener( t );
        }

        public void addListener( Class<? extends EventListener> listenerClass ) {
            delegate.addListener( listenerClass );
        }

        public <T extends EventListener> T createListener( Class<T> clazz ) throws ServletException {
            return delegate.createListener( clazz );
        }

        public JspConfigDescriptor getJspConfigDescriptor() {
            return delegate.getJspConfigDescriptor();
        }

        public ClassLoader getClassLoader() {
            return delegate.getClassLoader();
        }

        public void declareRoles( String... roleNames ) {
            delegate.declareRoles( roleNames );
        }
        
    }

}
