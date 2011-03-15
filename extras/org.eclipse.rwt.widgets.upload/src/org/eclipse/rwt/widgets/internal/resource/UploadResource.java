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
package org.eclipse.rwt.widgets.internal.resource;

import org.eclipse.rwt.resources.IResource;
import org.eclipse.rwt.resources.IResourceManager.RegisterOptions;

/**
 * Resource definition for "Upload.js".
 * 
 * @author tjarodrigues
 */
public class UploadResource implements IResource {

  /**
   * Retrives the resource Charset.
   * 
   * @return Resource <code>Charset</code>.
   */
  public final String getCharset() {
    return "ISO-8859-1";
  }

  /**
   * Gets the resource Class Loader.
   * 
   * @return Resource <code>ClassLoader</code>.
   */
  public final ClassLoader getLoader() {
    return this.getClass().getClassLoader();
  }

  /**
   * Gets the resource Register Options.
   * 
   * @return Resource <code>RegisterOptions</code>
   */
  public final RegisterOptions getOptions() {
    return RegisterOptions.VERSION_AND_COMPRESS;
  }

  /**
   * Gets the location of the resource.
   * 
   * @return Resource location <code>String</code>.
   */
  public final String getLocation() {
    return "org/eclipse/rwt/widgets/Upload.js";
  }

  /**
   * Checks if the resource is a JavaScript library.
   * 
   * @return <code>True</code> if the resource is a JavaScript library,
   *         <code>False</code> otherwise.
   */
  public final boolean isJSLibrary() {
    return true;
  }

  /**
   * Checks if the resource is an external library.
   * 
   * @return <code>True</code> if the resource is an external library,
   *         <code>False</code> otherwise.
   */
  public final boolean isExternal() {
    return false;
  }
}
