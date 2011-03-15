/*******************************************************************************
 * Copyright (c) 2002-2007 Critical Software S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tiago Rodrigues (Critical Software S.A.) - initial implementation
 *     Joel Oliveira (Critical Software S.A.) - initial commit
 ******************************************************************************/
package org.eclipse.rwt.widgets;

import org.eclipse.rwt.Adaptable;
import org.eclipse.swt.events.TypedEvent;
import org.eclipse.swt.widgets.Widget;

/**
 * Represents an Upload Event.
 *
 * @author tjarodrigues
 * @version $Revision: 1.4 $
 */
public class UploadEvent extends TypedEvent {
  
  private static final long serialVersionUID = 1L;
  /**
   * Hopefully, this isn't occupied by another custom event type
   */
  private static final int UPLOAD_FINISHED = 101;
  private static final int UPLOAD_IN_PROGRESS = 102;
  private static final Class LISTENER = UploadListener.class;
  private final boolean finished;
  private final int uploadedParcial;
  private final int uploadedTotal;

  /**
   * Checks if the Upload has finished.
   * 
   * @return <code>True</code> if the Upload has finished, <code>False</code>
   *         otherwise.
   */
  public final boolean isFinished() {
    return finished;
  }

  /**
   * Gets the parcial amount of data uploaded.
   *
   * @return The parcial amount of data uploaded.
   */
  public final int getUploadedParcial() {
    return this.uploadedParcial;
  }

  /**
   * Gets the total file size.
   *
   * @return The total file size.
   */
  public final int getUploadedTotal() {
    return this.uploadedTotal;
  }

  /**
   * Creates a new instance of the Upload Event.
   *
   * @param finished Indicates if the upload is finished.
   * @param uploadedParcial The parcial amount of data uploaded.
   * @param uploadedTotal The total file size.
   * @param widget The sender of the event, must not be null
   */
  public UploadEvent( final Widget widget,
                      final boolean finished,
                      final int uploadedParcial,
                      final int uploadedTotal )
  {
    super( widget, finished
                           ? UPLOAD_FINISHED
                           : UPLOAD_IN_PROGRESS );
    this.finished = finished;
    this.uploadedParcial = uploadedParcial;
    this.uploadedTotal = uploadedTotal;
  }
  
  protected void dispatchToObserver( final Object listener ) {
    switch( getID() ) {
      case UPLOAD_IN_PROGRESS:
        ( ( UploadListener )listener ).uploadInProgress( this );
      break;
      case UPLOAD_FINISHED:
        ( ( UploadListener )listener ).uploadFinished( this );
      break;
      default:
        throw new IllegalStateException( "Invalid event handler type." );
    }
  }

  protected Class getListenerType() {
    return LISTENER;
  }

  protected boolean allowProcessing() {
    return true;
  }

  public static void addListener( final Adaptable adaptable, 
                                  final UploadListener listener )
  {
    addListener( adaptable, LISTENER, listener );
  }

  public static void removeListener( final Adaptable adaptable, 
                                     final UploadListener listener )
  {
    removeListener( adaptable, LISTENER, listener );
  }
  
  public static boolean hasListener( final Adaptable adaptable ) {
    return hasListener( adaptable, LISTENER );
  }
  
  public static Object[] getListeners( final Adaptable adaptable ) {
    return getListener( adaptable, LISTENER );
  }
}
