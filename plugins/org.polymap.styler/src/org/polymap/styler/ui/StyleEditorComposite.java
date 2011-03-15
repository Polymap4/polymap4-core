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
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TreeItem;
import org.polymap.styler.StyleChangeListenerInterface;
import org.polymap.styler.SymbolizerWrapper;
import org.polymap.styler.helper.LayoutHelper;

/**
 * the view of the styler
 * 
 * @author Marcus -LiGi- B&uuml;schleb < mail: ligi (at) polymap (dot) de >
 * 
 */

public class StyleEditorComposite {

	private static final Log log = LogFactory
			.getLog(StyleEditorComposite.class);

	

	StyleChangeListenerInterface style_change_listener;
	SymbolizerWrapper symbolizer_w;

	
	public StyleEditorComposite(Composite parent,
			StyleChangeListenerInterface scl, SymbolizerWrapper _symbolizer_w,final TreeItem tree_item) {
		symbolizer_w = _symbolizer_w;

		log.info("creating mark editor layout");
		style_change_listener = scl;
		
		/*
		 * FillLayout toolbar_layout = new FillLayout(); toolbar_layout.type =
		 * SWT.HORIZONTAL; parent.setLayout(toolbar_layout);
		 */

	/*
		RowLayout layout = new RowLayout();
		layout.pack = false;
		layout.wrap = true;
		layout.type = SWT.VERTICAL | SWT.V_SCROLL;
		layout.fill = true;
		layout.marginLeft = 0;
		layout.marginRight = 0;
		layout.marginTop = 0;
		layout.marginBottom = 0;
		layout.spacing = 0;
		
		parent.setLayout(layout); */

		parent.setLayout(new FillLayout());
		
		
		//FillLayout fill_l=new FillLayout(SWT.VERTICAL |SWT.V_SCROLL|SWT.H_SCROLL);
		
		//	ScrolledComposite  sc_parent=new ScrolledComposite(parent,SWT.V_SCROLL | SWT.BORDER);
		
		Composite _parent = new Composite(/*sc_*/parent, SWT.NONE);
	    _parent.setLayout(LayoutHelper.getDefaultRowLayout());
	    
		//_parent.setLayoutData(layoutData)
		/*
		 * Composite actions = subpart(parent, "Special Actions", 1);
		 * add_geometry_btn = new Button(actions, SWT.PUSH);
		 * add_geometry_btn.setText("Add some geometry"); //
		 * add_geometry_btn.addMouseListener(this);
		 */

	    /*
	    Composite rule_name_composite = LayoutHelper.subpart(_parent, Messages.get().NAME,
				1);

		Text rule_name_txt = new Text(rule_name_composite, SWT.BORDER);

		rule_name_txt.setText(symbolizer_w.getName());
		rule_name_txt.setData(symbolizer_w);

		rule_name_txt.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent event) {
				String new_name=((Text) event.widget).getText();
				((SymbolizerWrapper) (event.widget.getData())).setName(new_name
						);
				tree_item.setText(new_name);
				//tree_item.setText(((Text) event.widget).getText());
			}
		});
*/	    
		if (symbolizer_w.isPointSymbolizer())
			new MarkEditorComposite(_parent,style_change_listener,symbolizer_w);
		
		if (symbolizer_w.isTextSymbolizer()) 
			new TextSymbolizerEditorComposite(_parent,style_change_listener,symbolizer_w);
		
		if (symbolizer_w.hasFill()) 
			new FillEditorComposite(_parent,style_change_listener,symbolizer_w);
		
		if (symbolizer_w.hasStroke()) 
			new StrokeEditorComposite(_parent,style_change_listener,symbolizer_w);
	
		/*_parent.pack();
		sc_parent.setContent(_parent);
	
		
		//sc_parent.setMinSize(_parent.getBounds().x, _parent.getBounds().y);
		sc_parent.setMinSize(400,400);
*/
		System.out.println(" " +_parent.getBounds().x + " - " + _parent.getBounds().y );
		/*
		sc_parent.setExpandHorizontal(true);
		sc_parent.setExpandVertical(true);
		*/
		
	}

}