/* 
 * polymap.org
 * Copyright 2009-2012, Polymap GmbH, and individual contributors as indicated
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
 */
package org.polymap.core.mapeditor.edit;

import java.util.HashMap;
import java.util.Map;

import java.io.StringReader;

import org.geotools.geojson.feature.FeatureJSON;
import org.opengis.feature.simple.SimpleFeature;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.widgets.Event;

import org.eclipse.jface.action.IAction;

import org.eclipse.ui.IEditorActionDelegate;

import org.polymap.openlayers.rap.widget.base.OpenLayersEventListener;
import org.polymap.openlayers.rap.widget.base.OpenLayersObject;
import org.polymap.openlayers.rap.widget.controls.DeleteFeatureControl;
import org.polymap.openlayers.rap.widget.controls.DrawFeatureControl;

/**
 * Editor action for the {@link EditFeatureSupport}. This actions controls
 * the {@link DrawFeatureControl}.
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @since 3.0
 */
public class DeleteFeatureEditorAction
        extends AbstractEditEditorAction
        implements IEditorActionDelegate, OpenLayersEventListener {

    private static Log log = LogFactory.getLog( DeleteFeatureEditorAction.class );

    
    public DeleteFeatureEditorAction() {
        controlType = DeleteFeatureControl.class;
    }


    public void runWithEvent( IAction _action, Event ev ) {
        log.debug( "ev= " + ev );
        assert support != null;
        mapEditor.activateSupport( support, action.isChecked() );
        
        if (action.isChecked()) {
            DeleteFeatureControl control = (DeleteFeatureControl)support.getControl( DeleteFeatureControl.class );
            if (control == null) {
                control = new DeleteFeatureControl( support.vectorLayer );
                support.addControl( control );

                // delete event
                Map<String,String> payload = new HashMap<String, String>();
                payload.put( "feature", "new OpenLayers.Format.GeoJSON().write(event.feature, false)" );
                control.events.register( this, DeleteFeatureControl.EVENT_FEATURE_DELETED, payload );

            }
            support.setControlActive( DeleteFeatureControl.class, true );
        }
    }

    // FIXME: event is never called; seems that the event name sent from JS is wrong;
    // mouseover instead of EVENT_FEATURE_DELETED
    public void process_event( OpenLayersObject obj, String name, HashMap<String,String> payload ) {
        log.info( "process_event() event: " + name + ", from: " + obj );
        for (Map.Entry entry : payload.entrySet()) {
            Object key = entry.getKey();
            Object value = entry.getValue();
            log.info( "    key: " + key + ", value: " + StringUtils.abbreviate( (String)value, 0, 50 ) );
        }
        try {
            // parse json feature
            FeatureJSON io = new FeatureJSON();
            SimpleFeature feature = io.readFeature( new StringReader( payload.get( "feature" ) ) );
            log.info( "Feature: " + feature );

//            // execute operation
//            PipelineFeatureSource fs = PipelineFeatureSource.forLayer( support.layer, true );
//            String property = fs.getSchema().getGeometryDescriptor().getLocalName();
//            ModifyFeaturesOperation op = new ModifyFeaturesOperation( support.layer,
//                    fs, feature.getID(), property, feature.getDefaultGeometry() );
//            
//            OperationSupport.instance().execute( op, true, false, new JobChangeAdapter() {
//                public void done( IJobChangeEvent event ) {
//                    Polymap.getSessionDisplay().asyncExec( new Runnable() {
//                        public void run() {
//                            WMSLayer olayer = (WMSLayer)mapEditor.findLayer( support.layer );
//                            olayer.redraw( true );
//                        }
//                    });
//                }
//            });
        }
        catch (Throwable e) {
            log.warn( "", e );
            
            // bad inside process_event()
            //PolymapWorkbench.handleError( MapEditorPlugin.PLUGIN_ID, this, "", e );
        }
    }

}
