/* 
 * polymap.org
 * Copyright 2009, 2011 Polymap GmbH. All rights reserved.
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
package org.polymap.core.mapeditor.edit;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.opengis.filter.FilterFactory;
import org.opengis.filter.spatial.BBOX;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.feature.FeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.gml2.bindings.GML2EncodingUtils;

import org.eclipse.core.runtime.ListenerList;

import org.polymap.core.data.PipelineFeatureSource;
import org.polymap.core.geohub.LayerFeatureSelectionManager;
import org.polymap.core.mapeditor.IEditFeatureSupport;
import org.polymap.core.mapeditor.IMapEditorSupport;
import org.polymap.core.mapeditor.MapEditor;
import org.polymap.core.mapeditor.MapEditorPlugin;
import org.polymap.core.mapeditor.services.JsonEncoder;
import org.polymap.core.mapeditor.services.JsonVectorLayer;
import org.polymap.core.mapeditor.services.SimpleJsonServer;
import org.polymap.core.project.ILayer;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.workbench.PolymapWorkbench;
import org.polymap.openlayers.rap.widget.base.OpenLayersEventListener;
import org.polymap.openlayers.rap.widget.base.OpenLayersObject;
import org.polymap.openlayers.rap.widget.base_types.Style;
import org.polymap.openlayers.rap.widget.base_types.StyleMap;
import org.polymap.openlayers.rap.widget.controls.Control;
import org.polymap.openlayers.rap.widget.controls.SelectFeatureControl;

/**
 * The 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @since 3.0
 */
@SuppressWarnings("deprecation")
public class EditFeatureSupport
        implements IEditFeatureSupport, OpenLayersEventListener, PropertyChangeListener {

    private static Log log = LogFactory.getLog( EditFeatureSupport.class );

    private MapEditor               mapEditor;

    protected JsonVectorLayer       vectorLayer;
    
    protected JsonVectorLayer[]     snapLayers;
    
    /** Controls handled by the editor actions of this package. */
    private Map<Object,Control>     controls = new HashMap();
    
    private SelectFeatureControl    hoverControl;
    
    private ListenerList            controlListeners = new ListenerList( ListenerList.IDENTITY );
    
    private boolean                 active;

    protected ILayer                layer;

    private LayerFeatureSelectionManager fsm;


    EditFeatureSupport( MapEditor mapEditor, ILayer layer )
    throws Exception {
        this.mapEditor = mapEditor;
        this.layer = layer;
        this.fsm = LayerFeatureSelectionManager.forLayer( layer );
        this.fsm.addSelectionChangeListener( this );

        this.mapEditor.addSupportListener( this );

        // jsonEncoder
        CoordinateReferenceSystem crs = mapEditor.getMap().getCRS();
        SimpleJsonServer jsonServer = SimpleJsonServer.instance();
        JsonEncoder jsonEncoder = jsonServer.newLayer( Collections.EMPTY_LIST, crs, false );
        // 3 decimals should be enough even for lat/long values
        //jsonEncoder.setDecimals( 8 );

        // filter features in map extent
        try {
            ReferencedEnvelope bbox = mapEditor.getMap().getExtent();
            
//            if (bbox != null) {
                CoordinateReferenceSystem dataCRS = layer.getCRS();
                ReferencedEnvelope dataBBox = bbox.transform( dataCRS, true );
                log.info( "dataBBox: " + dataBBox );

                FilterFactory ff = CommonFactoryFinder.getFilterFactory( GeoTools.getDefaultHints() );
                //JD: should this be applied to all geometries?
                //String name = featureType.getDefaultGeometry().getLocalName();
                //JD: changing to "" so it is
                String propname = "";
                String epsgCode = GML2EncodingUtils.crs( dataBBox.getCoordinateReferenceSystem() );

                final BBOX filter = ff.bbox( propname, dataBBox.getMinX(), dataBBox.getMinY(), 
                        dataBBox.getMaxX(), dataBBox.getMaxY(), epsgCode);

                PipelineFeatureSource fs = PipelineFeatureSource.forLayer( layer, false );
                FeatureCollection features = fs.getFeatures( filter );
                jsonEncoder.setFeatures( features );

                // select all features that are editable now;
                // allow GeoSelectionView to startup from are sibling operation concern;
                // do not use an operation because this is not undoable (as a single op)
                Polymap.getSessionDisplay().asyncExec( new Runnable() {
                    public void run() {
                        fsm.changeSelection( filter, null, EditFeatureSupport.this );
                    }
                });
//            }
        }
        catch (Exception e) {
            log.warn( e.getLocalizedMessage(), e );
            PolymapWorkbench.handleError( MapEditorPlugin.PLUGIN_ID, this, e.getLocalizedMessage(), e );
        }

        // vectorLayer
        Style standard = new Style();
        standard.setAttribute( "strokeWidth", 1 );
        standard.setAttribute( "strokeColor", "#ee5030" );
        Style temporary = new Style();
        temporary.setAttribute( "strokeWidth", 2 );
        temporary.setAttribute( "strokeColor", "#ff0000" );
        temporary.setAttribute( "fillColor", "#ff0000" );
        Style select = new Style();
        select.setAttribute( "strokeWidth", 1 );
        select.setAttribute( "strokeColor", "#ff0000" );
        select.setAttribute( "fillColor", "#ff0000" );

        StyleMap styles = new StyleMap();
        styles.setIntentStyle( "default", standard );
        styles.setIntentStyle( "temporary", temporary );
        styles.setIntentStyle( "select", select );

        vectorLayer = new JsonVectorLayer( "Edit", jsonServer, jsonEncoder, styles );

        vectorLayer.setVisibility( true );
        vectorLayer.setIsBaseLayer( false );
        vectorLayer.setZIndex( 10000 );

        this.mapEditor.addLayer( vectorLayer );

        // hover control
        hoverControl = new SelectFeatureControl( vectorLayer, SelectFeatureControl.FLAG_HOVER );
        hoverControl.setHighlightOnly( true );
        hoverControl.setRenderIntent( "temporary" );
        mapEditor.addControl( hoverControl );
        hoverControl.activate();
    }

    
    public void dispose() {
        this.active  = false;

        if (fsm != null) {
            fsm.removeSelectionChangeListener( this );
            fsm = null;
        }

        controlListeners.clear();
        for (Control control : controls.values()) {
            mapEditor.removeControl( control );
            control.destroy();
            control.dispose();
        }

        if (hoverControl != null) {
            mapEditor.removeControl( hoverControl );
            hoverControl.destroy();
            hoverControl.dispose();
            hoverControl = null;
        }

//        vectorLayer.events.unregister( this, ModifyFeatureControl.EVENT_SELECTED );
        mapEditor.removeLayer( vectorLayer );
        vectorLayer.dispose();
        vectorLayer = null;

        if (snapLayers != null) {
            for (JsonVectorLayer snapLayer : snapLayers) {
                mapEditor.removeLayer( snapLayer );
                snapLayer.dispose();
            }
            snapLayers = null;
        }
        
        this.mapEditor.removeSupportListener( this );
        this.mapEditor = null;
    }
    
    boolean addControl( Control control ) {
        Control old = controls.put( control.getClass(), control );
        if (old == null) {
            mapEditor.addControl( control );
        }
        return old == null;
    }
    
    boolean removeControl( Control control ) {
        Control old = controls.remove( control.getClass() );
        if (old != null) {
            mapEditor.removeControl( old );
        }
        return old == null;
        
    }
    
    Control getControl( Class<? extends Control> type ) {
        return controls.get( type );
    }
    
    void setControlActive( Class<? extends Control> type, boolean active ) {
        Control control = getControl( type );
        assert control != null;
        
        boolean old = control.isActive();
        if (active) {
            control.activate();
        } else {
            control.deactivate();
        }
        
        PropertyChangeEvent ev = new PropertyChangeEvent( control, "active", old, active );
        for (Object l : controlListeners.getListeners()) {
            ((PropertyChangeListener)l).propertyChange( ev );
        }
    }
    
    boolean isControlActive( Class<? extends Control> type ) {
        Control control = getControl( type );
        return control != null && control.isActive();
    }
    
    void addControlListener( PropertyChangeListener l ) {
        controlListeners.add( l );
    }
    
    public void removeControlListener( PropertyChangeListener l ) {
        controlListeners.remove( l );
    }

    public void supportStateChanged( MapEditor editor, IMapEditorSupport support, boolean activated ) {
        log.debug( "support= " + support + " activated= " + activated );
        if (this == support) {
            this.active = activated;
        }
    }
    
    public boolean isActive() {
        return active;
    }

    
    public void propertyChange( PropertyChangeEvent ev ) {
        assert fsm == ev.getSource();
        // hover
        if (ev.getPropertyName().equals( LayerFeatureSelectionManager.PROP_HOVER )) {
            hoverControl.unselectAll();
            hoverControl.selectFids( Collections.singletonList( (String)ev.getNewValue() ) );
        }
    }

    
    public void process_event( OpenLayersObject obj, String name, HashMap<String, String> payload ) {
        log.debug( "event: " + name + ", from: " + obj );
        for (Map.Entry entry : payload.entrySet()) {
            log.debug( "    key: " + entry.getKey() + ", value: " + entry.getValue() );
        }
    }

}
