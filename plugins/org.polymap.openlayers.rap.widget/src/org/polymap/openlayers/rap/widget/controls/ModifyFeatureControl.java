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
 * @author Marcus -LiGi- B&uuml;schleb < mail: ligi (at) polymap (dot) de >
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 */
public class ModifyFeatureControl 
        extends Control {

    /** Triggered before a feature is modified. */
    public final static String      EVENT_BEFORE_MODIFIED = "beforefeaturemodified";
    /** Triggered before a feature is modified. */
    public final static String      EVENT_AFTER_MODIFIED = "afterfeaturemodified";
    /** Triggered when the feature has been modified. */
    public final static String      EVENT_MODIFIED = "featuremodified";
    
    /** Constant used to make the control work in reshape mode. */
    public final static int         RESHAPE = 1;
    /** Constant used to make the control work in resize mode. */
    public final static int         RESIZE = 2;
    /** Constant used to make the control work in rotate mode. */
    public final static int         ROTATE = 4;
    /** Constant used to make the control work in drag mode. */
    public final static int         DRAG = 8;


	public ModifyFeatureControl(VectorLayer layer) {
		super.create("new OpenLayers.Control.ModifyFeature( "
				+ layer.getJSObjRef() + "   );");
	}

	public void setDeleteKeyCodes(int[] codes) {
        String code = "obj.deleteCodes = [";
        for (int i=0; i<codes.length; i++) {
            code += codes[i];
            code += (i+1 < codes.length ? "," : "]");
        }
        super.addObjModCode(code);
	}
	
	public void addMode(int mode) {
		super.addObjModCode("obj.mode |= OpenLayers.Control.ModifyFeature." + mode2name(mode));
	}

	public void removeMode(int mode) {
		super.addObjModCode("obj.mode &= ~OpenLayers.Control.ModifyFeature." + mode2name(mode));
	}

    private String mode2name(int mode) {
        switch (mode) {
            case DRAG:
                return "DRAG";
            case ROTATE:
                return "ROTATE";
            case RESIZE:
                return "RESIZE";
            case RESHAPE:
                return "RESHAPE";
            default:
                return "UNKNOWN_MODE";
        }
    }

}
