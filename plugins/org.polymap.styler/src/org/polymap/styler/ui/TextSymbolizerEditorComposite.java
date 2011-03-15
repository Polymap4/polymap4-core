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





import java.awt.GraphicsEnvironment;

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

import org.geotools.styling.SLD;
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

public class TextSymbolizerEditorComposite {

	private static final Log log = LogFactory
			.getLog(TextSymbolizerEditorComposite.class);

	
	StyleChangeListenerInterface style_change_listener;
	SymbolizerWrapper symbolizer_w;

	String[] align_h_combo_items = new String[] { Messages.get().LEFT, Messages.get().CENTER, Messages.get().RIGHT };

	String[] align_v_combo_items = new String[] { Messages.get().TOP, Messages.get().MIDDLE, Messages.get().BOTTOM };

	String[] font_weight_combo_items = new String[] { Messages.get().NORMAL, Messages.get().BOLD };
	String[] font_weight_combo_items4sld = new String[] { "normal", "bold"};
	// String[] font_weight_combo_items= new String[] { "lighter" , "normal" ,
	// "bold" , "bolder" };

	String[] font_family_combo_items ;

	String align_h_str = "c"; //$NON-NLS-1$
	String align_v_str = "m"; //$NON-NLS-1$

	String[] width_combo_items = new String[] { "1", "2", "3", "4", "5", "6", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
			"7", "8", "9", "10", "11", "12" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$

	String[] halo_radius_combo_items = new String[] { "1.0", "2.0" , "4.0","8.0","16.0","32.0"}; 
		
	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public TextSymbolizerEditorComposite(Composite parent,
			StyleChangeListenerInterface scl, SymbolizerWrapper _symbolizer_w) {
		
		
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		font_family_combo_items = ge.getAvailableFontFamilyNames();
	        	
		symbolizer_w = _symbolizer_w;
		
		final Display display=parent.getDisplay();

		log.info("creating style editor layout"); //$NON-NLS-1$
		style_change_listener = scl;
	
		if (symbolizer_w.hasLabel()) { // TODO chek if needed 
			Composite label_composite = LayoutHelper.subpart(parent, Messages.get().LABEL, 2);
			/*
			Text label = new Text(label_composite,SWT.BORDER);
			label.setSize(50,label.getSize().y);
			
			label.setText(symbolizer_w.getLabel());
			
			label.addModifyListener(new ModifyListener() {

				@Override
				public void modifyText(ModifyEvent event) {
					style_change_listener
							.label_changed(((Text) event.widget)
									.getText() ,false );

				}
			});
			*/
			Combo label_combo = new Combo(label_composite, SWT.NONE);
			label_combo.setItems(symbolizer_w.getFeatureAttributes());
			label_combo.setText(symbolizer_w.getLabel());
			label_combo.addModifyListener(new ModifyListener() {

				@Override
				public void modifyText(ModifyEvent event) {
					style_change_listener
							.label_changed(((Combo) event.widget)
									.getText() ,true );

				}
			});

			//label_combo.select(1);
			//label_combo.addModifyListener(new ModifyListener() {

				
//			Text attrs = new Text(label_composite,SWT.BORDER);
			//attrs.setText("" + symbolizer_w.get)
		}
	

		Composite font_composite = LayoutHelper.subpart(parent, Messages.get().FONT, 3);

		new FontAlignEditorComposite(parent,scl,symbolizer_w);
		/*
		Combo font_align_v_combo = new Combo(font_composite, SWT.NONE);
		font_align_v_combo.setItems(align_v_combo_items);
		font_align_v_combo.select( symbolizer_w.getLabelPlacementY());
		font_align_v_combo.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent event) {
				// TODO Auto-generated method stub
				switch (((Combo) event.widget).getSelectionIndex()) {
				case 0:
					align_v_str = "t"; //$NON-NLS-1$
					break;
				case 1:
					align_v_str = "m"; //$NON-NLS-1$
					break;
				case 2:
					align_v_str = "b"; //$NON-NLS-1$
					break;

				}
				style_change_listener.font_align_changed(align_h_str
						+ align_v_str);

			}
		});

		Combo font_align_h_combo = new Combo(font_composite, SWT.NONE);
		font_align_h_combo.setItems(align_h_combo_items);
		font_align_h_combo.select(symbolizer_w.getLabelPlacementX());
		font_align_h_combo.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent event) {
				// TODO Auto-generated method stub
				switch (((Combo) event.widget).getSelectionIndex()) {
				case 0:
					align_h_str = "l"; //$NON-NLS-1$
					break;
				case 1:
					align_h_str = "c"; //$NON-NLS-1$
					break;
				case 2:
					align_h_str = "r"; //$NON-NLS-1$
					break;

				}
				style_change_listener.font_align_changed(align_h_str
						+ align_v_str);

			}
		});
*/
		Combo font_weight_combo = new Combo(
				LayoutHelper.describingGroup(font_composite,Messages.get().WEIGHT), SWT.DROP_DOWN | SWT.READ_ONLY);
		font_weight_combo.setItems(font_weight_combo_items);
		font_weight_combo.select(0);

		font_weight_combo.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent event) {
				// TODO Auto-generated method stub

				style_change_listener
						.font_weight_changed( font_weight_combo_items4sld[((Combo) event.widget).getSelectionIndex()]
								);

			}
		});

		Combo font_size_combo = new Combo(
				LayoutHelper.describingGroup(font_composite,Messages.get().SIZE)
						, SWT.NONE);
		font_size_combo.setItems(width_combo_items);

		font_size_combo.setText(""+symbolizer_w.getFont().getSize()); //$NON-NLS-1$
		font_size_combo.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent event) {
				// TODO Auto-generated method stub
				int val = Integer
						.parseInt(((Combo) event.widget).getText());
				style_change_listener.font_size_changed(val);

			}
		});

		Combo font_family_combo = new Combo(
				LayoutHelper.describingGroup(font_composite,Messages.get().FAMILY), SWT.NONE);
		font_family_combo.setItems(font_family_combo_items);
		font_family_combo.setText(symbolizer_w.getFont().getFamily().get(0).toString());
		font_family_combo.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent event) {
				// TODO Auto-generated method stub

				style_change_listener
						.font_family_changed(((Combo) event.widget)
								.getText());

			}
		});

		/*Button mod_font_color_btn = new Button(font_composite, SWT.PUSH);
		mod_font_color_btn.setText(Messages.get().COLOR);
		
		mod_font_color_btn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				style_change_listener.font_color_changed((new ColorDialog(
						new Shell(display))).open());
			}
		});
		
		*/
		/*
		final Button mod_font_color_btn = new Button(font_composite, SWT.PUSH);
		mod_font_color_btn.setText(Messages.get().COLOR);
		
		mod_font_color_btn.setImage( ImageHelper.createColorRectImage(SLD.color(symbolizer_w.getTextFill().getColor()))  );
		
		mod_font_color_btn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				ColorDialog dialog=new ColorDialog(	new Shell(display));
				
				dialog.setRGB(ColorHelper.Color2RGB(SLD.color(symbolizer_w.getTextFill().getColor() ) ));
		
				RGB res=dialog.open();
				mod_font_color_btn.setImage(ImageHelper.createColorRectImage(res));
				style_change_listener.font_color_changed(res);
			}
		});
		*/
	
		/* HALO Section */
		
		
		Composite halo_composite = LayoutHelper.subpart(parent, Messages.get().HALO , 3);

		
		Button halo_active = new Button(
				halo_composite, SWT.CHECK);
		halo_active.setData(symbolizer_w);
			
		halo_active.setSelection(symbolizer_w.hasHalo());
		
	
		
		final Spinner halo_opacity_spinner = LayoutHelper.createPercentageSpinner(
				
				LayoutHelper.describingGroup(halo_composite,Messages.get().OPACITY),
				(int) (symbolizer_w.getHaloOpacity()* 100));

		final Combo halo_radius_combo = new Combo(
				LayoutHelper.describingGroup(halo_composite,Messages.get().RADIUS), SWT.NONE);
		
		final Button mod_halo_color_btn = new Button(
				LayoutHelper.describingGroup(halo_composite,Messages.get().COLOR), SWT.PUSH);
		
		halo_active.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				Button thisButton = (Button) e.widget;

				if (thisButton.getSelection()) {
					((SymbolizerWrapper)(e.widget.getData())).addDefaultHalo();
					
					mod_halo_color_btn.setImage( ImageHelper.createColorRectImage(SLD.color(symbolizer_w.getHaloFill().getColor()))  );	
					halo_opacity_spinner.setSelection((int)(((SymbolizerWrapper)(e.widget.getData())).getHaloOpacity()*100));
					halo_radius_combo.setText(""+((SymbolizerWrapper)(e.widget.getData())).getHaloRadius());
					
					mod_halo_color_btn.setEnabled(true);
					halo_opacity_spinner.setEnabled(true);
					halo_radius_combo.setEnabled(true);
					
				} else {
					((SymbolizerWrapper)(e.widget.getData())).setHalo(null);
					mod_halo_color_btn.setEnabled(false);
					halo_opacity_spinner.setEnabled(false);
					halo_radius_combo.setEnabled(false);
				}
			}
		});
		
		
		halo_opacity_spinner.setEnabled(symbolizer_w.hasHalo());
		
		
		halo_opacity_spinner.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent event) {
				style_change_listener
						.halo_opacity_changed(((Spinner) event.widget)
								.getSelection() / 100.0);

			}
		});
	
		
		if (symbolizer_w.hasHalo())		
			mod_halo_color_btn.setImage( ImageHelper.createColorRectImage(SLD.color(symbolizer_w.getHaloFill().getColor()))  );	
		else
			mod_halo_color_btn.setEnabled(false);
		
		mod_halo_color_btn.setText("");
		
		mod_halo_color_btn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				ColorDialog dialog=new ColorDialog(	new Shell(display));
				
				dialog.setRGB(ColorHelper.Color2RGB(SLD.color(symbolizer_w.getHaloFill().getColor() ) ));
		
				RGB res=dialog.open();
				mod_halo_color_btn.setImage(ImageHelper.createColorRectImage(res));
				style_change_listener.halo_color_changed(res);
			}
		});
		


		
		halo_radius_combo.setItems(halo_radius_combo_items);
		
		halo_radius_combo.setText(""+symbolizer_w.getFont().getSize()); //$NON-NLS-1$
		halo_radius_combo.setEnabled(symbolizer_w.hasHalo());
		
		halo_radius_combo.addModifyListener(new ModifyListener() {
		
			@Override
			public void modifyText(ModifyEvent event) {
				// TODO Auto-generated method stub
				double val = Double
						.parseDouble(((Combo) event.widget).getText());
				style_change_listener.halo_width_changed(val);

			}
		});


		
		

	}

}