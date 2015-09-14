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

import static org.eclipse.rap.selenium.xpath.Predicate.with;



public class ElementSelector {

  XPath xpath;
  private final String axis;

  ElementSelector( XPath xpath, String axis ) {
    this.xpath = xpath;
    this.axis = axis;
  }

  /**
   * Matches all elements with the given text value.
   *
   * @param string
   * @return the new XPath
   */
  public XPath textElement( String string ) {
    return element( Predicate.with().text( string ) );
  }

  /**
   * Matches all elements with the given string in their text value.
   *
   * @param string
   * @return the new XPath
   */
  public XPath textElementContaining( String string ) {
    return element( Predicate.with().string( string ) );
  }

  /**
   * Matches all elements of the current axis.
   *
   * @return the new XPath
   */
  public XPath element() {
    return element( null, null );
  }

  /**
   * Matches all elements of the current axis for which which the predicate is true.
   *
   * @param predicate
   * @return the new XPath
   */
  public XPath element( Predicate predicate ) {
    return element( null, predicate );
  }

  /**
   * Matches all elements that have this tag name.
   *
   * @param name
   * @return the new XPath
   */
  public XPath element( String name ) {
    return element( name, null );
  }

  /**
   * Matches all elements that have this tag name and for which the predicate is true.
   *
   * @param name
   * @param predicate
   * @return the new XPath
   */
  public XPath element( String name, Predicate predicate ) {
    StringBuilder selection = new StringBuilder( axis );
    appendElement( selection, name );
    appendPredicate( selection, predicate );
    return xpath.append( selection.toString() );
  }

  /**
   * Matches all elements that have the given string as the value of their "role" attribute.
   * Elements with aria-hidden="true" are excluded.
   *
   * @param role
   * @return the new XPath
   */
  public XPath widget( String role ) {
    return element( widgetWith( role ) );
  }

  /**
   * Matches all elements that have the given string as the value of their "role" attribute and
   * for which the predicate is true. Elements with aria-hidden="true" are excluded.
   *
   * @param role
   * @param predicate
   * @return the new XPath
   */
  public XPath widget( String role, Predicate predicate ) {
    return element( widgetWith( role ).and( predicate ) );
  }

  /**
   * Matches all elements that have the given string as the value of their "role" attribute and
   * and an "aria-" prefixed attribute of the given value. Elements with aria-hidden="true" are excluded.
   *
   * @param role
   * @param ariaAttribute
   * @param value
   * @return the new XPath
   */
  public XPath widget( String role, String ariaAttribute, String value ) {
    return element( widgetWith( role ).attr( "aria-" + ariaAttribute, value ) );
  }

  /**
   * Matches all elements that have the given string as the value of their "role" attribute and
   * and the given string as their text value. Elements with aria-hidden="true" are excluded.
   *
   * @param role
   * @param text
   * @return the new XPath
   */
  public XPath widget( String role, String text ) {
    return element( widgetWith( role ).textContent( text ) );
  }

  private static Predicate widgetWith( String role ) {
    // TODO: use for text also, include in ARIA addon
    return with().notAria( "hidden", "true" ).attr( "role", role );
  }

  private static void appendElement( StringBuilder selection, String name ) {
    selection.append( name != null ? name : "*" );
  }

  static void appendPredicate( StringBuilder selection, Predicate predicate ) {
    if( predicate != null ) {
      String predicateString = predicate.toString();
      if( predicateString.isEmpty() ) {
        throw new IllegalArgumentException( "Predicate is empty" );
      }
      selection.append( "[" ).append( predicate.toString() ).append( "]" );
    }
  }

}
