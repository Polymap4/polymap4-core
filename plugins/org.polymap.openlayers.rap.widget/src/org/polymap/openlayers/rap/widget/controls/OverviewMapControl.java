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

import java.util.HashSet;
import java.util.Set;

import org.polymap.openlayers.rap.widget.base_types.OpenLayersMap;
import org.polymap.openlayers.rap.widget.layers.Layer;

/**
 * 
 * @author Marcus -LiGi- B&uuml;schleb < mail: ligi (at) polymap (dot) de >
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 */
public class OverviewMapControl 
        extends MinimizeableControl {

    private Set<Layer>          layers = new HashSet();
    
    
    public OverviewMapControl() {
        super.create( "new OpenLayers.Control.OverviewMap();" );
    }

    public OverviewMapControl( OpenLayersMap map, Layer layer ) {
//  		"projection: " +projection.getJSObjRef() +
//  		" , displayProjection: " +display_projection.getJSObjRef() + 
//  		" , units: '" + units + "' , " +
//  	    "maxExtent: " + maxExtent.getJSObjRef() + 
//  	    " , maxResolution: " + maxResolution + " });",widget);

        super.create( "new OpenLayers.Control.OverviewMap({" +
            "mapOptions: {" + 
                "maxExtent:" + map.getMaxExtent().getJSObjRef() + "," + 
                "projection:" + map.getProjection().getJSObjRef() + "," +
                "displayProjection:" + map.getDisplayProjection().getJSObjRef() + "," +
                "maxResolution:" + map.getMaxResolution() + "," + 
                "units:'" + map.getUnits() + "'" + 
            "}," +
            // clone to allow layer have different attributes
            "layers:[" + layer.getJSObjRef() + ".clone()" + "]" +
        "});" );
        layers.add( layer );
    }

    public void dispose() {
        super.dispose();
        layers.clear();
    }

    public void addLayer( Layer layer ) {
        if (layers.add( layer )) {
            super.create( "obj.layers=[" + layer.getJSObjRef() + "];" );
        }
    }

    public void removeLayer( Layer layer ) {
        if (layers.remove( layer )) {
            super.create( "obj.layers=[" + "" + "];" );
        }
    }

}
