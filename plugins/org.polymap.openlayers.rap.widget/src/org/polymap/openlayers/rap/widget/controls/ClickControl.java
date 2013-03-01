/* 
 * polymap.org
 * Copyright 2013, Polymap GmbH. All rights reserved.
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
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @since 3.1
 */
public class ClickControl
        extends Control {

    public static final String          EVENT_CLICK = "cclick";
    
    public ClickControl() {
        super.create( "new OpenLayers.Control();" );
        addObjModCode( "OpenLayers.Util.extend(" + getJSObjRef() + ", {"
                //+ "EVENT_TYPES: [\"" + EVENT_BOX + "\"]"
                //+ ","
                + "initialize: function(options) {"
                + "    this.EVENT_TYPES = OpenLayers.Control.prototype.EVENT_TYPES.concat("
                + "        [\"" + EVENT_CLICK + "\"] );"
                //+ "    alert( this.EVENT_TYPES );"
                + "    OpenLayers.Control.prototype.initialize.apply(this, [options]);"
                + "},"
                + "draw: function() {"
                + "    this.click = new OpenLayers.Handler.Click( " + getJSObjRef() + ","
                + "        {'click': this.onClick,"
                + "         'dblclick': this.onClick},"
                + "        {'stopSingle': true,"
                + "         'stopDouble': true,"
                + "         'pixelTolerance': 5,"
                + "         'double': true}"
                + "    );"
                + "},"
                + "activate: function() {"
                + "    this.click.activate();"
                + "},"
                + "deactivate: function() {"
                + "    this.click.deactivate();"
                + "},"
                + "onClick: function( ev ) {"
                + "    var pos = this.map.getLonLatFromPixel( new OpenLayers.Pixel( ev.xy.y, ev.xy.y ) );"
                //+ "    alert( 'Pos:' + pos );"
                + "    this.events.triggerEvent('" + EVENT_CLICK + "', {'pos': pos});"
                + "}"
                + "});"
                + getJSObjRef() + ".initialize();"
                );
    }

}
