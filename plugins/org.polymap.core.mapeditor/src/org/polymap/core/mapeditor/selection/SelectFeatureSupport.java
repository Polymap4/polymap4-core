/* 
 * polymap.org
 * Copyright 2009, Polymap GmbH, and individual contributors as indicated
 * by the @authors tag.
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
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 * $Id$
 */
package org.polymap.core.mapeditor.selection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.gml2.bindings.GML2EncodingUtils;
import org.json.JSONObject;
import org.opengis.feature.Feature;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.spatial.BBOX;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import org.eclipse.rwt.RWT;

import org.eclipse.jface.dialogs.MessageDialog;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import org.polymap.core.geohub.GeoEventException;
import org.polymap.core.geohub.GeoEventListener;
import org.polymap.core.geohub.GeoEventSelector;
import org.polymap.core.geohub.GeoHub;
import org.polymap.core.geohub.GeoEventSelector.MapNameFilter;
import org.polymap.core.geohub.GeoEventSelector.TypeFilter;
import org.polymap.core.geohub.event.GeoEvent;
import org.polymap.core.mapeditor.IMapEditorSupport;
import org.polymap.core.mapeditor.ISelectFeatureSupport;
import org.polymap.core.mapeditor.MapEditor;
import org.polymap.core.mapeditor.MapEditorPlugin;
import org.polymap.core.mapeditor.services.JSONServer;
import org.polymap.core.project.ILayer;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.services.http.HttpServiceFactory;
import org.polymap.core.workbench.PolymapWorkbench;
import org.polymap.openlayers.rap.widget.base.OpenLayersEventListener;
import org.polymap.openlayers.rap.widget.base.OpenLayersObject;
import org.polymap.openlayers.rap.widget.base_types.Protocol;
import org.polymap.openlayers.rap.widget.base_types.Style;
import org.polymap.openlayers.rap.widget.base_types.StyleMap;
import org.polymap.openlayers.rap.widget.controls.BoxControl;
import org.polymap.openlayers.rap.widget.controls.SelectFeatureControl;
import org.polymap.openlayers.rap.widget.layers.VectorLayer;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
@SuppressWarnings("deprecation")
class SelectFeatureSupport
        implements ISelectFeatureSupport, OpenLayersEventListener, GeoEventListener {

    static Log log = LogFactory.getLog( SelectFeatureSupport.class );

    private static final int        DEFAULT_MAX_SELECTIONS = 100;
    
    private static int              selectionCount = 0;
    
    /** The {@link MapEditor} we are working with. */
    private MapEditor               mapEditor;

    private boolean                 active;
    
    private SelectFeatureControl    selectControl;
    
    private SelectFeatureControl    hoverControl;
    
    private BoxControl              boxControl;

    private VectorLayer             vectorLayer;
    
    private JSONServer              jsonServer;
    
    /** The features that were last selected via {@link #selectFeatures(Collection)}. */
    private Collection<Feature>     features = new ArrayList();

    private ILayer                  layer;
    

    /**
     * 
     * @param mapEditor
     * @throws Exception 
     */
    public SelectFeatureSupport( MapEditor mapEditor ) 
    throws Exception {
        this.mapEditor = mapEditor;
        this.mapEditor.addSupportListener( this );

        GeoHub.instance().subscribe( this, 
                new GeoEventSelector( 
                        new MapNameFilter( mapEditor.getMap().getLabel() ),
                        new TypeFilter( GeoEvent.Type.FEATURE_SELECTED, GeoEvent.Type.FEATURE_HOVERED ) ) );

        // jsonServer
        String pathSpec = "/" + RWT.getSessionStore().getId() + 
                "/geoselection" + selectionCount++;
        pathSpec = HttpServiceFactory.trimPathSpec( pathSpec );
        String url = pathSpec.substring( 1 );

        CoordinateReferenceSystem crs = mapEditor.getMap().getCRS();

        jsonServer = JSONServer.newServer( url, ListUtils.EMPTY_LIST, crs );
        // 3 decimals should be enough even for lat/long values
        jsonServer.setDecimals( 3 );
        log.info( "        Server: " + jsonServer.getURL() );
        HttpServiceFactory.registerServer( jsonServer, pathSpec, false );

        // vectorLayer
        Style standard = new Style();
        standard.setAttribute( "strokeWidth", 2 );
        standard.setAttribute( "strokeColor", "#0000f0" );
        standard.setAttribute( "strokeOpacity", "0.7" );
        standard.setAttribute( "fillColor", "#0000f0" );
        Style temporary = new Style();
        temporary.setAttribute( "strokeWidth", 2 );
        temporary.setAttribute( "strokeColor", "#00b0ff" );
        temporary.setAttribute( "fillColor", "#00b0ff" );
        Style select = new Style();
        select.setAttribute( "strokeWidth", 3 );
        select.setAttribute( "strokeColor", "#00b0ff" );
        select.setAttribute( "fillColor", "#00b0ff" );

        StyleMap styles = new StyleMap();
        styles.setIntentStyle( "default", standard );
        styles.setIntentStyle( "temporary", temporary );
        styles.setIntentStyle( "select", select );

        vectorLayer = new VectorLayer( "GeoSelection", 
                new Protocol( "HTTP", jsonServer.getURL(), "GeoJSON" ), styles );

        vectorLayer.setVisibility( true );
        vectorLayer.setIsBaseLayer( false );
        vectorLayer.setZIndex( 10000 );

        this.mapEditor.addLayer( vectorLayer );
    }


    public void dispose() {
        setActive( false );
        GeoHub.instance().unsubscribe( this );

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
        HttpServiceFactory.unregisterServer( jsonServer, false );
        jsonServer = null;

        this.mapEditor.removeSupportListener( this );
        this.mapEditor = null;
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

    
    public void onEvent( GeoEvent ev ) {
        log.info( "ev: " + ev );
        
        if (ev.getType() == GeoEvent.Type.FEATURE_SELECTED) {
            selectFeatures( ev.getBody() );
        }
        
        else if (ev.getType() == GeoEvent.Type.FEATURE_HOVERED) {
            Collection<String> fids = new ArrayList( ev.getBody().size() );
            for (Feature feature : ev.getBody()) {
                fids.add( feature.getIdentifier().getID() );
            }
            selectControl.unselectAll();
            selectControl.selectFids( fids  );
            
            // XXX reveal the selected feature
        }

        else {
            log.warn( "Unhandled event type: " + ev );
        }
    }


    public void selectFeatures( Collection<Feature> _features ) {
        features.clear();
        if (_features != null) {
            features.addAll( _features );
        }
        // still initializing?
        if (jsonServer != null && vectorLayer != null) {
            jsonServer.setFeatures( features );
            vectorLayer.refresh();

//            vectorLayer = new VectorLayer( "GeoSelection", 
//                    new Protocol( "HTTP", jsonServer.getURL(), "GeoJSON" ), styles );
//
//            vectorLayer.setVisibility( true );
//            vectorLayer.setIsBaseLayer( false );
//            vectorLayer.setZIndex( 10000 );
//
//            this.mapEditor.addLayer( vectorLayer );
        }
    }


    public void setActive( boolean active ) {
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
            log.debug( "    key: " + key + ", value: " + StringUtils.abbreviate( (String)value, 0, 60 ) );
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
                
                // geo event; 
                // XXX do nothing than just sending the event/filter; GeoSelectionView
                // filters the elements and send a event back; without GeoSelectionView
                // this does not work; should this be "fixed"?
                GeoEvent event = new GeoEvent( GeoEvent.Type.FEATURE_SELECTED, 
                        mapEditor.getMap().getLabel(), 
                        null );
                event.setFilter( filter );
                GeoHub.instance().send( event, this );
            }
            catch (Exception e) {
                log.warn( e.getLocalizedMessage(), e );
                PolymapWorkbench.handleError( MapEditorPlugin.PLUGIN_ID, this, e.getLocalizedMessage(), e );
            }
        }
        
        //
        else if (name.equals( SelectFeatureControl.EVENT_SELECTED )) {
            String fid = payload.get( "fid" );
            
            try {
                // geo event
                Collection<Feature> fc = new ArrayList( 1 );
                for (Feature feature : features) {
                    if (feature.getIdentifier().getID().equals( fid )) {
                        fc.add( feature );
                        break;
                    }
                }
                GeoEvent event = new GeoEvent( GeoEvent.Type.FEATURE_HOVERED, 
                        mapEditor.getMap().getLabel(), 
                        null );
                event.setBody( fc );
                GeoHub.instance().send( event, this );
            }
            catch (GeoEventException e) {
                PolymapWorkbench.handleError( MapEditorPlugin.PLUGIN_ID, this, e.getLocalizedMessage(), e );
            }

//            try {
//                log.debug( "features JSON: " + payload.get( "features" ) );
//                JSONArray json_fids = new JSONArray( payload.get( "features" ) );
//                Set<String> fids = new HashSet( json_fids.length() * 2 );
//                for (int i=0; i<json_fids.length(); i++) {
//                    String fid = json_fids.getString( i );
//                    log.info( "    feature: fid= " + fid );
//                    fids.add( fid );
//                }
//                fireEvent( fids );
//            }
//            catch (JSONException e) {
//                log.error( e.getMessage(), e );
//            }

            //                getSite().getShell().getDisplay().asyncExec( new Runnable() {
            //                    public void run() {
            //                        try {
            //                            IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
            //                            page.showView( GeoSelectionView.ID );
            //                        }
            //                        catch (PartInitException e) {
            //                            throw new RuntimeException( e.getMessage(), e );
            //                        }
            //                    }
            //                });
        }
    }


    public void connectLayer( ILayer _layer ) {
        this.layer = _layer;
    }

}
