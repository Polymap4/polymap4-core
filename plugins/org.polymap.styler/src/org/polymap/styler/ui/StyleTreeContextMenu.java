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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.rwt.graphics.Graphics;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.LineSymbolizer;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.Style;
import org.geotools.styling.TextSymbolizer;
import org.polymap.styler.DefaultFeatureTypeStyle;
import org.polymap.styler.DefaultRules;
import org.polymap.styler.DefaultSymbolizers;
import org.polymap.styler.FeatureTypeStyleWrapper;
import org.polymap.styler.FilterWrapper;
import org.polymap.styler.RuleWrapper;
import org.polymap.styler.StyleWrapper;
import org.polymap.styler.SymbolizerWrapper;

public class StyleTreeContextMenu implements IMenuListener {

	private static final Log log = LogFactory.getLog(StyleTreeContextMenu.class);

	private StyleView style_view;
	private StyleTree tree;
	
	public StyleTreeContextMenu(StyleTree tree,StyleView style_view) {
		this.tree=tree;
		this.style_view=style_view;
	}

	@Override
	public void menuAboutToShow(IMenuManager manager) {

		// Context-menu for a style
		if (tree.getTree().getSelection()[0].getData() instanceof StyleWrapper) {
			
			
			if (StylerSessionSingleton.getInstance().getExpertMode()) {
			
				Action add_fts_action = new Action() {
				public void run() {
					super.run();

					((StyleWrapper) tree.getTree().getSelection()[0].getData())
							.getStyle().featureTypeStyles().add(
									DefaultFeatureTypeStyle
											.getPlainFeatureTypeStyle());
					tree.refresh_tree();
				}
				};

				add_fts_action.setText(Messages.get().ADD_FTS);
				add_fts_action.setImageDescriptor(ImageDescriptor
					.createFromImage(Graphics.getImage("icons/StyleEntry.gif", //$NON-NLS-1$
							getClass().getClassLoader())));
				manager.add(add_fts_action);

			} else
			{// no expert
				
				addSymboilizerActions(manager,null,((StyleWrapper) tree.getTree().getSelection()[0].getData())
						.getStyle());
				/*
				Action add_poly_action = new Action() {
					public void run() {
						super.run();
						PolygonSymbolizer new_poly_symbolizer=DefaultSymbolizers.getDefaultPolygonSymbolizer();
				
						
						FeatureTypeStyle new_fts=DefaultFeatureTypeStyle
						.getPlainFeatureTypeStyle();
						
						
						Rule new_rule=DefaultRules.getPlainRule();
												
						new_rule.symbolizers().add(new_poly_symbolizer);
						new_fts.rules().add(new_rule);
						
						
						
						((StyleWrapper) tree.getTree().getSelection()[0].getData())
						.getStyle().featureTypeStyles().add(new_fts);
							refresh_tree(new_poly_symbolizer);
						

					}
				};
				add_poly_action.setText(Messages.get().ADD_POLYGON_SYMBOLIZER);
				add_poly_action.setImageDescriptor(ImageDescriptor
						.createFromImage(Graphics.getImage("icons/polygon.gif", //$NON-NLS-1$
								getClass().getClassLoader())));
				manager.add(add_poly_action);	
			*/	
			}
			

			Action switch_expert_action = new Action() {
				public void run() {
					super.run();

					StylerSessionSingleton.getInstance().setExpertMode(!StylerSessionSingleton.getInstance().getExpertMode());
					tree.refresh_tree();
				}
				};

				String switch_expert_text="";
				
				if (StylerSessionSingleton.getInstance().getExpertMode()) 
					switch_expert_text="switch to beginner mode";
				else
					switch_expert_text="switch to expert mode";
				
				switch_expert_action.setText(switch_expert_text);
				manager.add(switch_expert_action);
			
			
		// Context-menu for a Symbolizer
		} else if (tree.getTree().getSelection()[0].getData() instanceof SymbolizerWrapper) {
			Action remove_symbol_action = new Action() {
				public void run() {
					super.run();
					
			
					if (!StylerSessionSingleton.getInstance().getExpertMode()) {
					
					RuleWrapper rw=	((SymbolizerWrapper) tree.getTree().getSelection()[0].getData()).getRuleWrapper();
					FeatureTypeStyleWrapper fts_w=rw.getFeatureTypeStyleWrapper();
					
					if ((fts_w.getFeatureTypeStyle().rules().size()==1)&&(rw.getRule().symbolizers().size()==1))
					{
						
						((SymbolizerWrapper) tree.getTree().getSelection()[0].getData())
						.dispose();
						rw.dispose();
						fts_w.dispose();
						
					}
					else
						((SymbolizerWrapper) tree.getTree().getSelection()[0].getData())
						.dispose();
						
					}
					else // expert mode - just remove the symbolizer and leave the rest alone 
					((SymbolizerWrapper) tree.getTree().getSelection()[0].getData())
					.dispose();
					
					tree.refresh_tree();
				}
			};
			remove_symbol_action.setText(Messages.get().REMOVE);
			remove_symbol_action.setImageDescriptor(ImageDescriptor
					.createFromImage(Graphics.getImage("icons/delete.gif", //$NON-NLS-1$
							getClass().getClassLoader())));
			manager.add(remove_symbol_action);

		// Context-menu for a FeatureTypeStyle
		} else if (tree.getTree().getSelection()[0].getData() instanceof FeatureTypeStyleWrapper) {

			if (((FeatureTypeStyleWrapper) tree.getTree().getSelection()[0].getData()).canMoveUp()) {
				Action move_fts_up_action = new Action() {
					public void run() {
						super.run();
						((FeatureTypeStyleWrapper) tree.getTree().getSelection()[0].getData())
							.moveUp();
						tree.refresh_tree();
					}
				};
				move_fts_up_action.setText(Messages.get().MOVE_UP);
				move_fts_up_action.setImageDescriptor(ImageDescriptor
					.createFromImage(Graphics.getImage("icons/up_co.gif", //$NON-NLS-1$
							getClass().getClassLoader())));
				manager.add(move_fts_up_action);
			}
			
			if (((FeatureTypeStyleWrapper) tree.getTree().getSelection()[0].getData()).canMoveDown()) {
				Action move_fts_up_action = new Action() {
					public void run() {
						super.run();
						((FeatureTypeStyleWrapper) tree.getTree().getSelection()[0].getData())
							.moveDown();
						tree.refresh_tree();
					}
				};
				move_fts_up_action.setText(Messages.get().MOVE_DOWN);
				move_fts_up_action.setImageDescriptor(ImageDescriptor
					.createFromImage(Graphics.getImage("icons/down_co.gif", //$NON-NLS-1$
							getClass().getClassLoader())));
				manager.add(move_fts_up_action);
			}
			
			Action rm_fts_action = new Action() {
				public void run() {
					super.run();
					((FeatureTypeStyleWrapper) tree.getTree().getSelection()[0].getData())
							.dispose();
					tree.refresh_tree();
				}
			};
			rm_fts_action.setText(Messages.get().REMOVE_FTS);
			rm_fts_action.setImageDescriptor(ImageDescriptor
					.createFromImage(Graphics.getImage("icons/delete.gif", //$NON-NLS-1$
							getClass().getClassLoader())));
			manager.add(rm_fts_action);

			Action add_rule_action = new Action() {
				public void run() {
					super.run();
					((FeatureTypeStyleWrapper) tree.getTree().getSelection()[0].getData())
							.getFeatureTypeStyle().rules().add(
									DefaultRules.getPlainRule());
					tree.refresh_tree();
				}
			};
			add_rule_action.setText(Messages.get().ADD_RULE);
			add_rule_action.setImageDescriptor(ImageDescriptor
					.createFromImage(Graphics.getImage("icons/rule.gif", //$NON-NLS-1$
							getClass().getClassLoader())));
			manager.add(add_rule_action);

		// Context-menu for a Rule
		} else if (tree.getTree().getSelection()[0].getData() instanceof RuleWrapper) {
			final RuleWrapper rule_wrapper = ((RuleWrapper) tree.getTree().getSelection()[0]
					.getData());

			Action delete_rule_action = new Action() {
				public void run() {
					super.run();

					rule_wrapper.dispose();
					tree.refresh_tree();

				}
			};
			delete_rule_action.setText(Messages.get().REMOVE);
			delete_rule_action.setImageDescriptor(ImageDescriptor
					.createFromImage(Graphics.getImage("icons/delete.gif", //$NON-NLS-1$
							getClass().getClassLoader())));
			manager.add(delete_rule_action);


			if (!rule_wrapper.hasFilter()) // only one filter per rule
			{
			Action add_filter_action = new Action() {
				public void run() {
					super.run();

					rule_wrapper.addDefaultFilter();
					tree.refresh_tree();

				}
				};
				add_filter_action.setText(Messages.get().ADD_FILTER);
				add_filter_action.setImageDescriptor(ImageDescriptor
					.createFromImage(Graphics.getImage("icons/filter.gif", //$NON-NLS-1$
							getClass().getClassLoader())));
				manager.add(add_filter_action);
			}
			
			addSymboilizerActions(manager,rule_wrapper.getRule(),null);
			
		
		// Context-menu for a Filter
		} else if (tree.getTree().getSelection()[0].getData() instanceof FilterWrapper) {
			final FilterWrapper filter_wrapper = ((FilterWrapper) tree.getTree().getSelection()[0]
					.getData());

			
			Action delete_filter_action = new Action() {
				public void run() {
					super.run();

					filter_wrapper.dispose();
					tree.refresh_tree();

				}
			};
			delete_filter_action.setText(Messages.get().REMOVE);
			delete_filter_action.setImageDescriptor(ImageDescriptor
					.createFromImage(Graphics.getImage("icons/delete.gif", //$NON-NLS-1$
							getClass().getClassLoader())));
			manager.add(delete_filter_action);

			
			if (filter_wrapper.isLogicFilter())
			{
				Action add_subfilter_action = new Action() {
					public void run() {
						super.run();

						filter_wrapper.addDefaultFilter();
						tree.refresh_tree();

					}
					};
					add_subfilter_action.setText(Messages.get().ADD_FILTER);
					add_subfilter_action.setImageDescriptor(ImageDescriptor
						.createFromImage(Graphics.getImage("icons/filter.gif", //$NON-NLS-1$
								getClass().getClassLoader())));
					manager.add(add_subfilter_action);
			}
				
			
			
		}

	}
	
	/**
	 * 
	 * add actions to add symbolizers
	 * rule or style must not be null
	 * 
	 * @param manager
	 * @param rule
	 * @param style
	 */
	
	public void addSymboilizerActions(IMenuManager manager,final Rule rule,final Style style) {
		
		
		Action add_poly_action = new Action() {
			public void run() {
				super.run();
				
				Rule _rule=null;
				if (rule!=null) 
					_rule=rule;
				else 
				{
					if (style==null) 
					{
						log.error("either rule or style must not be null");
						return;
					}
					FeatureTypeStyle new_fts=DefaultFeatureTypeStyle
					.getPlainFeatureTypeStyle();
					_rule=DefaultRules.getPlainRule();
					new_fts.rules().add(_rule);
					style.featureTypeStyles().add(new_fts);
				}
				
				PolygonSymbolizer new_poly_symbolizer=DefaultSymbolizers.getDefaultPolygonSymbolizer();
				_rule.symbolizers().add(new_poly_symbolizer
						);
				style_view.refresh_tree(new_poly_symbolizer);

			}
		};
		
		
		add_poly_action.setText(Messages.get().ADD_POLYGON_SYMBOLIZER);
		add_poly_action.setImageDescriptor(ImageDescriptor
				.createFromImage(Graphics.getImage("icons/polygon.gif", //$NON-NLS-1$
						getClass().getClassLoader())));
		manager.add(add_poly_action);

		Action add_text_action = new Action() {
			public void run() {
				super.run();

				Rule _rule=null;
				if (rule!=null) 
					_rule=rule;
				else 
				{
					if (style==null) 
					{
						log.error("either rule or style must not be null");
						return;
					}
					FeatureTypeStyle new_fts=DefaultFeatureTypeStyle
					.getPlainFeatureTypeStyle();
					_rule=DefaultRules.getPlainRule();
					new_fts.rules().add(_rule);
					style.featureTypeStyles().add(new_fts);
				}
				
				TextSymbolizer new_txt_symbolizer=DefaultSymbolizers.getDefaultTextSymbolizer();
				_rule.symbolizers().add(
						new_txt_symbolizer);
				
				style_view.refresh_tree(new_txt_symbolizer);

			}
		};
		add_text_action.setText(Messages.get().ADD_TEXT_SYMBOLIZER);
		add_text_action.setImageDescriptor(ImageDescriptor
				.createFromImage(Graphics.getImage("icons/text.gif", //$NON-NLS-1$
						getClass().getClassLoader())));
		manager.add(add_text_action);

		Action add_point_action = new Action() {
			public void run() {
				super.run();

				Rule _rule=null;
				if (rule!=null) 
					_rule=rule;
				else 
				{
					if (style==null) 
					{
						log.error("either rule or style must not be null");
						return;
					}
					FeatureTypeStyle new_fts=DefaultFeatureTypeStyle
					.getPlainFeatureTypeStyle();
					_rule=DefaultRules.getPlainRule();
					new_fts.rules().add(_rule);
					style.featureTypeStyles().add(new_fts);
				}
				
				PointSymbolizer new_pnt_symbolizer=DefaultSymbolizers.getDefaultPointSymbolizer();
				_rule.symbolizers().add(
						new_pnt_symbolizer);
				style_view.refresh_tree(new_pnt_symbolizer);

			}
		};
		add_point_action.setText(Messages.get().ADD_POINT_SYMBOLIZER);
		add_point_action.setImageDescriptor(ImageDescriptor
				.createFromImage(Graphics.getImage("icons/point.gif", //$NON-NLS-1$
						getClass().getClassLoader())));
		manager.add(add_point_action);

		Action add_line_action = new Action() {
			public void run() {
				super.run();
				
				Rule _rule=null;
				if (rule!=null) 
					_rule=rule;
				else 
				{
					if (style==null) 
					{
						log.error("either rule or style must not be null");
						return;
					}
					FeatureTypeStyle new_fts=DefaultFeatureTypeStyle
					.getPlainFeatureTypeStyle();
					_rule=DefaultRules.getPlainRule();
					new_fts.rules().add(_rule);
					style.featureTypeStyles().add(new_fts);
				}
				
				LineSymbolizer new_line_symbolizer=DefaultSymbolizers.getDefaultLineSymbolizer();
				_rule.symbolizers().add(new_line_symbolizer
						);
				style_view.refresh_tree(new_line_symbolizer);
			}
		};
		add_line_action.setImageDescriptor(ImageDescriptor
				.createFromImage(Graphics.getImage("icons/line.gif", //$NON-NLS-1$
						getClass().getClassLoader())));
		add_line_action.setText(Messages.get().ADD_LINE_SYMBOLIZER);
		// add_line_action.setImageDescriptor(
		// Util.getImageRegistry().getDescriptor("copy_24"));
		manager.add(add_line_action);

	}






}
