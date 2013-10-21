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
import java.util.concurrent.ConcurrentHashMap;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class FileUploadServlet
        extends HttpServlet {

    private static Log log = LogFactory.getLog( FileUploadServlet.class );

    private static Map<String,IUploadHandler>    handlers = new ConcurrentHashMap();
    
    
    public static String addUploadHandler( IUploadHandler handler ) {
        String key = String.valueOf( handler.hashCode() );
        handlers.put( key, handler );
        return "fileupload?handler=" + key;
    }
    
    public static boolean removeUploadHandler( IUploadHandler handler ) {
        String key = String.valueOf( handler.hashCode() );
        boolean result = handlers.remove( key ) != null;
        //assert result : "Removing Upload handler failed.";
        return result;
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
                if (item.isFormField()) {
                    log.info( "Form field " + name + " with value " + Streams.asString( in ) + " detected.");
                } 
                else {
                    log.info( "File field " + name + " with file name " + item.getName() + " detected.");
                    
                    String key = req.getParameter( "handler" );
                    assert key != null;
                    IUploadHandler handler = handlers.get( key );
                    handler.uploadStarted( item.getName(), item.getContentType(), in );
                }
            }
        }
        catch (Exception e) {
            log.warn( "", e );
            throw new RuntimeException( e );
        }
    }

    
}
