/*
 * polymap.org
 * Copyright 2009-2013, Polymap GmbH. All rights reserved.
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

import org.polymap.openlayers.rap.widget.base_types.OpenLayersMap;


/**
 * 
 * @author Marcus -LiGi- B&uuml;schleb <mail:ligi (at) polymap (dot) de>
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class KeyboardDefaultsControl 
        extends Control {

	public KeyboardDefaultsControl() {
		super.create( "new OpenLayers.Control.KeyboardDefaults();" );
	}

    @Override
    public void setMap( OpenLayersMap map ) {
        super.setMap( map );
        if (map != null) {
//            addObjModCode( "alert(" + map.getJSObjRef() + ".div);" );
//            addObjModCode( getJSObjRef() + ".observeElement=" + map.getJSObjRef() + ".div;" );
        }
    }
	
}
