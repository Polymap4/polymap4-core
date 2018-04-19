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

package org.eclipse.rap.selenium;

import static org.eclipse.rap.selenium.AriaRoles.*;
import static org.eclipse.rap.selenium.RapBot.asXPath;
import static org.eclipse.rap.selenium.xpath.Predicate.with;
import static org.eclipse.rap.selenium.xpath.XPath.any;

import org.eclipse.rap.selenium.xpath.AbstractPath;
import org.eclipse.rap.selenium.xpath.Predicate;
import org.eclipse.rap.selenium.xpath.XPath;

public class AriaGridBot {
  // TODO [tb] : remove all " + " path concatenations left from old RapUtil impl.

  private final RapBot rap;
  private final XPath grid;

  public AriaGridBot( RapBot rap, AbstractPath<?> grid ) {
    this.rap = rap;
    this.grid = XPath.byId( rap.getId( grid ) );
  }

  public XPath byId() {
    return grid;
  }

  /**
   * The first line has index zero. Returns path to row rendering the given line
   * or null if it doesn't exist
   */
  public XPath scrollLineIntoView( int lineIndex ) {
    checkGrid();
    String scrollbar = grid + vScrollBar();
    if( rap.getXPathCount( scrollbar ) == 0 ) {
      return getRowAtPosition( lineIndex );
    } else {
      int offset = getLineOffset();
      int rowCount = getVisibleLineCount();
      int maxOffset = getMaxLineOffset();
      if( lineIndex >= offset && lineIndex < offset + rowCount ) {
        return getRowAtLine( lineIndex );
      }
      // we target the previous line because rap.scrollWheel is not precise
      // (scrolling 2 lines for a grid) AND because the pixel offset has to be
      // exactly 0 for line 0 to show
      int desiredOffset = Math.min( lineIndex - 1, maxOffset );
      int lineDelta = desiredOffset - offset;
      String clientarea = getAriaControls( scrollbar );
      int delta = ( int )Math.floor( -1 * lineDelta / 2F );
      rap.scrollWheel( clientarea, delta );
      return getRowAtLine( lineIndex );
    }
  }

  public XPath scrollCellIntoView( String cellContent ) {
    XPath needle = rowWithCellText( grid, cellContent );
    scrollIntoView( needle );
    return needle;
  }

  public XPath scrollCellIntoView( String columnName, String cellContent ) {
    String columnId = getColumnId( grid, columnName );
    XPath needle = rowWithCellText( grid, columnId, cellContent );
    scrollIntoView( needle );
    return needle;
  }

  /**
   * Waits for all visible items of a VIRTUAL Tree, Table or NebulaGrid to
   * cache.
   *
   * @param gridPath
   */
  public void waitForItems() {
    checkGrid();
    rap.waitForDisappear( grid.toString() + any().widget( "row", "busy", "true" ).toString() );
    checkGrid();
  }

  public void scrollPageDown() {
    scrollGridByPage( -1 );
  }

  public void scrollPageUp() {
    scrollGridByPage( 1 );
  }

  public int getLineOffset() {
    checkGrid();
    String scrollbar = grid + vScrollBar();
    if( rap.getXPathCount( scrollbar ) == 0 ) {
      return 0;
    } else {
      // this mirrors the grid implementation (Grid._updateTopItemIndex)
      int offset = Integer.parseInt( rap.getAttribute( scrollbar, "aria-valuenow" ) ) - 1;
      return offset < 0 ? 0 : ( offset / getLineHeight() ) + 1;
    }
  }

  public int getMaxLineOffset() {
    String scrollbar = grid + vScrollBar();
    if( rap.getXPathCount( scrollbar ) == 0 ) {
      return 0;
    } else {
      int max = Integer.parseInt( rap.getAttribute( scrollbar, "aria-valuemax" ) );
      return max / getLineHeight();
    }
  }

  public int getLineCount() {
    checkGrid();
    String scrollbar = grid + vScrollBar();
    if( rap.getXPathCount( scrollbar ) == 0 ) {
      return getVisibleLineCount();
    } else {
      int scrollHeight = Integer.parseInt( rap.getAttribute( scrollbar, "aria-valuemax" ) )
                         + rap.selenium.getElementHeight( scrollbar ).intValue();
      // NOTE: for nebula grid footer would have to be subtracted
      return ( scrollHeight / getLineHeight() );
    }
  }

  public int getVisibleLineCount() {
    return rap.getXPathCount( getClientArea().children().widget( ROW ) );
  }

  /**
   * Returns the absolute path to the client area element of the given widget.
   * If there are multiple (fixed columns), the first is returned;
   *
   * @param grid
   * @return
   */
  public XPath getClientArea() {// TODO [tb] : cache!
    // don't use "widget" to include invisible scrollbar (as opposed to vScrollBar())
    XPath scrollbar = asXPath( grid ).descendants().element(
      with().attr( "role", SCROLL_BAR ).aria( "orientation", "vertical" )
    );
    return getAriaControls( scrollbar );
  }

  /**
   * Returns the path to the row with the given lineIndex. The line has to be
   * visible on screen. If the grid has fixed columns, the leftside row is
   * returned.
   *
   * @param grid
   * @param rowIndex
   * @return
   */
  public XPath getRowAtLine( int lineIndex ) {
    return getRowAtPosition( lineIndex - getLineOffset() );
  }

  /**
   * Returns the path to the row with the given position relative to the first
   * visible row, or null if the index is out of bounds. If fixed columns are
   * used, the leftside row is returned.
   *
   * @param grid
   * @param position
   * @return
   */
  public XPath getRowAtPosition( int position ) {
    checkGrid();
    waitForItems();
    String rowId = rap.getFlowTo( grid );
    for( int i = 0; i < position && rowId != null; i++ ) {
      rowId = rap.getAttribute( XPath.byId( rowId ).toString(), "aria-flowto" );
      if( rowId != null && rowId.endsWith( "_1" ) ) {
        rowId = rap.getAttribute( XPath.byId( rowId ).toString(), "aria-flowto" );
      }
    }
    return rowId != null ? XPath.byId( rowId ) : null;
  }

  /**
   * Returns the path to the row with the described cell. Returns null if no
   * such cell is visible on screen. Throws {@link IllegalStateException} if the
   * column does not exist or there are multiple visible rows with that cell.
   */
  public XPath getRowByCell( String columnName, String cellContent ) {
    String columnId = getColumnId( grid, columnName );
    XPath row = rowWithCellText( grid, columnId, cellContent );
    int count = rap.getXPathCount( row );
    if( count == 1 ) {
      return XPath.byId( rap.getId( row ) );
    } else if( count == 0 ) {
      return null;
    } else {
      throw new IllegalStateException( "There are multiple rows matching " + row );
    }
  }

  /**
   * Returns the path to the row with the described cell. Returns null if no
   * such cell is visible on screen. Throws {@link IllegalStateException} if the
   * column does not exist or there are multiple visible rows with that cell.
   */
  public XPath getRowByCell( String cellContent ) {
    XPath row = rowWithCellText( grid, cellContent );
    int count = rap.getXPathCount( row );
    if( count == 1 ) {
      return XPath.byId( rap.getId( row ) );
    } else if( count == 0 ) {
      return null;
    } else {
      throw new IllegalStateException( "There are multiple rows matching " + row );
    }
  }

  /**
   * @param grid
   * @param row path to row relative to grid. Expects left-side row for fixed column grids
   * @param columnName
   * @return
   */
  public String getCellContent( XPath row, String columnName ) {
    rap.checkElementCount( row.toString() );
    String columnId = getColumnId( grid, columnName );
    String cell = row + cellDescribedBy( columnId );
    if( rap.getXPathCount( cell ) == 0 && rap.getFlowTo( row ).endsWith( "_1" ) ) {
      cell = XPath.byId( rap.getFlowTo( row ) ).toString() + cellDescribedBy( columnId );
    }
    rap.checkElementCount( cell );
    return rap.selenium.getText( cell );
  }

  public String getColumnId( String grid, String columnName ) {
    return getColumnId( asXPath( grid ), columnName );
  }

  public String getColumnId( AbstractPath<?> grid, String columnName ) {
    return rap.getId( asXPath( grid ).descendants().widget( COLUMN_HEADER, with().textContent( columnName ) ) );
  }

  public int getLineHeight() {
    return rap.selenium.getElementHeight( row( with().offset( 1 ) ).toString() ).intValue();
  }

  public XPath rows() {
    // excludes header:
    return getClientArea().children().widget( ROW );
  }

  public XPath row( Predicate predicate ) {
    return getClientArea().children().widget( ROW, predicate );
  }

  /////////
  // Helper

  private void scrollIntoView( XPath needle ) {
    if( rap.isElementAvailable( needle ) ) {
      return;
    }
    int startOffset = getLineOffset();
    int currentOffset = getLineOffset();
    int max = getMaxLineOffset();
    while( !rap.isElementAvailable( needle ) && currentOffset < max ) {
      scrollPageDown();
      currentOffset = getLineOffset();
      waitForItems();
      if( currentOffset == startOffset ) {
        break;
      }
    }
    if( rap.isElementAvailable( needle ) ) {
      return;
    }
    scrollLineIntoView( 0 );
    waitForItems();
    while( !rap.isElementAvailable( needle ) && getLineOffset() < startOffset ) {
      scrollPageDown();
      waitForItems();
    }
    if( !rap.isElementAvailable( needle ) ) {
      throw new IllegalStateException( "Item not found in grid" );
    }
  }

  private void scrollGridByPage( int direction ) {
    checkGrid();
    String scrollbar = grid + vScrollBar();
    if( rap.getXPathCount( scrollbar ) == 0 ) {
      return;
    } else {
      String clientarea = getAriaControls( scrollbar );
      int rows = getVisibleLineCount();
      int delta = ( int )Math.floor( rows / 2F ) * direction; // one wheel
                                                              // "click" scrolls
                                                              // by 2 items
      rap.scrollWheel( clientarea, delta );
    }
  }

  private String getAriaControls( String xpath ) {
    return getAriaControls( asXPath( xpath ) ).toString();
  }

  private XPath getAriaControls( AbstractPath<?> xpath ) {
    String controls = rap.getAttribute( xpath, "aria-controls" );
    return XPath.byId( controls.split( "\\s" )[ 0 ] );
  }

  private void checkGrid() {
    rap.checkElementCount( grid.toString() );
  }

  private static String vScrollBar() {
    return any().widget( "scrollbar", "orientation", "vertical" ).toString();
  }

  private static XPath rowWithCellText( AbstractPath<?> grid, String cellContent ) {
    return asXPath( grid ).append( any().widget( ROW, with().content( cellWithText( cellContent ) ) ) );
  }

  private static XPath rowWithCellText( AbstractPath<?> grid, String columnId, String cellContent ) {
    return asXPath( grid ).append( any().widget( ROW, with().content( cellWithText( columnId, cellContent ) ) ) );
  }

  private static XPath cellWithText( String content ) {
    return any().widget( GRID_CELL, content );
  }

  private static XPath cellWithText( String columnId, String content ) {
    return any().widget( GRID_CELL, with().text( content ).aria( "describedby", columnId ) );
  }

  private static String cellDescribedBy( String columnId ) {
    return any().widget( GRID_CELL, with().aria( "describedby", columnId ) ).toString();
  }

}
