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



import org.eclipse.swt.graphics.RGB;

/**
 * Class to handle style changes
 * 
 * @author Marcus -LiGi- B&uuml;schleb < mail: ligi (at) polymap (dot) de >
 * 
 */

public interface StyleChangeListenerInterface {

	abstract void line_color_changed(RGB rgb) ;
	abstract void line_width_changed(double width);	
	abstract void font_weight_changed(String weight);
	abstract void font_family_changed(String family);
	abstract void font_size_changed(int size);
	abstract void line_opacity_changed(double opacity);
	abstract void line_style_changed(String style);
	abstract void line_linecap_changed(String style);
	abstract void font_align_changed(String align);
	abstract void fill_color_changed(RGB rgb);
	abstract void font_color_changed(RGB rgb);
	
	abstract void fill_opacity_changed(double opacity);

	abstract void halo_color_changed(RGB rgb);
	abstract void halo_opacity_changed(double opacity);
	abstract void halo_width_changed(double width);	
	
	abstract void label_changed(String label,boolean as_attribute);
	

}