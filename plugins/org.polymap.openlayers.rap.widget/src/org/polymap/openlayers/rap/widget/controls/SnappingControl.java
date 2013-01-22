/*
 * polymap.org
 * Copyright 2009-2012, Polymap GmbH. All rights reserved.
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

import org.polymap.openlayers.rap.widget.layers.VectorLayer;

/**
 * Acts as a snapping agent while editing vector features.
 * 
 * @author Marcus -LiGi- B&uuml;schleb < mail: ligi (at) polymap (dot) de >
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class SnappingControl extends Control {

    /**
     * Triggered before a snap occurs. Listeners receive an event object with
     * point, x, y, distance, layer, and snapType properties. The point property
     * will be original point geometry considered for snapping. The x and y
     * properties represent coordinates the point will receive. The distance is
     * the distance of the snap. The layer is the target layer. The snapType
     * property will be one of node, vertex, or edge.
     **/
    public final static String EVENT_BEFORE_SNAP ="beforesnap";

    /**
     * Triggered when a snap occurs. Listeners receive an event with point,
     * snapType, layer, and distance properties. The point will be the location
     * snapped to. The snapType will be one of node, vertex, or edge. The layer
     * will be the target layer. The distance will be the distance of the snap
     * in map units.
     **/
    public final static String EVENT_SNAP ="snap";
   
    /**
     * Triggered when a vertex is unsnapped.  Listeners receive an event with a point property.
     */
    public final static String EVENT_UNSNAP ="unsnap";
    
    
	public SnappingControl(VectorLayer layer, VectorLayer target, Boolean greedy) {
		_create(layer, target.getJSObjRef(), greedy);
	}


    /**
     * @param layer {OpenLayers.Layer.Vector} The editable layer. Features from
     *        this layer that are digitized or modified may have vertices
     *        snapped to features from any of the target layers.
     * @param targets {Array(Object | OpenLayers.Layer.Vector)} A list of
     *        objects for configuring target layers. See valid properties of the
     *        target objects. If the items in the targets list are vector layers
     *        (instead of configuration objects), the defaults from the
     *        <defaults> property will apply. The editable layer itself may be a
     *        target
     * @param greedy
     * @param tolerance Snapping tolerance in pixels. Default: 10.
     */
	public SnappingControl(VectorLayer layer, VectorLayer[] targets,
			boolean greedy, int tolerance) {
	    assert tolerance >= 0 && tolerance <= 50 : "Invalid tolerance value: " + tolerance;
		StringBuilder targetsCode = new StringBuilder( 256 );
		for (VectorLayer target : targets) {
		    targetsCode.append( targetsCode.length() != 0 ? "," : "" );
            targetsCode.append( "{" );
            targetsCode.append( "layer:" ).append( target.getJSObjRef() ).append( ',' );
            targetsCode.append( "tolerance:" ).append( tolerance );
            targetsCode.append( "}" );
		}
		_create(layer, targetsCode.toString(), greedy);
	}

	private void _create(VectorLayer layer, String target_code, Boolean greedy) {
		super.create("new OpenLayers.Control.Snapping({ " + "layer:"
				+ layer.getJSObjRef() + "," + "targets: [" + target_code + "],"
				+ "greedy:" + greedy + "});");
	}
}
