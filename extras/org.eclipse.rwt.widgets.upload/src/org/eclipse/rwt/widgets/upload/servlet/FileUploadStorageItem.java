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

import java.io.InputStream;


/**
 * This Pojo is used to store a file reference and a progress listener.
 * It is used for communication between service handler and rap application.
 * Due to the asynchronous nature of ServiceHandler-requests and access from
 * the UIThread to instances of this class, all members are access
 * synchronized.
 * 
 * @author stefan.roeck
 */
public class FileUploadStorageItem {
  private InputStream fileInputStream;
  private String contentType;
  private String uploadProcessId;
  private long bytesRead;
  private long contentLength;
  
  public synchronized InputStream getFileInputStream() {
    return this.fileInputStream;
  }
  
  public synchronized void setFileInputStream( final InputStream fileInputStream ) {
    this.fileInputStream = fileInputStream;
  }
  
  public synchronized void setContentType( final String contentType ) {
    this.contentType = contentType;
  }
  
  public synchronized String getContentType() {
    return this.contentType;
  }

  public synchronized void setUploadProcessId( final String uploadProcessId ) {
    this.uploadProcessId = uploadProcessId;
  }
  
  public synchronized String getUploadProcessId() {
    return this.uploadProcessId;
  }
  
  public synchronized void updateProgress(final long bytesRead, final long contentLength) {
    this.bytesRead = bytesRead;
    this.contentLength = contentLength;
  }

  public synchronized long getBytesRead() {
    return bytesRead;
  }
  
  public synchronized long getContentLength() {
    return contentLength;
  }

}
