/* 
 * polymap.org
 * Copyright (C) 2013-2015, Falko Bräutigam. All rights reserved.
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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import org.polymap.core.data.util.Geometries;
import org.polymap.core.runtime.config.Concern;
import org.polymap.core.runtime.config.Config;
import org.polymap.core.runtime.config.ConfigurationFactory;
import org.polymap.core.runtime.config.DefaultPropertyConcern;
import org.polymap.core.runtime.config.Immutable;
import org.polymap.core.runtime.config.Mandatory;
import org.polymap.core.runtime.i18n.IMessages;

import org.polymap.rap.openlayers.base.OlEvent;
import org.polymap.rap.openlayers.base.OlEventListener;
import org.polymap.rap.openlayers.base.OlMap;
import org.polymap.rap.openlayers.control.Control;
import org.polymap.rap.openlayers.layer.Layer;
import org.polymap.rap.openlayers.types.Projection;
import org.polymap.rap.openlayers.types.Projection.Units;
import org.polymap.rap.openlayers.view.View;

/**
 * Provides a JFace style {@link Viewer} on an OpenLayers map.
 * 
 * @param <CL> The type of the layers the content providers returns.
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class MapViewer<CL>
        extends Viewer
        implements OlEventListener {

    private static Log log = LogFactory.getLog( MapViewer.class );
    
    public static final IMessages       i18n = Messages.forPrefix( "MapViewer" ); //$NON-NLS-1$

    @Mandatory
    public Config<IStructuredContentProvider> contentProvider;
    
    @Mandatory
    public Config<ILayerProvider<CL>>   layerProvider;

    @Mandatory
    @Concern(MapExtentConcern.class)
    public Config<Envelope>             mapExtent;
    
    /** Setting max extent also sets the {@link CoordinateReferenceSystem} of the map. */
    @Mandatory
    @Immutable
    public Config<ReferencedEnvelope>   maxExtent;
    
    private Composite                   parent;
    
    private OlMap                       olmap;
    
    private Object                      input;

    /** The currently visible layers, excluding the {@link #visibleBaseLayer}. */
    private List<CL>                    visibleLayers = new ArrayList();
    
    private Map<CL,Layer>               layers;

    
    public static class MapExtentConcern
            extends DefaultPropertyConcern<Envelope> {

        @Override
        public Envelope doSet( Object obj, Config<Envelope> prop, Envelope value ) {
            throw new RuntimeException( "Setting map extent is not yet implemented." );
//            ((MapViewer)obj).olmap.view.get().center
        }
    }
    
    
    /**
     * 
     */
    public MapViewer( Composite parent ) {
        assert parent != null && !parent.isDisposed();
        this.parent = parent;
        ConfigurationFactory.inject( this );
    }

    public void dispose() {
        if (olmap != null) {
            olmap.dispose();
            olmap = null;
        }
    }
    
    @Override
    public void setInput( Object newInput ) {
        contentProvider.get().inputChanged( this, input, newInput );
        input = newInput;
        inputChanged( input, null );
    }

    @Override
    public Object getInput() {
        return input;
    }

    @Override
    public void setSelection( ISelection selection, boolean reveal ) {
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public ISelection getSelection() {
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public void refresh() {
        throw new RuntimeException( "not yet implemented." );
    }


    @Override
    protected void inputChanged( @SuppressWarnings("hiding") Object input, Object oldInput ) {
        super.inputChanged( input, oldInput );
        if (olmap == null) {
            createMap();
        }
        else {
            throw new RuntimeException( "Changing input is not yet supported." );
        }
//        // maxExtent
        View view = olmap.view.get();
        view.extent.set( maxExtent.map( new ToOlExtent() ).get() );
        
        // center
        Coordinate center = mapExtent.orElse( maxExtent.get() ).centre();
        view.center.set( ToOlCoordinate.map( center ) );
        
        // build layers map
        ILayerProvider<CL> lp = layerProvider.get();
        layers = Arrays.stream( contentProvider.get().getElements( input ) )
                .collect( Collectors.toMap( elm -> (CL)elm, elm -> lp.getLayer( (CL)elm ) ) );
        
        // add sorted layers to the map
        layers.keySet().stream()
                .sorted( (elm1, elm2) -> lp.getPriority( elm1 ) - lp.getPriority( elm2 ) )
                .map( elm -> layers.get( elm ) )
                .forEach( layer -> olmap.addLayer( layer ) );
    }


    protected void createMap() {
        String srs = Geometries.srs( maxExtent.get().getCoordinateReferenceSystem() );
        // XXX
        Units units = srs.equals( "EPSG:4326" ) ? Units.degrees : Units.m;
        olmap = new OlMap( parent, SWT.NONE, new View()
                .projection.put( new Projection( srs, units ) )
                // without this map is not displayed at all
                .zoom.put( 5 ) );
        
//        olmap.addEventListener( EVENT.view, this );
    }


    @Override
    public Composite getControl() {
        throw new RuntimeException( "not yet implemented." );
    }
    
    public OlMap getMap() {
        return olmap;
    }


//    public void zoomTo( ReferencedEnvelope extent ) {
//        try {
//            mapExtent = extent.transform( getCRS(), true );            
//            getMap().zoomToExtent( new Bounds( 
//                    mapExtent.getMinX(), mapExtent.getMinY(), 
//                    mapExtent.getMaxX(), mapExtent.getMaxY() ), true );
//            onZoomPan();
//        }
//        catch (Exception e) {
//            throw new RuntimeException( e );
//        }
//    }

    
    public MapViewer<CL> addMapControl( Control control ) {
        olmap.addControl( control );
        return this;
    }

    
    public MapViewer setLayerVisible( CL layer, boolean visible ) {
        assert layers.containsKey( layer );
        throw new RuntimeException( "Changing layer visibility is not yet supported." );
        
//        Layer olLayer = layers.get( layer );
//        if (layer.isBaseLayer()) {
//            map.setBaseLayer( layer );
//            visibleBaseLayer = (WMSLayer)layer;
//        } 
//        else {
//            layer. setVisibility( visible );
//            if (visible) {
//                visibleLayers.add( (WMSLayer)layer );
//            } else {
//                visibleLayers.remove( layer );
//            }
//        }
//        return this;
    }
    
    
    public boolean isVisible( CL layer ) {
        return visibleLayers.contains( layer );
    }


    @Override
    public void handleEvent( OlEvent ev ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

//    @Override
//    public void handleEvent( OlObject obj, String name, JsonObject props ) {
//        if (olwidget.getMap() != obj) {
//            return;
//        }
//        // map zoom/pan
//        String left = props.get( "left" );
//        if (left != null) {
//            try {
//                mapExtent = new ReferencedEnvelope(
//                        Double.parseDouble( payload.get( "left" ) ),
//                        Double.parseDouble( payload.get( "right" ) ),
//                        Double.parseDouble( payload.get( "bottom" ) ),
//                        Double.parseDouble( payload.get( "top" ) ),
//                        getCRS() );
////                mapScale = Float.parseFloat( payload.get( "scale" ) );
////                log.info( "scale=" + mapScale + ", mapExtent= " + mapExtent );
//                
//                onZoomPan();
//            }
//            catch (Exception e) {
//                log.error( "unhandled:", e );
//            }
//        }
//    }

    
    protected void onZoomPan() {
        log.info( /*"scale=" + mapScale +*/ "mapExtent= " + mapExtent );
    }
    
}
