/*
 * polymap.org
 * Copyright (C) 2009-2014, Polymap GmbH. All rights reserved.
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
 */
package org.polymap.openlayers.rap.widget.layers;

/**
 * 
 * @author Marcus -LiGi- B&uuml;schleb < mail: ligi (at) polymap (dot) de >
 */
public class WMSLayer extends GridLayer {

    private String          wmsUrl;

    private String          wmsLayers;
    
    
	public WMSLayer(String name, String wms_url, String wms_layers) {
		super.setName(name);
        this.wmsUrl = wms_url;
        this.wmsLayers = wms_layers;
		super.create("new OpenLayers.Layer.WMS( '" + name + "','" + wms_url
				+ "',{layers:'" + wms_layers + "',transparent:true},{removeBackBufferDelay:0});");
	}

	
    public WMSLayer(String name, String wms_url, String wms_layers, int buffer) {
		super.setName(name);
        this.wmsUrl = wms_url;
        this.wmsLayers = wms_layers;
		super.create("new OpenLayers.Layer.WMS( '" + name + "','" + wms_url
				+ "',{layers:'" + wms_layers + "'},{'buffer':"+buffer+"});");
	}


    public String getWmsUrl() {
        return wmsUrl;
    }

    
    public String getWmsLayers() {
        return wmsLayers;
    }

    
    public void setFormat(String new_format) {
		super.addObjModCode("obj.params.FORMAT='" + new_format + "'");
	}

}
