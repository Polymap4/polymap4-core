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
package org.polymap.core.data.operation;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.rwt.RWT;
import org.eclipse.rwt.internal.util.URLHelper;
import org.eclipse.rwt.service.IServiceHandler;

import org.polymap.core.runtime.ConcurrentReferenceHashMap;
import org.polymap.core.runtime.ConcurrentReferenceHashMap.ReferenceType;

/**
 * General download service for feature operation results and other content.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
@SuppressWarnings("restriction")
public class DownloadServiceHandler
        implements IServiceHandler {

    private static Log log = LogFactory.getLog( DownloadServiceHandler.class );

    private static final String         SERVICE_HANDLER_ID = "org.polymap.core.data.DownloadServiceHandler";

    private static final String         ID_REQUEST_PARAM = "id";

    static ConcurrentReferenceHashMap<String,ContentProvider>   providers 
            = new ConcurrentReferenceHashMap( ReferenceType.STRONG, ReferenceType.SOFT );


//    static {
//        DownloadServiceHandler instance = new DownloadServiceHandler();
//        RWT.getServiceManager().registerServiceHandler( SERVICE_HANDLER_ID, instance );
//    }

    
    /**
     * Register the given provider for downloading.
     * 
     * @param id
     * @param provider
     * @return The download URL for the given provider.
     */
    public static String registerContent( ContentProvider provider ) {
        String id = String.valueOf( provider.hashCode() );

        providers.put( id, provider );
        
        // XXX code from RAP; its a bit confusing, don't like it but it works
        StringBuffer url = new StringBuffer();
        url.append( URLHelper.getURLString( false ) );

        URLHelper.appendFirstParam( url, REQUEST_PARAM, SERVICE_HANDLER_ID );
        URLHelper.appendParam( url, ID_REQUEST_PARAM, id );

        // convert to relative URL
        int firstSlash = url.indexOf( "/" , url.indexOf( "//" ) + 2 ); // first slash after double slash of "http://"
        url.delete( 0, firstSlash + 1 ); // Result is sth like "/rap?custom_service_handler..."
        return RWT.getResponse().encodeURL( url.toString() );
    }
    

    /**
     * 
     */
    public interface ContentProvider {
        
        public String getFilename();
        
        /**
         * The content type. For example: "text/csv; charset=ISO-8859-1"
         */
        public String getContentType();


        /**
         * Get the content of this provider. The returned InputStream is savely
         * closed by the caller.
         * 
         * @return Newly created InputStream.
         */
        public InputStream getInputStream() throws Exception;
        
        public void done( boolean success );
    }
    
    
    // instance *******************************************
    
    public DownloadServiceHandler() {
    }


    public void service()
    throws IOException, ServletException {
        HttpServletRequest request = RWT.getRequest();
        HttpServletResponse response = RWT.getResponse();
        
        String pathInfo = request.getPathInfo();
        log.info( "Request: " + pathInfo );
        
        try {
            // download.html
            if (pathInfo != null && pathInfo.startsWith( "/download.html" )) {
                // sending an HTML page helps debugging on IE, which often blocks or
                // otherwise fails to download directly
                String id = request.getParameter( ID_REQUEST_PARAM );
                String filename = request.getParameter( "filename" );
                String linkTarget = "../download/" 
                        + (filename != null ? filename : "polymap3_export.tmp")
                        + "?id=" + id;

                response.setContentType( "text/html; charset=ISO-8859-1" );

                PrintWriter out = response.getWriter();
                out.println( "<html><head>" );
                out.println( "<meta HTTP-EQUIV=\"REFRESH\" content=\"0; url=" + linkTarget + "\">" );
                out.println( "</head>" );
                out.println( "<a href=\"" + linkTarget + "\">Download starten</a>" );
                out.println( "</html>" );
                out.flush();
            }

            // download
            else {
                String id = request.getParameter( ID_REQUEST_PARAM );
                log.info( "Request: id=" + id );

                ContentProvider provider = providers.remove( id );
                
                String[] pathInfos = StringUtils.split( request.getPathInfo(), "/" );
                String filename = provider.getFilename();

                response.setContentType( provider.getContentType() );
                response.setHeader( "Content-disposition", "attachment; filename=" + filename );
                response.setHeader( "Pragma", "public" );
                response.setHeader( "Cache-Control", "must-revalidate, post-check=0, pre-check=0" );
                response.setHeader( "Cache-Control", "public" );
                response.setHeader( "Expires", "0" );
                
                InputStream in = provider.getInputStream();
                ServletOutputStream out = response.getOutputStream();
                try {
                    IOUtils.copy( in, out );
                    out.flush();
                    response.flushBuffer();
                    
                    provider.done( true );
                }
                catch (Exception e) {
                    provider.done( false );
                }
                finally {
                    IOUtils.closeQuietly( in );
                }
            }
        }
        catch (Exception e) {
            log.debug( "", e );
            throw new ServletException( e );
        }
    }
    
}
