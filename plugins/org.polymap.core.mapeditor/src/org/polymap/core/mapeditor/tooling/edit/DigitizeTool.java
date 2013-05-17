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

import java.util.HashMap;
import java.util.Map;

import org.opengis.feature.type.GeometryDescriptor;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;

import org.polymap.core.data.PipelineFeatureSource;
import org.polymap.core.data.operations.NewFeatureOperation;
import org.polymap.core.mapeditor.MapEditorPlugin;
import org.polymap.core.mapeditor.Messages;
import org.polymap.core.operation.OperationSupport;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.workbench.PolymapWorkbench;

import org.polymap.openlayers.rap.widget.base.OpenLayersEventListener;
import org.polymap.openlayers.rap.widget.base.OpenLayersObject;
import org.polymap.openlayers.rap.widget.controls.DrawFeatureControl;
import org.polymap.openlayers.rap.widget.controls.KeyboardDefaultsControl;
import org.polymap.openlayers.rap.widget.controls.NavigationControl;
import org.polymap.openlayers.rap.widget.layers.WMSLayer;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class DigitizeTool
        extends BaseLayerEditorTool 
        implements OpenLayersEventListener {

    private static Log log = LogFactory.getLog( DigitizeTool.class );

    private EditVectorLayer         vectorLayer;

    private DrawFeatureControl      drawControl;
    
    private NavigationControl       naviControl;
    
    private KeyboardDefaultsControl keyboardControl;

    
    @Override
    public void dispose() {
        onDeactivate();
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
        
        // vector layer
        vectorLayer = new EditVectorLayer( getSite().getEditor(), getSelectedLayer() );
        vectorLayer.activate();

        // after digitize often a editor is opened, which cannot be used since KeyboardDefaultsControl
        // catches all key event; so disable until observeElement is correctly set
//        // keyboardControl
//        keyboardControl = new KeyboardDefaultsControl();
//        getSite().getEditor().addControl( keyboardControl );
//        keyboardControl.activate();

        // naviControl
        naviControl = new NavigationControl();
        getSite().getEditor().addControl( naviControl );
        naviControl.activate();

        // drawControl
        try {
            // find geometry type
            PipelineFeatureSource fs = PipelineFeatureSource.forLayer( getSelectedLayer(), true );
            GeometryDescriptor geom = fs.getSchema().getGeometryDescriptor();
            String geomType = geom.getType().getBinding().getSimpleName();
            log.debug( "Geometry: " + geomType );

            // choose appropriate handler
            String handler = null;
            if ("MultiLineString".equals( geomType )
                    || "LineString".equals( geomType )) {
                handler = DrawFeatureControl.HANDLER_LINE;
            }
            else if ("MultiPolygon".equals( geomType )
                    || "Polygon".equals( geomType )) {
                handler = DrawFeatureControl.HANDLER_POLYGON;
            }
            else if ("MultiPoint".equals( geomType )
                    || "Point".equals( geomType )) {
                handler = DrawFeatureControl.HANDLER_POINT;
            }
            else {
                log.warn( "Unhandled geometry type: " + geomType + ". Using polygone handler..." );
                handler = DrawFeatureControl.HANDLER_POLYGON;
                throw new Exception( "Dieser Geometrietyp kann nicht bearbeitet werden: " + geom.getType().getName() );
            }
            drawControl = new DrawFeatureControl( vectorLayer.getVectorLayer(), handler );
            getSite().getEditor().addControl( drawControl );
            drawControl.activate();

            // register event handler
            Map<String, String> payload = new HashMap<String, String>();
            payload.put( "features", "new OpenLayers.Format.GeoJSON().write(event.feature, false)" );
            vectorLayer.getVectorLayer().events.register( this, DrawFeatureControl.EVENT_ADDED, payload );
            vectorLayer.getVectorLayer().redraw();
        }
        catch (Exception e) {
            PolymapWorkbench.handleError( MapEditorPlugin.PLUGIN_ID, this, i18n( "errorMsg" ), e );
        }
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
        if (drawControl != null) {
            getSite().getEditor().removeControl( drawControl );
            drawControl.deactivate();
            // FIXME this crashes
//            drawControl.destroy();
            drawControl.dispose();
            drawControl = null;
        }
        if (vectorLayer != null) {
            vectorLayer.dispose();
            vectorLayer = null;
        }
    }

    
    @Override
    public void process_event( OpenLayersObject obj, String name, HashMap<String, String> payload ) {
        log.debug( "process_event() event: " + name + ", from: " + obj );
        for (Map.Entry entry : payload.entrySet()) {
            Object key = entry.getKey();
            Object value = entry.getValue();
            log.debug( "    key: " + key + ", value: " + StringUtils.abbreviate( (String)value, 0, 500 ) );
        }
        
        try {
            NewFeatureOperation op = new NewFeatureOperation( getSelectedLayer(), null, payload.get( "features" ) );
            
            OperationSupport.instance().execute( op, true, false, new JobChangeAdapter() {
                public void done( IJobChangeEvent event ) {
                    Polymap.getSessionDisplay().asyncExec( new Runnable() {
                        public void run() {
                            WMSLayer olayer = (WMSLayer)getSite().getEditor().findLayer( getSelectedLayer() );
                            if (olayer != null) {
                                olayer.redraw( true );
                            }
                        }
                    });
                }
            });

//            // redraw map layer
//            WMSLayer olayer = (WMSLayer)mapEditor.findLayer( support.layer );
//            olayer.redraw( true );
        }
        catch (Throwable e) {
            PolymapWorkbench.handleError( MapEditorPlugin.PLUGIN_ID, this, i18n( "errorMsg" ), e );
        }
    }

    
    @Override
    public String i18n( String key, Object... args ) {
        return Messages.get( "DigitizeTool_" + key, args );    
    }
    
}
