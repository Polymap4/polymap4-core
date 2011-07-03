/* 
 * polymap.org
 * Copyright 2009, 2011 Falko Bräutigam. All rights reserved.
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
package org.polymap.core.mapeditor.selection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.feature.FeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.gml2.bindings.GML2EncodingUtils;
import org.json.JSONObject;
import org.opengis.feature.Feature;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.spatial.BBOX;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import org.eclipse.jface.dialogs.MessageDialog;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import org.polymap.core.geohub.LayerFeatureSelectionManager;
import org.polymap.core.geohub.LayerFeatureSelectionOperation;
import org.polymap.core.mapeditor.IMapEditorSupport;
import org.polymap.core.mapeditor.ISelectFeatureSupport;
import org.polymap.core.mapeditor.MapEditor;
import org.polymap.core.mapeditor.MapEditorPlugin;
import org.polymap.core.mapeditor.services.JsonEncoder;
import org.polymap.core.mapeditor.services.JsonVectorLayer;
import org.polymap.core.mapeditor.services.SimpleJsonServer;
import org.polymap.core.operation.OperationSupport;
import org.polymap.core.project.ILayer;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.workbench.PolymapWorkbench;
import org.polymap.openlayers.rap.widget.base.OpenLayersEventListener;
import org.polymap.openlayers.rap.widget.base.OpenLayersObject;
import org.polymap.openlayers.rap.widget.base_types.Style;
import org.polymap.openlayers.rap.widget.base_types.StyleMap;
import org.polymap.openlayers.rap.widget.controls.BoxControl;
import org.polymap.openlayers.rap.widget.controls.SelectFeatureControl;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @since 3.0
 */
@SuppressWarnings("deprecation")
class SelectFeatureSupport
        implements ISelectFeatureSupport, OpenLayersEventListener, PropertyChangeListener {

    static Log log = LogFactory.getLog( SelectFeatureSupport.class );

    private static final int        DEFAULT_MAX_SELECTIONS = 100;
    
    /** The {@link MapEditor} we are working with. */
    private MapEditor               mapEditor;

    private boolean                 active;
    
    private SelectFeatureControl    selectControl;
    
    private SelectFeatureControl    hoverControl;
    
    private BoxControl              boxControl;

    private JsonVectorLayer         vectorLayer;
    
    /** The features that were last selected via {@link #selectFeatures(Collection)}. */
    private Collection<Feature>     features = new ArrayList();

    private ILayer                  layer;

    private LayerFeatureSelectionManager fsm;
    

    /**
     * 
     * @param mapEditor
     * @throws Exception 
     */
    public SelectFeatureSupport( MapEditor mapEditor ) 
    throws Exception {
        this.mapEditor = mapEditor;
        this.mapEditor.addSupportListener( this );

        // jsonEncoder
        CoordinateReferenceSystem crs = mapEditor.getMap().getCRS();
        SimpleJsonServer jsonServer = SimpleJsonServer.instance();
        JsonEncoder jsonEncoder = jsonServer.newLayer( features, crs, false );
        // 3 decimals should be enough even for lat/long values
        //jsonEncoder.setDecimals( 3 );

        // vectorLayer
        Style standard = new Style();
        standard.setAttribute( "strokeWidth", 1 );
        standard.setAttribute( "strokeColor", "#0000ff" );
        //standard.setAttribute( "strokeOpacity", "0.7" );
        standard.setAttribute( "fillColor", "#0000ff" );
        Style temporary = new Style();
        temporary.setAttribute( "strokeWidth", 1 );
        temporary.setAttribute( "strokeColor", "#00b0ff" );
        temporary.setAttribute( "fillColor", "#00b0ff" );
        Style select = new Style();
        select.setAttribute( "strokeWidth", 2 );
        select.setAttribute( "strokeColor", "#00b0ff" );
        select.setAttribute( "fillColor", "#00b0ff" );

        StyleMap styles = new StyleMap();
        styles.setIntentStyle( "default", standard );
        styles.setIntentStyle( "temporary", temporary );
        styles.setIntentStyle( "select", select );

        vectorLayer = new JsonVectorLayer( "selection", 
                jsonServer, jsonEncoder , styles );

        vectorLayer.setVisibility( true );
        vectorLayer.setIsBaseLayer( false );
        vectorLayer.setZIndex( 10000 );

        this.mapEditor.addLayer( vectorLayer );
    }


    public void dispose() {
        setActive( false );

        if (fsm != null) {
            fsm.removeChangeListener( this );
            fsm = null;
        }

        boxControl.events.unregister( this, BoxControl.EVENT_BOX );
        mapEditor.removeControl( boxControl );
        boxControl.destroy();
        boxControl.dispose();
        boxControl = null;

        mapEditor.removeControl( selectControl );
        selectControl.destroy();
        selectControl.dispose();
        selectControl = null;

        if (hoverControl != null) {
            mapEditor.removeControl( hoverControl );
            hoverControl.destroy();
            hoverControl.dispose();
            hoverControl = null;
        }

        vectorLayer.events.unregister( this, SelectFeatureControl.EVENT_SELECTED );
        mapEditor.removeLayer( vectorLayer );
        vectorLayer.dispose();
        vectorLayer = null;
    
        this.mapEditor.removeSupportListener( this );
        this.mapEditor = null;
    }

    
    public void connectLayer( ILayer _layer ) {
        assert layer == null;
        this.layer = _layer;
        this.fsm = LayerFeatureSelectionManager.forLayer( layer );
        this.fsm.addChangeListener( this );
    }


    /**
     * The max number of selected features that is currently supported. The
     * actual result depends on default, configuration and client browser.
     */
    public static int getMaxSelections() {
        return DEFAULT_MAX_SELECTIONS;
    }


    public void supportStateChanged( MapEditor editor, IMapEditorSupport support, boolean activated ) {
        log.debug( "support= " + support + " activated= " + activated );
        if (this == support) {
            setActive( activated );
        }
    }


    /**
     * Listen to feature selection changes from {@link LayerFeatureSelectionManager}.
     */
    public void propertyChange( PropertyChangeEvent ev ) {
        assert fsm == ev.getSource();
        
        //select
        if (ev.getPropertyName().equals( LayerFeatureSelectionManager.PROP_FILTER )) {
            selectFeatures( fsm.getFeatureCollection() );
        }
        // hover
        else if (ev.getPropertyName().equals( LayerFeatureSelectionManager.PROP_HOVER )) {
            selectControl.unselectAll();
            selectControl.selectFids( Collections.singletonList( (String)ev.getNewValue() ) );
        }
    }


    public void selectFeatures( FeatureCollection _features ) {
        features.clear();
        Iterator it = _features.iterator();
        try {
            while (it.hasNext()) {
                features.add( (Feature)it.next() );
            }
        }
        finally {
            _features.close( it );
        }

        // still initializing?
        if (vectorLayer != null) {
            vectorLayer.getJsonEncoder().setFeatures( features );
            vectorLayer.refresh();
        }
    }


    protected void setActive( boolean active ) {
        log.debug( "active= " + active );
        if (isActive() == active) {
            return;
        }
        this.active = active;
        if (active) {
            assert vectorLayer != null : "no vectorLayer";

            if (selectControl == null) {
                boxControl = new BoxControl();
                mapEditor.addControl( boxControl );
                HashMap<String, String> payload1 = new HashMap<String, String>();
                payload1.put( "bbox", "new OpenLayers.Format.JSON().write( event.bbox, false )" );
//                payload1.put( "keyMask", "new OpenLayers.Format.JSON().write( event.bbox, false )" );
                boxControl.events.register( this, BoxControl.EVENT_BOX, payload1 );
                
                hoverControl = new SelectFeatureControl( vectorLayer, SelectFeatureControl.FLAG_HOVER );
                hoverControl.setHighlightOnly( true );
                hoverControl.setRenderIntent( "temporary" );
                mapEditor.addControl( hoverControl );

                selectControl = new SelectFeatureControl( vectorLayer/*, SelectFeatureControl.FLAG_HOVER*/ );
                mapEditor.addControl( selectControl );

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
                vectorLayer.events.register( this, SelectFeatureControl.EVENT_SELECTED, payload );
            }
            boxControl.activate();
            hoverControl.activate();
            selectControl.activate();
        } 
        else {
            if (boxControl != null) {
                boxControl.deactivate();
            }
            if (selectControl != null) {
                selectControl.deactivate();
            }
            if (hoverControl != null) {
                hoverControl.deactivate();
            }
        }
    }

    
    public boolean isActive() {
        return active;
    }

    
    public void process_event( OpenLayersObject obj, String name, HashMap<String, String> payload ) {
        log.debug( "process_event() event: " + name + ", from: " + obj );
        for (Map.Entry entry : payload.entrySet()) {
            Object key = entry.getKey();
            Object value = entry.getValue();
            log.info( "    key: " + key + ", value: " + StringUtils.abbreviate( (String)value, 0, 60 ) );
        }

        //
        if (name.equals( BoxControl.EVENT_BOX )) {
            try {
                JSONObject json = new JSONObject( payload.get( "bbox" ) );
                ReferencedEnvelope bbox = null;
                try {
                    bbox = new ReferencedEnvelope(
                            json.getDouble( "left" ),
                            json.getDouble( "right" ),
                            json.getDouble( "bottom" ),
                            json.getDouble( "top" ),
                            mapEditor.getMap().getCRS() );
                }
                catch (final Exception e) {
                    Polymap.getSessionDisplay().asyncExec( new Runnable() {
                        public void run() {                                
                            IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                            MessageDialog.openInformation( window.getShell(), "Achtung", "Bitte markieren Sie immer ein gesamtes Rechteck.\nFehlerhafte Koordinaten: " + e.getLocalizedMessage() );
                        }
                    });
                    return;
                }
                
                CoordinateReferenceSystem dataCRS = layer.getCRS();
                ReferencedEnvelope dataBBox = bbox.transform( dataCRS, true );
                log.info( "dataBBox: " + dataBBox );

                FilterFactory ff = CommonFactoryFinder.getFilterFactory( GeoTools.getDefaultHints() );
                //JD: should this be applied to all geometries?
                //String name = featureType.getDefaultGeometry().getLocalName();
                //JD: changing to "" so it is
                String propname = "";
                String epsgCode = GML2EncodingUtils.crs( dataBBox.getCoordinateReferenceSystem() );
                
                BBOX filter = ff.bbox( propname, dataBBox.getMinX(), dataBBox.getMinY(), 
                        dataBBox.getMaxX(), dataBBox.getMaxY(), epsgCode);
                
                // change feature selection
                LayerFeatureSelectionOperation op = new LayerFeatureSelectionOperation();
                op.init( layer, filter, null, this );
                OperationSupport.instance().execute( op, false, false );
//                fsm.changeSelection( filter, null, this );
                
                selectFeatures( fsm.getFeatureCollection() );
            }
            catch (final Exception e) {
                log.warn( e.getLocalizedMessage(), e );
                Polymap.getSessionDisplay().asyncExec( new Runnable() {
                    public void run() {                                
                        PolymapWorkbench.handleError( MapEditorPlugin.PLUGIN_ID, this, e.getLocalizedMessage(), e );
                    }
                });
            }
        }
        
        //
        else if (name.equals( SelectFeatureControl.EVENT_SELECTED )) {
            String fid = payload.get( "fid" );
            fsm.setHovered( fid );
        }
    }

}
