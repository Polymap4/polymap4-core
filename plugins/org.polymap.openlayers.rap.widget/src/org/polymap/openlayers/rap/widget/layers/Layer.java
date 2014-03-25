/*
 * polymap.org
 * Copyright (C) 2009-2014, Polymap GmbH. All rights reserved.
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
package org.polymap.openlayers.rap.widget.layers;

import org.polymap.openlayers.rap.widget.base.OpenLayersObject;
import org.polymap.openlayers.rap.widget.base_types.OpenLayersMap;

/**
 * 
 * @author Marcus -LiGi- B&uuml;schleb < mail: ligi (at) polymap (dot) de >
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class Layer extends OpenLayersObject {

    /** Constant to be used in {@link #setTransitionEffect(String)}. */
    public static final String TRANSITION_RESIZE = "resize";
	
	
    private String          name;
    
    private boolean         isBaseLayer;
    

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setIsBaseLayer(Boolean is_base_layer) {
		addObjModCode("setIsBaseLayer", is_base_layer);
		isBaseLayer = is_base_layer;
	}

	public boolean isBaseLayer() {
	    return isBaseLayer;
    }

	
    /**
     * Set the visibility flag for the layer and hide/show & redraw accordingly. Fire
     * event unless otherwise specified.
     * <p/>
     * For base layers use {@link OpenLayersMap#setBaseLayer(Layer)} instead.
     */
	public void setVisibility(Boolean is_visible) {
	    if (isBaseLayer) {
	        System.err.print( "WARN: For base layers use OpenLayersMap#setBaseLayer(Layer) instead." );
	    }
		addObjModCode("setVisibility", is_visible);
	}

	public void display(Boolean flag) {
		addObjModCode("display", flag);
	}

	public void setOpacity(double newOpacity) {
		addObjModCode("setOpacity", newOpacity);
	}

    /**
     * Redraws the layer.
     */
    public void redraw() {
        addObjModCode("obj.redraw();");
    }

    /**
     * <b>Note:</b> This does not seem effectivelly change layer index in the map.
     *
     * @param index
     */
	public void setZIndex(int index) {
	    addObjModCode("setZIndex", index);  
	}

	/**
	 *
	 * @param effect {@link #TRANSITION_RESIZE}
	 */
	public void setTransitionEffect(String effect) {
        setObjAttr("transitionEffect", effect);  
    }


    /**
     * Determines the width (in pixels) of the gutter around image tiles to
     * ignore. By setting this property to a non-zero value, images will be
     * requested that are wider and taller than the tile size by a value of 2 x
     * gutter. This allows artifacts of rendering at tile edges to be ignored.
     * Set a gutter value that is equal to half the size of the widest symbol
     * that needs to be displayed. Defaults to zero. Non-tiled layers always
     * have zero gutter.
     */
	public void setGutter(int gutter) {
        setObjAttr("gutter", gutter);  
	}

}
