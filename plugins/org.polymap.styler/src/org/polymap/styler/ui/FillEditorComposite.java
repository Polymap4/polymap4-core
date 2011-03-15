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
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.polymap.styler.StyleChangeListenerInterface;
import org.polymap.styler.SymbolizerWrapper;
import org.polymap.styler.helper.ColorHelper;
import org.polymap.styler.helper.ImageHelper;
import org.polymap.styler.helper.LayoutHelper;

/**
 * the view of the styler
 * 
 * @author Marcus -LiGi- B&uuml;schleb < mail: ligi (at) polymap (dot) de >
 * 
 */

public class FillEditorComposite {

	private static final Log log = LogFactory
			.getLog(FillEditorComposite.class);

	public final static String[] strokestyle_combo_items = new String[] { Messages.get().SOLID, Messages.get().DOT, Messages.get().DASH,
			Messages.get().DASHDOT, Messages.get().LONGDASH, Messages.get().LONGDASHDOT, Messages.get().SOLID };

	public final static String[] strokelinecap_combo_items = new String[] { Messages.get().BUTT, Messages.get().ROUND,
	Messages.get().SQUARE };
	
	StyleChangeListenerInterface style_change_listener;
	SymbolizerWrapper symbolizer_w;

	
	


	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public FillEditorComposite(Composite parent,
			StyleChangeListenerInterface scl, SymbolizerWrapper _symbolizer_w) {
		
		log.info("creating FillEditorComposite");
		symbolizer_w = _symbolizer_w;
		
		final Display display=parent.getDisplay();

		style_change_listener = scl;
	
		Composite fill = LayoutHelper.subpart(parent, Messages.get().FILL, 3);

		
		
		Spinner fill_alpha_spinner = LayoutHelper.createPercentageSpinner(
				LayoutHelper.describingGroup(fill, Messages.get().OPACITY),
				(int) (symbolizer_w.getFillOpacity() * 100));

		fill_alpha_spinner.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent event) {
				style_change_listener
						.fill_opacity_changed(((Spinner) event.widget)
								.getSelection() / 100.0);

			}
		});

		/*
		 * fill_alpha_combo = new Combo(fill, SWT.NONE);
		 * fill_alpha_combo.setItems(percentage_combo_items);
		 * fill_alpha_combo.select(3);
		 * 
		 * fill_alpha_combo.addModifyListener(new ModifyListener() {
		 * 
		 * @Override public void modifyText(ModifyEvent event) { // TODO
		 * Auto-generated method stub Double
		 * val=Integer.parseInt(fill_alpha_combo.getText().replace("%",
		 * ""))/100.0; style_change_listener.fill_opacity_changed( val);
		 * 
		 * } });
		 */

		final Button mod_fill_color_btn = new Button(
				LayoutHelper.describingGroup(fill, Messages.get().COLOR), SWT.PUSH);
		mod_fill_color_btn.setText("");
		
		mod_fill_color_btn.setImage( ImageHelper.createColorRectImage(symbolizer_w.getFillColor())  );
		
		mod_fill_color_btn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				ColorDialog dialog=new ColorDialog(new Shell(display));
				dialog.setRGB(ColorHelper.Color2RGB(symbolizer_w.getFillColor()  ));
				RGB res=dialog.open();
				mod_fill_color_btn.setImage(ImageHelper.createColorRectImage(res));
				style_change_listener.fill_color_changed(res);
			}
		});
		
	}

}