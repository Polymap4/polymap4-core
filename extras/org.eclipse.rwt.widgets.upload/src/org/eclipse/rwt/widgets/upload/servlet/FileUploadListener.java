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
package org.eclipse.rwt.widgets.upload.servlet;

import org.apache.commons.fileupload.ProgressListener;

/**
 * This is a File Upload Listener that is used by Apache Commons File Upload to
 * monitor the progress of the uploaded file.
 */
public class FileUploadListener implements ProgressListener {

  private volatile long bytesRead = 0L, contentLength = 0L, item = 0L;

  /**
   * Creates a new File Upload Listener.
   */
  public FileUploadListener() {
    super();
  }

  /**
   * Updates the File properties.
   * 
   * @param aBytesRead Number of bytes read.
   * @param aContentLength The file length.
   * @param anItem The monitored item.
   */
  public final void update( final long aBytesRead,
                            final long aContentLength,
                            final int anItem )
  {
    bytesRead = aBytesRead;
    contentLength = aContentLength;
    item = anItem;
  }

  /**
   * Gets the number of bytes read.
   * 
   * @return Number of bytes read.
   */
  public final long getBytesRead() {
    return bytesRead;
  }

  /**
   * Gets the File length.
   * 
   * @return File length.
   */
  public final long getContentLength() {
    return contentLength;
  }

  /**
   * Gets the monitored item.
   * 
   * @return Monitored item.
   */
  public final long getItem() {
    return item;
  }
}
