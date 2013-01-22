/*
 * polymap.org
 * Copyright 2009, Polymap GmbH, and individual contributors as indicated
 * by the @authors tag.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 */
package org.polymap.styler.ui;


import java.io.IOException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.rwt.graphics.Graphics;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.geotools.styling.Rule;
import org.geotools.styling.Style;
import org.opengis.filter.Filter;
import org.polymap.core.style.IStyle;
import org.polymap.styler.FeatureTypeStyleWrapper;
import org.polymap.styler.FilterWrapper;
import org.polymap.styler.RuleWrapper;
import org.polymap.styler.StyleHelper;
import org.polymap.styler.StyleWrapper;
import org.polymap.styler.helper.SymbolizerHelper;
import org.polymap.styler.SymbolizerWrapper;

public class StyleTree {

	private static final Log log = LogFactory.getLog(StyleTree.class);

	private Tree tree;
	
	
	private StyleView style_view;
	private StyleTreeContextMenu context_menu;

	
	public StyleTree(Composite parent_composite,StyleView style_view) {

		
		this.style_view=style_view;
		context_menu=new StyleTreeContextMenu(this,style_view);
	
		tree = new Tree(parent_composite, SWT.FILL);
		
		MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(context_menu);
		Menu menu = menuMgr.createContextMenu(tree);
		tree.setMenu(menu);
		
		/*
		try {
		getSite().registerContextMenu(menuMgr, this);
		}
		catch(Exception e) {
			
		}*/
		
	
	}
	
	
	public Object extract_from_wrapper(Object obj) {
		
		if (obj instanceof SymbolizerWrapper)
			return ((SymbolizerWrapper)obj).getSymbolizer();
		
		
		if (obj instanceof RuleWrapper)
			return ((RuleWrapper)obj).getRule();


		if (obj instanceof FeatureTypeStyleWrapper)
			return ((FeatureTypeStyleWrapper)obj).getFeatureTypeStyle();
		
		if (obj instanceof StyleWrapper)
			return ((StyleWrapper)obj).getStyle();
		
		
		if (obj instanceof FilterWrapper)
			return ((FilterWrapper)obj).getFilter();
		
		return obj;
	}
	
	
	
	public boolean compare_inner(Object left,Object right) {
		return extract_from_wrapper(left)==extract_from_wrapper(right);
	}
	
	public void create_tree(IStyle istyle_from_reader,Object preselected) {

		Style style_from_reader=null;
		
		try {
			style_from_reader=istyle_from_reader.resolve(Style.class, null);
		} catch (IOException e1) {}
		
		try {
			style_view.editor_composite.dispose();
			
		} catch (Exception e) {
		}
		;
		style_view.editor_composite = new Composite(style_view.editor_outer_composite, SWT.BORDER);

		
		Object act_selected=null;
		

		if (preselected==null) {
			log.info("act selection count " + tree.getSelection().length );
			if (tree.getSelection().length>0)		
				act_selected=tree.getSelection()[0].getData();
		}
		else {
			act_selected=preselected;
		}
		
		//TreeItem item2select=null;
		
		tree.removeAll();

		tree.setLayoutData(new GridData(GridData.FILL_BOTH
				| GridData.GRAB_HORIZONTAL));

		
		boolean flat=!StylerSessionSingleton.getInstance().getExpertMode();
		
		TreeItem root_item = new TreeItem(tree, SWT.NULL);
		root_item.setText("/"); //$NON-NLS-1$
		
		StyleWrapper style_w=new StyleWrapper(istyle_from_reader,style_view.act_layer);
		root_item.setData(style_w);

		root_item.setImage(Graphics.getImage("icons/style_tsk.gif", getClass() //$NON-NLS-1$
				.getClassLoader()));

		// adding featurestyle types to root node
		for (int fts_id = 0; fts_id < style_from_reader.featureTypeStyles()
				.size(); fts_id++) {
			TreeItem fts_item=null;
			
			FeatureTypeStyleWrapper fts_w = new FeatureTypeStyleWrapper(
					style_from_reader.featureTypeStyles().get(fts_id),
					style_from_reader);
			if (!flat) {
				fts_item = new TreeItem(root_item, SWT.NULL);
				fts_item.setText(style_from_reader.featureTypeStyles().get(fts_id)
					.getName());
				fts_item.setImage(Graphics.getImage("icons/StyleEntry.gif", //$NON-NLS-1$
					getClass().getClassLoader()));
				fts_item.setExpanded(true);

				
				fts_item.setData(fts_w);
			}  
			// adding rule items to featuretypestyle nodes
			for (int rule_id = 0; rule_id < style_from_reader
					.featureTypeStyles().get(fts_id).rules().size(); rule_id++) {
				
				TreeItem rule_item=null;
				Rule rule = style_from_reader.featureTypeStyles().get(fts_id)
				.rules().get(rule_id);
				RuleWrapper rule_wrapper=new RuleWrapper(rule, style_view.act_layer, fts_w);
				if (!flat) {
				
					rule_item = new TreeItem(fts_item, SWT.NULL);
					
					rule_item.setText(StyleHelper.Rule2Name(rule));

					
					rule_item.setData(rule_wrapper);

					rule_item.setImage(Graphics.getImage("icons/rule.gif", //$NON-NLS-1$
						getClass().getClassLoader()));
				
				
					if (rule_wrapper.hasFilter()) {
						FilterWrapper filter_wrapper=new FilterWrapper(rule_wrapper.getRule(),style_view.act_layer);
						add_filter_to_tree(filter_wrapper,rule_item);
					}
						
				}
				
				for (int s = 0; s < rule.getSymbolizers().length; s++) {
					TreeItem symbolizer_item=null;
					
					if (flat)
						symbolizer_item = new TreeItem(root_item, SWT.NULL);
					else
						symbolizer_item = new TreeItem(rule_item, SWT.NULL);
					/*
					 * String s_name = (rule.getSymbolizers()[s].getClass())
					 * .toString(); if (s_name == null) s_name = "unknown name";
					 */
					SymbolizerWrapper symbolizer_wrapper = new SymbolizerWrapper(
							rule.getSymbolizers()[s], style_view.act_layer ,rule_wrapper);

/*					symbolizer_item.setText(SymbolizerHelper
							.Symbolizer2TypeName(rule.getSymbolizers()[s]));
*/
					symbolizer_item.setText(symbolizer_wrapper.getName());

					symbolizer_item.setData(symbolizer_wrapper);


					
					symbolizer_item.setImage(Graphics.getImage("icons/" //$NON-NLS-1$
							+ SymbolizerHelper.Symbolizer2ShortTypeName(rule
									.getSymbolizers()[s]) + ".gif", getClass() //$NON-NLS-1$
							.getClassLoader()));

				}
				if (rule_item!=null)
					rule_item.setExpanded(true);
			}
			if (fts_item!=null)
				fts_item.setExpanded(true);

		}
		root_item.setExpanded(true);
		
		TreeItem item2select =recursiveFindSelected(root_item,act_selected);
	
		
		log.info("item 2 select new method " + item2select);
	
		if (item2select !=null) {
			item2select.setExpanded(true);
			tree.select(item2select);
			style_view.refreshEditor(item2select );
		}
	}
	
	
	public TreeItem recursiveFindSelected(TreeItem search_tree,Object selected_data) {
		
		if (compare_inner(search_tree.getData(),selected_data))
			return search_tree;
		
		for (TreeItem act : search_tree.getItems())
		{
			if (compare_inner(act.getData(),selected_data))
				return act;
			 
			TreeItem found=recursiveFindSelected(act,selected_data);
			if (found!=null)
				return found;
		}		
		return null;
	}
	
	private TreeItem add_filter_to_tree(FilterWrapper filter2add,TreeItem parent_tree_item) {
		TreeItem new_filter_item = new TreeItem(parent_tree_item,SWT.NULL);
		new_filter_item.setText(filter2add.getFilterAsString());
		new_filter_item.setImage(Graphics.getImage("icons/filter.gif", //$NON-NLS-1$
				getClass().getClassLoader()));
		new_filter_item.setData(filter2add);
		recursive_filter_adder(new_filter_item,filter2add);
		return new_filter_item;
		
	}
	
	private void recursive_filter_adder(TreeItem filterItem,
			FilterWrapper filterWrapper) {
		List<Filter> filter_iterator=filterWrapper.getChildFilterIterator();
		
		// return if there is no iterator to iterate on 
		if (filterWrapper.getChildFilterIterator()==null) 
			return;
		
		for (int filter_id=0;filter_id<filter_iterator.size();filter_id++)
		{
			Filter act_filter= filter_iterator.get(filter_id);
			FilterWrapper filter_wrapper=new FilterWrapper(act_filter,filterWrapper.getFilter(),style_view.act_layer);
			add_filter_to_tree(filter_wrapper,filterItem);
		}
		
	}

	public void refresh_tree() {
		create_tree(style_view.act_style,null);
	}
	
	public Tree getTree() {
		return tree;
	}
	
}
