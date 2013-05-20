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
package org.polymap.core.mapeditor.tooling.edit;

import java.util.HashMap;
import java.util.Map;

import java.io.StringReader;

import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.simple.SimpleFeature;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Predicate;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;

import org.polymap.core.data.PipelineFeatureSource;
import org.polymap.core.data.operations.ModifyFeaturesOperation;
import org.polymap.core.data.ui.featureselection.FeatureSelectionView;
import org.polymap.core.geohub.LayerFeatureSelectionManager;
import org.polymap.core.mapeditor.MapEditorPlugin;
import org.polymap.core.mapeditor.Messages;
import org.polymap.core.mapeditor.tooling.IEditorToolSite;
import org.polymap.core.model.security.ACLUtils;
import org.polymap.core.model.security.AclPermission;
import org.polymap.core.operation.OperationSupport;
import org.polymap.core.project.ILayer;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.workbench.PolymapWorkbench;

import org.polymap.openlayers.rap.widget.base.OpenLayersEventListener;
import org.polymap.openlayers.rap.widget.base.OpenLayersObject;
import org.polymap.openlayers.rap.widget.controls.KeyboardDefaultsControl;
import org.polymap.openlayers.rap.widget.controls.ModifyFeatureControl;
import org.polymap.openlayers.rap.widget.controls.NavigationControl;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class EditTool
        extends BaseLayerEditorTool 
        implements OpenLayersEventListener {

    private static Log log = LogFactory.getLog( EditTool.class );

    private EditVectorLayer         vectorLayer;

    private ModifyFeatureControl    modifyControl;

    private NavigationControl       naviControl;
    
    private KeyboardDefaultsControl keyboardControl;

    @Override
    public boolean init( IEditorToolSite site ) {
        boolean result = super.init( site );
        
        additionalLayerFilter = new Predicate<ILayer>() {
            public boolean apply( ILayer input ) {
                return ACLUtils.checkPermission( input, AclPermission.WRITE, false );
            }
        };
        return result;
    }
    

    @Override
    public void dispose() {
        if (isActive()) {
            onDeactivate();
        }
        super.dispose();
    }

    @Override
    public EditVectorLayer getVectorLayer() {
        return vectorLayer;
    }


    @Override
    public void onActivate() {
        super.onActivate();
        
        if (getSelectedLayer() == null) {
            return;
        }
//      // keyboardControl
        keyboardControl = new KeyboardDefaultsControl();
        getSite().getEditor().addControl( keyboardControl );
        keyboardControl.activate();

        // naviControl
        naviControl = new NavigationControl();
        getSite().getEditor().addControl( naviControl );
        naviControl.activate();
        
        vectorLayer = new EditVectorLayer( getSite(), getSelectedLayer() );
        vectorLayer.enableHover();
        vectorLayer.activate();
        
        // re-create the styler controls if this activation is due to a layer change
        if (getParent() != null && !getParent().isDisposed()) {
            vectorLayer.getStyler().createPanelControl( getParent(), this );
            getParent().layout( true );
        }
        
        ReferencedEnvelope bounds = getSite().getEditor().getMap().getExtent();
        vectorLayer.selectFeatures( bounds, true );

        // XXX find an indirect way to signal that the layer has selected
        // features; GeoHub? 
        FeatureSelectionView.open( getSelectedLayer() );

        // control
        modifyControl = new ModifyFeatureControl( vectorLayer.getVectorLayer() );
        getSite().getEditor().addControl( modifyControl );
        //modifyControl.setObjAttr( "createVertices", false );
        modifyControl.addMode( ModifyFeatureControl.RESHAPE );
        modifyControl.addMode( ModifyFeatureControl.DRAG );
        modifyControl.activate();

        // modification event
        Map<String, String> payload = new HashMap<String, String>();
        payload.put( "fid", "event.feature.fid" );
        payload.put( "feature", "new OpenLayers.Format.GeoJSON().write(event.feature, false)" );
        vectorLayer.getVectorLayer().events.register( this, 
                ModifyFeatureControl.EVENT_AFTER_MODIFIED, payload );

        // selection event -> hover feature
        final Display display = Polymap.getSessionDisplay();
        Map<String, String> payload2 = new HashMap<String, String>();
        payload2.put( "feature", "new OpenLayers.Format.GeoJSON().write(event.feature, false)" );
        vectorLayer.getVectorLayer().events.register( new OpenLayersEventListener() {
            public void process_event( OpenLayersObject srcObj, String eventName,
                    final HashMap<String,String> _payload ) {

                display.asyncExec( new Runnable() {
                    public void run() {
                        try {
                            // parse json feature
                            FeatureJSON io = new FeatureJSON();
                            final SimpleFeature feature = io.readFeature( new StringReader( _payload.get( "feature" ) ) );

                            LayerFeatureSelectionManager fsm = LayerFeatureSelectionManager.forLayer( getSelectedLayer() );
                            fsm.setHovered( feature.getID() );
                        }
                        catch (Exception e) {
                            PolymapWorkbench.handleError( MapEditorPlugin.PLUGIN_ID, this, e.getMessage(), e );
                        }
                    }
                });
            }
        }, ModifyFeatureControl.EVENT_BEFORE_MODIFIED, payload2 );
        
        fireEvent( this, PROP_LAYER_ACTIVATED, getSelectedLayer() );
    }


    @Override
    public void createPanelControl( Composite parent ) {
        super.createPanelControl( parent );
        
        vectorLayer.getStyler().createPanelControl( parent, this );     
    }

    
    @Override
    public void onDeactivate() {
        super.onDeactivate();
        
        if (keyboardControl != null) {
            getSite().getEditor().removeControl( keyboardControl );
            keyboardControl.deactivate();
            keyboardControl.dispose();
            keyboardControl = null;
        }
        if (naviControl != null) {
            getSite().getEditor().removeControl( naviControl );
            naviControl.deactivate();
            naviControl.dispose();
            naviControl = null;
        }
        if (modifyControl != null) {
            getSite().getEditor().removeControl( modifyControl );
            modifyControl.deactivate();
            modifyControl.destroy();
            modifyControl.dispose();
            modifyControl = null;
        }
        if (vectorLayer != null) {
            vectorLayer.dispose();
            vectorLayer = null;
            lastControl = layersList;
        }
    }

    
    @Override
    public void process_event( OpenLayersObject obj, String name, HashMap<String, String> payload ) {
        //log.debug( "process_event() event: " + name + ", from: " + obj );
        for (Map.Entry entry : payload.entrySet()) {
            Object key = entry.getKey();
            Object value = entry.getValue();
            //log.debug( "    key: " + key + ", value: " + StringUtils.abbreviate( (String)value, 0, 50 ) );
        }
        try {
            // parse json feature
            FeatureJSON io = new FeatureJSON();
            SimpleFeature feature = io.readFeature( new StringReader( payload.get( "feature" ) ) );
            log.debug( "Feature: " + feature );

            // execute operation
            PipelineFeatureSource fs = PipelineFeatureSource.forLayer( getSelectedLayer(), true );
            String property = fs.getSchema().getGeometryDescriptor().getLocalName();
            ModifyFeaturesOperation op = new ModifyFeaturesOperation( getSelectedLayer(),
                    fs, feature.getID(), property, feature.getDefaultGeometry() );
            
            OperationSupport.instance().execute( op, true, false, new JobChangeAdapter() {
                public void done( IJobChangeEvent event ) {
                    Polymap.getSessionDisplay().asyncExec( new Runnable() {
                        public void run() {
                            // let RenderManager listeners update
//                            WMSLayer olayer = (WMSLayer)getSite().getEditor().findLayer( getSelectedLayer() );
//                            olayer.redraw( true );
                            
//                            // XXX hack to remove 'duplicate" geometries introduces by OL 2.12
//                            ReferencedEnvelope bounds = getSite().getEditor().getMap().getExtent();
//                            vectorLayer.selectFeatures( bounds, false );
                        }
                    });
                }
            });
        }
        catch (Throwable e) {
            PolymapWorkbench.handleError( MapEditorPlugin.PLUGIN_ID, this, i18n( "errorMsg" ), e );
        }
    }

    
    @Override
    public String i18n( String key, Object... args ) {
        return Messages.get( "EditTool_" + key, args );    
    }
    
}
