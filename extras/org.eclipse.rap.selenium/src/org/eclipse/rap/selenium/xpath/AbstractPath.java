/*******************************************************************************
 * Copyright (c) 2014 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/

package org.eclipse.rap.selenium.xpath;


public abstract class AbstractPath<S> {

  /////////////////
  // Axis selection

  /**
   * Select the child axis.
   *
   * @return a selector for elements on the child axis
   */
  public S children() {
    return createSelector( "/" );
  }

  /**
   * Select the descendant axis.
   *
   * @return a selector for elements on the descendant axis
   */
  public S descendants() {
    return createSelector( "/descendant::" );
  }

  /**
   * Select the following-sibling axis.
   *
   * @return a selector for elements on the following-sibling axis
   */
  public S followingSiblings() {
    return createSelector( "/following-sibling::" );
  }

  /**
   * Select the preceding-sibling axis.
   *
   * @return a selector for elements on the preceding-sibling axis
   */
  public S precedingSiblings() {
    return createSelector( "/preceding-sibling::" );
  }

  /**
   * Select the self-or-descendant axis.
   *
   * @return a selector for elements on the self-or-descendant axis
   */
  public S selfAndDescendants() {
    return createSelector( "//" );
  }

  abstract S createSelector( String axis );

}
