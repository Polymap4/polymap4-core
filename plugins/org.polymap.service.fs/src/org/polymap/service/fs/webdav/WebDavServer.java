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

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.AuthenticationService;
import com.bradmcevoy.http.CompressingResponseHandler;
import com.bradmcevoy.http.HttpManager;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.ResourceFactory;
import com.bradmcevoy.http.Response;
import com.bradmcevoy.http.SecurityManager;

import org.polymap.core.runtime.DefaultSessionContextProvider;
import org.polymap.core.runtime.ISessionListener;
import org.polymap.core.runtime.SessionContext;
import org.polymap.core.runtime.Timer;

import org.polymap.service.fs.ContentManager;
import org.polymap.service.fs.FsPlugin;

/**
 * WebDAV server/servlet based on Milton.
 *
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
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
            
//            Map<String,String> users = new HashMap();
//            users.put( "admin", "admin" );
//            users.put( "falko", "." );
            securityManager = new SecurityManagerAdapter( "POLYMAP3 WebDAV" );
            
            resourceFactory = new WebDavResourceFactory( securityManager, "webdav" );
            
            AuthenticationService authService = new AuthenticationService();
            BalkonCacheControl cacheControl = new BalkonCacheControl( false );
            
            BalkonWebDavResponseHandler defaultHandler = new BalkonWebDavResponseHandler( authService );
            defaultHandler.setCacheControl( cacheControl );

            CompressingResponseHandler compressHandler = 
                    new CompressingResponseHandler( defaultHandler );
            compressHandler.setMaxMemorySize( 1024*1024 );
            compressHandler.setCacheControlHelper( cacheControl );

            httpManager = new HttpManager( resourceFactory, compressHandler, authService );
            httpManager.addFilter( 0, new PreAuthenticationFilter( 
                    httpManager.getResponseHandler(), securityManager ) );
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
            Request request = new com.bradmcevoy.http.ServletRequest( req );
            Response response = new com.bradmcevoy.http.ServletResponse( resp );
            threadRequest.set( request );
            threadResponse.set( response );

            Auth auth = request.getAuthorization();
            log.debug( "Auth: " + auth );
            
            // map/create session context
            final HttpSession session = req.getSession( true );
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
        HttpServletRequest req = com.bradmcevoy.http.ServletRequest.getRequest();
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
