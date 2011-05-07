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
package org.polymap.core.mapeditor.edit;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.opengis.feature.type.GeometryDescriptor;

import org.eclipse.swt.widgets.Event;

import org.eclipse.jface.action.IAction;

import org.eclipse.ui.IEditorActionDelegate;

import org.polymap.core.data.PipelineFeatureSource;
import org.polymap.core.data.operations.NewFeatureOperation;
import org.polymap.core.mapeditor.MapEditorPlugin;
import org.polymap.core.operation.OperationSupport;
import org.polymap.core.workbench.PolymapWorkbench;
import org.polymap.openlayers.rap.widget.base.OpenLayersEventListener;
import org.polymap.openlayers.rap.widget.base.OpenLayersObject;
import org.polymap.openlayers.rap.widget.controls.DrawFeatureControl;

/**
 * Editor action for the {@link EditFeatureSupport}. This actions controls
 * the {@link DrawFeatureControl}.
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class DrawFeatureEditorAction
        extends AbstractEditorAction
        implements IEditorActionDelegate, OpenLayersEventListener {

    private static Log log = LogFactory.getLog( DrawFeatureEditorAction.class );

    
    public DrawFeatureEditorAction() {
        controlType = DrawFeatureControl.class;
    }


    public void runWithEvent( IAction _action, Event ev ) {
        log.debug( "ev= " + ev );
        assert support != null;
        mapEditor.activateSupport( support, action.isChecked() );
        
        if (action.isChecked()) {
            try {
                DrawFeatureControl control = (DrawFeatureControl)support.getControl( DrawFeatureControl.class );
                if (control == null) {
                    // find geometry type
                    PipelineFeatureSource fs = PipelineFeatureSource.forLayer( support.layer, true );
                    GeometryDescriptor geom = fs.getSchema().getGeometryDescriptor();
                    String geomName = geom.getType().getName().toString();
                    log.debug( "Geometry: " + geomName );

                    String handler = null;
                    if ("MultiLineString".equals( geomName )
                            || "LineString".equals( geomName )) {
                        handler = DrawFeatureControl.HANDLER_LINE;
                    }
                    else if ("MultiPolygon".equals( geomName )
                            || "Polygon".equals( geomName )) {
                        handler = DrawFeatureControl.HANDLER_POLYGON;
                    }
                    else if ("Point".equals( geomName )) {
                        handler = DrawFeatureControl.HANDLER_POINT;
                    }
                    else {
                        log.warn( "Unhandled geometry type: " + geomName + ". Using polygone handler..." );
                        handler = DrawFeatureControl.HANDLER_POLYGON;
//                        throw new Exception( "Dieser Geometrietyp kann nicht bearbeitet werden: " + geom.getType().getName() );
                    }
                    control = new DrawFeatureControl( support.vectorLayer, handler );
                    support.addControl( control );

                    // register event handler
                    Map<String, String> payload = new HashMap<String, String>();
                    payload.put( "features", "new OpenLayers.Format.GeoJSON().write(event.feature, false)" );
                    support.vectorLayer.events.register( this, 
                            DrawFeatureControl.EVENT_ADDED, payload );
                }
                support.setControlActive( DrawFeatureControl.class, true );

            }
            catch (Exception e) {
                PolymapWorkbench.handleError( MapEditorPlugin.PLUGIN_ID, this, "", e );
            }
        }

    }

    
    public void process_event( OpenLayersObject obj, String name, HashMap<String, String> payload ) {
        log.debug( "process_event() event: " + name + ", from: " + obj );
        for (Map.Entry entry : payload.entrySet()) {
            Object key = entry.getKey();
            Object value = entry.getValue();
            log.debug( "    key: " + key + ", value: " + StringUtils.abbreviate( (String)value, 0, 500 ) );
        }
        
        try {
            NewFeatureOperation op = new NewFeatureOperation( support.layer, null, payload.get( "features" ) );
            OperationSupport.instance().execute( op, true, false );

// XXX inside operation now
//            // geo event: added
//            GeoEvent event = new GeoEvent( GeoEvent.Type.FEATURE_CREATED, 
//                    mapEditor.getMap().getLabel(), 
//                    null );
//            event.setBody( Collections.singletonList( (Feature)op.getFeature() ) );
//            GeoHub.instance().send( event );
//
//            // geo event: hovered
//            event = new GeoEvent( GeoEvent.Type.FEATURE_HOVERED, 
//                    mapEditor.getMap().getLabel(), 
//                    null );
//            event.setBody( Collections.singletonList( (Feature)op.getFeature() ) );
//            GeoHub.instance().send( event );
        }
        catch (Throwable e) {
            log.warn( "", e );
            PolymapWorkbench.handleError( MapEditorPlugin.PLUGIN_ID, this, "", e );
        }
    }

}
