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

import java.io.IOException;

import org.geotools.styling.Style;
import org.polymap.core.project.ILayer;
import org.polymap.core.style.IStyle;


/**
 * 
 * @author Marcus -LiGi- B&uuml;schleb < mail: ligi (at) polymap (dot) de >
 * 
 */

public class StyleWrapper {
	
	IStyle style;
	ILayer layer;

	public StyleWrapper(IStyle style,ILayer layer) {
		this.style=style;
		this.layer=layer;
	}
	
	public Style getStyle() {
		try {
			return style.resolve(Style.class, null);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public IStyle getIStyle() {
		return style;
	}
	
	
	
/*
 	public void setStyle(Style style) {

	//	Object bar = foo.clone();
		
		this.style=style;
		
		
		//System.out.println("old style" + this.layer.getStyle().getName() );
		this.layer.setStyle(style);
		//System.out.println("new style" + this.layer.getStyle().getName());
	}
*/
	
}