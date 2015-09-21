/* 
 * polymap.org
 * Copyright 2011, Polymap GmbH. All rights reserved.
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
package org.polymap.service.fs.webdav;

import io.milton.config.HttpManagerBuilder;
import io.milton.http.Auth;
import io.milton.http.Filter;
import io.milton.http.HttpManager;
import io.milton.http.Request;
import io.milton.http.ResourceFactory;
import io.milton.http.Response;
import io.milton.http.SecurityManager;

import java.util.List;
import java.util.Locale;

import java.io.IOException;
import java.security.Principal;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.runtime.Timer;
import org.polymap.core.runtime.session.DefaultSessionContextProvider;
import org.polymap.core.runtime.session.ISessionListener;
import org.polymap.core.runtime.session.SessionContext;

import org.polymap.service.fs.ContentManager;
import org.polymap.service.fs.FsPlugin;

/**
 * WebDAV server/servlet based on Milton.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class WebDavServer
        extends HttpServlet {

    private static Log log = LogFactory.getLog( WebDavServer.class );
    
    private static final String                     NULL_USER_NAME = "null";
    
    private static final ThreadLocal<Request>       threadRequest  = new ThreadLocal<Request>();

    private static final ThreadLocal<Response>      threadResponse = new ThreadLocal<Response>();

    private static final ThreadLocal<ServletConfig> threadServletConfig  = new ThreadLocal<ServletConfig>();


    public static Request request() {
        return WebDavServer.threadRequest.get();
    }

    public static Response response() {
        return WebDavServer.threadResponse.get();
    }

    /**
     * Make the servlet config available to any code on this thread.
     */
    public static ServletConfig servletConfig() {
        return threadServletConfig.get();
    }

    
    // instance *******************************************
    
    private ServletConfig               config;

    private HttpManager                 httpManager;
    
    private ResourceFactory             resourceFactory;
    
    private SecurityManager             securityManager;


    public void init( ServletConfig _config )
    throws ServletException {
        super.init( _config );
        log.info( "WebDAV Server: " + _config.getServletContext().getContextPath() );
        try {
            this.config = _config;
            
            securityManager = new SecurityManagerAdapter( "Polymap4 WebDAV" );
            
            HttpManagerBuilder builder = new HttpManagerBuilder();
            resourceFactory = new WebDavResourceFactory( securityManager, "webdav" );
            builder.setResourceFactory( resourceFactory );
            
//            AuthenticationService authService = new AuthenticationService();
//            builder.setAuthenticationService( authService );
            
            // Milton2 seems to handle 304 correctly by itself
//            BalkonCacheControl cacheControl = new BalkonCacheControl( false );
//            BalkonWebDavResponseHandler defaultHandler = new BalkonWebDavResponseHandler( authService );
//            defaultHandler.setCacheControl( cacheControl );

            // compression should be done on an other layer
//            CompressingResponseHandler compressHandler = 
//                    new CompressingResponseHandler( defaultHandler );
//            compressHandler.setMaxMemorySize( 1024*1024 );
//            compressHandler.setCacheControlHelper( cacheControl );
//            builder.setResourceHandlerHelper( compressHandler ); 

            builder.init();
            List<Filter> filters = builder.getFilters();
            filters.add( 0, new PreAuthenticationFilter( builder.getHttp11ResponseHandler(), securityManager ) );
            httpManager = builder.buildHttpManager();
        }
        catch (Throwable ex) {
            log.error( "Exception while starting", ex );
            throw new RuntimeException( ex );
        }
    }
    
    
    public void destroy() {
        httpManager = null;
        //resourceFactory.dispose();
        resourceFactory = null;
        securityManager = null;
    }


    public void service( ServletRequest servletRequest, ServletResponse servletResponse ) 
    throws ServletException, IOException {
        Timer timer = new Timer();
        HttpServletRequest req = (HttpServletRequest)servletRequest;
        HttpServletResponse resp = (HttpServletResponse)servletResponse;
        
        DefaultSessionContextProvider contextProvider = FsPlugin.getDefault().sessionContextProvider;
        
        try {
            Request request = new io.milton.servlet.ServletRequest( req, servletRequest.getServletContext() );
            Response response = new io.milton.servlet.ServletResponse( resp );
            threadRequest.set( request );
            threadResponse.set( response );

            Auth auth = request.getAuthorization();
            log.debug( "Auth: " + auth );
            
            // map/create session context
            req.getSession( true );
            if (auth != null) {
                contextProvider.mapContext( auth.getUser(), true );
                log.debug( "SessionContext: " + SessionContext.current() );
            }

            httpManager.process( request, response );
            log.debug( "Request: " + Request.Header.ACCEPT_ENCODING.code + ": " + request.getHeaders().get( Request.Header.ACCEPT_ENCODING.code ) +
                    " --> Response: " + Response.Header.CONTENT_ENCODING.code + ": " + response.getHeaders().get( Response.Header.CONTENT_ENCODING.code ) );
            log.debug( "Response: " + response.getStatus() );
        }
        finally {
            if (contextProvider.currentContext() != null) {
                contextProvider.unmapContext();
            }

            threadRequest.set( null );
            threadResponse.set( null );
            
            servletResponse.getOutputStream().flush();
            servletResponse.flushBuffer();
        }
        log.info( "WebDAV request: " + timer.elapsedTime() + "ms" );
    }

    
    /**
     * Initializes a new session for the given user.
     * <p/>
     * This method is called by {@link SecurityManagerAdapter}
     * 
     * @param user
     * @return The specified user.
     */
    public static Principal createNewSession( final Principal user ) {
        HttpServletRequest req = io.milton.servlet.ServletRequest.getRequest();
        final HttpSession session = req.getSession();
  
        // HTTP session timeout: 30min
        session.setMaxInactiveInterval( 30*60 );
        
        FsPlugin.getDefault().sessionContextProvider.mapContext( user.getName(), true );
        final SessionContext sessionContext = SessionContext.current();
        
        // ContentManager
        Locale locale = req.getLocale();
        sessionContext.setAttribute( "contentManager", 
                ContentManager.forUser( user.getName(), locale, sessionContext ) ); 
        
        // invalidate HTTP session when context is destroyed
        sessionContext.addSessionListener( new ISessionListener() {
            public void beforeDestroy() {
                log.info( "SessionContext is destroyed -> invalidating HTTP session" );
                try {
                    //sessionContext.removeSessionListener( this );
                    session.invalidate();
                }
                catch (Exception e) {
                    log.warn( "HTTP session already invalidated: " + e );
                }
            }
        });
        // session destroy listener
        session.setAttribute( "sessionListener", new HttpSessionBindingListener() {
            public void valueBound( HttpSessionBindingEvent ev ) {
            }
            public void valueUnbound( HttpSessionBindingEvent ev ) {
                //
                sessionContext.execute( new Runnable() {
                    public void run() {
                        ContentManager.releaseSession( user.getName() );
                    }
                });
                // prevent life-lock
                if (!sessionContext.isDestroyed() && sessionContext.getAttribute( "destroying" ) == null) {
                    sessionContext.setAttribute( "destroying", true );
                    FsPlugin.getDefault().sessionContextProvider.destroyContext(
                            sessionContext.getSessionKey() );
                    log.info( "HTTP Session destroyed: " + session.getId() + ", user: " + user );
                }
            }
        });
        log.info( "New HTTP session: " + session.getId() + ", user: " + user );
        return user;
    }

}
