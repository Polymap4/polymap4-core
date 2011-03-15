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

package org.polymap.styler;

import java.util.Vector;

import org.eclipse.swt.graphics.RGB;

/**
 * Class to Dispatch Style Change Events to a Vector of listeners 
 * In ruby this could be a one-liner with the help of method_missing ;-)
 * 
 * @author Marcus -LiGi- B&uuml;schleb < mail: ligi (at) polymap (dot) de >
 * 
 */

public class DispatchStyleChangeListener implements
		StyleChangeListenerInterface {


	Vector<StyleChangeListenerInterface> listeners;

	public DispatchStyleChangeListener() {
		listeners = new Vector<StyleChangeListenerInterface>();

	}

	public void addListener(StyleChangeListenerInterface listener) {
		listeners.add(listener);
	}

	@Override
	public void fill_color_changed(RGB rgb) {
		for (StyleChangeListenerInterface listener:listeners)
			listener.fill_color_changed(rgb);
	}

	@Override
	public void fill_opacity_changed(double opacity) {
		for (StyleChangeListenerInterface listener:listeners)
			listener.fill_opacity_changed(opacity);
	}

	@Override
	public void font_align_changed(String align) {
		for (StyleChangeListenerInterface listener:listeners)
			listener.font_align_changed(align);
	}

	@Override
	public void font_color_changed(RGB rgb) {
		for (StyleChangeListenerInterface listener:listeners)
			listener.font_color_changed(rgb);
	}

	@Override
	public void font_family_changed(String family) {
		for (StyleChangeListenerInterface listener:listeners)
			listener.font_family_changed(family);
	}

	@Override
	public void font_size_changed(int size) {
		for (StyleChangeListenerInterface listener:listeners)
			listener.font_size_changed(size);
	}

	@Override
	public void font_weight_changed(String weight) {
		for (StyleChangeListenerInterface listener:listeners)
			listener.font_weight_changed(weight);
	}

	@Override
	public void line_color_changed(RGB rgb) {
		for (StyleChangeListenerInterface listener:listeners)
			listener.line_color_changed(rgb);
	}

	@Override
	public void line_linecap_changed(String style) {
		for (StyleChangeListenerInterface listener:listeners)
			listener.line_linecap_changed(style);
	}

	@Override
	public void line_opacity_changed(double opacity) {
		for (StyleChangeListenerInterface listener:listeners)
			listener.line_opacity_changed(opacity);
	}

	@Override
	public void line_style_changed(String style) {
		for (StyleChangeListenerInterface listener:listeners)
			listener.line_style_changed(style);
	}

	@Override
	public void line_width_changed(double width) {
		for (StyleChangeListenerInterface listener:listeners)
			listener.line_width_changed(width);
	}

	@Override
	public void label_changed(String label,boolean as_attribute) {
		for (StyleChangeListenerInterface listener:listeners)
			listener.label_changed(label,as_attribute);
		
	}

	@Override
	public void halo_color_changed(RGB rgb) {
		for (StyleChangeListenerInterface listener:listeners)
			listener.halo_color_changed(rgb);
	}

	@Override
	public void halo_opacity_changed(double opacity) {
		for (StyleChangeListenerInterface listener:listeners)
			listener.halo_opacity_changed(opacity);
	}

	@Override
	public void halo_width_changed(double width) {
		for (StyleChangeListenerInterface listener:listeners)
			listener.halo_width_changed(width);
	}

	
}