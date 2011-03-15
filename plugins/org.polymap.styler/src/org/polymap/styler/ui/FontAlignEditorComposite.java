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
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.polymap.styler.StyleChangeListenerInterface;
import org.polymap.styler.SymbolizerWrapper;
import org.polymap.styler.helper.LayoutHelper;

/**
 * the view of the styler
 * 
 * @author Marcus -LiGi- B&uuml;schleb < mail: ligi (at) polymap (dot) de >
 * 
 */

public class FontAlignEditorComposite {

	private static final Log log = LogFactory
			.getLog(FontAlignEditorComposite.class);

	
	StyleChangeListenerInterface style_change_listener;
	SymbolizerWrapper symbolizer_w;

	String[] align_h_combo_items = new String[] { Messages.get().LEFT, Messages.get().CENTER, Messages.get().RIGHT };

	String[] align_v_combo_items = new String[] { Messages.get().TOP, Messages.get().MIDDLE, Messages.get().BOTTOM };

	String[] font_weight_combo_items = new String[] { Messages.get().NORMAL, Messages.get().BOLD };
	String[] font_weight_combo_items4sld = new String[] { "normal", "bold"};
	// String[] font_weight_combo_items= new String[] { "lighter" , "normal" ,
	// "bold" , "bolder" };

	String[] font_family_combo_items = new String[] { Messages.get().VERDANA, Messages.get().TIMES,
			Messages.get().WESTERN, Messages.get().FANTASY };

	String align_h_str = "c"; //$NON-NLS-1$
	String align_v_str = "m"; //$NON-NLS-1$

	String[] width_combo_items = new String[] { "1", "2", "3", "4", "5", "6", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
			"7", "8", "9", "10", "11", "12" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$

	String[] halo_radius_combo_items = new String[] { "1.0", "2.0" , "4.0","8.0","16.0","32.0"}; 
		
	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public FontAlignEditorComposite(Composite parent,
			StyleChangeListenerInterface scl, SymbolizerWrapper _symbolizer_w) {
		symbolizer_w = _symbolizer_w;
		
		log.info("creating Font Align editor layout"); //$NON-NLS-1$
		style_change_listener = scl;
	
		Composite font_composite = LayoutHelper.subpart(parent,Messages.get().TEXT_ALIGN, 3);

		Combo font_align_v_combo = new Combo(
				
		LayoutHelper.describingGroup(font_composite,Messages.get().VERTICAL), SWT.DROP_DOWN | SWT.READ_ONLY);
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

		Combo font_align_h_combo = new Combo(
				LayoutHelper.describingGroup(font_composite,Messages.get().HORIZONTAL), SWT.DROP_DOWN | SWT.READ_ONLY);
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


	}

}