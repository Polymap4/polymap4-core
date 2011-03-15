/*******************************************************************************
 * Copyright (c) 2002-2007 Critical Software S.A. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html Contributors: Tiago
 * Rodrigues (Critical Software S.A.) - initial implementation Joel Oliveira
 * (Critical Software S.A.) - initial commit
 ******************************************************************************/
package org.eclipse.rwt.widgets.internal.uploadkit;

import java.io.IOException;

import org.eclipse.rwt.graphics.Graphics;
import org.eclipse.rwt.lifecycle.*;
import org.eclipse.rwt.widgets.Upload;
import org.eclipse.rwt.widgets.UploadEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Widget;

/**
 * Class that interfaces between the Java and the JavaScript.
 * 
 * @author tjarodrigues
 */
public class UploadLCA extends AbstractWidgetLCA {
  private static final String PROP_LASTFILEUPLOADED = "lastFileUploaded";

  private static final String JS_PROP_LASTFILEUPLOADED = "lastFileUploaded";
  private static final String JS_PROP_BROWSE_BUTTON_TEXT = "browseButtonText";
  private static final String JS_PROP_UPLOAD_BUTTON_TEXT = "uploadButtonText";

  /**
   * Preserves the property values between the Java and the JavaScript.
   * 
   * @param widget The <code>Widget</code>.
   */
  public final void preserveValues( final Widget widget ) {
    Upload upload = ( Upload )widget;
    ControlLCAUtil.preserveValues( upload );
    IWidgetAdapter adapter = WidgetUtil.getAdapter( widget );
    adapter.preserve( PROP_LASTFILEUPLOADED, upload.getLastFileUploaded() );
    adapter.preserve( JS_PROP_BROWSE_BUTTON_TEXT, upload.getBrowseButtonText() );
    adapter.preserve( JS_PROP_UPLOAD_BUTTON_TEXT, upload.getUploadButtonText() );
  }

  /**
   * Reads data from the <code>Widget</code>.
   * 
   * @param widget The <code>Widget</code>.
   */
  public final void readData( final Widget widget ) {
    final Upload upload = ( Upload )widget;
    String lastFileUploaded = WidgetLCAUtil.readPropertyValue( upload,
                                                               "lastFileUploaded" );
    String path = WidgetLCAUtil.readPropertyValue( upload, "path" );
    final IUploadAdapter adapter = getAdapter( upload );
    adapter.setPath( path );
    adapter.setLastFileUploaded( lastFileUploaded );
    final String finished = WidgetLCAUtil.readPropertyValue( upload, "finished" );
    
    // TODO: [sr] handle if long
    final int uploadPartial = ( int )adapter.getBytesRead();
    final int uploadTotal = ( int )adapter.getContentLength();
    
    if( finished != null ) 
    {
      // At the moment, the event must be fire directly via the ProcessActionRunner
      // because delayed execution doesn't work at the moment for custom events.
      // If this changes one day, processEvent() can be called directly.
      ProcessActionRunner.add( new Runnable() {
        
        public void run() {
          UploadEvent evt = new UploadEvent( upload,
                                             Boolean.valueOf( finished )
                                             .booleanValue(),
                                             uploadPartial,
                                             uploadTotal );
          evt.processEvent();

        }
      });
    }
  }

  /**
   * Creates the initial <code>Widget</code> rendering.
   * 
   * @param widget The <code>Widget</code>.
   * @throws IOException If the <code>Widget</code> JavaScript is not found.
   */
  public final void renderInitialization( final Widget widget )
    throws IOException
  {
    final JSWriter writer = JSWriter.getWriterFor( widget );
    final Upload upload = ( Upload )widget;
    
    final String servletPath = getAdapter( upload ).getServletPath();
    
    writer.newWidget( "org.eclipse.rwt.widgets.Upload", new Object[] {
      servletPath, 
      new Integer( getAdapter( upload ).getFlags() ) 
    } );
    writer.set( "appearance", "composite" );
    writer.set( "overflow", "hidden" );
    ControlLCAUtil.writeStyleFlags( ( Upload )widget );
  }

  /**
   * Renders the <code>Widget</code> changes in the JavaScript.
   * 
   * @param widget The <code>Widget</code>.
   * @throws IOException If the <code>Widget</code> JavaScript is not found.
   */
  public final void renderChanges( final Widget widget ) throws IOException {
    final Upload upload = ( Upload )widget;
    ControlLCAUtil.writeChanges( upload );
    final JSWriter writer = JSWriter.getWriterFor( widget );
    IUploadAdapter uploadAdapter = getAdapter( upload );
    
    ////////////////////////////////////////////////////////////////////////////
    // TODO [fappel]: check whether this is useful and if so, whether preserve
    //                works properly
    final String lastFileUploaded = upload.getLastFileUploaded();
    writer.set( PROP_LASTFILEUPLOADED,
                JS_PROP_LASTFILEUPLOADED,
                lastFileUploaded );
    ////////////////////////////////////////////////////////////////////////////
    
    
    IWidgetAdapter adapter = WidgetUtil.getAdapter( widget );
    boolean changed;
    
    changed = !adapter.isInitialized()
      || WidgetLCAUtil.hasChanged( widget, JS_PROP_BROWSE_BUTTON_TEXT, upload.getBrowseButtonText() );
    if( changed ) {
      final Point textExtent = Graphics.stringExtent( upload.getFont(), upload.getBrowseButtonText());
      final Object textWidth = new Integer( textExtent.x + 7);
      writer.set( JS_PROP_BROWSE_BUTTON_TEXT, new Object[] {upload.getBrowseButtonText(), textWidth});
    }
    
    if( ( uploadAdapter.getFlags() & Upload.SHOW_UPLOAD_BUTTON ) > 0 ) {
      changed = !adapter.isInitialized()
      || WidgetLCAUtil.hasChanged( widget, JS_PROP_UPLOAD_BUTTON_TEXT, upload.getUploadButtonText() );
    
      if( changed ) {
        final Point textExtent = Graphics.stringExtent( upload.getFont(), upload.getUploadButtonText());
        final Object textWidth = new Integer( textExtent.x + 7);
        writer.set( JS_PROP_UPLOAD_BUTTON_TEXT, new Object[] {upload.getUploadButtonText(), textWidth});
      }
      
    }
    
    if( uploadAdapter.performUpload() ) {
      writer.call( upload, "_performUpload", null );
    }
    
    if( uploadAdapter.isResetUpload() ) {
      writer.call( upload, "_resetUpload", null );
      uploadAdapter.setResetUpload( false );
    }
  }

  /**
   * Renders the <code>Widget</code> dispose in the JavaScript.
   * 
   * @param widget The <code>Widget</code>.
   * @throws IOException If the <code>Widget</code> JavaScript is not found.
   */
  public final void renderDispose( final Widget widget ) throws IOException {
    final JSWriter writer = JSWriter.getWriterFor( widget );
    writer.dispose();
  }
  
  private IUploadAdapter getAdapter( final Upload upload ) {
    return ( IUploadAdapter )upload.getAdapter( IUploadAdapter.class );
  }
}
