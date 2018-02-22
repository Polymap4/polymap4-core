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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.json.JSONArray;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
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
import org.polymap.core.runtime.config.Mandatory;
import org.polymap.core.runtime.i18n.IMessages;

import org.polymap.rap.openlayers.base.OlEvent;
import org.polymap.rap.openlayers.base.OlEventListener;
import org.polymap.rap.openlayers.base.OlMap;
import org.polymap.rap.openlayers.control.Control;
import org.polymap.rap.openlayers.interaction.Interaction;
import org.polymap.rap.openlayers.layer.Layer;
import org.polymap.rap.openlayers.types.Extent;
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

    /** Read/write access to the current extent of the map. */
    @Mandatory
    @Concern( PropagateExtentToViewConcern.class )
    public Config<Envelope>             mapExtent;

    /** Read/write access to the current extent of the map. */
    @Mandatory
    @Concern( PropagateResolutionToViewConcern.class )
    public Config<Float>                resolution;
    
    /**
     * Setting max extent also sets the {@link CoordinateReferenceSystem} of the map.
     * <p/>
     * <b>XXX</b> Not yet used/propagated to the {@link OlMap}.
     */
    @Mandatory
    public Config<ReferencedEnvelope>   maxExtent;
    
    private Composite                   parent;
    
    private OlMap                       olmap;
    
    private Object                      input;

    /** The currently visible layers. */
    private Set<CL>                     visibleLayers = new HashSet();
    
    private Map<CL,Layer>               layers = new HashMap();
    
    private List<Control>               controls = new ArrayList();
    
    private List<Interaction>           interactions = Lists.newArrayList();

    /**
     * 
     */
    public static class PropagateExtentToViewConcern
            extends DefaultPropertyConcern<Envelope> {

        @Override
        public Envelope doSet( Object obj, Config<Envelope> prop, Envelope value ) {
            MapViewer<?> viewer = (MapViewer<?>)obj;
            Extent extent = new Extent( value.getMinX(), value.getMinY(), value.getMaxX(), value.getMaxY() );
            viewer.olmap.view.get().fit( extent, null );
            return value;
        }
    }
    

    public static class PropagateResolutionToViewConcern
            extends DefaultPropertyConcern<Float> {

        @Override
        public Float doSet( Object obj, Config<Float> prop, Float value ) {
            MapViewer<?> viewer = (MapViewer<?>)obj;
            viewer.olmap.view.get().resolution.set( value );
            return value;
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
    protected void inputChanged( @SuppressWarnings("hiding") Object input, Object oldInput ) {
        super.inputChanged( input, oldInput );
        if (olmap == null) {
            createMap();
        }
        else {
            throw new RuntimeException( "Changing input is not yet supported." );
        }
        // maxExtent
        View view = olmap.view.get();
        // view.extent.set( maxExtent.map( new ToOlExtent() ).get() );
        view.addEventListener( View.Event.resolution, this );
        view.addEventListener( View.Event.rotation, this );
        view.addEventListener( View.Event.center, this );
    
        // center
        Coordinate center = mapExtent.orElse( maxExtent.get() ).centre();
        log.info( "center: " + center );
        view.center.set( ToOlCoordinate.map( center ) );
        
        // read layers from contentProvider
        refresh( true );
    
        // every layer is visible by default
        layers.keySet().stream().forEach( layer -> visibleLayers.add( layer ) );
        
        // add controls
        controls.forEach( control -> olmap.addControl( control ) );
    
        // add interactions
        interactions.forEach( interaction -> olmap.addInteraction( interaction ) );
    }

    
    protected void createMap() {
        String srs = Geometries.srs( maxExtent.get().getCoordinateReferenceSystem() );
        // XXX
        Units units = srs.equals( "EPSG:4326" ) ? Units.degrees : Units.m;
        olmap = new OlMap( parent, SWT.NONE, new View()
                .projection.put( new Projection( srs, units ) )
                // without this map is not displayed at all
                .zoom.put( 5 ) );
       // olmap.addEventListener( EVENT.view, this );
    }

    @Override
    public void refresh() {
        refresh( true );
    }


    public void refresh( boolean changed ) {
        if (!changed) {
            for (CL layer : layers.keySet()) {
                refresh( layer, false );
            }
        }
        else {
            Map<CL,Layer> copy = new HashMap( layers );
            for (CL layer : copy.keySet()) {
                removeLayer( layer );
            }
            // zIndex is not (yet) supported by our OpenLayers version (?)
            Multimap<Integer,CL> sorted = Multimaps.newListMultimap( new TreeMap(), () -> new ArrayList() );
            for (Object layer : contentProvider.get().getElements( input ) ) {
                int prio = layerProvider.get().getPriority( (CL)layer );
                sorted.put( prio, (CL)layer );
            }
            for (CL layer : sorted.values()) {
                // XXX refresh() gives newly created layer a default/unique value for the t param;
                // this prevents reload after refresh to display old tiles - but it also re-renders
                // unchanged layers
                addLayer( layer ); //.refresh();
            }
        }
    }

    
    /**
     * Refresh this layer by {@link Layer#refresh() re-fetching} data from the
     * server.
     * <p/>
     * If <b>changed</b> is <code>true</code> then the layer is completely
     * re-created, in order to reflect (structural) changes of the backend layer and
     * its rendering. Otherwise neither the {@link Layer} instance nor its connected
     * backend is changed.
     * 
     * @param changed Force layer and its complete backend to be re-created.
     */
    public void refresh( CL layer, boolean changed ) {
        if (changed) {
            removeLayer( layer );
            addLayer( layer );
        }
        else {
            throw new RuntimeException( "refresh() with added param is a bad idea, see code" );
//            Layer olayer = layers.get( layer );
//            olayer.refresh();
        }
    }

    
    protected Layer addLayer( CL layer ) {
        // create new layer
        assert !layers.containsKey( layer ) : "Layer is already added: " + layer;
        Layer olayer = layerProvider.get().getLayer( layer );
        olmap.addLayer( olayer );
        layers.put( layer, olayer );
        return olayer;
    }
    
    
    protected void removeLayer( CL layer ) {
        Layer olayer = layers.remove( layer );
        assert olayer != null : "No such layer: " + layer;
        olmap.removeLayer( olayer );
        olayer.dispose();  // ???
    }

    
//    /**
//     * Read layers from {@link #contentProvider}.
//     */
//    protected void refreshLayers() {
//        // remove current layers
//        new ArrayList<CL>( layers.keySet() ).forEach( layer -> removeLayer( layer ) );
//        assert layers.isEmpty();
//        
//        // build layers map
//        ILayerProvider<CL> lp = layerProvider.get();
//        Arrays.stream( contentProvider.get().getElements( input ) )
//                .forEach( elm -> layers.put( (CL)elm, lp.getLayer( (CL)elm ) ) );
//        
//        // add sorted layers to the map
//        layers.keySet().stream()
//                .sorted( (elm1, elm2) -> (lp.getPriority(elm1) - lp.getPriority(elm2)) )
//                .map( elm -> layers.get( elm ) )
//                .forEach( layer -> olmap.addLayer( layer ) );
//    }

    
    @Override
    public void setSelection( ISelection selection, boolean reveal ) {
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public ISelection getSelection() {
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public Composite getControl() {
        assert olmap != null : "Call setInput() first."; 
        return olmap.getControl();
    }
    
    
    public OlMap getMap() {
        return olmap;
    }

    
    /**
     * The {@link CoordinateReferenceSystem} that is currently used to display the map.
     * This is set via {@link #maxExtent}.
     */
    public CoordinateReferenceSystem getMapCRS() {
        return maxExtent.get().getCoordinateReferenceSystem();
    }

    
    public void zoomTo( Envelope extent ) {
        if (extent instanceof ReferencedEnvelope) {
            try {
                extent = ((ReferencedEnvelope)extent).transform( getMapCRS(), true );
            }
            catch (Exception e) {
                throw new RuntimeException( e );
            }
        }
        Extent olExtent = ToOlExtent.map( extent );
        olmap.view.get().fit( olExtent, null );
    }

    
    public MapViewer<CL> addMapControl( Control control ) {
        controls.add( control );
        if (olmap != null) {
            olmap.addControl( control );
        }
        return this;
    }
    
    public MapViewer<CL> addMapInteraction( final Interaction interaction ) {
        interactions.add( interaction );
        if (olmap != null) {
            olmap.addInteraction( interaction );
        }
        return this;
    }

    
    public MapViewer setVisible( CL layer, boolean visible ) {
        Layer olayer = layers.get( layer );
        if (olayer != null) {
            olayer.visible.set( visible );
            if (visible) {
                visibleLayers.add( layer );
            } else {
                visibleLayers.remove( layer );
            }
            return this;
        }
        else {
            throw new RuntimeException( "No such layer: " + layer );
        }
    }
    
    
    public boolean isVisible( CL layer ) {
        return visibleLayers.contains( layer );
    }

    
    public Set<CL> getLayers() {
        return Collections.unmodifiableSet( layers.keySet() );
    }
    
    
    @Override
    public void handleEvent( OlEvent ev ) {
        JSONArray extent = ev.properties().optJSONArray( "extent" );
        if (extent != null) {
            Envelope envelope = new Envelope( extent.getDouble( 0 ), extent.getDouble( 2 ), extent.getDouble( 1 ),
                    extent.getDouble( 3 ) );
            // bypass concern, prevent loop
            this.mapExtent.info().setRawValue( envelope );
        }
        Double newResolution = ev.properties().optDouble( "resolution" );
        if (newResolution != null) {
            this.resolution.info().setRawValue( newResolution.floatValue() );
        }
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
