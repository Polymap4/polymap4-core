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

import java.awt.Color;

import org.eclipse.swt.graphics.RGB;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.SLD;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;
import org.geotools.styling.Symbolizer;
import org.geotools.styling.TextSymbolizer;
import org.opengis.style.LineSymbolizer;
import org.opengis.style.PointSymbolizer;

/**
 * Class to handle style changes
 * 
 * @author Marcus -LiGi- B&uuml;schleb < mail: ligi (at) polymap (dot) de >
 * 
 */

public class SLDStyleChangeListener implements StyleChangeListenerInterface {

	
	Style style;
	PolygonSymbolizer polygon_symbolizer;
	LineSymbolizer line_symbolizer;
	PointSymbolizer point_symbolizer;
	
	
	StyleBuilder sb = new StyleBuilder();
	
	SymbolizerWrapper symbolizer_w;
	
	
	public SLDStyleChangeListener(SymbolizerWrapper _symbolizer_w) {
			symbolizer_w=_symbolizer_w;
	}
	
	public void _SLDStyleChangeListener(Symbolizer s) {
 

		sb = new StyleBuilder();
		
		polygon_symbolizer = sb.createPolygonSymbolizer();
		Rule polygon_rule = sb.createRule(polygon_symbolizer);
	
	//	polygon_rule.setIsElseFilter(true);

		line_symbolizer = sb.createLineSymbolizer();
		Rule line_rule = sb.createRule((Symbolizer) line_symbolizer);
		
		/*
		
		point_symbolizer = sb.createPointSymbolizer();
		Rule point_rule = sb.createRule(point_symbolizer);
		point_rule.setIsElseFilter(true);
*/
		
		FeatureTypeStyle fts = sb.createFeatureTypeStyle("Feature",
				new Rule[] { polygon_rule,line_rule /* , rule2 */});

		style = sb.createStyle();
		//style.addFeatureTypeStyle(fts);
		style.featureTypeStyles().add(fts);
		//.accept(fts);
		
		// layers.add(new
		// DefaultMapLayer(dataStore.getFeatureSource(), style));

		//sym = sb.createLineSymbolizer(Color.BLACK, 1);

		// return style;
	}

	@Override
	public void fill_color_changed(RGB rgb) {
		symbolizer_w.getFill().setColor(sb.colorExpression(new Color(rgb.red,rgb.green,rgb.blue)));
	}

	@Override
	public void fill_opacity_changed(double opacity) {
		symbolizer_w.getFill().setOpacity(sb.literalExpression(opacity));
	}

	@Override
	public void font_align_changed(String align_str) {
		
		double h_align=SLD.ALIGN_CENTER;
		if (align_str.startsWith("l"))
			h_align=SLD.ALIGN_LEFT;
		else if (align_str.startsWith("r"))
			h_align=SLD.ALIGN_RIGHT;
		
		double v_align=SLD.ALIGN_MIDDLE;
		if (align_str.endsWith("t"))
			v_align=SLD.ALIGN_TOP;
		else if (align_str.endsWith("b"))
			v_align=SLD.ALIGN_BOTTOM;
		
		((TextSymbolizer)(symbolizer_w.getSymbolizer())).setLabelPlacement(sb.createPointPlacement(h_align,v_align,0.0));
		
		//LabelPlacement l =new LabelPlacement(sb.createPointPlacement(0.0,0.0,0.0));
		
		//TextSymbolizer t;
		//t.setL
	}

	@Override
	public void font_color_changed(RGB rgb) {
		symbolizer_w.getTextFill().setColor(sb.colorExpression(new Color(rgb.red,rgb.green,rgb.blue)));
	}

	@Override
	public void font_family_changed(String family) {
		symbolizer_w.getFont().setFontFamily(sb.literalExpression(family));
	}

	@Override
	public void font_size_changed(int size) {
		symbolizer_w.getFont().setSize(sb.literalExpression(size));
	}

	@Override
	public void font_weight_changed(String weight) {
		symbolizer_w.getFont().setWeight(sb.literalExpression(weight));
	}

	@Override
	public void line_color_changed(RGB rgb) {
		symbolizer_w.getStroke().setColor(sb.colorExpression(new Color(rgb.red,rgb.green,rgb.blue)));
	}

	@Override
	public void line_linecap_changed(String linecap) {
		symbolizer_w.getStroke().setLineCap(sb.literalExpression(linecap));
	}

	@Override
	public void line_opacity_changed(double opacity) {
		symbolizer_w.getStroke().setOpacity(sb.literalExpression(opacity));
	}

	@Override
	public void line_style_changed(String style) {
		if (style.equals("solid"))
			symbolizer_w.getStroke().setDashArray(SymbolizerWrapper.STROKE_STYLE_ARR_SOLID);
		else if (style.equals("dot"))
			symbolizer_w.getStroke().setDashArray(SymbolizerWrapper.STROKE_STYLE_ARR_DOT);
		else if (style.equals("dash"))
			symbolizer_w.getStroke().setDashArray(SymbolizerWrapper.STROKE_STYLE_ARR_DASH);
		else if (style.equals("dashdot"))
			symbolizer_w.getStroke().setDashArray(SymbolizerWrapper.STROKE_STYLE_ARR_DASHDOT);
		else if (style.equals("longdash"))
			symbolizer_w.getStroke().setDashArray(SymbolizerWrapper.STROKE_STYLE_ARR_LONGDASH);
	}

	@Override
	public void line_width_changed(double width) {
		symbolizer_w.getStroke().setWidth(sb.literalExpression(width));
		
		//line_symbolizer.   .setWidth(width);
	}

	@Override
	public void label_changed(String label,boolean as_attribute) {
		if (as_attribute)
			symbolizer_w.setLabel(sb.attributeExpression(label));
		else
			symbolizer_w.setLabel(sb.literalExpression(label));
		
	}

	@Override
	public void halo_color_changed(RGB rgb) {
		symbolizer_w.getHaloFill().setColor(sb.colorExpression(new Color(rgb.red,rgb.green,rgb.blue)));
	}

	@Override
	public void halo_opacity_changed(double opacity) {
		symbolizer_w.getHaloFill().setOpacity(sb.literalExpression(opacity));
	}

	@Override
	public void halo_width_changed(double width) {
		symbolizer_w.getHalo().setRadius(sb.literalExpression(width));
	}

}