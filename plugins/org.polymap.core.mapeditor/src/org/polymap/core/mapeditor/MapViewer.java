/* 
 * polymap.org
 * Copyright (C) 2013-2015, Falko Br‰utigam. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.core.mapeditor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.layout.RowLayoutFactory;

import org.eclipse.rap.json.JsonObject;

import org.polymap.core.data.util.Geometries;
import org.polymap.core.runtime.IMessages;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;

import org.polymap.rhei.batik.IPanelSite;
import org.polymap.rhei.batik.internal.Messages;

import org.polymap.rap.openlayers.OpenLayersWidget;
import org.polymap.rap.openlayers.base.OpenLayersEventListener;
import org.polymap.rap.openlayers.base.OpenLayersObject;
import org.polymap.rap.openlayers.base_types.Bounds;
import org.polymap.rap.openlayers.base_types.OpenLayersMap;
import org.polymap.rap.openlayers.base_types.Projection;
import org.polymap.rap.openlayers.base_types.Size;
import org.polymap.rap.openlayers.controls.Control;
import org.polymap.rap.openlayers.controls.LayerSwitcherControl;
import org.polymap.rap.openlayers.controls.LoadingPanelControl;
import org.polymap.rap.openlayers.controls.MousePositionControl;
import org.polymap.rap.openlayers.controls.NavigationControl;
import org.polymap.rap.openlayers.controls.PanZoomBarControl;
import org.polymap.rap.openlayers.controls.ScaleLineControl;
import org.polymap.rap.openlayers.layers.GridLayer;
import org.polymap.rap.openlayers.layers.Layer;
import org.polymap.rap.openlayers.layers.WMSLayer;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Br‰utigam</a>
 */
public class MapViewer
        implements OpenLayersEventListener {

    private static Log log = LogFactory.getLog( MapViewer.class );
    
    public static final IMessages   i18n = Messages.forPrefix( "MapViewer" ); //$NON-NLS-1$

    private IPanelSite              site;

    private OpenLayersWidget        olwidget;

    private OpenLayersMap           map;

    private List<WMSLayer>          layers = new ArrayList();

    private WMSLayer                visibleBaseLayer;

    /** The currently visible layers, excluding the {@link #visibleBaseLayer}. */
    private List<WMSLayer>          visibleLayers = new ArrayList();

    private Composite               contents;

    private List<IContributionItem> toolbarItems = new ArrayList();

    private Composite               toolbar;

    private ReferencedEnvelope      mapExtent;
    
    private ReferencedEnvelope      maxExtent;
    
//    private float                   mapScale = -1;

    
    /**
     * 
     * @param site
     * @param maxExtent The max extent and {@link CoordinateReferenceSystem} of the map.
     */
    public MapViewer( IPanelSite site, ReferencedEnvelope maxExtent ) {
        this.site = site;
        this.maxExtent = maxExtent;
    }


    public void dispose() {
        for (IContributionItem item : toolbarItems) {
            item.dispose();
        }
        toolbarItems.clear();

        if (olwidget != null) {
            map.dispose();
            map = null;
            olwidget.dispose();
            olwidget = null;
        }
    }
    
    
    public Composite createContents( Composite _body ) {
            this.contents = site.toolkit().createComposite( _body );
            contents.setLayout( FormLayoutFactory.defaults().spacing( 5 ).margins( 0 ).create() );
    
            // toolbar
            toolbar = site.toolkit().createComposite( contents );
            toolbar.setLayoutData( FormDataFactory.filled().bottom( -1 ).create() );
            
            // map widget (styling/background color from azv.css)
            olwidget = new OpenLayersWidget( contents, SWT.MULTI | SWT.WRAP | SWT.BORDER/*, "openlayers/full/OpenLayers-2.12.1.js"*/ );
            olwidget.setLayoutData( FormDataFactory.filled().top( toolbar ).create() );
    
            String srs = Geometries.srs( getCRS() );
            Projection proj = new Projection( srs );
            String units = srs.equals( "EPSG:4326" ) ? "degrees" : "m";
            float maxResolution = srs.equals( "EPSG:4326" ) ? (360/256) : 500000;
            //Bounds maxExtent = new Bounds( 12.80, 53.00, 14.30, 54.50 );
            Bounds bounds = new Bounds( maxExtent.getMinX(), maxExtent.getMinY(), maxExtent.getMaxX(), maxExtent.getMaxY() );
            map = new OpenLayersMap( olwidget, proj, proj, units, bounds, maxResolution );
            map = olwidget.getMap();
    
            map.addControl( new NavigationControl( true ) );
            map.addControl( new PanZoomBarControl() );
            map.addControl( new LayerSwitcherControl() );
            map.addControl( new MousePositionControl() );
            map.addControl( new ScaleLineControl() );
//            map.addControl( new ScaleControl() );
//            map.addControl( new LoadingPanelControl() );
    
           // map.setRestrictedExtend( maxExtent );
    //        map.zoomToExtent( bounds, true );
    //        map.zoomTo( 2 );
    
//            // map events
//            HashMap<String, String> payload = new HashMap<String, String>();
//            payload.put( "left", "event.object.getExtent().toArray()[0]" );
//            payload.put( "bottom", "event.object.getExtent().toArray()[1]" );
//            payload.put( "right", "event.object.getExtent().toArray()[2]" );
//            payload.put( "top", "event.object.getExtent().toArray()[3]" );
//            payload.put( "scale", map.getJSObjRef() + ".getScale()" );
//            
//            map.events.register( this, OpenLayersMap.EVENT_MOVEEND, payload );
//            map.events.register( this, OpenLayersMap.EVENT_ZOOMEND, payload );
    
            // after olwidget is initialized
            createToolbar();
            return getControl();
        }


    public Composite getControl() {
        return contents;
    }
    
    public IPanelSite getPanelSite() {
        return site;
    }
    
    public OpenLayersMap getMap() {
        return map;
    }

    public CoordinateReferenceSystem getCRS() {
        return maxExtent.getCoordinateReferenceSystem();
    }

    public ReferencedEnvelope getMapExtent() {
        return mapExtent;
    }

    public ReferencedEnvelope getMaxExtent() {
        return maxExtent;
    }


    public void zoomTo( ReferencedEnvelope extent ) {
        try {
            mapExtent = extent.transform( getCRS(), true );            
            getMap().zoomToExtent( new Bounds( 
                    mapExtent.getMinX(), mapExtent.getMinY(), 
                    mapExtent.getMaxX(), mapExtent.getMaxY() ), true );
            onZoomPan();
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }
    }

    
    public void addMapControl( Control control ) {
        map.addControl( control );
    }

    
    public void addLayersFromMessages( String prefix ) {
        // layers
        int suffix = 1;
        while (i18n.contains( "layerName"+suffix )) { //$NON-NLS-1$
            WMSLayer layer = new WMSLayer( i18n.get( "layerName"+suffix ),  //$NON-NLS-1$
                    i18n.get( "layerWmsUrl"+suffix ), //$NON-NLS-1$
                    i18n.get( "layerWmsName"+suffix ) ); //$NON-NLS-1$
            String format = i18n.get( "layerWmsFormat"+suffix );
            if (format != null && format.length() > 0) {
                layer.setFormat( format );
            }
            addLayer( layer, true, false );
            suffix ++;
        }
    }
    
    
    public MapViewer addLayer( Layer layer, boolean isBaseLayer, boolean isSingleTile ) {
        assert map != null : "addLayer() has to be called before addLayer()";
        layer.setIsBaseLayer( isBaseLayer );
        
        if (layer instanceof GridLayer) {
            if (isSingleTile) {
                ((GridLayer)layer).setSingleTile( true );                
            }
            else {
                ((GridLayer)layer).setTileSize( new Size( 400, 400 ) );
                ((GridLayer)layer).setBuffer( 0 );
            }
        }
        map.addLayer( layer );
        
        if (layer instanceof WMSLayer) {
            layers.add( (WMSLayer)layer );
            if (!isBaseLayer) {
                setLayerVisible( layer, true );
            }
            else {
                visibleBaseLayer = visibleBaseLayer == null ? (WMSLayer)layer : visibleBaseLayer;
            }
        }
        return this;
    }
    
    
    public MapViewer setLayerVisible( Layer layer, boolean visible ) {
        assert layers.contains( layer );
        
        if (layer.isBaseLayer()) {
            map.setBaseLayer( layer );
            visibleBaseLayer = (WMSLayer)layer;
        } 
        else {
            layer.setVisibility( visible );
            if (visible) {
                visibleLayers.add( (WMSLayer)layer );
            } else {
                visibleLayers.remove( layer );
            }
        }
        return this;
    }
    
    
    public List<WMSLayer> getLayers() {
        return Collections.unmodifiableList( layers );
    }


    public boolean isVisible( WMSLayer layer ) {
        return visibleLayers.contains( layer );
    }


    public void addToolbarItem( IContributionItem item ) {
        toolbarItems.add( item );
        if (toolbar != null) {
            item.fill( toolbar );
        }
    }


    protected void createToolbar() {
        toolbar.setLayout( RowLayoutFactory.fillDefaults().fill( true ).create() );
        
        for (IContributionItem item : toolbarItems) {
            item.fill( toolbar );
        }
    }

    
//    private WMSLayer    userDefinedBaseLayer;
//    
//    public void updateLayerVisibility() {
//        // XXX default map width if no layout yet
//        int imageWidth = olwidget.getSize().x > 0 ? olwidget.getSize().x : 500;
//        // XXX no geodetic CRS supported
//        double mapScale = mapExtent.getWidth() / (imageWidth / 90/*dpi*/ * 0.0254);
//        
//        if (mapScale <= 0 || dop == null) {
//            return;
//        }
//        else if (mapScale < 5000) {
//            if (visibleBaseLayer != dop) {
//                userDefinedBaseLayer = visibleBaseLayer;
//                setLayerVisible( dop, true );
//            }
//        }
//        else if (mapScale > 5000) {
//            // XXX das zur√ºck umschalten funktioniert nicht, da das umschalten per
//            // layerSwitcher nicht ausgewertet wird
//            if (userDefinedBaseLayer != null && visibleBaseLayer != userDefinedBaseLayer) {
//                setLayerVisible( userDefinedBaseLayer, true );
//                userDefinedBaseLayer = null;
//            }
//        }
//    }

    
    
    @Override
    public void handleEvent( OpenLayersObject obj, String name, JsonObject props ) {
        if (olwidget.getMap() != obj) {
            return;
        }
        // map zoom/pan
        String left = props.get( "left" );
        if (left != null) {
            try {
                mapExtent = new ReferencedEnvelope(
                        Double.parseDouble( payload.get( "left" ) ),
                        Double.parseDouble( payload.get( "right" ) ),
                        Double.parseDouble( payload.get( "bottom" ) ),
                        Double.parseDouble( payload.get( "top" ) ),
                        getCRS() );
//                mapScale = Float.parseFloat( payload.get( "scale" ) );
//                log.info( "scale=" + mapScale + ", mapExtent= " + mapExtent );
                
                onZoomPan();
            }
            catch (Exception e) {
                log.error( "unhandled:", e );
            }
        }
    }

    
    protected void onZoomPan() {
        log.info( /*"scale=" + mapScale +*/ "mapExtent= " + mapExtent );
    }
    
}
