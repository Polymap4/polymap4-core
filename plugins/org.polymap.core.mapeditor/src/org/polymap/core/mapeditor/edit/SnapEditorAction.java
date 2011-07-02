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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import java.beans.PropertyChangeEvent;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.feature.FeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.gml2.bindings.GML2EncodingUtils;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.spatial.BBOX;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.widgets.Event;

import org.eclipse.jface.action.IAction;

import org.eclipse.ui.IEditorActionDelegate;

import org.polymap.core.data.PipelineFeatureSource;
import org.polymap.core.mapeditor.IMapEditorSupport;
import org.polymap.core.mapeditor.MapEditor;
import org.polymap.core.mapeditor.MapEditorPlugin;
import org.polymap.core.mapeditor.services.JsonEncoder;
import org.polymap.core.mapeditor.services.JsonVectorLayer;
import org.polymap.core.mapeditor.services.SimpleJsonServer;
import org.polymap.core.project.ILayer;
import org.polymap.core.workbench.PolymapWorkbench;

import org.polymap.openlayers.rap.widget.base_types.Style;
import org.polymap.openlayers.rap.widget.base_types.StyleMap;
import org.polymap.openlayers.rap.widget.controls.Control;
import org.polymap.openlayers.rap.widget.controls.SnappingControl;
import org.polymap.openlayers.rap.widget.layers.VectorLayer;

/**
 * Editor action for the {@link EditFeatureSupport}. This actions manipulates
 * the {@link SnappingControl}.
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @since 3.0
 */
public class SnapEditorAction
        extends AbstractEditEditorAction
        implements IEditorActionDelegate {

    private static Log log = LogFactory.getLog( SnapEditorAction.class );

    
    public SnapEditorAction() {
        controlType = SnappingControl.class;
    }


    public void runWithEvent( IAction _action, Event ev ) {
        log.debug( "ev= " + ev );
        assert support != null;
        //mapEditor.activateSupport( support, action.isChecked() );
        
        SnappingControl control = (SnappingControl)support.getControl( SnappingControl.class );

        if (action.isChecked()) {
            if (control == null) {
                JsonVectorLayer[] targetLayers = buildTargetLayers();
                support.snapLayers = targetLayers;
                control = new SnappingControl( support.vectorLayer, targetLayers, Boolean.FALSE );
                support.addControl( control );
            }
            control.activate();
        }
        else {
            // remove control and layers so that everything is re-created next
            // time and reflects new layers visibility status
            control.deactivate();
            support.removeControl( control );
            
            if (support.snapLayers != null) {
                for (JsonVectorLayer snapLayer : support.snapLayers) {
                    mapEditor.removeLayer( snapLayer );
                    snapLayer.dispose();
                    support.snapLayers = null;
                }
            }
        }
    }

    
    public void supportStateChanged( MapEditor _editor, IMapEditorSupport _support, boolean _activated ) {
        
        super.supportStateChanged( _editor, support, _activated );
        
        if (this.support == _support) {
            if (_activated) {
                // if there is a control, then it is always activated
                Control control = support.getControl( controlType );
                if (control != null) {
                    control.activate();
                }
            }
        }
    }


    public void propertyChange( PropertyChangeEvent ev ) {
        log.debug( "propertyChange(): ev= " + ev );
        // don't deactivate if another action was enabled
    }

    
    protected JsonVectorLayer[] buildTargetLayers() {
        List<VectorLayer> vectorLayers = new ArrayList();
        // XXX maps may contain maps
        for (ILayer layer : support.layer.getMap().getLayers()) {
            if (layer.isVisible()) {
                VectorLayer vectorLayer = buildVectorLayer( layer );
                vectorLayers.add( vectorLayer );
                mapEditor.addLayer( vectorLayer );
            }
        }
        return vectorLayers.toArray( new JsonVectorLayer[vectorLayers.size()] );
    }
    
    
    protected JsonVectorLayer buildVectorLayer( ILayer layer ) {
        // jsonEncoder
        CoordinateReferenceSystem crs = mapEditor.getMap().getCRS();
        SimpleJsonServer jsonServer = SimpleJsonServer.instance();
        JsonEncoder jsonEncoder = jsonServer.newLayer( Collections.EMPTY_LIST, crs, false );
        // 3 decimals should be enough even for lat/long values
        //jsonEncoder.setDecimals( 8 );

        // filter features in map extent
        try {
            ReferencedEnvelope bbox = mapEditor.getMap().getExtent();
            
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
        }
        catch (Exception e) {
            log.warn( e.getLocalizedMessage(), e );
            PolymapWorkbench.handleError( MapEditorPlugin.PLUGIN_ID, this, e.getLocalizedMessage(), e );
        }

        // vectorLayer
        Style standard = new Style();
        standard.setAttribute( "strokeWidth", 1 );
        standard.setAttribute( "strokeColor", "#505050" );
        standard.setAttribute( "fillOpacity", "0" );
        Style temporary = new Style();
        temporary.setAttribute( "strokeWidth", 1 );
        temporary.setAttribute( "strokeColor", "#ff0000" );
//        temporary.setAttribute( "fillColor", "#ff0000" );
        Style select = new Style();
        select.setAttribute( "strokeWidth", 1 );
        select.setAttribute( "strokeColor", "#00ff00" );
//        select.setAttribute( "fillColor", "#00ff00" );

        StyleMap styles = new StyleMap();
        styles.setIntentStyle( "default", standard );
//        styles.setIntentStyle( "temporary", temporary );
//        styles.setIntentStyle( "select", select );

        JsonVectorLayer vectorLayer = new JsonVectorLayer( "snap-" + layer.getLabel(), 
                jsonServer, jsonEncoder, styles );

        vectorLayer.setVisibility( true );
        vectorLayer.setIsBaseLayer( false );
        vectorLayer.setZIndex( 9999 );
        return vectorLayer;
    }
    
}
