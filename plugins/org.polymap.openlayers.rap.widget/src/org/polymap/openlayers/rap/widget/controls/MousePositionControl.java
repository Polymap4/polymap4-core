/*
 * polymap.org
 * Copyright 2009-2013, Polymap GmbH. ALl rights reserved.
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
package org.polymap.openlayers.rap.widget.controls;

/**
 * 
 * @author Marcus -LiGi- B&uuml;schleb < mail: ligi (at) polymap (dot) de >
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class MousePositionControl extends Control {

	public MousePositionControl() {
		super.create("new OpenLayers.Control.MousePosition();");

		addObjModCode( "OpenLayers.Util.extend(" + getJSObjRef() + ", {"
                + "activate: function() {"
                + "    OpenLayers.Control.MousePosition.prototype.activate.apply(this, arguments);"
                + "    this.map.events.unregister('mousemove', this, this.redraw);"
                + "    this.map.events.register('mousemove', this, this.deferredRedraw);"
                + "    this.granularity = 1000;"
                + "},"
                + "deactivate: function() {"
                + "    OpenLayers.Control.DrawFeature.prototype.deactivate.apply(this, arguments);"
                + "    this.map.events.unregister('mousemove', this, this.deferredRedraw);"
                + "},"
                + "deferredRedraw: function(evt) {"
                + "    this.lastEvent = evt;"
                + "    if (!this.timeout) {"
                + "        var self = this;"
                + "        this.timeout = setTimeout( function() {"
                + "            self.timeout = null;"
//                + "            alert( self.lastEvent.xy );"
                + "            self.redraw( self.lastEvent )"
                + "        }, 500 );"
                + "    }"
                + "}"
                + "});"
        );
	}
	
}
