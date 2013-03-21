/* 
 * polymap.org
 * Copyright 2009-2013, Polymap GmbH. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
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
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @since 3.0
 */
public class BoxControl
        extends Control {

    /** Triggered before a feature is highlighted. */
    public static final String          EVENT_BOX = "box";

    
    public BoxControl() {
        super.create( "new OpenLayers.Control();" );
        addObjModCode( "OpenLayers.Util.extend(" + getJSObjRef() + ", {"
                //+ "EVENT_TYPES: [\"" + EVENT_BOX + "\"]"
                //+ ","
                + "initialize: function(options) {"
//                + "    this.EVENT_TYPES = OpenLayers.Control.prototype.EVENT_TYPES.concat("
//                + "        ['" + EVENT_BOX + "'] );"
                //+ "    alert( this.EVENT_TYPES );"
                + "    OpenLayers.Control.prototype.initialize.apply(this, [options]);"
                + "},"
                + "draw: function() {"
                //     Handler.Box will intercept the shift-mousedown
                //     before Control.MouseDefault gets to see it
                + "    this.box = new OpenLayers.Handler.Box( " + getJSObjRef() + ","
                + "        {'done': this.onBox} );"
                //+ "        {keyMask: OpenLayers.Handler.MOD_SHIFT});"
                + "},"
                + "activate: function() {"
                + "    this.box.activate();"
                + "},"
                + "deactivate: function() {"
                + "    this.box.deactivate();"
                + "},"
                + "onBox: function( bounds ) {"
                + "    if (bounds.left) {"
                + "        var map = this.map;"
                + "        var events = this.events;"
                // XXX     hack: since OpenLayer 2.12 this is needed, for what reason ever; it doe not work without
                + "        setTimeout( function() {"
                + "            var minXY = map.getLonLatFromPixel( new OpenLayers.Pixel( bounds.left, bounds.bottom ) );"
                + "            var maxXY = map.getLonLatFromPixel( new OpenLayers.Pixel( bounds.right, bounds.top ) );"
                + "            var bbox = new OpenLayers.Bounds( minXY.lon, minXY.lat, maxXY.lon, maxXY.lat );"
                + "            events.triggerEvent('" + EVENT_BOX + "', {bbox: bbox});"
                + "        }, 250 );"
                + "    } else {"
                // XXX     hack: for single click: get xy from MousePositionControl
                + "        var mouseXY = this.map.getControlsByClass('OpenLayers.Control.MousePosition')[0].lastXy;"
                + "        var pos = this.map.getLonLatFromPixel( new OpenLayers.Pixel( mouseXY.x, mouseXY.y ) );"
                + "        this.events.triggerEvent('" + EVENT_BOX + "', {pos: pos});"
                + "    }"
                + "}"
                + "});"
                + getJSObjRef() + ".initialize();"
                );
    }

}
