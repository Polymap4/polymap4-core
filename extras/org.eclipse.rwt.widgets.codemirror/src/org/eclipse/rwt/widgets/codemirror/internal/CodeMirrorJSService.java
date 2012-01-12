/*
 * polymap.org
 * Copyright 2011, Falko Bräutigam, and other contributors as indicated by
 * the @authors tag.
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */
package org.eclipse.rwt.widgets.codemirror.internal;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.rwt.RWT;
import org.eclipse.rwt.internal.util.URLHelper;
import org.eclipse.rwt.service.IServiceHandler;
import org.eclipse.rwt.widgets.codemirror.CodeMirrorPlugin;

/**
 * Provides JavaScript and CSS files from the codemirror.zip. 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
@SuppressWarnings("restriction")
public class CodeMirrorJSService
        implements IServiceHandler {

    private static Log log = LogFactory.getLog( CodeMirrorJSService.class );

    private static final String     SERVICE_HANDLER_ID = CodeMirrorJSService.class.getSimpleName();
    
    private static final String     RESOURCE_BASE = "codemirror";

    private static final String     FILENAME_PARAM = "res";

    
    /**
     * Registers this service handler. This method should be called only once.
     */
    public static void register() {
        CodeMirrorJSService instance = new CodeMirrorJSService();
        RWT.getServiceManager().registerServiceHandler( SERVICE_HANDLER_ID, instance );
    }

    /**
     * Builds a url which points to the service handler and encodes the given parameters
     * as url parameters. 
     */
    public static String getBaseUrl() {
        StringBuffer url = new StringBuffer();
        url.append( URLHelper.getURLString() );

        URLHelper.appendFirstParam( url, REQUEST_PARAM, SERVICE_HANDLER_ID );

        // convert to relative URL
        int firstSlash = url.indexOf( "/" , url.indexOf( "//" ) + 2 ); // first slash after double slash of "http://"
        url.delete( 0, firstSlash + 1 ); // Result is sth like "/rap?custom_service_handler..."
        return RWT.getResponse().encodeURL( url.toString() );
    }

    
    // instance *******************************************
    
    public void service()
    throws IOException, ServletException {
        // request
        final HttpServletRequest request = RWT.getRequest();
        log.debug( "Request: " + request.getPathInfo() );
        String filename = request.getParameter( FILENAME_PARAM );

        // response
        final HttpServletResponse response = RWT.getResponse();
        final PrintWriter out = response.getWriter();
        
        if (filename.endsWith( ".js" )) {
            response.setContentType( "text/javascript" );
        }
        else if (filename.endsWith( ".css" )) {
            response.setContentType( "text/css" );
        }
        response.setHeader( "Cache-Control", "no-cache" );
        response.setBufferSize( 4096 );

        // copy resource to response out
        URL res = CodeMirrorPlugin.getDefault().getBundle().getResource( RESOURCE_BASE + "/" + filename );
        InputStream in = null;
        try {
            in = new BufferedInputStream( res.openStream() );
            for (int c=in.read(); c != -1; c=in.read()) {
                out.write( c );
            }
        }
        finally {
            if (in != null) {
                in.close();
            }
        }
    }
    
}
