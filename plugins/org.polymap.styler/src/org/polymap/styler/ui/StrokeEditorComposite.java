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
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.geotools.styling.SLD;
import org.polymap.styler.StyleChangeListenerInterface;
import org.polymap.styler.SymbolizerWrapper;
import org.polymap.styler.helper.ColorHelper;
import org.polymap.styler.helper.ImageHelper;
import org.polymap.styler.helper.LayoutHelper;

/**
 * Class for Editing Strokes
 * 
 * @author Marcus -LiGi- B&uuml;schleb < mail: ligi (at) polymap (dot) de >
 * 
 */

public class StrokeEditorComposite {

	private static final Log log = LogFactory
			.getLog(StrokeEditorComposite.class);

	public String[] strokestyle_combo_items ;

	public String[] strokestyle_combo_items4sld;
	
	public String[] strokelinecap_combo_items;
	
	StyleChangeListenerInterface style_change_listener;
	SymbolizerWrapper symbolizer_w;

	
	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public StrokeEditorComposite(Composite parent,
			StyleChangeListenerInterface scl, SymbolizerWrapper _symbolizer_w) {
		
		// initialize the strings
		strokelinecap_combo_items = new String[] { Messages.get().BUTT, Messages.get().ROUND,
				Messages.get().SQUARE };
		strokestyle_combo_items = new String[] { Messages.get().SOLID, Messages.get().DOT, Messages.get().DASH,
				Messages.get().DASHDOT, Messages.get().LONGDASH };
		
		strokestyle_combo_items4sld = new String[] { 
				Messages.get().SOLID,Messages.get().DOT,Messages.get().DASH,Messages.get().DASHDOT,Messages.get().LONGDASH};
			
		symbolizer_w = _symbolizer_w;
		
		final Display display=parent.getDisplay();

		log.info("creating style editor layout"); //$NON-NLS-1$
		style_change_listener = scl;
	

		if (symbolizer_w.hasStroke()) {
			Composite border = LayoutHelper.subpart(parent, Messages.get().STROKE, 5);

			Spinner line_alpha_spinner = LayoutHelper.createPercentageSpinner(
					LayoutHelper.describingGroup(border,Messages.get().OPACITY),
					(int) (symbolizer_w.getStrokeOpacity() * 100));

			line_alpha_spinner.addModifyListener(new ModifyListener() {

				@Override
				public void modifyText(ModifyEvent event) {
					style_change_listener
							.line_opacity_changed(((Spinner) event.widget)
									.getSelection() / 100.0);

				}
			});

			/*
			 * 
			 * Combo line_alpha_combo = new Combo(border, SWT.NONE);
			 * line_alpha_combo.setItems(percentage_combo_items);
			 * line_alpha_combo.select(3);
			 * 
			 * line_alpha_combo.addModifyListener(new ModifyListener() {
			 * 
			 * @Override public void modifyText(ModifyEvent event) { // TODO
			 * Auto-generated method stub Double val = Integer.parseInt(((Combo)
			 * event.widget).getText() .replace("%", "")) / 100.0;
			 * style_change_listener.line_opacity_changed(val);
			 * 
			 * } });
			 */

			Combo line_style_combo = new Combo(
					LayoutHelper.describingGroup(border, Messages.get().LINESTYLE), SWT.DROP_DOWN | SWT.READ_ONLY);
			line_style_combo.setItems(strokestyle_combo_items);
			line_style_combo.select(symbolizer_w.getStrokeStyleId());
			line_style_combo.addModifyListener(new ModifyListener() {

				@Override
				public void modifyText(ModifyEvent event) {

					switch(((Combo) event.widget).getSelectionIndex()) {
					case SymbolizerWrapper.LINESTYLE_SOLID:
						style_change_listener.line_style_changed("solid");
						break;
						
					case  SymbolizerWrapper.LINESTYLE_DOT:
						style_change_listener.line_style_changed("dot");
						break;
						
					case  SymbolizerWrapper.LINESTYLE_DASH:
						style_change_listener.line_style_changed("dash");
						break;
						

					case  SymbolizerWrapper.LINESTYLE_DASHDOT:
						style_change_listener.line_style_changed("dashdot");
						break;
					

					case  SymbolizerWrapper.LINESTYLE_LONGDASH:
						style_change_listener.line_style_changed("longdash");
						break;
					
					}

				}
			});

			Combo line_linecap_combo = new Combo(
					LayoutHelper.describingGroup(border,Messages.get().LINECAP), SWT.DROP_DOWN | SWT.READ_ONLY);
			line_linecap_combo.setItems(strokelinecap_combo_items);

			if (symbolizer_w.getStrokeLineCap().equals("butt"))
				line_linecap_combo.select(SymbolizerWrapper.LINECAP_BUTT);
			else if (symbolizer_w.getStrokeLineCap().equals("round"))
				line_linecap_combo.select(SymbolizerWrapper.LINECAP_ROUND); 
			else if (symbolizer_w.getStrokeLineCap().equals("square"))
				line_linecap_combo.select(SymbolizerWrapper.LINECAP_SQUARE); 
				
			line_linecap_combo.addModifyListener(new ModifyListener() {

				@Override
				public void modifyText(ModifyEvent event) {
					
					switch(((Combo) event.widget).getSelectionIndex()) {
					case SymbolizerWrapper.LINECAP_BUTT:
						style_change_listener.line_linecap_changed("butt");
						break;
						
					case SymbolizerWrapper.LINECAP_ROUND:
						style_change_listener.line_linecap_changed("round");
						break;
						
					case SymbolizerWrapper.LINECAP_SQUARE:
						style_change_listener.line_linecap_changed("square");
						break;
					}
					
					/*style_change_listener
							.line_linecap_changed(((Combo) event.widget)
									.getText());
*/
				}
			});

			Text line_width_spinner = new Text(
					LayoutHelper.describingGroup(border,Messages.get().WIDTH), SWT.BORDER);

			line_width_spinner.setText(""+symbolizer_w.getStrokeWidth());
			line_width_spinner.setData(symbolizer_w);
			line_width_spinner.addModifyListener(new ModifyListener() {
				
				@Override
				public void modifyText(ModifyEvent event) {
					if ((((Text) event.widget).getText()).equals(""))
						return;
					try {
						Double new_val=Double.parseDouble((((Text) event.widget).getText()));
					
						style_change_listener.line_width_changed(new_val);
					} catch (NumberFormatException e){
						
						Double old_val=((SymbolizerWrapper)(event.widget.getData())).getStrokeWidth();
						(((Text) event.widget)).setText(""+old_val);
					}
					
				}
			});

			final Button mod_line_style_btn = new Button(
					LayoutHelper.describingGroup(border,Messages.get().COLOR), SWT.PUSH);
			mod_line_style_btn.setText("");
			
			mod_line_style_btn.setImage( ImageHelper.createColorRectImage(SLD.color(symbolizer_w.getStroke().getColor()))  );
			
			mod_line_style_btn.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					ColorDialog dialog=new ColorDialog(	new Shell(display));
					
					dialog.setRGB(ColorHelper.Color2RGB(symbolizer_w.getStrokeColor()  ));
			
					RGB res=dialog.open();
					mod_line_style_btn.setImage(ImageHelper.createColorRectImage(res));
					style_change_listener.line_color_changed(res);
				}
			});
		}

	}

}