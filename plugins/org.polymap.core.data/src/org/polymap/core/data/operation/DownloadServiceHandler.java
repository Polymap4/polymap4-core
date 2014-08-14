/* 
 * polymap.org
 * Copyright (C) 2011-2013, Polymap GmbH. All rights reserved.
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

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.rwt.RWT;
import org.eclipse.rwt.internal.util.URLHelper;
import org.eclipse.rwt.service.IServiceHandler;
import org.eclipse.rwt.widgets.ExternalBrowser;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;

import org.polymap.core.runtime.ConcurrentReferenceHashMap;
import org.polymap.core.runtime.ConcurrentReferenceHashMap.ReferenceType;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;

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

    /**
     * The parameter used to identify the download provider.
     * <p/>
     * Because of a bug in SWL Label this must not be "id" :( Otherwise the created
     * URLs could not be used in SWT Labels with {@link RWT#MARKUP_ENABLED}.
     */
    private static final String         ID_REQUEST_PARAM = "id";

    static ConcurrentReferenceHashMap<String,ContentProvider>   providers 
            = new ConcurrentReferenceHashMap( ReferenceType.STRONG, ReferenceType.SOFT );


//    static {
//        DownloadServiceHandler instance = new DownloadServiceHandler();
//        RWT.getServiceManager().registerServiceHandler( SERVICE_HANDLER_ID, instance );
//    }

    
    /**
     * Registers the given provider for downloading. An unique id of the newly
     * registered download is build automatically.
     * 
     * @param provider
     * @return The download URL for the given provider.
     */
    public static String registerContent( ContentProvider provider ) {
        return registerContent( String.valueOf( provider.hashCode() ), provider );
    }


    /**
     * Registers the given provider for downloading.
     * 
     * @param id
     * @param provider
     * @return The download URL for the given provider.
     */
    public static String registerContent( String id, ContentProvider provider ) {
        if (providers.put( id, provider ) != null) {
            log.warn( "ContetProvider already registered for id: " + id );
        }
        
        // XXX code from RAP; its a bit confusing, don't like it but it works
        StringBuilder url = new StringBuilder( 256 );
        url.append( URLHelper.getURLString() );

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
        
        /**
         *
         * @param success True if the doenload was completed successfully, falso otherwise.
         * @return True specifies that this provider is no longer used and disposed.
         */
        public boolean done( boolean success );
    }
    
    
    // instance *******************************************
    
    public DownloadServiceHandler() {
    }


    public void service()
    throws IOException, ServletException {
        HttpServletRequest request = RWT.getRequest();
        HttpServletResponse response = RWT.getResponse();
        
        String pathInfo = request.getPathInfo();
        
        try {
            // download.html
            if (pathInfo != null && pathInfo.startsWith( "/download.html" )) {
                // sending an HTML page helps debugging on IE, which often blocks or
                // otherwise fails to download directly
                String id = request.getParameter( ID_REQUEST_PARAM );
                String filename = request.getParameter( "filename" );
                String linkTarget = "../download/" 
                        + (filename != null ? filename : "polymap3_export.tmp")
                        + "?" + ID_REQUEST_PARAM + "=" + id;

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
                if (id == null) {
                    log.warn( "No 'id' param in request." );
                    response.sendError( 404 );
                    return;
                }

                ContentProvider provider = providers.get( id );
                if (provider == null) {
                    log.warn( "No content provider registered for id: " + id );
                    response.sendError( 404 );
                    return;
                }
                
                String[] pathInfos = StringUtils.split( request.getPathInfo(), "/" );

                String contentType = provider.getContentType();
                response.setContentType( contentType );
                // display any HTML content in browser instead of downloading it
                if (contentType != null && !contentType.toLowerCase().contains( "html" )) {
                    response.setHeader( "Content-disposition", "attachment; filename=" + provider.getFilename() );
                }
                response.setHeader( "Pragma", "public" );
                response.setHeader( "Cache-Control", "must-revalidate, post-check=0, pre-check=0" );
                response.setHeader( "Cache-Control", "public" );
                response.setHeader( "Expires", "0" );
                
                InputStream in = provider.getInputStream();
                ServletOutputStream out = response.getOutputStream();
                boolean providerDone = false;
                try {
                    IOUtils.copy( in, out );
                    out.flush();
                    response.flushBuffer();
                    
                    providerDone = provider.done( true );
                }
                catch (Exception e) {
                    providerDone = provider.done( false );
                }
                finally {
                    IOUtils.closeQuietly( in );
                }
                
                if (providerDone) {
                    providers.remove( id );
                }
            }
        }
        catch (Exception e) {
            log.debug( "", e );
            throw new ServletException( e );
        }
    }
    
    
    // DownloadDialog
    
    /**
     * 
     * 
     */
    public static class DownloadDialog
            extends Dialog {

        private String          url;
        private String          windowName;
        private int             windowFlags;
        private String          msg;
        
        
        public DownloadDialog( Shell parentShell, String url ) {
            super( parentShell );
            this.url = url;
            this.msg = "<a href=\"" + url + "\">Link...</a>";
            setShellStyle( SWT.RESIZE | SWT.DIALOG_TRIM | SWT.SHEET );
        }

        @SuppressWarnings("hiding")
        public DownloadDialog openBrowserWindow( String windowName, int windowFlags ) {
            this.windowName = windowName;
            this.windowFlags = windowFlags;
            return this;
        }

        public DownloadDialog setMessage( String msg ) {
            this.msg = msg;
            log.info( msg );
            return this;
        }
        
        @Override
        public int open() {
            if (windowName != null) {
                ExternalBrowser.open( windowName, url, windowFlags );
            }
            return super.open();
        }

        @Override
        protected void createButtonsForButtonBar(Composite parent) {
            createButton( parent, IDialogConstants.CANCEL_ID, IDialogConstants.get().CANCEL_LABEL, false );
        }

        @Override
        protected Control createDialogArea( Composite parent ) {
            getShell().setText( "Download" );
            getShell().setSize( 520, 350 );
            Rectangle displaySize = getShell().getDisplay().getClientArea();
            getShell().setLocation( (displaySize.width - 520) / 2, (displaySize.height - 350) / 2 );

            Composite result = (Composite)super.createDialogArea( parent );
            result.setLayout( FormLayoutFactory.defaults().create() );
            
            Label link = new Label( result, SWT.WRAP );
            link.setText( msg );
            // after setText() in order to prevent validation, which fails
            link.setData( RWT.MARKUP_ENABLED, Boolean.TRUE );
            link.setLayoutData( FormDataFactory.filled()/*.clearRight().width( 400 )*/.create() );
            
            return result;
        }
        
    }
    
}
