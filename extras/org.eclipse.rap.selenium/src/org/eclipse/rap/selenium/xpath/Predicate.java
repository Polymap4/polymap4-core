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


public class Predicate {

  final private StringBuilder stringBuilder;

  /**
   * Create a new predicate. Intended to be used as a static import.
   *
   * @return
   */
  public static Predicate with() {
    return new Predicate();
  }

  /**
   * Create a new predicate form the given string. The string is not validated.
   *
   * @param initial
   * @return the new predicate
   */
  public static Predicate with( String initial ) {
    return new Predicate().and( initial );
  }

  /**
   * Negates the given predicate
   *
   * @param predicate
   * @return the new predicate
   */
  public static Predicate not( Predicate predicate ) {
    return new Predicate().andNot( predicate );
  }

  /**
   * Creates a new Predicate instance. Same as {@link Predicate#with()}.
   *
   * @param predicate
   * @return the new predicate
   */
  public Predicate() {
    stringBuilder = new StringBuilder();
  }

  /**
   * Creates a new Predicate instance with the given string.
   * Same as {@link Predicate#with(String))}.
   *
   * @param predicate
   * @return the new predicate
   */
  public Predicate( String initial ) {
    stringBuilder = new StringBuilder( initial );
  }

  /**
   * Appends the given predicate to the current predicate with "and".
   *
   * @param predicate
   * @return the new predicate
   */
  public Predicate and( Predicate predicate ) {
    return predicate != null ? and( predicate.toString() ) : this;
  }

  /**
   * Appends the given predicate to the current predicate with "and".
   *
   * @param predicate
   * @return the new predicate
   */
  public Predicate and( String predicate ) {
    return andAdd( "(", predicate, ")" );
  }

  /**
   * Appends the given predicate to the current predicate with "or".
   *
   * @param predicate
   * @return the new predicate
   */
  public Predicate or( String predicate ) {
    return orAdd( "(", predicate, ")" );
  }

  /**
   * Appends the given predicate to the current predicate with "or".
   *
   * @param predicate
   * @return the new predicate
   */
  public Predicate or( Predicate predicate ) {
    return orAdd( "(", predicate.toString(), ")" );
  }

  /**
   * Requires a node to have the given string as it's text value.
   *
   * @param string
   * @return the new predicate
   */
  public Predicate text( String string ) {
    return andAdd( "text()='", string, "'" );
  }

  /**
   * Requires a node to have at least one match for the given XPath relative to itself.
   *
   * @param content
   * @return the new predicate
   */
  public Predicate content( XPath content ) {
    return andAdd( "count(.", content.toString(), ")>0" );
  }

  /**
   * Requires a node to have at least one element with the given text within it's descendants,
   * or to have the text itself.
   *
   * @param elementText
   * @return the new predicate
   */
  public Predicate textContent( String elementText ) {
    return andAdd( "count(descendant-or-self::*[text()='", elementText, "'])>0" );
  }

  /**
   * Requires a node to have the given string within it's own text at least once.
   *
   * @param string
   * @return the new predicate
   */
  public Predicate string( String string ) {
    return andAdd( "contains(text(),'", string, "')" );
  }

  /**
   * Requires a node to have the given string within it's own or any descendants text
   * at least once.
   *
   * @param string
   * @return the new predicate
   */
  public Predicate stringContent( String string ) {
    return andAdd( "count(descendant-or-self::*[contains(text(),'", string, "')])>0" );
  }

  /**
   * Requires an elements "id" attribute to be the given string.
   *
   * @param id
   * @return the new predicate
   */
  public Predicate id( String id ) {
    if( "".equals( id ) ) {
      throw new IllegalArgumentException( "id may not be empty string" );
    }
    return attr( "id", id );
  }

  /**
   * Requires an elements to have an attribute of the given name and value.
   *
   * @param attribute
   * @param value
   * @return the new predicate
   */
  public Predicate attr( String attribute, String value ) {
    checkAttrParameter( attribute, value );
    return andAdd( "@", attribute, "='", value, "'" );
  }

  /**
   * Requires an elements to have an aria-attribute of the given name and value.
   * Same as {@link Predicate#attr(String, String)}, but add the "aria-"
   * prefix to the attribute name
   *
   * @param attribute
   * @param value
   * @return the new predicate
   */
  public Predicate aria( String attribute, String value ) {
    checkAttrParameter( attribute, value );
    return andAdd( "@aria-", attribute, "='", value, "'" );
  }

  /**
   * Requires an elements to not have an attribute of the given name and value.
   *
   * @param attribute
   * @param value
   * @return the new predicate
   */
  public Predicate notAttr( String attribute, String value ) {
    checkAttrParameter( attribute, value );
    return andAdd( "not(@", attribute, "='", value, "')" );
  }

  /**
   * Requires an elements to not have an aria-attribute of the given name and value.
   * Same as {@link Predicate#notAttr(String, String)}, but add the "aria-"
   * prefix to the attribute name
   *
   * @param attribute
   * @param value
   * @return the new predicate
   */
  public Predicate notAria( String attribute, String value ) {
    checkAttrParameter( attribute, value );
    return andAdd( "not(@aria-", attribute, "='", value, "')" );
  }

  /**
   * Requires a node to be the n-th child of it's parent. If n is negative, it requires a
   * node to be the n-th last child of it's parent. (E.g. -1 is the last child.)
   *
   * @param n Any number except 0.
   * @return the new predicate
   */
  public Predicate offset( int n ) {
    if( n == 0 ) {
      throw new IllegalArgumentException( "position must be != 0" );
    }
    // NOTE: [<position>] or [position()=<position>] would ONLY work correctly the match consists
    //       exactly of all children of the parent in the right order (not always the case)
    String ownPosition =   n > 0
                         ? "(count(preceding-sibling::*)+1)"
                         : "(count(following-sibling::*)+1)";
    return andAdd( ownPosition + "=" + Math.abs( n ) );
  }

  @Override
  public String toString() {
    return stringBuilder.toString();
  }

  ////////////
  // Internals

  private Predicate andNot( Predicate predicate ) {
    return andAdd( "not(", predicate.toString(), ")" );
  }

  private Predicate andAdd( String... str ) {
    if( stringBuilder.length() > 0 ) {
      stringBuilder.append( " and " );
    }
    return add( str );
  }

  private Predicate orAdd( String... str ) {
    if( stringBuilder.length() > 0 ) {
      stringBuilder.append( " or " );
    }
    return add( str );
  }

  private Predicate add( String... str ) {
    for( String part : str ) {
      stringBuilder.append( part );
    }
    return this;
  }

  private void checkAttrParameter( String attribute, String value ) {
    if( attribute == null || value == null ) {
      throw new NullPointerException( "parameter is null" );
    }
    if( attribute.equals( "" ) ) {
      throw new IllegalArgumentException( "Attribute is empty string" );
    }
  }

}
