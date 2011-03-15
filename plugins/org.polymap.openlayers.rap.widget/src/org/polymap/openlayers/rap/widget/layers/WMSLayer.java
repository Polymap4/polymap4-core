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

package org.polymap.openlayers.rap.widget.layers;

/**
 * 
 * @author Marcus -LiGi- B&uuml;schleb < mail: ligi (at) polymap (dot) de >
 * 
 */

public class WMSLayer extends GridLayer {

	public WMSLayer(String name, String wms_url, String wms_layers) {
		super.setName(name);
		super.create("new OpenLayers.Layer.WMS( '" + name + "','" + wms_url
				+ "',{layers:'" + wms_layers + "'});");
	}

    public WMSLayer(String name, String wms_url, String wms_layers, int buffer) {
		super.setName(name);
		super.create("new OpenLayers.Layer.WMS( '" + name + "','" + wms_url
				+ "',{layers:'" + wms_layers + "'},{'buffer':"+buffer+"});");
	}
	
	public void setFormat(String new_format) {
		super.addObjModCode("obj.params.FORMAT='" + new_format + "'");
	}

}
