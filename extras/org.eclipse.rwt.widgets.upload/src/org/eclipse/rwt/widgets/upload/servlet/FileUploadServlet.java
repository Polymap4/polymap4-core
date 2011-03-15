/*******************************************************************************
 * Copyright (c) 2002-2007 Critical Software S.A. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html Contributors: Tiago
 * Rodrigues (Critical Software S.A.) - initial implementation Joel Oliveira
 * (Critical Software S.A.) - initial commit
 ******************************************************************************/
package org.eclipse.rwt.widgets.upload.servlet;

import java.io.*;
import java.util.Iterator;
import java.util.List;

import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.commons.fileupload.*;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

/**
 * This is a File Upload Servlet that is used with AJAX to monitor the progress
 * of the uploaded file. It will return an XML object containing the meta
 * information as well as the percent complete.
 * @deprecated is replaced by {@link FileUploadServiceHandler} and will be deleted
 * in a future version.
 */
public class FileUploadServlet extends HttpServlet implements Servlet {
  private static final long serialVersionUID = 2740693677625051632L;
  private static final String CONTEXT_TEMP_DIR
    = "javax.servlet.context.tempdir";
  private static final String XML_HEAD
    = "<?xml version=\"1.0\" encoding=\"utf-8\"?><response>";

  /**
   * Creates a new instance of the File Upload Servlet.
   */
  public FileUploadServlet() {
    super();
  }

  /**
   * Handles the GET to return the upload status.
   * 
   * @param request HTTP request.
   * @param response HTTP response.
   */
  protected void doGet( final HttpServletRequest request,
                        final HttpServletResponse response )
    throws ServletException, IOException
  {
    final PrintWriter out = response.getWriter();
    HttpSession session = request.getSession();
    FileUploadListener listener = null;
    final StringBuffer buffy = new StringBuffer( XML_HEAD );
    long bytesRead = 0;
    long contentLength = 0;
    if( session != null ) {
      // Check to see if we've created the listener object yet
      listener = ( FileUploadListener )session.getAttribute( "LISTENER" );
      response.setContentType( "text/xml" );
      response.setHeader( "Cache-Control", "no-cache" );
      if( listener != null ) {
        // Get the meta information
        bytesRead = listener.getBytesRead();
        contentLength = listener.getContentLength();
        /*
         * XML Response Code
         */
        buffy.append( "<bytes_read>" );
        buffy.append( bytesRead );
        buffy.append( "</bytes_read><content_length>" );
        buffy.append( contentLength );
        buffy.append( "</content_length>" );
        // Check to see if we're done
        if( contentLength != 0 ) {
          if( bytesRead == contentLength ) {
            buffy.append( "<finished />" );
            // No reason to keep listener in session since we're done
            session.setAttribute( "LISTENER", null );
          } else {
            // Calculate the percent complete
            buffy.append( "<percent_complete>" );
            buffy.append( ( 100 * bytesRead / contentLength ) );
            buffy.append( "</percent_complete>" );
          }
        }
      }
      buffy.append( "</response>" );
      out.println( buffy.toString() );
      out.flush();
      out.close();
    }
  }

  /**
   * Handles the POST to receive the file and saves it to the user TMP
   * directory.
   * 
   * @param request HTTP request.
   * @param response HTTP response.
   */
  protected void doPost( final HttpServletRequest request,
                         final HttpServletResponse response )
    throws ServletException, IOException
  {
    // Create file upload factory and upload servlet
    FileItemFactory factory = new DiskFileItemFactory();
    ServletFileUpload upload = new ServletFileUpload( factory );
    // Set file upload progress listener
    final FileUploadListener listener = new FileUploadListener();
    HttpSession session = request.getSession();
    session.setAttribute( "LISTENER", listener );
    // Upload servlet allows to set upload listener
    upload.setProgressListener( listener );
    FileItem fileItem = null;
    final File filePath = getUploadTempDir( session );
    try {
      // Iterate over all uploaded files
      final List uploadedItems = upload.parseRequest( request );
      final Iterator iterator = uploadedItems.iterator();
      while( iterator.hasNext() ) {
        fileItem = ( FileItem )iterator.next();
        if( !fileItem.isFormField() && fileItem.getSize() > 0 ) {
          final String myFullFileName = fileItem.getName();
          final String slashType =   myFullFileName.lastIndexOf( "\\" ) > 0
                                   ? "\\"
                                   : "/";
          final int startIndex = myFullFileName.lastIndexOf( slashType );
          // Ignore the path and get the filename
          String myFileName 
            = myFullFileName.substring( startIndex + 1,
                                        myFullFileName.length() );
          // Write the uploaded file to the system
          File file = new File( filePath, myFileName );
          fileItem.write( file );
        }
      }
    } catch( FileUploadException e ) {
      e.printStackTrace();
    } catch( final Exception e ) {
      e.printStackTrace();
    }
  }

  public static File getUploadTempDir( final HttpSession session ) {
    ServletContext context = session.getServletContext();
    StringBuffer path = new StringBuffer();
    File tempDir = ( File )context.getAttribute( CONTEXT_TEMP_DIR );
    path.append( tempDir.getAbsolutePath() );
    path.append( File.separatorChar );
    path.append( session.getId() );
    File result = new File ( path.toString() );
    if( !result.exists() ) {
      result.mkdir();
    }
    return result;
  }
}
