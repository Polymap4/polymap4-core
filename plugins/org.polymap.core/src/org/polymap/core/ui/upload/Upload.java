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

import java.util.concurrent.Callable;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;

import org.eclipse.rwt.graphics.Graphics;
import org.eclipse.rwt.lifecycle.UICallBack;
import org.eclipse.rwt.widgets.FileUpload;

import org.polymap.core.Messages;
import org.polymap.core.runtime.IMessages;
import org.polymap.core.runtime.SessionContext;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class Upload
        extends Composite
        implements IUploadHandler {

    private static Log log = LogFactory.getLog( Upload.class );

    public static final int         SHOW_UPLOAD_BUTTON = 1;
    public static final int         SHOW_PROGRESS = 2;
    
    private static final IMessages  i18n = Messages.forPrefix( "Upload" );

    private FileUpload              fileUpload;

    private ProgressBar             progress;

    private Label                   label;
    
    private IUploadHandler          handler;
    
    private SessionContext          sessionContext = SessionContext.current();
    
    private Display                 display = getDisplay();
    
    
    public Upload( Composite parent, int style, int... uploadStyles ) {
        super( parent, style );
        setLayout( FormLayoutFactory.defaults().create() );

        fileUpload = new FileUpload( this, SWT.NONE );
        fileUpload.setLayoutData( FormDataFactory.filled().right( -1 ).create() );
        fileUpload.setText( i18n.get( "select" ) );
        fileUpload.addSelectionListener( new SelectionAdapter() {
            public void widgetSelected( SelectionEvent ev ) {
                UICallBack.activate( "upload" );
                fileUpload.submit( FileUploadServlet.addUploadHandler( Upload.this ) );
            }
        });
        
        progress = new ProgressBar( this, SWT.HORIZONTAL );
        progress.setLayoutData( FormDataFactory.filled().left( fileUpload ).create() );
        progress.setMaximum( Integer.MAX_VALUE );

        label = new Label( this, SWT.NONE );
        label.setLayoutData( FormDataFactory.filled().top( 0, 5 ).left( fileUpload, 20 ).create() );
        label.setText( i18n.get( "label" ) );
        label.setForeground( Graphics.getColor( 0x60, 0x60, 0x60 ) );
        label.moveAbove( progress );
    }

    @Override
    public void dispose() {
        super.dispose();
        FileUploadServlet.removeUploadHandler( this );
        UICallBack.deactivate( "upload" );
    }

    public IUploadHandler getHandler() {
        return handler;
    }
    
    public void setText( String text ) {
        fileUpload.setText( text );
    }

    public void setHandler( IUploadHandler handler ) {
        this.handler = handler;
    }

    @Override
    public void uploadStarted( final String name, final String contentType,
            final int contentLength, final InputStream in ) throws Exception {
        assert handler != null : "No handler set for Upload.";

        // give the thread the proper session context (but outside UI thread)
        sessionContext.execute( new Callable() {
            public Object call() throws Exception {
                handler.uploadStarted( name, contentType, contentLength, 
                        new ProgressInputStream( in, name, contentLength ) );
                return null;
            }
        });
    }

    
    /**
     * 
     */
    class ProgressInputStream
            extends FilterInputStream {
    
        private String      name;
    
        private int         count = 0;

        private int         contentLength;
    
    
        private ProgressInputStream( InputStream in, String name, int contentLength ) {
            super( in );
            this.name = name;
            this.contentLength = contentLength > 0 ? contentLength : 10*1024*1024;
        }
    
    
        @Override
        public int read() throws IOException {
            throw new RuntimeException( "not implemented." );
        }
    
    
        @Override
        public int read( byte[] b, int off, int len ) throws IOException {
            final int result = super.read( b, off, len );

//            try { Thread.sleep( 1000 ); } catch (InterruptedException e) {}
            
            if (progress != null && !progress.isDisposed()) {
                display.asyncExec( new Runnable() {
                    public void run() {
                        count += result;
                        // init maximum (inside UI thread)
                        if (progress.getMaximum() == Integer.MAX_VALUE) {
                            progress.setMaximum( contentLength );
                        }
                        if (result == -1) {
                            progress.setSelection( progress.getMaximum() );
                            UICallBack.deactivate( "upload" );
                        }
                        else {
                            progress.setSelection( count );
                        }
                        // adjust contentLength
                        if (count >= contentLength) {
                            contentLength = count * 2;
                            progress.setMaximum( contentLength );
                        }
                        //int percent = 100 * progress.getSelection() / progress.getMaximum();
                        label.setText( name + " (" + FileUtils.byteCountToDisplaySize( count ) 
                                /*+ " - " + percent + "%"*/ + ")" );
                    }
                });
            }
            return result;
        }
    }

}
