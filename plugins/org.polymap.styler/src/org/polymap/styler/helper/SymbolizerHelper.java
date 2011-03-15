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


import org.geotools.styling.Symbolizer;
import org.polymap.styler.Messages;
import org.polymap.styler.SymbolizerWrapper;
/**
 * 
 * Helper functions for Symbolizers
 *
 * @author Marcus -LiGi- B&uuml;schleb < mail: ligi (at) polymap (dot) de >
 * 
 */


public class SymbolizerHelper  {

	public static String Symbolizer2TypeName(Symbolizer symbolizer )
	{
		SymbolizerWrapper symbolizer_w=new SymbolizerWrapper(symbolizer);
		
		if (symbolizer_w.isLineSymbolizer())
			return Messages.get().LINE_SYMBOLIZER;
		if (symbolizer_w.isPointSymbolizer())
			return Messages.get().POINT_SYMBOLIZER;
		if (symbolizer_w.isPolygonSymbolizer())
			return Messages.get().POLYGON_SYMBOLIZER;
		if (symbolizer_w.isTextSymbolizer())
			return Messages.get().TEXT_SYMBOLIZER;
		else
			return Messages.get().UNKNOWN_SYMBOLIZER;
	}
	
	/*
	 * used e.g. to identify the icons for the symbolizers
	 */
	
	public static String Symbolizer2ShortTypeName(Symbolizer symbolizer )
	{
		SymbolizerWrapper symbolizer_w=new SymbolizerWrapper(symbolizer);
		
		if (symbolizer_w.isLineSymbolizer())
			return "line";
		if (symbolizer_w.isPointSymbolizer())
			return "point";
		if (symbolizer_w.isPolygonSymbolizer())
			return "polygon";
		if (symbolizer_w.isTextSymbolizer())
			return "text";
		else
			return "unknown";
	}
}