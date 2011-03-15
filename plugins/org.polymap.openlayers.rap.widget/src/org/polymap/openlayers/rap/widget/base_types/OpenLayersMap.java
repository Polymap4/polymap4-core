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
package org.polymap.openlayers.rap.widget.base_types;

import org.polymap.openlayers.rap.widget.OpenLayersWidget;
import org.polymap.openlayers.rap.widget.base.OpenLayersObject;
import org.polymap.openlayers.rap.widget.controls.Control;
import org.polymap.openlayers.rap.widget.layers.Layer;

/**
 * 
 * @author Marcus -LiGi- B&uuml;schleb < mail: ligi (at) polymap (dot) de >
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 */
public class OpenLayersMap 
        extends OpenLayersObject {

    /** Event: triggered after mouseover the map. */
    public final static String        EVENT_MOUSE_OVER = "mouseover";
    
    /** Event: triggered after mouseout the map. */
    public final  static String       EVENT_MOUSE_OUT = "mouseout";
    
    /** Event: triggered after mousemove the map. */
    public final  static String       EVENT_MOUSE_MOVE = "mousemove";
    
   /** triggered before a layer has been added.  The event object will include a layer property that references the layer to be added. **/
    public  final static String       EVENT_PREADDLAYER = "preaddlayer";

    /** triggered after a layer has been added.  The event object will include a layer property that references the added layer. **/
    public final static String        EVENT_ADDLAYER = "addlayer";
    
    /** triggered after a layer has been removed.  The event object will include a layer property that references the removed layer. **/
    public final static String        EVENT_REMOVELAYER = "removelayer";

    /** triggered after a layer name change, order change, or visibility change (due to resolution thresholds).  Listeners will receive an event object with layer and property properties.  The layer property will be a reference to the changed layer.  The property property will be a key to the changed property (name, visibility, or order). **/
    public final static String        EVENT_CHANGELAYER = "changelayer";

    /** triggered after the start of a drag, pan, or zoom **/
    public final static String        EVENT_MOVESTART = "movestart";
    
    /** triggered after each drag, pan, or zoom **/
    public final static String        EVENT_MOVE = "move";
    
    /** triggered after a drag, pan, or zoom completes **/
    public final static String        EVENT_MOVEEND = "moveend";
    
    /**  triggered after a zoom completes **/
    public final static String        EVENT_ZOOMEND = "zoomend";
    
    /**  triggered after a marker has been added **/
    public final static String        EVENT_ADDMARKER = "addmarker";
   
    /** triggered after a marker has been removed **/
    public final static String        EVENT_REMOVEMARKER = "removemarker";
    
    /** triggered after markers have been cleared **/
    public final static String        EVENT_CLEARMARKERS = "clearmarkers";
    
    /** triggered after the base layer changes **/
    public final static String        EVENT_CHANGEBASELAYER ="changebaselayer";
    
	private OpenLayersWidget           widget;
	
	private Projection                 projection;
	
	private Projection                 display_projection;
	
	private String                     units;
	
	private Bounds                     maxExtent;
	
	private float                      maxResolution;
	
	
    public OpenLayersMap(OpenLayersWidget widget,Projection projection,Projection display_projection,String units,Bounds maxExtent,float maxResolution) {
        this.widget = widget;
        this.projection = projection;
        this.display_projection = display_projection;
        this.maxResolution = maxResolution;
        this.units = units;
        this.maxExtent = maxExtent;
        
		super.create_with_widget ("new OpenLayers.Map( { div : document.getElementById( this._id),	controls : []	,projection: " +projection.getJSObjRef() +" , displayProjection: " +display_projection.getJSObjRef() + " , units: '" + units + "' , maxExtent: " + maxExtent.getJSObjRef() + " , maxResolution: " + maxResolution + " });",widget);
	}
	
	public OpenLayersMap(OpenLayersWidget widget) {
        this.widget = widget;

        super.create_with_widget ("new OpenLayers.Map( { div : document.getElementById( this._id),  controls : []  });",widget);
    }
    
	
	public void addLayer(Layer layer2add) {
		super.addObjModCode("addLayer",layer2add);
	}

	public void removeLayer(Layer layer2rm) {
		super.addObjModCode("removeLayer",layer2rm);
	}
		
	public void addControl(Control control2add) {
		control2add.has_map=true;
		super.addObjModCode("addControl",control2add);
	}

	public void setProxy(String proxy){
		super.addObjModCode("OpenLayers.ProxyHost='"+proxy+"';");
	}
	
	public void removeControl(Control control2rm) {
		super.addObjModCode("removeControl",control2rm);
	}

	public void zoomTo(int zoom) {
		super.addObjModCode("zoomTo",zoom);
	}

	public void zoomToExtent(Bounds extent , boolean closest) {
		super.addObjModCode("zoomToExtent",extent,closest);
	}
	
	public void zoomToScale(double scale , boolean closest) {
		super.addObjModCode("zoomToScale",scale,closest);
	}
	
	public void setCenter(double center_lon, double center_lat) {
		super.addObjModCode("setCenter", new LonLat(center_lon,center_lat));
	}

	public void setBaseLayer(Layer layer) {
		super.addObjModCode("setBaseLayer",layer);
	}
	
	public Projection getProjection() {
        return projection;
    }

    public void setProjection(Projection projection){
		super.setObjAttr("projection", projection);
	}
	
    public Projection getDisplayProjection() {
        return display_projection;
    }
    
    public float getMaxResolution() {
        return maxResolution;
    }

    public void setMaxScale(float scale){
        super.setObjAttr("maxScale", scale);
    }

    public void setMinScale(float scale){
        super.setObjAttr("minScale", scale);
    }
    
    
    public void setNumZoomLevels(int num) {
        super.setObjAttr("numZoomLevels", num);
    }

    public void setDisplayProjection(Projection projection){
        this.display_projection = projection;
        super.setObjAttr("displayProjection", projection);
    }

    public Bounds getMaxExtent() {
        return maxExtent;
    }

    public void setMaxExtent(Bounds extent) {
        this.maxExtent = extent;
        super.setObjAttr("maxExtent", extent);
    }

    public String getUnits() {
        return units;
    }

    /**
     * This property is what allows OpenLayers to know what scale things are
     * being rendered at, which is important for scale-based methods of zooming
     * and the Scale display control.
     * 
     * @param units The map units. Defaults to "degrees". Possible values are
     *        "degrees" (or "dd"), "m", "ft", "km", "mi", "inches".
     */
    public void setUnits(String units) {
        this.units = units;
        super.setObjAttr("units", units);
    }
}
