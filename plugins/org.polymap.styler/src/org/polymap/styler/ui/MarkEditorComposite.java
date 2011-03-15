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

public class MarkEditorComposite {

	private static final Log log = LogFactory
			.getLog(MarkEditorComposite.class);

	StyleChangeListenerInterface style_change_listener;
	SymbolizerWrapper symbolizer_w;

	
	public MarkEditorComposite(Composite _parent,
			StyleChangeListenerInterface scl, SymbolizerWrapper _symbolizer_w) {
		symbolizer_w = _symbolizer_w;

		log.info("creating style editor layout");
		style_change_listener = scl;
	
		
		
		if (symbolizer_w.isPointSymbolizer())
		{
		Composite mark_composite=LayoutHelper.subpart(_parent,Messages.get().MARK,0);
		
		Combo line_style_combo = new Combo(
				LayoutHelper.describingGroup(mark_composite,Messages.get().MARK_TYPE), SWT.DROP_DOWN | SWT.READ_ONLY);
		line_style_combo.setItems(new String[] { Messages.get().CIRCLE,Messages.get().SQUARE,Messages.get().TRIANGLE,Messages.get().STAR,Messages.get().CROSS,"X" } );
		line_style_combo.select(symbolizer_w.getMarkType());
		line_style_combo.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent event) {
				int selected_id=((Combo)(event.widget)).getSelectionIndex();
				symbolizer_w.setMark(selected_id);
			}
		});

		Combo mark_size_combo = new Combo(
				LayoutHelper.describingGroup(mark_composite,Messages.get().SIZE), SWT.NONE);
		 mark_size_combo.setItems(new String[] { "1.0","2.0","4.0","8.0","9.0","14.0" } );
		 mark_size_combo.setText(""+symbolizer_w.getMarkSize());
		 mark_size_combo.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent event) {
				double size =Double.parseDouble(((Combo)(event.widget)).getText());
				symbolizer_w.setMarkSize(size);
				
			}
		});

		
		 Combo mark_rot_combo = new Combo(
				 LayoutHelper.describingGroup(mark_composite,Messages.get().ROTATION), SWT.NONE);
		 mark_rot_combo.setItems(new String[] { "0.0","45.0","90.0","180.0" } );
		 mark_rot_combo.setText(""+symbolizer_w.getMarkRotation());
		 mark_rot_combo.addModifyListener(new ModifyListener() {

			 
	     @Override
		 public void modifyText(ModifyEvent event) {
			double rot =Double.parseDouble(((Combo)(event.widget)).getText());
			symbolizer_w.setMarkRotation(rot);
	     }
		});
		
		}
		
		
	}

}