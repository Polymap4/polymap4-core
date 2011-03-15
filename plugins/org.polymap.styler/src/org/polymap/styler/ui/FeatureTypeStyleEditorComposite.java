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
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeItem;

import org.polymap.styler.FeatureTypeStyleWrapper;
import org.polymap.styler.helper.LayoutHelper;

/**
 * the view of the styler
 * 
 * @author Marcus -LiGi- B&uuml;schleb < mail: ligi (at) polymap (dot) de >
 * 
 */

public class FeatureTypeStyleEditorComposite {

	private static final Log log = LogFactory
			.getLog(FeatureTypeStyleEditorComposite.class);

	FeatureTypeStyleWrapper fts_w;
	
	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public FeatureTypeStyleEditorComposite(Composite parent,
			FeatureTypeStyleWrapper fts_w,final TreeItem tree_item) {
		
		this.fts_w=fts_w;
		
		
		
		parent.setLayout(LayoutHelper.getDefaultRowLayout());
		
		
		log.info("creating style fts layout"); //$NON-NLS-1$
			
		Composite fts_composite=LayoutHelper.subpart(parent, "FeatureTypeStyle Name",2);
		
		fts_composite.setLayout(new FillLayout());
		

		Text rule_name_txt = new Text(fts_composite, SWT.BORDER);

		rule_name_txt.setText(fts_w.getName());
		rule_name_txt.setData(fts_w);

		rule_name_txt.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent event) {
				String new_name=((Text) event.widget).getText();
				((FeatureTypeStyleWrapper)(event.widget.getData())).setName(new_name);
				tree_item.setText(new_name);
			}
		});
		
		
		Composite fts_abstract_composite=LayoutHelper.subpart(parent, "FeatureTypeStyle Abstract",2);
		
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		//shell.setLayout(gridLayout);

		fts_abstract_composite.setLayout(gridLayout);
		

		Text fts_title_txt = new Text(fts_abstract_composite, SWT.BORDER);
	
		fts_title_txt.setText(fts_w.getTitle());
		fts_title_txt.setData(fts_w);

		fts_title_txt.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent event) {
				String new_name=((Text) event.widget).getText();
				
				
				((FeatureTypeStyleWrapper)(event.widget.getData())).setTitle(new_name);
			}
		});

		
		Text fts_abstract_txt = new Text(fts_abstract_composite, SWT.BORDER | SWT.MULTI | SWT.WRAP);
		
		fts_abstract_txt.setText(fts_w.getAbstract());
		fts_abstract_txt.setData(fts_w);

		fts_abstract_txt.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent event) {
				String new_name=((Text) event.widget).getText();
				
				
				((FeatureTypeStyleWrapper)(event.widget.getData())).setAbstract(new_name);
			}
		});

	}
}