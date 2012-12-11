/* 
 * polymap.org
 * Copyright 2012, Falko Bräutigam. All rights reserved.
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
package org.polymap.core.mapeditor.tooling.edit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.feature.FeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureVisitor;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.spatial.BBOX;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Function;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;

import org.polymap.core.data.PipelineFeatureSource;
import org.polymap.core.geohub.LayerFeatureSelectionManager;
import org.polymap.core.geohub.LayerFeatureSelectionOperation;
import org.polymap.core.mapeditor.MapEditor;
import org.polymap.core.mapeditor.MapEditorPlugin;
import org.polymap.core.mapeditor.services.JsonEncoder;
import org.polymap.core.mapeditor.services.JsonVectorLayer;
import org.polymap.core.mapeditor.services.SimpleJsonServer;
import org.polymap.core.operation.OperationSupport;
import org.polymap.core.project.ILayer;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.runtime.event.EventHandler;
import org.polymap.core.workbench.PolymapWorkbench;

import org.polymap.openlayers.rap.widget.base_types.StyleMap;
import org.polymap.openlayers.rap.widget.controls.SelectFeatureControl;

/**
 * {@link JsonVectorLayer} connected to the features of an {@link ILayer}.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class BaseVectorLayer 
        implements PropertyChangeListener {

    private static Log log = LogFactory.getLog( BaseVectorLayer.class );
    
    public static Function<BaseVectorLayer,JsonVectorLayer> toVectorLayer() {
        return  new Function<BaseVectorLayer,JsonVectorLayer>() {
            public JsonVectorLayer apply( BaseVectorLayer input ) {
                return input.getVectorLayer();
            }
        };
    }

    // instance *******************************************
    
    /** The {@link MapEditor} we are working with. */
    private MapEditor               mapEditor;
    
    private ILayer                  layer;

    private JsonVectorLayer         vectorLayer;
    
    private VectorLayerStyler       styler;

    private StyleMap                styleMap;
    
    private SelectFeatureControl    hoverControl;
    
    /** The features that were last selected via {@link #selectFeatures(Collection)}. */
    private List<Feature>           features = new ArrayList();
    
    /** Maximum number of features; default: limited by JSON/browser only. */
    private int                     maxFeatures = Integer.MAX_VALUE;

    protected LayerFeatureSelectionManager fsm;

    private boolean                 active;

    
    public BaseVectorLayer( MapEditor mapEditor, ILayer layer ) {
        this.mapEditor = mapEditor;
        this.layer = layer;
        this.fsm = LayerFeatureSelectionManager.forLayer( layer );
        this.fsm.addSelectionChangeListener( this );

        // FIXME 
        
        // jsonEncoder
        CoordinateReferenceSystem crs = mapEditor.getMap().getCRS();
        SimpleJsonServer jsonServer = SimpleJsonServer.instance();
        JsonEncoder jsonEncoder = jsonServer.newLayer( features, crs, false );
        // 3 decimals should be enough even for lat/long values
        //jsonEncoder.setDecimals( 3 );

        // init default style
        styler = new VectorLayerStyler() {
            protected void styleChanged( StyleMap newStyleMap ) {
                if (styleMap != null) {
                    styleMap.dispose();
                }
                styleMap = newStyleMap;
                vectorLayer.setStyleMap( styleMap );
                vectorLayer.redraw();
            }
        };
        styleMap = styler.createStyleMap();
        
        vectorLayer = new JsonVectorLayer( "selection", jsonServer, jsonEncoder, styleMap );
        vectorLayer.setVisibility( true );
        vectorLayer.setIsBaseLayer( false );
        vectorLayer.setZIndex( 10000 );

        this.mapEditor.addLayer( vectorLayer );
        
        // hover control
        hoverControl = new SelectFeatureControl( vectorLayer, SelectFeatureControl.FLAG_HOVER );
        hoverControl.setHighlightOnly( true );
        hoverControl.setRenderIntent( "temporary" );
        mapEditor.addControl( hoverControl );
        
        // XXX try to chnage select tolerance
        vectorLayer.addObjModCode( "OpenLayers.Handler.Feature.prototype.clickTolerance = 10;" );
    }

    
    public void dispose() {
        if (fsm != null) {
            fsm.removeSelectionChangeListener( this );
            fsm = null;
        }
        deactivate();
        if (hoverControl != null) {
            mapEditor.removeControl( hoverControl );
            hoverControl.destroy();
            hoverControl.dispose();
            hoverControl = null;
        }
        if (styleMap != null) {
            styleMap.dispose();
            styleMap = null;
        }
        if (styler != null) {
            styler = null;
        }
        if (vectorLayer != null) {
//            vectorLayer.events.unregister( this, SelectFeatureControl.EVENT_SELECTED );
            mapEditor.removeLayer( vectorLayer );
            vectorLayer.dispose();
            vectorLayer = null;
        }
        mapEditor = null;
    }

    
    public JsonVectorLayer getVectorLayer() {
        return vectorLayer;
    }
    
    public VectorLayerStyler getStyler() {
        return styler;
    }


    public void activate() {
        if (isActive() == true) {
            return;
        }
        this.active = true;
        assert vectorLayer != null : "no vectorLayer";
        hoverControl.activate();
        
        //selectFeatures( fsm.getFeatureCollection() );
    }
    

    public void deactivate() {
        if (isActive() == false) {
            return;
        }
        this.active = false;
        if (hoverControl != null) {
            hoverControl.deactivate();
        }
    }


    public boolean isActive() {
        return active;
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
            selectFeatures( fsm.getFeatureCollection() );
        }
//        // hover
//        else if (ev.getPropertyName().equals( LayerFeatureSelectionManager.PROP_HOVER )) {
//            selectControl.unselectAll();
//            selectControl.selectFids( Collections.singletonList( (String)ev.getNewValue() ) );
//        }
    }


    public void selectFeatures( FeatureCollection _features ) { 
        // copy features
        features.clear();
        final AtomicBoolean exceeded = new AtomicBoolean();
        try {
            _features.accepts( new FeatureVisitor() {
                public void visit( Feature feature ) {
                    if (features.size() <= maxFeatures) {
                        features.add( feature );
                    } else {
                        exceeded.set( true );
                    }
                }
            }, null );
        }
        catch (IOException e) {
            PolymapWorkbench.handleError( MapEditorPlugin.PLUGIN_ID, this, "Unable to access selected features.", e );
        }
        
        // still initializing?
        if (vectorLayer != null) {
            vectorLayer.getJsonEncoder().setFeatures( features );
            vectorLayer.refresh();
        }
    }

    
    /**
     *
     * @param bounds
     * @param setLayerSelection Use the resulting filter to run a {@link LayerFeatureSelectionOperation}.
     */
    public void selectFeatures( ReferencedEnvelope bounds, boolean runOperation ) {
        try {
            CoordinateReferenceSystem dataCRS = layer.getCRS();
            ReferencedEnvelope dataBBox = bounds.transform( dataCRS, true );
            //log.debug( "dataBBox: " + dataBBox );

            FilterFactory ff = CommonFactoryFinder.getFilterFactory( GeoTools.getDefaultHints() );
            //JD: should this be applied to all geometries?
            //String name = featureType.getDefaultGeometry().getLocalName();
            //JD: changing to "" so it is
            String propname = "";
            String epsgCode = CRS.toSRS( dataBBox.getCoordinateReferenceSystem() );  //GML2EncodingUtils.crs( dataBBox.getCoordinateReferenceSystem() );
            
            BBOX filter = ff.bbox( propname, dataBBox.getMinX(), dataBBox.getMinY(), 
                    dataBBox.getMaxX(), dataBBox.getMaxY(), epsgCode);
            
            if (runOperation) {
                // change feature selection
                LayerFeatureSelectionOperation op = new LayerFeatureSelectionOperation();
                op.init( layer, filter, null, this );
                OperationSupport.instance().execute( op, true, false, new JobChangeAdapter() {
                    public void done( IJobChangeEvent event ) {
                        selectFeatures( fsm.getFeatureCollection() );
                    }
                });
            }
            else {
                // select features directly 
                PipelineFeatureSource fs = PipelineFeatureSource.forLayer( layer, false );
                selectFeatures ( fs.getFeatures( filter ) );
            }
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
    
}
