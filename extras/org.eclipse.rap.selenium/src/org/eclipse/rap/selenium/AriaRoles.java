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


public final class AriaRoles {

  private AriaRoles() {
    // creating instances not allowed
  }

  /**
   * Matches org.eclipse.swt.widget.Button/org.eclipse.swt.widgets.ToolItem with PUSH style
   * and org.eclipse.swt.widgets.Link
   */
  public static final String BUTTON = "button";

  /**
   * Matches org.eclipse.swt.widget.Button/org.eclipse.swt.widgets.ToolItem
   * with CHECK OR TOGGLE style
   */
  public static final String CHECK_BOX = "checkbox";

  /**
   * Matches org.eclipse.swt.widget.Button/org.eclipse.swt.widgets.ToolItem with RADIO style
   */
  public static final String RADIO = "radio";

  /**
   * Matches the input element of org.eclipse.swt.widget.Combo/org.eclipse.swt.custom.CCombo
   * or org.eclipse.swt.widgets.DateTime with DROP_DOWN style
   */
  public static final String COMBO_BOX = "combobox";

  /**
   * Matches org.eclipse.swt.widget.List and the drop-down of org.eclipse.swt.widget.Combo
   */
  public static final String LIST_BOX = "listbox";

  /**
   * Matches items in org.eclipse.swt.widget.List and org.eclipse.swt.widget.Combo
   */
  public static final String OPTION = "option";

  /**
   * Matches org.eclipse.swt.widget.Composite and org.eclipse.swt.widget.ScrolledComposite
   */
  public static final String GROUP = "group";

  /**
   * Matches org.eclipse.swt.widgets.Spinner
   * and org.eclipse.swt.widgets.DateTime with DATE or TIME style
   */
  public static final String SPIN_BUTTON = "spinbutton";

  /**
   * Matches org.eclipse.swt.widgets.Group
   */
  public static final String REGION = "region";

  /**
   * Matches org.eclipse.swt.widgets.Menu
   */
  public static final String MENU = "menu";

  /**
   * Matches org.eclipse.swt.widgets.MenuItem without CHECK or RADIO style
   */
  public static final String MENU_ITEM = "menuitem";

  /**
   * Matches org.eclipse.swt.widgets.MenuItem with CHECK style
   */
  public static final String MENU_ITEM_CHECK_BOX = "menuitemcheckbox";

  /**
   * Matches org.eclipse.swt.widgets.MenuItem with RADIO style
   */
  public static final String MENU_ITEM_RADIO = "menuitemradio";

  /**
   * Matches org.eclipse.swt.widgets.ProgressBar
   */
  public static final String PROEGRESS_BAR = "progressbar";

  /**
   * Matches org.eclipse.swt.widgets.Scale
   */
  public static final String SLIDER = "slider";

  /**
   * Matches org.eclipse.swt.widgets.Shell if the parent is Display
   */
  public static final String APPLICATION = "application";

  /**
   * Matches org.eclipse.swt.widgets.Shell if the parent is another Shell
   */
  public static final String DIALOG = "dialog";

  /**
   * Matches org.eclipse.swt.widgets.Table
   */
  public static final String GRID = "grid";

  /**
   * Matches org.eclipse.swt.widgets.TableColumn and org.eclipse.swt.widgets.TreeColumn
   */
  public static final String COLUMN_HEADER = "columnheader";

  /**
   * Matches VISIBLE org.eclipse.swt.widgets.TableItem, org.eclipse.swt.widgets.TreeItem
   * AND the header of org.eclipse.swt.widgets.Table and org.eclipse.swt.widgets.Tree
   */
  public static final String ROW = "row";

  /**
   * Matches VISIBLE cells of org.eclipse.swt.widgets.TableItem and org.eclipse.swt.widgets.TreeItem
   */
  public static final String GRID_CELL = "gridcell";

  /**
   * Matches the input element of org.eclipse.swt.widgets.Text
   */
  public static final String TEXT_BOX = "textbox";

  /**
   * Matches org.eclipse.swt.widgets.ToolBar
   */
  public static final String TOOL_BAR = "toolbar";

  /**
   * Matches org.eclipse.swt.widgets.Tree
   */
  public static final String TREE_GRID = "treegrid";

  /**
   * Matches org.eclipse.swt.widgets.CTabFolder
   */
  public static final String TAB_LIST = "tablist";

  /**
   * Matches org.eclipse.swt.widgets.CTabItem
   */
  public static final String TAB = "tab";

  /**
   * Matches org.eclipse.swt.widgets.ScrollBar
   */
  public static final String SCROLL_BAR = "scrollbar";

}
