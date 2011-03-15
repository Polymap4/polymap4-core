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

package org.polymap.openlayers.rap.widget.base_types;

import org.polymap.openlayers.rap.widget.base.OpenLayersObject;

/**
 * 
 * @author Marcus -LiGi- B&uuml;schleb < mail: ligi (at) polymap (dot) de >
 * 
 */

public class StyleMap extends OpenLayersObject {

    /**
     * all intents with default style
     */
    public StyleMap() {
        super.create("new OpenLayers.StyleMap( );");
    }
    
    /**
     * all intents get the style specified in style
     */
	public StyleMap(Style style) {
		super.create("new OpenLayers.StyleMap( " +  style.getJSObjRef()+ ");");
	}
	
	/**
	 * set the style for a sepcific intent ( e.g.  default / select / temporary / delete
	 */
	public void setIntentStyle(String intent, Style style) {
	    super.addObjModCode("obj.styles['"+intent+"']="+style.getJSObjRef() +";");
	}
	

}
