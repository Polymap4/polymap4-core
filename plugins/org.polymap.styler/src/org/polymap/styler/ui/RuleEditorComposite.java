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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeItem;
import org.polymap.styler.RuleWrapper;
import org.polymap.styler.StyleHelper;
import org.polymap.styler.helper.LayoutHelper;

/**
 * the view of the styler
 * 
 * @author Marcus -LiGi- B&uuml;schleb < mail: ligi (at) polymap (dot) de >
 * 
 */

public class RuleEditorComposite {

	private static final Log log = LogFactory.getLog(RuleEditorComposite.class);

	public String[] val_types = { Messages.get().LITERAL, Messages.get().ATTRIBUTE };

	public String[] scale_types = { "100.0", "1000.0", "10000.0", "100000.0", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			"1000000.0", "10000000.0" }; //$NON-NLS-1$ //$NON-NLS-2$

	
	private TreeItem tree_item;
	
	/*
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
	*/

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public RuleEditorComposite(final Composite parent, RuleWrapper rule_wrapper,TreeItem _tree_item) {
		tree_item=_tree_item;
		
		parent.setLayout(new FillLayout());
		/*
		 * Composite very_rule_outer_composite=new Composite(parent,SWT.NONE);
		 * very_rule_outer_composite.setLayout(new FillLayout());
		 */
		final Composite rule_outer_composite = new Composite(parent, SWT.NONE);
		// rule_outer_composite.setLayout(new FillLayout());

		// final Composite rule_outer_composite=parent;

		log.info("creating rule editor layout"); //$NON-NLS-1$

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
		rule_outer_composite.setLayout(layout);

		/*
		 * Composite actions = subpart(parent, "Special Actions", 1);
		 * add_geometry_btn = new Button(actions, SWT.PUSH);
		 * add_geometry_btn.setText("Add some geometry"); //
		 * add_geometry_btn.addMouseListener(this);
		 */

		/*Composite topic = LayoutHelper.subpart(rule_outer_composite, 
				Messages.get().RULE_EDITOR, 1);
		Label label = new Label(topic, SWT.NONE);
		label.setText(""); //$NON-NLS-1$
*/
		
		Composite rule_name_composite = LayoutHelper.subpart(rule_outer_composite, Messages.get().NAME,
				1);

		Text rule_name_txt = new Text(rule_name_composite, SWT.BORDER);

		rule_name_txt.setText(StyleHelper.Rule2Name(rule_wrapper.getRule()));
		rule_name_txt.setData(rule_wrapper);

		rule_name_txt.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent event) {

				((RuleWrapper) (event.widget.getData())).getRule().setName(
						((Text) event.widget).getText());

				tree_item.setText(((Text) event.widget).getText());
			}
		});

		// RuleWrapper rule_wrapper=new RuleWrapper(rule);

		Composite min_scale_composite = LayoutHelper.subpart(rule_outer_composite,
				Messages.get().MINSCALE, 2);

		Button min_scale_active = new Button(min_scale_composite, SWT.CHECK);
		min_scale_active.setData(rule_wrapper);
		final Combo min_scale_combo = new Combo(min_scale_composite, SWT.NONE);
		min_scale_active
				.setSelection(rule_wrapper.getMinScaleDenominator() != 0.0);

		min_scale_active.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				Button thisButton = (Button) e.widget;

				if (thisButton.getSelection()) {
					min_scale_combo.setEnabled(true);
					try {
						((RuleWrapper) e.widget.getData())
								.setMinScaleDenominator(Double
										.parseDouble(min_scale_combo.getText()));
					} catch (NumberFormatException ex) {
					}
				} else {
					min_scale_combo.setEnabled(false);
					((RuleWrapper) thisButton.getData())
							.setMinScaleDenominator(0.0);
				}
			}
		});

		min_scale_combo.setItems(scale_types);

		// Text min_scale_text=new Text(min_scale_composite,SWT.BORDER);
		// min_scale_text.setText(" " + rule_wrapper.getMinScaleDenominator());
		min_scale_combo.setData(rule_wrapper);

		if (rule_wrapper.getMinScaleDenominator() == 0.0)
			min_scale_combo.setEnabled(false);
		else
			min_scale_combo.setText("" + rule_wrapper.getMinScaleDenominator()); //$NON-NLS-1$

		min_scale_combo.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent event) {
				try {
					((RuleWrapper) event.widget.getData())
							.setMinScaleDenominator(Double
									.parseDouble(((Combo) event.widget)
											.getText()));
				} catch (NumberFormatException e) {
				}

			}
		});

		Composite max_scale_composite = LayoutHelper.subpart(rule_outer_composite, //rule_outer_composite,
				Messages.get().MAXSCALE, 2);

		Button max_scale_active = new Button(max_scale_composite, SWT.CHECK);
		max_scale_active.setData(rule_wrapper);
		max_scale_active.setSelection(!rule_wrapper.getMaxScaleDenominator()
				.isInfinite());

		final Combo max_scale_combo = new Combo(max_scale_composite, SWT.NONE);

		max_scale_active.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				Button thisButton = (Button) e.widget;

				if (thisButton.getSelection()) {
					max_scale_combo.setEnabled(true);
					try {
						((RuleWrapper) e.widget.getData())
								.setMaxScaleDenominator(Double
										.parseDouble(max_scale_combo.getText()));
					} catch (NumberFormatException ex) {
					}
				} else {
					max_scale_combo.setEnabled(false);
					((RuleWrapper) thisButton.getData())
							.setMaxScaleDenominator(Double.POSITIVE_INFINITY);
				}
			}
		});

		max_scale_combo.setItems(scale_types);

		// Text min_scale_text=new Text(min_scale_composite,SWT.BORDER);
		// min_scale_text.setText(" " + rule_wrapper.getMinScaleDenominator());
		max_scale_combo.setData(rule_wrapper);

		if (rule_wrapper.getMaxScaleDenominator().isInfinite())
			max_scale_combo.setEnabled(false);
		else
			max_scale_combo.setText("" + rule_wrapper.getMaxScaleDenominator()); //$NON-NLS-1$

		max_scale_combo.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent event) {
				try {
					((RuleWrapper) event.widget.getData())
							.setMaxScaleDenominator(Double
									.parseDouble(((Combo) event.widget)
											.getText()));
				} catch (NumberFormatException e) {
				}

			}
		});


	}

}