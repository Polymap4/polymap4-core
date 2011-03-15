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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.graphics.RGB;
import org.polymap.openlayers.rap.widget.base.OpenLayersSessionHandler;
import org.polymap.openlayers.rap.widget.base_types.Bounds;
import org.polymap.openlayers.rap.widget.base_types.Style;
import org.polymap.openlayers.rap.widget.base_types.StyleMap;
import org.polymap.openlayers.rap.widget.features.VectorFeature;
import org.polymap.openlayers.rap.widget.layers.VectorLayer;

/**
 * Class to handle style changes
 * 
 * @author Marcus -LiGi- B&uuml;schleb < mail: ligi (at) polymap (dot) de >
 * 
 */

public class MapStyleChangeListener implements StyleChangeListenerInterface {

	private static Log log = LogFactory.getLog(MapStyleChangeListener.class);

	// private IWorkbenchWindow window;

	Style act_style = null;
	VectorLayer vl = null;

	/**
	 * The constructor.
	 */
	public MapStyleChangeListener() {
		
	}
	StyleMap style_map;
	public Style getStyle() {
		
		if (act_style == null) {
			act_style = new Style();
			act_style.setAttribute("fillColor", "#FF0000");
			act_style.setAttribute("label", "test");
			style_map=new StyleMap();
			style_map.setIntentStyle("default", act_style);
		}
		if (vl == null)
			try {
				vl = new VectorLayer("test",style_map);
				OpenLayersSessionHandler.getInstance().getWidget().getMap()
						.addLayer(vl);

				VectorFeature vector_feature = new VectorFeature(new Bounds(
						-100, 40, -80, 60).toGeometry());
				vl.addFeatures(vector_feature);
				vector_feature = new VectorFeature(new Bounds(-90, 70, -60, 80)
						.toGeometry());

				vl.addFeatures(vector_feature);
			} catch (Exception e) {
				log.warn("cant create VectorLayer! no Map yet?");

			}
		return act_style;
	}

	public String INT2HEX(int val) {
		String res = Integer.toHexString(val);
		if (res.length() == 1)
			return "0" + res;
		else
			return res;
	}

	public String RGB2HEX(RGB rgb) {
		return "#" + INT2HEX(rgb.red) + INT2HEX(rgb.green) + INT2HEX(rgb.blue);
	}

	public void change_style(String attr_name, String value) {
		/*
		getStyle().setAttribute(attr_name, value);
		if (vl != null)
			vl.redraw(); 
		*/
	}

	public void line_color_changed(RGB rgb) {
		change_style("strokeColor", RGB2HEX(rgb));
	}

	public void line_width_changed(double width) {
		change_style("strokeWidth",""+width );
	}
	
	
	public void font_weight_changed(String weight) {
		change_style("fontWeight",weight );
	}
	
	public void font_family_changed(String family) {
		change_style("fontFamily",family );
	}
	
	public void font_size_changed(int size) {
		change_style("fontSize",""+size );
	}
	public void line_opacity_changed(double opacity) {
		change_style("strokeOpacity",""+opacity );
	}
	
	public void line_style_changed(String style) {
		change_style("strokeDashstyle",style );
	}
	
	public void line_linecap_changed(String style) {
	
		change_style("strokeLinecap",style );
	}
	
	public void font_align_changed(String align) {
		log.info("label align changed 2 " + align);
		change_style("labelAlign",align );
	}
	
	public void fill_color_changed(RGB rgb) {
		change_style("fillColor", RGB2HEX(rgb));
	}
	
	public void font_color_changed(RGB rgb) {
		change_style("fontColor", RGB2HEX(rgb));
	}
	
	public void fill_opacity_changed(double opacity) {
		change_style("fillOpacity",""+opacity );
	}

	@Override
	public void label_changed(String label,boolean as_attribute) {
		if (as_attribute)
			change_style("attr",label );
		else
			change_style("label",label );
	}

	@Override
	public void halo_color_changed(RGB rgb) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void halo_opacity_changed(double opacity) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void halo_width_changed(double width) {
		// TODO Auto-generated method stub
		
	}
	
	
}