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

import java.util.EventListener;


/**
 * Use a UploadListener to get notified when a file upload has 
 * finished.
 * 
 * @author tjarodrigues
 * @version $Revision: 1.1 $
 */
public interface UploadListener extends EventListener{

  /**
   * Is called, when uploading a file is finished.
   * @param uploadEvent The Upload Event to be fired. 
   * {@link UploadEvent#getSource()} returns the upload
   * widget which triggered the event. All other fields are empty
   */
  public void uploadFinished( final UploadEvent uploadEvent );
  
  
  /**
   * Is called when the upload is in progress. You may use the
   * {@link UploadEvent} to get details on the progress.
   */
  public void uploadInProgress( final UploadEvent uploadEvent );
}
