/*                                                                                           
 * polymap.org                                                                               
 * Copyright 2012, Polymap GmbH, and individual contributors as indicated                    
 * by the @authors tag.                                                                      
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
package org.polymap.core.workbench.dnd;

import java.util.ArrayList;
import java.util.List;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import sun.misc.IOUtils;

/**
 * This servlet handles DnD upload requests.
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @since 3.1
 */
public class DndUploadServlet
        extends HttpServlet {

    private static final Log  log = LogFactory.getLog( DndUploadServlet.class );

    private static final String     CONTEXT_TEMP_DIR = "javax.servlet.context.tempdir";

    private static DndUploadServlet instance = null;
    
    public static DndUploadServlet instance() {
        assert instance != null;
        return instance;
    }
    
    public static File getUploadTempDir( final HttpSession session ) {
        ServletContext context = session.getServletContext();
        StringBuffer path = new StringBuffer();
        File tempDir = (File)context.getAttribute( CONTEXT_TEMP_DIR );
        File result = new File( tempDir.getAbsolutePath(), session.getId() );
        if (!result.exists()) {
            result.mkdir();
        }
        return result;
    }


    // instance *******************************************
    
    private ListMultimap<String,DesktopDropEvent> uploads = ArrayListMultimap.create();
    
    
    public DndUploadServlet() throws Exception {
        assert instance == null;
        instance = this;
        log.info( "initialized." );
    }
    
    
    public List<DesktopDropEvent> uploads( HttpSession session ) {
        assert session != null;
        ArrayList result = new ArrayList( uploads.get( session.getId() ) );
        uploads.clear();
        return result;
    }

    
    public void doPost( HttpServletRequest request, HttpServletResponse response )
    throws ServletException, IOException {
        log.debug( "POST-Request: " + request.getContentType() + ", length: " + request.getContentLength() );
        OutputStream fout = null;
        try {
            // copy content into temp file
            final String contentType = request.getContentType();
            File dir = getUploadTempDir( request.getSession() );
            final String filename = request.getHeader( "X-Filename" );
            assert filename != null;
            final File f = new File( dir, filename + "_" + System.currentTimeMillis() ); 
            fout = new FileOutputStream( f );                
            IOUtils.copy( request.getInputStream(), fout );
            fout.close();
            log.debug( "    uploaded: " + f.getAbsolutePath() );
            
            // create event
            final DesktopDropEvent event = new FileDropEvent() {
                public InputStream getInputStream() throws IOException {
                    return new BufferedInputStream( new FileInputStream( f ) );
                }
                public String getContentType() {
                    return contentType != null ? contentType : "";
                }
                public String getFileName() {
                    return filename;
                }
            };
            synchronized (uploads) {
                uploads.put( request.getSession().getId(), event );
            }
        }
        catch (Exception e) {
            log.error( e.getLocalizedMessage(), e );
            //throw new ServletException( e.getLocalizedMessage() );
            response.setStatus( 409 );
            response.getWriter().append( e.getLocalizedMessage() ).flush();            
            //sendError( 409, e.getLocalizedMessage() );
        }
        finally {
            IOUtils.closeQuietly( fout );
        }
    }

    
    public void doGet( HttpServletRequest request, HttpServletResponse response )
    throws ServletException, IOException {
	   	log.debug( "Request: " + request.getQueryString() );
 	}


    protected String findClientIP( HttpServletRequest request ) {
        String forwarded = request.getHeader( "X-Forwarded-For" );
        if (forwarded != null) {
            return forwarded;
        }
        else {
            return request.getRemoteHost();
        }
    }

}
