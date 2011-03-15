/*******************************************************************************
 * Copyright (c) 2002-2006 Innoopract Informationssysteme GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Innoopract Informationssysteme GmbH - initial API and implementation
 ******************************************************************************/

package org.eclipse.rwt.widgets.upload.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.*;

import org.apache.commons.fileupload.*;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.eclipse.rwt.RWT;
import org.eclipse.rwt.internal.util.URLHelper;
import org.eclipse.rwt.service.IServiceHandler;


/**
 * Handles file uploads and upload progress updates. 
 * <p> 
 * Implementation note: uploaded files are currently stored in the  
 * java.io.tmpdir. See 
 * {@link #handleFileUpload(HttpServletRequest, FileUploadStorageItem)} on
 * how to change this.
 * 
 * @author stefan.roeck 
 */
public class FileUploadServiceHandler implements IServiceHandler {

  private static final String REQUEST_WIDGET_ID = "widgetId";
  private static final String REQUEST_PROCESS_ID = "processId";
  private static final String XML_HEAD = "<?xml version=\"1.0\" encoding=\"utf-8\"?><response>";

  /**
   * Requests to this service handler without a valid session id are ignored for
   * security reasons. The same applies to request with widgetIds which haven't been
   * registered at the session singleton {@link FileUploadStorage}.
   */
  public void service() throws IOException, ServletException {
    
    final HttpServletRequest request = RWT.getRequest();
    final String widgetId = request.getParameter( REQUEST_WIDGET_ID );
    final String uploadProcessId = request.getParameter( REQUEST_PROCESS_ID );
    final HttpSession session = request.getSession( false );
    
    if( session != null
        && widgetId != null
        && !"".equals( widgetId )
        && uploadProcessId != null
        && !"".equals( uploadProcessId ) )
    {
      final FileUploadStorage fileUploadStorage = FileUploadStorage.getInstance();
      final FileUploadStorageItem fileUploadStorageItem = fileUploadStorage.getUploadStorageItem( widgetId );
      
      // fileUploadStorageItem can be null, if Upload widget is dispsed!
      if (ServletFileUpload.isMultipartContent(request)) {
        // Handle post-request which contains the file to upload
        handleFileUpload( request, fileUploadStorageItem, uploadProcessId );
      } else {
        // This is probably a request for updating the progress bar
        handleUpdateProgress( fileUploadStorageItem, uploadProcessId );
      }
      
    }
  }

  /**
   * Treats the request as a post request which contains the file to be
   * uploaded. Uses the apache commons fileupload library to
   * extract the file from the request, attaches a {@link FileUploadListener} to 
   * get notified about the progress and writes the file content
   * to the given {@link FileUploadStorageItem}
   * @param request Request object, must not be null
   * @param fileUploadStorageitem Object where the file content is set to.
   * If null, nothing happens.
   * @param uploadProcessId Each upload action has a unique process identifier to
   * match subsequent polling calls to get the progress correctly to the uploaded file.
   *
   */
  private void handleFileUpload( final HttpServletRequest request,
                                 final FileUploadStorageItem fileUploadStorageitem, 
                                 final String uploadProcessId )
  {
    // Ignore upload requests which have no valid widgetId
    if (fileUploadStorageitem != null && uploadProcessId != null && !"".equals( uploadProcessId )) {
      
      // Create file upload factory and upload servlet
      // You could use new DiskFileItemFactory(threshold, location)
      // to configure a custom in-memory threshold and storage location.
      // By default the upload files are stored in the java.io.tmpdir
      FileItemFactory factory = new DiskFileItemFactory();
      ServletFileUpload upload = new ServletFileUpload( factory );
      
      // Create a file upload progress listener
      final ProgressListener listener = new ProgressListener() {

        public void update( final long aBytesRead,
                            final long aContentLength,
                            final int anItem  ) {
          fileUploadStorageitem.updateProgress( aBytesRead, aContentLength );
        }
        
      };
      // Upload servlet allows to set upload listener
      upload.setProgressListener( listener );
      fileUploadStorageitem.setUploadProcessId( uploadProcessId );
      
      FileItem fileItem = null;
      try {
        final List uploadedItems = upload.parseRequest( request );
        // Only one file upload at once is supported. If there are multiple files, take
        // the first one and ignore other
        if ( uploadedItems.size() > 0 ) {
          fileItem = ( FileItem )uploadedItems.get( 0 );
          // Don't check for file size 0 because this prevents uploading new empty office xp documents
          // which have a file size of 0.
          if( !fileItem.isFormField() ) {
            fileUploadStorageitem.setFileInputStream( fileItem.getInputStream() );
            fileUploadStorageitem.setContentType(fileItem.getContentType());
          }
        }
      } catch( FileUploadException e ) {
        e.printStackTrace();
      } catch( final Exception e ) {
        e.printStackTrace();
      }
    }
  }

  /**
   * Treats the request as a get request which is triggered by the
   * browser to retrieve the progress state. Gets the registered 
   * {@link FileUploadListener} from the given {@link FileUploadStorageItem}
   * to check the current progress. The result is written to the response
   * in an XML format.
   * <br>
   * <b>Note:</b> It is important that a valid response is written in any case
   * to let the Browser know, when polling can be stopped. 
   * @param uploadProcessId 
   */
  private void handleUpdateProgress( final FileUploadStorageItem fileUploadStorageitem, String uploadProcessId )
    throws IOException
  {
    final HttpServletResponse response = RWT.getResponse();
    final PrintWriter out = response.getWriter();
    
    final StringBuffer buffy = new StringBuffer( XML_HEAD );
    long bytesRead = 0;
    long contentLength = 0;
    // Check to see if we've created the listener object yet
    response.setContentType( "text/xml" );
    response.setHeader( "Cache-Control", "no-cache" );
    
    if( fileUploadStorageitem != null ) {
      
      if ( uploadProcessId != null && uploadProcessId.equals( fileUploadStorageitem.getUploadProcessId() )) {
        
          // Get the meta information
          bytesRead = fileUploadStorageitem.getBytesRead();
          contentLength = fileUploadStorageitem.getContentLength();
          /*
           * XML Response Code
           */
          buffy.append( "<bytes_read>" );
          buffy.append( bytesRead );
          buffy.append( "</bytes_read><content_length>" );
          buffy.append( contentLength );
          buffy.append( "</content_length>" );
          // Check to see if we're done
          // Even files with a size of 0 have a content length > 0
          if( contentLength != 0 ) {
            if( bytesRead == contentLength ) {
              buffy.append( "<finished />" );
            } else {
              // Calculate the percent complete
              buffy.append( "<percent_complete>" );
              buffy.append( ( 100 * bytesRead / contentLength ) );
              buffy.append( "</percent_complete>" );
            }
          } else {
            // Contentlength should not be 0, however, send finished to make sure
            // the Browser side polling stops.
            buffy.append( "<finished />" );
          }
      } else {
        //System.out.println("No match: " + uploadProcessId + " " + fileUploadStorageitem.getUploadProcessId());
        // if the processId doesn't match, return nothing
        // which causes the client script to send another
        // request after waiting. This could happen,
        // if the first GET-request was send, before the
        // Upload-POST request arrived.
      }
      
    } else {
      // if fileUploadStorageitem is null, the upload widget is disposed
      // return "finished" to stop monitoring
      buffy.append( "<finished />" );
    }
    
    buffy.append( "</response>" );
    out.println( buffy.toString() );
    out.flush();
    out.close();
  }

  /**
   * Registers this service handler. This method should be called only once.
   */
  public static void register() {
    FileUploadServiceHandler instance = new FileUploadServiceHandler();
    final String serviceHandlerId = getServiceHandlerId();
    RWT.getServiceManager().registerServiceHandler(serviceHandlerId, instance);
  }

  /**
   * Returns a unique id for this service handler class.
   */
  private static String getServiceHandlerId() {
    final String serviceHandlerId = FileUploadServiceHandler.class.getName();
    return serviceHandlerId;
  }

  /**
   * Builds a url which points to the service handler and encodes the given parameters
   * as url parameters. 
   */
  public static String getUrl(final String widgetId) {
    StringBuffer url = new StringBuffer();
    url.append(URLHelper.getURLString(false));

    URLHelper.appendFirstParam(url, REQUEST_PARAM, getServiceHandlerId());
    URLHelper.appendParam(url, REQUEST_WIDGET_ID, widgetId);

    // convert to relative URL
    int firstSlash = url.indexOf( "/" , url.indexOf( "//" ) + 2 ); // first slash after double slash of "http://"
    url.delete( 0, firstSlash + 1 ); // Result is sth like "/rap?custom_service_handler..."
    return RWT.getResponse().encodeURL(url.toString());
  }
}
