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

import java.util.Collections;
import java.util.List;

import java.io.IOException;
import java.io.StringReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geojson.feature.FeatureJSON;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.filter.identity.FeatureId;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;

import net.refractions.udig.ui.OffThreadProgressMonitor;

import org.eclipse.swt.widgets.Display;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.polymap.core.data.PipelineFeatureSource;
import org.polymap.core.geohub.GeoHub;
import org.polymap.core.geohub.event.GeoEvent;
import org.polymap.core.mapeditor.MapEditorPlugin;
import org.polymap.core.mapeditor.Messages;
import org.polymap.core.operation.JobMonitors;
import org.polymap.core.project.ILayer;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.workbench.PolymapWorkbench;

/**
 * Creates a new feature. This operation is triggered by
 * {@link DrawFeatureEditorAction} but might be used by other classes as well.
 * <p>
 * ...
 * 
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class NewFeatureOperation
        extends AbstractOperation
        implements IUndoableOperation {
    
    private static Log log = LogFactory.getLog( NewFeatureOperation.class );

    private ILayer                  layer;
    
    /** Feature read from json. */
    private SimpleFeature           feature;
    
    /** Feature created in the FeatureStore. */
    private SimpleFeature           createdFeature;
    

    public NewFeatureOperation( ILayer layer, String json ) 
    throws IOException {
        super( Messages.get( "NewFeatureOperation_labelPrefix" ) + layer.getLabel() );
        this.layer = layer;

        // parse json feature
        log.debug( json );
        FeatureJSON io = new FeatureJSON();
        feature = io.readFeature( new StringReader( json ) );
        log.debug( "Feature: " + feature );
    }

    
    /**
     * Returns the feature after the operation was executed.
     */
    public SimpleFeature getFeature() {
        return createdFeature;        
    }

    
    public IStatus execute( IProgressMonitor _monitor, IAdaptable info )
            throws ExecutionException {
        try {
            // display and monitor
            Display display = Polymap.getSessionDisplay();
            log.debug( "### Display: " + display );
            OffThreadProgressMonitor monitor = new OffThreadProgressMonitor( _monitor );
            JobMonitors.set( monitor );
            monitor.subTask( getLabel() );
            
            // FeatureStore for layer
            PipelineFeatureSource fs = PipelineFeatureSource.forLayer( layer, true );
            SimpleFeatureType type = fs.getSchema();
            GeometryDescriptor geomDesc = type.getGeometryDescriptor();

            // build feature
            SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder( type );
            // XXX do we need this anyway?
//            for (Property prop : feature.getProperties()) {
//                if (!(prop instanceof GeometryAttribute)) {
//                    featureBuilder.set( prop.getName(), prop.getValue() );
//                }
//            }
            
            // convert to MultiXXX geometry if necessary
            GeometryFactory gf = new GeometryFactory();
            String geomName = geomDesc.getType().getName().toString();
            log.debug( "    geometry: " + geomName );
            Object geom = feature.getDefaultGeometry();
            if ("MultiLineString".equals( geomName )) {
                log.debug( "    convert geometry: " + geom + " -> " + geomName ); 
                geom = gf.createMultiLineString( new LineString[] {(LineString)geom} );
            }
            else if ("MultiPolygon".equals( geomName )) {
                log.debug( "    convert geometry: " + geom + " -> " + geomName ); 
                geom = gf.createMultiPolygon( new Polygon[] {(Polygon)geom} );
            }
            featureBuilder.set( geomDesc.getLocalName(), geom );

            // add feature
            FeatureCollection<SimpleFeatureType, SimpleFeature> features = 
                    FeatureCollections.newCollection();
            createdFeature = featureBuilder.buildFeature( null );
            features.add( createdFeature );
            List<FeatureId> fids = fs.addFeatures( features );
            log.info( "### Feature created: " + fids.get( 0 ) );

            monitor.worked( 1 );
            
            // update UI
            display.asyncExec( new Runnable() {
                public void run() {
                    try {
                        // XXX update map editor
                        
                        // geo event: added
                        GeoEvent event = new GeoEvent( GeoEvent.Type.FEATURE_CREATED, 
                                layer.getMap().getLabel(), 
                                null );
                        event.setBody( Collections.singletonList( (Feature)createdFeature ) );
                        GeoHub.instance().send( event );

//                        // geo event: hovered
//                        event = new GeoEvent( GeoEvent.Type.FEATURE_HOVERED, 
//                                layer.getMap().getLabel(), 
//                                null );
//                        event.setBody( Collections.singletonList( (Feature)feature ) );
//                        GeoHub.instance().send( event );
                    }
                    catch (Exception e) {
                        PolymapWorkbench.handleError( MapEditorPlugin.PLUGIN_ID, this, e.getMessage(), e );
                    }
                }
            });

        }
        catch (Exception e) {
            throw new ExecutionException( "Failure...", e );
        }
        finally {
            JobMonitors.remove();
        }
        return Status.OK_STATUS;
    }


    public boolean canUndo() {
        return false;
    }

    public IStatus undo( IProgressMonitor monitor, IAdaptable info ) {
        throw new RuntimeException( "not yet implemented." );
    }

    public boolean canRedo() {
        return false;
    }

    public IStatus redo( IProgressMonitor monitor, IAdaptable info )
            throws ExecutionException {
        throw new RuntimeException( "not yet implemented." );
    }

}
