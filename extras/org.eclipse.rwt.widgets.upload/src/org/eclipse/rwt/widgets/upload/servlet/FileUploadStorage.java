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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.rwt.SessionSingletonBase;


/**
 * This session singleton is used to communicate between service handler and rap
 * application and to exchange a reference to the uploaded file. To support multiple
 * file uploads a key must be used to get and store items. This key must be unique as
 * it is used as a hash map key.
 * @author stefan.roeck
 */
public class FileUploadStorage extends SessionSingletonBase {

  private Map items = new HashMap();
  
  /**
   * Returns an instance to this session singleton.
   */
  public static FileUploadStorage getInstance() {
    return ( FileUploadStorage )getInstance( FileUploadStorage.class );
  }
  
  private FileUploadStorage() {
    // prevent instantiation
  }
  
  /**
   * Sets a {@link FileUploadStorageItem} or removes an existing one, if the item is null.
   */
  public void setUploadStorageItem(final String key, final FileUploadStorageItem item) {
    if( item == null ) {
      items.remove( key );
    } else {
      items.put( key, item );
    }
  }
  
  /**
   * Returns a {@link FileUploadStorageItem} for the given key or null, if not existant
   * in map.
   */
  public FileUploadStorageItem getUploadStorageItem(final String key) {
    return ( FileUploadStorageItem )items.get( key );
  }
  
}
