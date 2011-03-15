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

package org.eclipse.rwt.widgets.internal.uploadkit;

public interface IUploadAdapter {
  boolean performUpload();
  int getFlags();
  void setPath( final String path );
  void setLastFileUploaded( final String lastFileUploaded );
  String getServletPath();
  boolean isResetUpload();
  void setResetUpload(boolean resetUpload);
  public long getBytesRead();
  public long getContentLength();
}
