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

import static com.google.common.base.Throwables.propagateIfPossible;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.servlet.DispatcherServlet;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.data.pipeline.DepthFirstStackExecutor;
import org.polymap.core.data.pipeline.Pipeline;
import org.polymap.core.data.pipeline.PipelineExecutor;
import org.polymap.core.data.pipeline.PipelineProcessor;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.IMap;
import org.polymap.core.runtime.Stringer;
import org.polymap.core.runtime.cache.Cache;
import org.polymap.core.runtime.cache.CacheConfig;
import org.polymap.core.runtime.session.SessionContext;

import org.polymap.service.geoserver.spring.PipelineMapResponse;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class GeoServerServlet
        extends HttpServlet {

    private static final Log log = LogFactory.getLog( GeoServerServlet.class );

    /** First, hackish attempt to pass info to GeoServerLoader inside Spring. */
    public static final ThreadLocal<GeoServerServlet>   instance = new ThreadLocal<GeoServerServlet>();
    
    /**
     * XXX Bad hack. I just don't find the right way through GeoServer code
     * to get HTTP response in a {@link PipelineMapResponse}.
     */
    public static final ThreadLocal<HttpServletResponse> response = new ThreadLocal<HttpServletResponse>();
    
    private List<ServletContextListener>    loaders = new ArrayList();
    
    private DispatcherServlet               dispatcher;
    
    private BundleServletContext            servletContext;
    
    private File                            dataDir;
    
    private Cache<String,Pipeline>          pipelines;

    public String                           alias;

    public IMap                             map;

    private ServiceContext2                 session;
    

    public GeoServerServlet( String alias ) {
        this.alias = alias;
        this.pipelines = CacheConfig.defaults().initSize( 128 ).createCache();
        this.session = new ServiceContext2( alias );
    }

    /**
     * Returns the {@link IMap} to be published by this servlet. This method is
     * called within the proper {@link SessionContext} of the servlet.
     *
     * @return
     */
    protected abstract IMap createMap();
    
    /**
     * Actually creates a new {@link Pipeline} for the layer with the given name and
     * usecase. Caching of the result is done by the caller.
     * <p/>
     * This is called inside the servlet request thread.
     * @throws Exception 
     */
    protected abstract Pipeline createPipeline( ILayer layer, Class<? extends PipelineProcessor> usecase )
            throws Exception;

    
    public abstract String createSLD( ILayer layer );


    public Pipeline getOrCreatePipeline( final ILayer layer, Class<? extends PipelineProcessor> usecase ) 
            throws Exception {
        try {
            String key = layer.id().toString() + usecase.getSimpleName();
            return pipelines.get( key, k -> {
                return createPipeline( layer, usecase );
            });
        }
        catch (ExecutionException e) {
            throw (Exception)e.getCause();
        }
    }

    
    /**
     * This default implementation creates {@link DepthFirstStackExecutor}. Override
     * this to change the executor to use.
     *
     * @return Newly created {@link PipelineExecutor}.
     */
    public PipelineExecutor createPipelineExecutor() {
        return new DepthFirstStackExecutor();
    }

        
    @Override
    public void init( ServletConfig config ) throws ServletException {
        super.init( config );
        
        servletContext = new BundleServletContext( getServletContext() );
        log.info( "initGeoServer(): contextPath=" + servletContext.getContextPath() );

        try {
            session.execute( () -> initGeoServer() );
        }
        catch (Exception e) {
            propagateIfPossible( e, ServletException.class );
            throw new ServletException( e );
        }
    }


    public void destroy() {
        log.debug( "destroy(): ..." );
        super.destroy();
        if (dispatcher != null) {
            session.execute( () -> {
                try {
                    // listeners
                    ServletContextEvent ev = new ServletContextEvent( servletContext );
                    for (ServletContextListener loader : loaders) {
                        loader.contextDestroyed( ev );
                    }
                    loaders.clear();

                    // dispatcher
                    dispatcher.destroy();
                    dispatcher = null;

                    servletContext.destroy();
                    servletContext = null;
                }
                catch (IOException e) {
                    log.warn( "", e );
                }
            });
            session.destroy();
            session = null;
        }
    }


    protected void initGeoServer() throws Exception {
        this.map = createMap();
        
        File cacheDir = GeoServerPlugin.instance().getCacheDir();
        dataDir = new File( cacheDir, Stringer.of( mapLabel() ).toFilename( "_" ).toString() );
        log.debug( "    dataDir=" + dataDir.getAbsolutePath() );
        dataDir.mkdirs();
        // TODO: have to create styles folder here, as otherwise 
        // org.geoserver.wms.WMSLifecycleHandler.loadFontsFromDataDirectory() 
        // will get null at data.findStyleDir() there
        // before such a code was executed in GeoServerLoader when creating a style 
        // file for each layer
        String stylesPath = dataDir.getAbsolutePath() + "/data/styles/dummy.sld";
        File styleFile = new File( stylesPath );
        styleFile.mkdirs();
        //FileUtils.forceDeleteOnExit( dataDir );

        // web.xml
        servletContext.setAttribute( "serviceStrategy", "SPEED" );
        servletContext.setAttribute( "contextConfigLocation", "classpath*:/applicationContext.xml classpath*:/applicationSecurityContext.xml" );
        servletContext.setAttribute( "enableVersioning", "false" );
        servletContext.setAttribute( "GEOSERVER_DATA_DIR", dataDir.getAbsoluteFile() );

        try {
            instance.set( this );

            //loaders.add( new LoggingStartupContextListener() );
            loaders.add( new ContextLoaderListener() );

            ServletContextEvent ev = new ServletContextEvent( servletContext );
            for (Object loader : loaders) {
                ((ServletContextListener)loader).contextInitialized( ev );
            }
        }
        finally {
            instance.set( null );
        }

        dispatcher = new DispatcherServlet();
        log.debug( "Dispatcher: " + dispatcher.getClass().getClassLoader() );
        dispatcher.init( new ServletConfig() {
            @Override
            public String getInitParameter( String name ) {
                return GeoServerServlet.this.getInitParameter( name );
            }
            @Override
            public Enumeration<String> getInitParameterNames() {
                return GeoServerServlet.this.getInitParameterNames();
            }
            @Override
            public ServletContext getServletContext() {
                return servletContext;
            }
            @Override
            public String getServletName() {
                return "dispatcher";
            }
        });
    }
    
    
    protected String mapLabel() {
    	return map.label.get();
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
                try (
                    InputStream in = res.openStream();
                    OutputStream out = resp.getOutputStream();
                ){
                    IOUtils.copy( in, out );
                    out.flush();
                    resp.flushBuffer();
                }
            }
            else {
                log.warn( "No such resource found: " + resName );
            }
            return;
        }
        
        String origin = req.getHeader( "Origin" );
        resp.addHeader( "Access-Control-Allow-Origin", StringUtils.isBlank( origin ) ? "*" : origin );
        resp.addHeader( "Access-Control-Allow-Headers", req.getHeader( "Access-Control-Request-Headers" ));
        resp.addHeader( "Access-Control-Allow-Methods", "GET,POST,OPTIONS" );
        
        // service
        session.execute( () -> {
            try {
                instance.set( this );
                response.set( resp );
                dispatcher.service( req, resp );
            }
            catch (ServletException e) {
                throw e;
            }
            catch (IOException e) {
                throw new RuntimeException( e );
            }
            finally {
                response.set( null );
                instance.set( null );
            }
        });
    }


    /**
     * A {@link ServletContext} facade to support GeoServer by given it
     * proper resources, real path, mime.
     */
    @SuppressWarnings("deprecation")
    protected class BundleServletContext
            implements ServletContext {
     
        private ServletContext          delegate;
        
        /** 
         * {@link GeoServerClassLoader} if separate class loading is enabled, or
         * the ordinary loader of the Bundle. 
         */
        private ClassLoader             cl;

        
        protected BundleServletContext( ServletContext delegate ) {
            super();
            assert delegate != null;
            this.delegate = delegate;
            ClassLoader bundleLoader = GeoServerPlugin.class.getClassLoader();
            log.debug( "ClassLoader: " + bundleLoader );
            this.cl = bundleLoader;  // new GeoServerClassLoader( bundleLoader );
            log.info( "ClassLoader: " + cl );
        }

        public void destroy() throws IOException {
//            LogFactory.release( cl );
            if (cl instanceof GeoServerClassLoader) {
                ((GeoServerClassLoader)cl).close();
            }
            cl = null;
            delegate = null;
        }

        public Object getAttribute( String name ) {
            return delegate.getAttribute( name );
        }

        public Enumeration<String> getAttributeNames() {
            return delegate.getAttributeNames();
        }

        public ServletContext getContext( String uripath ) {
            return delegate.getContext( uripath );
        }

        public String getContextPath() {
            return alias;
        }

        public String getInitParameter( String name ) {
            return delegate.getInitParameter( name );
        }

        public Enumeration<String> getInitParameterNames() {
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

        public Set<String> getResourcePaths( String path ) {
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

        public Enumeration<String> getServletNames() {
            return delegate.getServletNames();
        }

        public Enumeration<Servlet> getServlets() {
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
