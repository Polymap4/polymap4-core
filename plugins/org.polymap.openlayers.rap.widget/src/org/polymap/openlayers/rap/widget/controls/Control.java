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

import org.polymap.openlayers.rap.widget.base.OpenLayersObject;
import org.polymap.openlayers.rap.widget.base_types.ControlPosition;
import org.polymap.openlayers.rap.widget.base_types.OpenLayersMap;

/**
 * 
 * @author Marcus -LiGi- B&uuml;schleb < mail: ligi (at) polymap (dot) de >
 */
public class Control extends OpenLayersObject {

    /** Triggered when activated. **/
    public final static String EVENT_ACTIVATE = "activate";
    
    /** Triggered when deactivated. **/
    public final static String EVENT_DEACTIVATE = "deactivate";
    
	public boolean         has_map = false;
	
	protected boolean      active = false;
	
	
	public Control() {
	}

	public Control(String construct_code) {
		super.create(construct_code);
		this.active = true;
	}

	/**
	 * Activates this non-visual control. The control has to be added to a map previously.
	 */
	public void activate() {
		assert has_map : "control needs a map before activating it";
		super.addObjModCode("obj.activate();");
		this.active = true;
	}

	public void deactivate() {
		super.addObjModCode("obj.deactivate();");
        this.active = true;
	}

    /**
     * The activation state of this control This represents the server side
     * state. If the control was activated on the client, then this has to
     * be handled by listening for the events explicitly.
     */
    public boolean isActive() {
        return active;
    }

    public ControlPosition getPosition() {
		return new ControlPosition(this);
	}

	public void setMap(OpenLayersMap map) {
	    super.addObjModCode("setMap",map);
	}
	
	public void destroy() {
        super.addObjModCode("obj.destroy();");
    }

}
