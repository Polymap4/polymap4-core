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

import org.eclipse.swt.graphics.RGB;
import java.awt.Color;

/**
 * class to help with colors
 * 
 * @author Marcus -LiGi- B&uuml;schleb < mail: ligi (at) polymap (dot) de >
 * 
 */

public class ColorHelper {

	/*
	 * converts from RGB to java.awt.Color
	 */
	public static Color RGB2Color(RGB rgb) {
		return new Color( rgb.red,rgb.green,rgb.blue);
	}
	
	/*
	 * converts from java.awt.Color to RGB
	 */
	public static RGB Color2RGB(Color color) {
		if (color==null)
			return new RGB(0,0,0);
		
		return new RGB(color.getRed(),color.getGreen(),color.getBlue()) ;
	}
	
}