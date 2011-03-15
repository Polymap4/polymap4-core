/* 
 * polymap.org
 * Copyright 2009, Polymap GmbH, and individual contributors as indicated
 * by the @authors tag.
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
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 * $Id$
 */
package org.polymap.openlayers.rap.widget.controls;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
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
                + "    this.EVENT_TYPES = OpenLayers.Control.prototype.EVENT_TYPES.concat("
                + "        [\"" + EVENT_BOX + "\"] );"
                //+ "    alert( this.EVENT_TYPES );"
                + "    OpenLayers.Control.prototype.initialize.apply(this, [options]);"
                + "},"
                + "draw: function() {"
                //     this Handler.Box will intercept the shift-mousedown
                //     before Control.MouseDefault gets to see it
                + "    this.box = new OpenLayers.Handler.Box( " + getJSObjRef() + ","
                + "        {'done': this.notice} );"
                //+ "        {keyMask: OpenLayers.Handler.MOD_SHIFT});"
                + "    this.box.activate();"
                + "},"
                + "activate: function() {"
                + "    this.box.activate();"
                + "},"
                + "deactivate: function() {"
                + "    this.box.deactivate();"
                + "},"
                + "notice: function(bounds) {"
                + "    var minXY = this.map.getLonLatFromPixel( new OpenLayers.Pixel( bounds.left, bounds.bottom ) );"
                + "    var maxXY = this.map.getLonLatFromPixel( new OpenLayers.Pixel( bounds.right, bounds.top ) );"
                + "    var bbox = new OpenLayers.Bounds( minXY.lon, minXY.lat, maxXY.lon, maxXY.lat );"
                //+ "    alert( bbox );"
                + "    this.events.triggerEvent('" + EVENT_BOX + "', {bbox: bbox});"
                + "}"
                + "});"
                + getJSObjRef() + ".initialize();"
                );
    }

}
