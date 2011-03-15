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

package org.polymap.styler.helper;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Spinner;
import org.polymap.styler.ui.StylerSessionSingleton;

/**
 * the view of the styler
 * 
 * @author Marcus -LiGi- B&uuml;schleb < mail: ligi (at) polymap (dot) de >
 * 
 */

public class LayoutHelper {

	/*
	 * function stolen from UDIG
	 * 
	 */
	public static Composite subpart(Composite parent, String tag, int width) {
		
		Composite outer_border=new Composite(parent,SWT.NONE);
		
		FillLayout outer_l = new FillLayout();
		/*outer_l.marginLeft=10;
		outer_l.wrap=true;
		outer_l.pack=true;
		outer_l.fill=true; */
		outer_l.type=SWT.HORIZONTAL;
		
		outer_border.setLayout(outer_l);
		
		Group border = new Group(outer_border, SWT.SHADOW_OUT);
		
		
		RowLayout across = new RowLayout();
		across.type = SWT.HORIZONTAL;
		across.wrap = true;
		across.pack = true;
		across.fill = true;
		across.marginBottom = 1;
		across.marginRight = 2;
		across.marginLeft = 3;
		border.setLayout(across);
		
		border.setText(tag);
	
		return border;
		//return null;
		/*
		 * last
		 * 
		 * Composite subpart = new Composite(parent, SWT.NONE);
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
		data.width = 40;
		data.height = 20;
		label.setLayoutData(data); 
		*/
		/*
		Composite subpart = new Composite(parent, SWT.NONE);
		Label label = new Label(subpart, SWT.NONE);
		label.setText(tag);
		*/
	//	return subpart;
	}

	static boolean do_descr=true;
	
	public static Composite describingGroup(Composite parent,String label) {
	
		if (StylerSessionSingleton.getInstance().getExpertMode()) 
			return parent;
		
		// if it is no expert -> label all the 
		Group res=new Group(parent,SWT.NONE);
		res.setLayout(new FillLayout());
		res.setText(label);
		return res;
	}
	
	
	public static Spinner createPercentageSpinner(Composite parent, int value) {
		Spinner res = new Spinner(parent, SWT.BORDER);
		res.setMaximum(100);
		res.setMinimum(0);
		res.setSelection(value);
		return res;
	}
	
	
	public static RowLayout getDefaultRowLayout() {
		RowLayout layout = new RowLayout();
		layout.fill=true;
		
		/*layout.pack = false;
		layout.wrap = true;
		layout.type = SWT.HORIZONTAL;
		layout.fill = true;
		layout.marginLeft = 0;
		layout.marginRight = 0;
		layout.marginTop = 0;
		layout.marginBottom = 0;
		layout.spacing = 0;*/
		return layout;
		
	}

}