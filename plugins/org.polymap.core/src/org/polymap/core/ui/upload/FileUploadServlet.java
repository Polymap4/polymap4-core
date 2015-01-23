/* 
 * polymap.org
 * Copyright (C) 2013, Falko Bräutigam. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.core.ui.upload;

import java.util.Map;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import com.google.common.collect.MapMaker;

import sun.misc.IOUtils;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class FileUploadServlet
        extends HttpServlet {

    private static Log log = LogFactory.getLog( FileUploadServlet.class );

    private static Map<String,IUploadHandler>    handlers = new MapMaker().concurrencyLevel( 2 ).softValues().makeMap();
    
    
    /**
     * Registers the given upload handler with the global upload servlet.
     * 
     * @param handler The handler to register. The handler is stored in a soft reference.
     * @return The URL to use for the given handler.
     */
    public static String addUploadHandler( IUploadHandler handler ) {
        String key = String.valueOf( handler.hashCode() );
        handlers.put( key, handler );
        return "fileupload?handler=" + key;
    }
    
    
    public static boolean removeUploadHandler( IUploadHandler handler ) {
        String key = String.valueOf( handler.hashCode() );
        return handlers.remove( key ) != null;
    }
    
    
    // instance *******************************************
    
    private ServletFileUpload   fileUpload = new ServletFileUpload();
    

    @Override
    protected void doPost( HttpServletRequest req, HttpServletResponse resp ) 
            throws ServletException, IOException {
        try {
            FileItemIterator it = fileUpload.getItemIterator( req );
            while (it.hasNext()) {
                FileItemStream item = it.next();
                String name = item.getFieldName();
                InputStream in = item.openStream();
                try {
                    if (item.isFormField()) {
                        log.info( "Form field " + name + " with value " + Streams.asString( in ) + " detected.");
                    } 
                    else {
                        log.info( "File field " + name + " with file name " + item.getName() + " detected.");
                        
                        String key = req.getParameter( "handler" );
                        assert key != null;
                        IUploadHandler handler = handlers.get( key );
                        // for the upload field we always get just one item (which has the length of the request!?)
                        int length = req.getContentLength();
                        handler.uploadStarted( item.getName(), item.getContentType(), length, in );
                    }
                }
                finally {
                    IOUtils.closeQuietly( in );
                }
            }
        }
        catch (Exception e) {
            log.warn( "", e );
            throw new RuntimeException( e );
        }
    }

    
}
