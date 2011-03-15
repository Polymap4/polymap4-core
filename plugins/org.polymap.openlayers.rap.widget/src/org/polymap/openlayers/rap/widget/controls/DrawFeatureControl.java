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
package org.polymap.openlayers.rap.widget.controls;

import org.polymap.openlayers.rap.widget.layers.VectorLayer;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class DrawFeatureControl 
        extends Control {
    
    /** Triggered when a feature has been added. */
    public final static String      EVENT_ADDED = "featureadded";

    public final static String      HANDLER_LINE = "OpenLayers.Handler.Path";

    public final static String      HANDLER_POLYGON = "OpenLayers.Handler.Polygon";

    public final static String      HANDLER_POINT = "OpenLayers.Handler.Point";


    /**
     * 
     * @param layer
     * @param handler The handler to use to draw the feature. One of the
     *        <code>HANDLER_XXX</code> constants.
     */
	public DrawFeatureControl(VectorLayer layer, String handler) {
		super.create( "new OpenLayers.Control.DrawFeature("
				+ layer.getJSObjRef() + ", "
				+ handler + ");");
	}

//    public void addMode( int mode ) {
//        super.addObjModCode( "obj.mode |=  OpenLayers.Control.ModifyFeature." + mode2name( mode ) );
//    }
//
//    public void rmMode( int mode ) {
//        super.addObjModCode( "obj.mode &=  ~OpenLayers.Control.ModifyFeature." + mode2name( mode ) );
//    }

}
