/* 
 * polymap.org
 * Copyright 2012-2013, Falko Bräutigam. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.core.mapeditor.tooling.select;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.json.JSONObject;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import org.eclipse.swt.widgets.Composite;

import org.polymap.core.data.ui.featureselection.FeatureSelectionView;
import org.polymap.core.data.util.Geometries;
import org.polymap.core.geohub.LayerFeatureSelectionManager;
import org.polymap.core.mapeditor.Messages;
import org.polymap.core.mapeditor.tooling.edit.BaseLayerEditorTool;
import org.polymap.core.mapeditor.tooling.edit.BaseVectorLayer;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.runtime.event.EventHandler;

import org.polymap.openlayers.rap.widget.base.OpenLayersEventListener;
import org.polymap.openlayers.rap.widget.base.OpenLayersObject;
import org.polymap.openlayers.rap.widget.controls.BoxControl;
import org.polymap.openlayers.rap.widget.controls.SelectFeatureControl;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class SelectionTool
        extends BaseLayerEditorTool 
        implements PropertyChangeListener, OpenLayersEventListener {

    private static Log log = LogFactory.getLog( SelectionTool.class );
    
    private SelectionVectorLayer            vectorLayer;

    private SelectFeatureControl            selectControl;
    
    private BoxControl                      boxControl;

    private LayerFeatureSelectionManager    fsm;


    @Override
    public void dispose() {
        log.debug( "dispose(): ..." );
        onDeactivate();
        super.dispose();
    }


    @Override
    public BaseVectorLayer getVectorLayer() {
        return vectorLayer;
    }


    @Override
    public void onActivate() {
        super.onActivate();
        if (getSelectedLayer() == null) {
            return;
        }
        this.fsm = LayerFeatureSelectionManager.forLayer( getSelectedLayer() );
        this.fsm.addSelectionChangeListener( this );
        
        vectorLayer = new SelectionVectorLayer( getSite().getEditor(), getSelectedLayer() );
        vectorLayer.enableHover();
        vectorLayer.activate();
        vectorLayer.selectFeatures( fsm.getFeatureCollection() );

//        clickControl = new ClickControl();
//        getSite().getEditor().addControl( clickControl );
//        HashMap<String, String> payload2 = new HashMap<String, String>();
//        payload2.put( "pos", "new OpenLayers.Format.JSON().write( event.pos, false )" );
//        clickControl.events.register( this, ClickControl.EVENT_CLICK, payload2 );

        boxControl = new BoxControl();
        getSite().getEditor().addControl( boxControl );
        HashMap<String, String> payload1 = new HashMap<String, String>();
        payload1.put( "bbox", "new OpenLayers.Format.JSON().write( event.bbox, false )" );
        payload1.put( "pos", "new OpenLayers.Format.JSON().write( event.pos, false )" );
        boxControl.events.register( this, BoxControl.EVENT_BOX, payload1 );

        selectControl = new SelectFeatureControl( vectorLayer.getVectorLayer() );
        getSite().getEditor().addControl( selectControl );

        // featureselected event
        HashMap<String, String> payload = new HashMap<String, String>();
        payload.put( "fid", "event.feature.fid" );
        //                payload.put( "features", "new OpenLayers.Format.GeoJSON().write(event.object.features, false)" );
        //                payload.put( "features", 
        //                        "eval( 'var fids = new Array();" + 
        //                        "for (var i=0; i<event.object.features.length; i++) {" + 
        //                        "    fids[i] = event.object.features[i].fid;" +
        //                        "}" +
        //                        "new OpenLayers.Format.JSON().write( fids, false );" +
        //                        " )'"
        //                );
        vectorLayer.getVectorLayer().events.register( this, SelectFeatureControl.EVENT_SELECTED, payload );

        boxControl.activate();
        selectControl.activate();    

        // XXX find an indirect way to signal that the layer has selected
        // features; GeoHub? 
        FeatureSelectionView.open( getSelectedLayer() );
    }


    @Override
    public void createPanelControl( Composite parent ) {
        super.createPanelControl( parent );
        vectorLayer.getStyler().createPanelControl( parent, this );
    }

    
    @Override
    public void onDeactivate() {
        if (fsm != null) {
            fsm.removeSelectionChangeListener( this );
            fsm = null;
        }
        super.onDeactivate();
        if (selectControl != null) {
            getSite().getEditor().removeControl( selectControl );
            selectControl.deactivate();
            selectControl.destroy();
            selectControl.dispose();
            selectControl = null;
        }
        if (boxControl != null) {
            getSite().getEditor().removeControl( boxControl );
            boxControl.deactivate();
            boxControl.destroy();
            boxControl.dispose();
            boxControl = null;
        }
//        if (clickControl != null) {
//            getSite().getEditor().removeControl( clickControl );
//            clickControl.destroy();
//            clickControl.dispose();
//            clickControl = null;
//        }
        if (vectorLayer != null) {
            vectorLayer.dispose();
            vectorLayer = null;
        }
    }

    
    /**
     * Listen to feature selection changes from {@link LayerFeatureSelectionManager}.
     */
    @Override
    @EventHandler
    public void propertyChange( PropertyChangeEvent ev ) {
        assert fsm == ev.getSource();
        
        //select
        if (ev.getPropertyName().equals( LayerFeatureSelectionManager.PROP_FILTER )) {
            vectorLayer.selectFeatures( fsm.getFeatureCollection() );
        }
        // hover
        else if (ev.getPropertyName().equals( LayerFeatureSelectionManager.PROP_HOVER )) {
            selectControl.unselectAll();
            selectControl.selectFids( Collections.singletonList( (String)ev.getNewValue() ) );
        }
    }

    
    @Override
    public void process_event( OpenLayersObject obj, String name, final HashMap<String,String> payload ) {
        log.debug( "process_event() event: " + name + ", from: " + obj );
        for (Map.Entry entry : payload.entrySet()) {
            log.info( "    key: " + entry.getKey() + ", value: " + StringUtils.abbreviate( (String)entry.getValue(), 0, 60 ) );
        }
        
        // box selected
        if (name.equals( BoxControl.EVENT_BOX )) {
            Polymap.getSessionDisplay().asyncExec( new Runnable() {
                public void run() {                                
                    try {
                        String bbox = payload.get( "bbox" );
                        String pos = payload.get( "pos" );
                        
                        // bbox
                        if (bbox != null && !bbox.equals( "null" )) {
                            JSONObject json = new JSONObject( bbox );
                            vectorLayer.selectFeatures( new ReferencedEnvelope(
                                    json.getDouble( "left" ), json.getDouble( "right" ),
                                    json.getDouble( "bottom" ), json.getDouble( "top" ),
                                    getSite().getEditor().getMap().getCRS() ), true );
                        }
                        // single click
                        else if (pos != null && !pos.equals( "null" )) {
                            JSONObject json = new JSONObject( pos );
                            
                            CoordinateReferenceSystem crs = getSite().getEditor().getMap().getCRS();
                            GeometryFactory gf = new GeometryFactory();

                            Point point = gf.createPoint( new Coordinate( json.getDouble( "lon" ), json.getDouble( "lat" ) ) );
                            Point norm = Geometries.transform( point, crs, Geometries.crs( "EPSG:3857" ) );

                            double buffer = 50;
                            ReferencedEnvelope buffered = new ReferencedEnvelope(
                                    norm.getX()-buffer, norm.getX()+buffer, norm.getY()-buffer, norm.getY()+buffer,
                                    Geometries.crs( "EPSG:3857" ) );

                            vectorLayer.selectFeatures( buffered.transform( crs, true ), true );                            
                        }
                        else {
                            throw new RuntimeException( "No bbox or pos in payload: " + payload );
                        }
                    }
                    catch (final Exception e) {
                        log.warn( "", e );
                    }
                };
            });
        }
        
        // feature clicked
        else if (name.equals( SelectFeatureControl.EVENT_SELECTED )) {
            String fid = payload.get( "fid" );
            fsm.setHovered( fid );
        }
    }

    
    @Override
    public String i18n( String key, Object... args ) {
        return Messages.get( "SelectionTool_" + key, args );    
    }
    
}
