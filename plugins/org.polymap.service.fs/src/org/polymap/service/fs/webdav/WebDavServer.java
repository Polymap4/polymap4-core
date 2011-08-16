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

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bradmcevoy.http.HttpManager;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.ResourceFactory;
import com.bradmcevoy.http.Response;
import com.ettrema.http.fs.FileSystemResourceFactory;
import com.ettrema.http.fs.NullSecurityManager;

import org.polymap.service.http.WmsService;

/**
 * HTTP servlet based on Milton.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class WebDavServer
        extends WmsService {

    private static Log log = LogFactory.getLog( WebDavServer.class );
    
    private static final ThreadLocal<HttpServletRequest>  originalRequest  = new ThreadLocal<HttpServletRequest>();

    private static final ThreadLocal<HttpServletResponse> originalResponse = new ThreadLocal<HttpServletResponse>();

    private static final ThreadLocal<ServletConfig>       tlServletConfig  = new ThreadLocal<ServletConfig>();


    public static HttpServletRequest request() {
        return originalRequest.get();
    }

    public static HttpServletResponse response() {
        return originalResponse.get();
    }

    /**
     * Make the servlet config available to any code on this thread.
     */
    public static ServletConfig servletConfig() {
        return tlServletConfig.get();
    }

    
    // instance *******************************************
    
    private ServletConfig               config;

    private HttpManager                 httpManager;
    
    private ResourceFactory             resourceFactory;


    public void init( ServletConfig _config )
    throws ServletException {
        log.info( "WebDAV Server: " + _config.getServletContext().getContextPath() );
        try {
            this.config = _config;
            
            this.resourceFactory = new FileSystemResourceFactory( new File( "/home/falko/packages" ), new NullSecurityManager(), "webdav" );
            this.httpManager = new HttpManager( resourceFactory );
        }
        catch (Throwable ex) {
            log.error( "Exception while starting", ex );
            throw new RuntimeException( ex );
        }
    }
    
    
    public void destroy() {
    }


    public void service( ServletRequest servletRequest, ServletResponse servletResponse ) 
    throws ServletException, IOException {
        HttpServletRequest req = (HttpServletRequest)servletRequest;
        HttpServletResponse resp = (HttpServletResponse)servletResponse;
        log.info( "Request: " + req.getPathInfo() );
        try {
            Request request = new com.bradmcevoy.http.ServletRequest( req );
            Response response = new com.bradmcevoy.http.ServletResponse( resp );
            httpManager.process( request, response );
        }
        finally {
            servletResponse.getOutputStream().flush();
            servletResponse.flushBuffer();
        }
    }
    
    
//    /*
//     * 
//     */
//    class TestResourceFactory
//            implements ResourceFactory {
//
//        public Resource getResource( String host, String path ) {
//            log.info( "WebDav Request: " + path );
//        }
//        
//    }
    
}
