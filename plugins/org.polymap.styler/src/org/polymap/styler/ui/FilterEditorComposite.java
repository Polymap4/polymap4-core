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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeItem;
import org.polymap.styler.FilterWrapper;

/**
 * the view of the styler
 * 
 * @author Marcus -LiGi- B&uuml;schleb < mail: ligi (at) polymap (dot) de >
 * 
 */

public class FilterEditorComposite {

	public String[] val_types = { Messages.get().LITERAL,
			Messages.get().ATTRIBUTE };

	TreeItem filter_tree_item;
	FilterWrapper filter_wrapper;
	
	private Composite subpart(Composite parent, String tag, int width) {
		Composite subpart = new Composite(parent, SWT.NONE);
		RowLayout across = new RowLayout();
		across.type = SWT.HORIZONTAL;
		across.wrap = true;
		across.pack = true;
		across.fill = true;
		across.marginBottom = 1;
		across.marginRight = 2;

		subpart.setLayout(across);

		Label label = new Label(subpart, SWT.NONE);
		label.setText(tag);
		label.setAlignment(SWT.RIGHT);
		RowData data = new RowData();
		data.width = 70;
		data.height = 10;
		label.setLayoutData(data);

		return subpart;
	}
	public void refresh_tree_item_label() {
		filter_tree_item.setText(filter_wrapper.getFilterAsString());
	}
	Composite parent;
	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public FilterEditorComposite(Composite _parent,
			FilterWrapper _filter_wrapper,TreeItem _filter_tree_item,final StyleView style_view) {
		parent=_parent;
		this.filter_wrapper=_filter_wrapper;
		filter_tree_item=_filter_tree_item;
		parent.setLayout(new FillLayout());
		/*
		 * Composite very_rule_outer_composite=new Composite(parent,SWT.NONE);
		 * very_rule_outer_composite.setLayout(new FillLayout());
		 */
		final Composite filter_outer_composite = new Composite(parent, SWT.NONE);
		// rule_outer_composite.setLayout(new FillLayout());

		// final Composite rule_outer_composite=parent;

		/*
		 * FillLayout toolbar_layout = new FillLayout(); toolbar_layout.type =
		 * SWT.HORIZONTAL; parent.setLayout(toolbar_layout);
		 */

		RowLayout layout = new RowLayout();
		layout.pack = false;
		layout.wrap = true;
		layout.type = SWT.VERTICAL;
		layout.fill = true;
		layout.marginLeft = 0;
		layout.marginRight = 0;
		layout.marginTop = 0;
		layout.marginBottom = 0;
		layout.spacing = 0;
		filter_outer_composite.setLayout(layout);

		Composite filter_type_composite = subpart(filter_outer_composite,
				"Filter Type: ", 2);
		Combo filter_type_combo = new Combo(filter_type_composite, SWT.BORDER | SWT.DROP_DOWN);
		filter_type_combo.setItems(new String[] { Messages.get().LOGIC_FILTER,
				Messages.get().COMPARE_FILTER });
		//filter_type_combo.s
		if (filter_wrapper.isLogicFilter())
			filter_type_combo.setText(Messages.get().LOGIC_FILTER);
		else
			filter_type_combo.setText(Messages.get().COMPARE_FILTER);

		filter_type_combo.setData(filter_wrapper);
		
		filter_type_combo.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent event) {
				
				FilterWrapper filter_w= ((FilterWrapper) (event.widget.getData()));
				
				switch (((Combo) (event.widget)).getSelectionIndex())
				{
				case 0:
					filter_w.setToDefaultLogicFilter();	
					break;

				case 1:
					filter_w.setToDefaulCompareFilter();	
					break;
				}
				
						//.setFilterExpression1((((Combo) event.widget)
								//.getText()), false);
				//filter_outer_composite.dispose();
				//new FilterEditorComposite(parent, filter_wrapper,filter_tree_item);
				//refresh_tree_item_label();
				//refresh_tree_item_label();
				//style_view.refreshEditor(filter_tree_item);
				style_view.refresh_tree(filter_w.getFilter());
			}
		});
		
		
		if (filter_wrapper.isCompareFilter()) {
			Composite op1_composite = subpart(filter_outer_composite, Messages
					.get().OPERANT
					+ " 1:", 2);

			Combo op1_type = new Combo(op1_composite, SWT.BORDER);
								
			op1_type.setItems(filter_wrapper.getFeatureAttributes());

			op1_type.setData(filter_wrapper);
			op1_type.addModifyListener(new ModifyListener() {

				@Override
				public void modifyText(ModifyEvent event) {
					((FilterWrapper) (event.widget.getData()))
							.setFilterExpression1((((Combo) event.widget)
									.getText()), false);
					refresh_tree_item_label();
				}
			});
			op1_type.setText("" + filter_wrapper.getFilterExpression1()); //$NON-NLS-1$

			/*
			 * Text op1_value = new Text(op1_composite, SWT.BORDER);
			 * op1_value.setText("" + rule_wrapper.getFilterExpression1());
			 */

			Composite operator_composite = subpart(filter_outer_composite,
					Messages.get().OPERATOR, 1);
			Combo operator_combo = new Combo(operator_composite, SWT.NONE);
			operator_combo.setItems(FilterWrapper.operators);

			operator_combo.setText(filter_wrapper.getFilterOperatorStr());
			operator_combo.setData(filter_wrapper);
			operator_combo.addModifyListener(new ModifyListener() {

				@Override
				public void modifyText(ModifyEvent event) {
					((FilterWrapper) (event.widget.getData()))
							.changeFilterOperator((((Combo) event.widget)
									.getText()));
					refresh_tree_item_label();

				}

			});

			Composite op2_composite = subpart(filter_outer_composite, Messages
					.get().OPERANT
					+ " 2:", 2); //$NON-NLS-1$

			Text op2_value = new Text(op2_composite, SWT.BORDER);

			op2_value.setText("" + filter_wrapper.getFilterExpression2()); //$NON-NLS-1$
			op2_value.setData(filter_wrapper);
			op2_value.addModifyListener(new ModifyListener() {

				@Override
				public void modifyText(ModifyEvent event) {
					((FilterWrapper) (event.widget.getData()))
							.setFilterExpression2((((Text) event.widget)
									.getText()), true);
					refresh_tree_item_label();
				}
			});

			op2_value.setSize(20, 100);
		}
		else // logic filter
		{
			
			Composite logic_type_composite = subpart(filter_outer_composite,
					Messages.get().OPERATOR, 1);
			Combo logic_type_combo = new Combo(logic_type_composite, SWT.NONE);
			logic_type_combo.setItems(new String[] {Messages.get().OR,Messages.get().AND});

			if (filter_wrapper.isOrFilter())
				logic_type_combo.setText(Messages.get().OR);
			else if (filter_wrapper.isAndFilter())
				logic_type_combo.setText(Messages.get().AND);
			else
				logic_type_combo.setText("Unknown Logic Filter");
			
			logic_type_combo.setData(filter_wrapper);
			logic_type_combo.addModifyListener(new ModifyListener() {

				@Override
				public void modifyText(ModifyEvent event) {
					FilterWrapper filter_w= ((FilterWrapper) (event.widget.getData()));
					
					switch (((Combo) (event.widget)).getSelectionIndex())
					{
					case 0:
						filter_w.setToOrFilter();	
						break;

					case 1:
						filter_w.setToAndFilter();	
						break;
					}
					
					refresh_tree_item_label();

				}

			});

		
		}
	}

}