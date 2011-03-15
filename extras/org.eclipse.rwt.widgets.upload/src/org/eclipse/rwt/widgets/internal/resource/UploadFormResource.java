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

package org.eclipse.rwt.widgets.internal.resource;

import org.eclipse.rwt.resources.IResource;
import org.eclipse.rwt.resources.IResourceManager.RegisterOptions;


public class UploadFormResource implements IResource {

  public String getCharset() {
    return "ISO-8859-1";
  }

  public ClassLoader getLoader() {
    return getClass().getClassLoader();
  }

  public String getLocation() {
    return "org/eclipse/rwt/widgets/internal/resource/qxcontrib/UploadForm.js";
  }

  public RegisterOptions getOptions() {
    return RegisterOptions.VERSION_AND_COMPRESS;
  }

  public boolean isExternal() {
    return false;
  }

  public boolean isJSLibrary() {
    return true;
  }
}
