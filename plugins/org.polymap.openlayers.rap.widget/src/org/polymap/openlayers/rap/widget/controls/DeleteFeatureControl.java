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
public class DeleteFeatureControl 
        extends Control {

	public DeleteFeatureControl(VectorLayer layer) {
	    String declaration = "var DeleteFeature = OpenLayers.Class(OpenLayers.Control, {\n" + 
	    "    initialize: function(layer, options) {\n" + 
	    "        OpenLayers.Control.prototype.initialize.apply(this, [options]);\n" + 
	    "        this.layer = layer;\n" + 
	    "        this.handler = new OpenLayers.Handler.Feature(\n" + 
	    "            this, layer, {click: this.clickFeature}\n" + 
	    "        );\n" + 
	    "    },\n" + 
	    "    clickFeature: function(feature) {\n" + 
	    "        // if feature doesn't have a fid, destroy it\n" + 
	    "        if(feature.fid == undefined) {\n" + 
	    "            this.layer.destroyFeatures([feature]);\n" + 
	    "        } else {\n" + 
	    "            feature.state = OpenLayers.State.DELETE;\n" + 
	    "            this.layer.events.triggerEvent(\"afterfeaturemodified\", \n" + 
	    "                                           {feature: feature});\n" + 
	    "            feature.renderIntent = \"select\";\n" + 
	    "            this.layer.drawFeature(feature);\n" + 
	    "        }\n" + 
	    "    },\n" + 
	    "    setMap: function(map) {\n" + 
	    "        this.handler.setMap(map);\n" + 
	    "        OpenLayers.Control.prototype.setMap.apply(this, arguments);\n" + 
	    "    },\n" + 
	    "    CLASS_NAME: \"OpenLayers.Control.DeleteFeature\"\n" + 
	    "});\n";
	    
		super.create( "new OpenLayers.Control.DrawFeature( "
				+ layer.getJSObjRef() + " );");
	}

//    public void addMode( int mode ) {
//        super.addObjModCode( "obj.mode |=  OpenLayers.Control.ModifyFeature." + mode2name( mode ) );
//    }
//
//    public void rmMode( int mode ) {
//        super.addObjModCode( "obj.mode &=  ~OpenLayers.Control.ModifyFeature." + mode2name( mode ) );
//    }

}
