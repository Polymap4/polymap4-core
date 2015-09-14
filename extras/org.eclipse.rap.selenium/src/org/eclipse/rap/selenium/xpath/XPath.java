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

import static java.lang.String.valueOf;
import static org.eclipse.rap.selenium.xpath.Predicate.with;

public class XPath extends AbstractPath<ElementSelector> {

  public static XPath root() {
    return createXPath( "/*" );
  }

  /**
   * Find an element by it's "id" attribute.
   *
   * @param id
   * @return the new XPath
   */
  public static XPath byId( String id ) {
    return any().element( with().id( id ) );
  }

  /**
   * Find an element by it's "test-id" attribute.
   *
   * @param id
   * @return the new XPath
   */
  public static XPath byTestId( String id ) {
    return any().element( with().attr( "test-id", id ) );
  }

  /**
   * Matches all elements in the document.
   *
   * @param id
   * @return the new XPath
   */
  public static ElementSelector any() {
    return createXPath( "/" ).children();
  }

  /**
   * Creates a new XPath object using the given string. The string is not validated.
   *
   * @param id
   * @return the new XPath
   */
  public static XPath createXPath( String initial ) {
    return new XPath( initial );
  }

  private final String path;

  private XPath( String path ) {
    this.path = path;
  }

  /////////////////////
  // relative selection

  /**
   * Matches the parent of the current node.
   * @return the new XPath
   */
  public XPath parent() {
    return append( "/.." );
  }

  /**
   * Matches the n-th child of the current node.
   * @return the new XPath
   */
  public XPath child( int position ) {
    if( position <= 0 ) {
      throw new IllegalArgumentException( "position must be > 0" );
    }
    return append( "/*[", valueOf( position ), "]" );
  }

  /**
   * Matches the first child of the current node.
   * @return the new XPath
   */
  public XPath firstChild() {
    return append( "/*[1]" );
  }

  /**
   * Matches the last child of the current node.
   * @return the new XPath
   */
  public XPath lastChild() {
    return append( "/*[last()]" );
  }

  ///////////////////
  // reduce selection

  /**
   * Matches a node in the current result set if the given predicate is true
   * @return the new XPath
   */
  public XPath self( Predicate predicate ) {
    StringBuilder stringBuilder = new StringBuilder( toString() );
    ElementSelector.appendPredicate( stringBuilder, predicate );
    return new XPath( stringBuilder.toString() );
  }

  /**
   * Matches the first node of the current result set.
   * @return the new XPath
   */
  public XPath firstMatch() {
    return match( 1 );
  }

  /**
   * Matches the last node of the current result set.
   * @return the new XPath
   */
  public XPath lastMatch() {
    return insert( 0, "(" ).append( ")[last()]" );
  }

  /**
   * Matches the n-th node of the current result set.
   * @return the new XPath
   */
  public XPath match( int offset ) {
    if( offset <= 0 ) {
      throw new IllegalArgumentException( "Offset has to be > 0" );
    }
    // TODO [tb] : Selenium does seem to have issues with syntax (but not JS) - alternatives?
    return insert( 0, "(" ).append( ")[", valueOf( offset ), "]" );
  }

  //////////////
  // Overwritten

  @Override
  public XPath clone() {
    return createXPath( this.toString() );
  }

  @Override
  public String toString() {
    return path;
  }

  @Override
  public int hashCode() {
    return toString().hashCode();
  }

  @Override
  public boolean equals( Object obj ) {
    if( obj instanceof XPath ) {
      return toString().equals( ( ( XPath )obj ).toString() );
    }
    return false;
  }

  public XPath append( XPath xpath ) {
    return new XPath( toString() + xpath.toString() );
  }

  ////////////
  // Internals

  XPath append( String... strings ) {
    StringBuilder stringBuilder = new StringBuilder( path );
    for( int i = 0; i < strings.length; i++ ) {
      stringBuilder.append( strings[ i ] );
    }
    return new XPath( stringBuilder.toString() );
  }

  XPath insert( int offset, String string ) {
    StringBuilder stringBuilder = new StringBuilder( path );
    stringBuilder.insert( offset, string );
    return new XPath( stringBuilder.toString() );
  }

  @Override
  ElementSelector createSelector( String axis ) {
    return new ElementSelector( this, axis );
  }

}
