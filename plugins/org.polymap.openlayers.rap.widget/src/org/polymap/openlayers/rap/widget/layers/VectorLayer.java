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

package org.polymap.openlayers.rap.widget.layers;

import org.polymap.openlayers.rap.widget.base_types.Protocol;
import org.polymap.openlayers.rap.widget.base_types.Style;
import org.polymap.openlayers.rap.widget.base_types.StyleMap;
import org.polymap.openlayers.rap.widget.features.VectorFeature;

/**
 * 
 * @author Marcus -LiGi- B&uuml;schleb < mail: ligi (at) polymap (dot) de >
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 */
public class VectorLayer
        extends Layer {

    /** Triggered before a feature is added.  
     *  Listeners will receive an object with a feature property referencing the feature to be added.   
     **/
    public final static String EVENT_BEFOREFEATUREADDED="beforefeatureadded";
    
    /**
     * Triggered before an array of features is added.  
     * Listeners will receive an object with a features property referencing the feature to be added. 
     **/
    public final static String EVENT_BEFOREFEATURESADDED="beforefeaturesadded";

    /** Triggered after a feature is added.  
     *  Listeners will receive an object with a feature property referencing the feature to be added.   
     **/
    public final static String EVENT_FEATUREADDED="featureadded";
    
    /**
     * Triggered after an array of features is added.  
     * Listeners will receive an object with a features property referencing the feature to be added. 
     **/
    public final static String EVENT_FEATURESADDED="featuresadded";

    /**
     * Triggered before a feature is removed.
     *  Listeners will receive an object with a feature property referencing the feature to be removed.
     */
    public final static String EVENT_BEFOREFEATUREREMOVED="beforefeatureremoved";
        
    /** 
     * Triggerd after a feature is removed.  The event object passed to listeners will have a feature property with a reference to the removed feature. 
    **/
    public final static String EVENT_FEATUREREMOVED="beforefeatureremoved";
    
    /** 
     * Triggered after features are removed.  The event object passed to listeners will have a features property with a reference to an array of removed features.
     **/
    public final static String EVENT_FEATURESREMOVED="beforefeaturesremoved";
    
    /**  
     * Triggered after a feature is selected.  Listeners will receive an object with a feature property referencing the selected feature. 
    **/
    public final static String EVENT_FEATURESELECTED="featureselected";
    
    /**  
     * Triggered after a feature is unselected.  Listeners will receive an object with a feature property referencing the unselected feature.
    **/
    public final static String EVENT_FEATURESUNELECTED="featureunselected";
    
    /**
     * Triggered when a feature is selected to be modified.  Listeners will receive an object with a feature property referencing the selected feature.
     **/
     public final static String EVENT_BEFOREFEATUREMODIFIED="beforefeaturemodified";
 
    /**
     * Triggered when a feature has been modified.  Listeners will receive an object with a feature property referencing the modified feature.
     **/
     public final static String EVENT_FEATUREMODIFIED="featuremodified";
 
     /**
      * Triggered when a feature is finished being modified.  Listeners will receive an object with a feature property referencing the modified feature.
      **/
      public final static String EVENT_AFTERFEATUREMODIFIED="afterfeaturemodified";
     
    /**
     * Triggered when a vertex within any feature geometry has been modified.  Listeners will receive an object with a feature property referencing the modified feature, a vertex property referencing the vertex modified (always a point geometry), and a pixel property referencing the pixel location of the modification.
     **/
     public final static String EVENT_VERTEXMODIFIED="vertexmodified";
 
    /**
     * Triggered when a feature sketch bound for this layer is started.  Listeners will receive an object with a feature property referencing the new sketch feature and a vertex property referencing the creation point.
     **/
     public final static String EVENT_SKETCHSTARTED="sketchstarted";
 
    /**
     * Triggered when a feature sketch bound for this layer is modified.  Listeners will receive an object with a vertex property referencing the modified vertex and a feature property referencing the sketch feature.
     **/
     public final static String EVENT_SKETCHMODIFIED="sketchmodified";
 
    /**
     * Triggered when a feature sketch bound for this layer is complete.  Listeners will receive an object with a feature property referencing the sketch feature. 
     **/
     public final static String EVENT_SKETCHCOMPLETE="sketchcomplete";
    
    /**
     * Triggered when something wants a strategy to ask the protocol for a new set of features.
     **/
    public final static String EVENT_REFRESH="refresh";
    
    
    public VectorLayer( String name ) {
        super.setName( name );
        super.create( "new OpenLayers.Layer.Vector( '" + name + "' );" );
    }
    
    
    public VectorLayer( String name, StyleMap style_map ) {
        super.setName( name );
        super.create( "new OpenLayers.Layer.Vector( '" + name + "',{" + ""
                + "" + "styleMap:" + style_map.getJSObjRef() + "} );" );
    }


    public VectorLayer( String name, Protocol protocol ) {
        super.setName( name );
        super.create( "new OpenLayers.Layer.Vector( '" + name + "',{" + ""
                + "strategies: [new OpenLayers.Strategy.Fixed()]," + "protocol:"
                + protocol.getJSObjRef() + "} );" );
    }


    public VectorLayer( String name, Protocol protocol, Style style ) {
        super.setName( name );
        super.create( "new OpenLayers.Layer.Vector( '" + name + "',{" + ""
                + "strategies: [new OpenLayers.Strategy.Fixed()]," + "protocol:"
                + protocol.getJSObjRef() + "," + "style:" + style.getJSObjRef() + "} );" );
    }


    public VectorLayer( String name, Protocol protocol, StyleMap style_map ) {
        super.setName( name );
        super.create( "new OpenLayers.Layer.Vector( '" + name + "',{" + ""
                + "strategies: [new OpenLayers.Strategy.Fixed()]," + "protocol:"
                + protocol.getJSObjRef() + "," + "styleMap:" + style_map.getJSObjRef() + "} );" );
    }


    public void addFeatures( VectorFeature vf ) {
        super.addObjModCode( "addFeatures", vf );
    }

    
    /**
     * Ask the layer to request features again and redraw them. Triggers the
     * refresh event if the layer is in range and visible.
     */
    public void refresh() {
        addObjModCode("obj.refresh();");
    }

}
