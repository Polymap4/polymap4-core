/* 
 * polymap.org
 * Copyright 2009-2013, Polymap GmbH. All rights reserved.
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
package org.polymap.core.data.operations;

import java.io.IOException;
import java.io.StringReader;

import org.geotools.data.FeatureStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.filter.Id;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Iterables;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;

import org.eclipse.swt.widgets.Display;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.polymap.core.data.DataPlugin;
import org.polymap.core.data.Messages;
import org.polymap.core.data.PipelineFeatureSource;
import org.polymap.core.data.feature.FidSet;
import org.polymap.core.data.feature.buffer.LayerFeatureBufferManager;
import org.polymap.core.geohub.LayerFeatureSelectionManager;
import org.polymap.core.project.ILayer;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.workbench.PolymapWorkbench;

/**
 * Creates a new feature. This operation is triggered by
 * {@link DrawFeatureEditorAction} but might be used by other classes as well.
 * <p/>
 * Undo is implemented by simply removing the newly created feature. The
 * {@link LayerFeatureBufferManager} is able to revert changes for one feature,
 * however this would not work for features of an EntitySourceProcessor.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @since 3.0
 */
public class NewFeatureOperation
        extends AbstractOperation
        implements IUndoableOperation {
    
    private static Log log = LogFactory.getLog( NewFeatureOperation.class );

    private ILayer                  layer;
    
    private FeatureStore            fs;
    
    private String                  jsonParam; 
    
    /** The fids created on last execute() call. */
    private FidSet                  fids;
    

    /**
     * Create a new feature from the given JSON string.
     * <p>
     * Any exceptions while parsing, creating or adding is thrown by the
     * {@link #execute(IProgressMonitor, IAdaptable)} method.
     *  
     * @param layer
     * @param jsonParam
     */
    public NewFeatureOperation( ILayer layer, FeatureStore fs, String json ) {
        super( Messages.get( "NewFeatureOperation_label", layer.getLabel() ) );
        this.layer = layer;
        this.jsonParam = json;
    }

    
    /**
     * Create a new, empty feature.
     * <p>
     * Any exceptions while parsing, creating or adding is thrown by the
     * {@link #execute(IProgressMonitor, IAdaptable)} method.
     *  
     * @param layer
     * @param jsonParam
     */
    public NewFeatureOperation( ILayer layer, FeatureStore fs ) {
        super( Messages.get( "NewFeatureOperation_label", layer.getLabel() ) );
        this.layer = layer;
    }

    /**
     * Returns the feature after the operation was executed.
     * @throws IOException 
     */
    public Feature getCreatedFeature() 
    throws IOException {
        Id fidFilter = DataPlugin.ff.id( fids );
        FeatureCollection coll = fs.getFeatures( fidFilter );
        return (Feature)coll.toArray( new Feature[1] )[0];
    }

    public ILayer getLayer() {
        return layer;
    }

    public FeatureStore getFeatureStore() {
        return fs;
    }


    public IStatus execute( IProgressMonitor monitor, IAdaptable info )
    throws ExecutionException {
        try {
            // display and monitor
            Display display = Polymap.getSessionDisplay();
            log.debug( "### Display: " + display );
            monitor.subTask( getLabel() );
            
            // FeatureStore for layer
            if (fs == null) {
                fs = PipelineFeatureSource.forLayer( layer, true );
            }
            SimpleFeatureType schema = (SimpleFeatureType)fs.getSchema();

            // create feature
            SimpleFeature newFeature;
            if (jsonParam != null) {
                newFeature = parseJSON( schema, jsonParam );
            }
            else {
                SimpleFeatureBuilder fb = new SimpleFeatureBuilder( schema );
                newFeature = fb.buildFeature( null );
            }

            // add feature
            FeatureCollection<SimpleFeatureType, SimpleFeature> features = 
                    FeatureCollections.newCollection();
            features.add( newFeature );
            
            fids = new FidSet( fs.addFeatures( features ) );
            log.info( "### Feature created: " + fids );

            monitor.worked( 1 );

            // update UI
            display.asyncExec( new Runnable() {
                public void run() {
                    try {
                        // hover event
                        LayerFeatureSelectionManager fsm = LayerFeatureSelectionManager.forLayer( layer );
                        fsm.setHovered( Iterables.getFirst( fids, null ).getID() );
                    }
                    catch (Exception e) {
                        PolymapWorkbench.handleError( DataPlugin.PLUGIN_ID, this, e.getMessage(), e );
                    }
                }
            });
            return Status.OK_STATUS;
        }
        catch (Exception e) {
            throw new ExecutionException( Messages.get( "NewFeatureOperation_errorMsg" ), e );
        }
    }

    
    protected SimpleFeature parseJSON( FeatureType schema, String json ) 
    throws Exception {
        // parse JSON
        log.debug( json );
        FeatureJSON io = new FeatureJSON();
        SimpleFeature feature = io.readFeature( new StringReader( json ) );
        log.debug( "JSON Feature: " + feature );

        // builder
        SimpleFeatureBuilder fb = new SimpleFeatureBuilder( (SimpleFeatureType)schema );
        // XXX do we need this anyway?
//        for (Property prop : feature.getProperties()) {
//            if (!(prop instanceof GeometryAttribute)) {
//                featureBuilder.set( prop.getName(), prop.getValue() );
//            }
//        }
        
        // convert to multi geometry if necessary
        GeometryDescriptor geomDesc = schema.getGeometryDescriptor();
        GeometryFactory gf = new GeometryFactory();
        String geomName = geomDesc.getType().getName().toString();
        log.debug( "    geometry: " + geomName );
        Geometry geom = (Geometry)feature.getDefaultGeometry();
        if ("MultiLineString".equals( geomName )) {
            log.debug( "    convert geometry: " + geom + " -> " + geomName ); 
            geom = gf.createMultiLineString( new LineString[] {(LineString)geom} );
        }
        else if ("MultiPolygon".equals( geomName )) {
            log.debug( "    convert geometry: " + geom + " -> " + geomName ); 
            geom = gf.createMultiPolygon( new Polygon[] {(Polygon)geom} );
        }
        
        // transform CRS
        CoordinateReferenceSystem layerCRS = schema.getCoordinateReferenceSystem();
        CoordinateReferenceSystem mapCRS = layer.getMap().getCRS();
        if (!mapCRS.equals( layerCRS )) {
            //log.debug( "    transforming geom: " + mapCRS.getName() + " -> " + layerCRS.getName() );
            MathTransform transform = CRS.findMathTransform( mapCRS, layerCRS );
            geom = JTS.transform( geom, transform );
        }
        
        // create feature
        fb.set( geomDesc.getLocalName(), geom );
        return fb.buildFeature( null );
    }

    
    public boolean canUndo() {
        return true;
    }

    @Override
    public IStatus undo( IProgressMonitor monitor, IAdaptable info )
            throws ExecutionException {
        try {
            fs.removeFeatures( DataPlugin.ff.id( fids ) );
            monitor.worked( 1 );
            return Status.OK_STATUS;
        }
        catch (Exception e) {
            throw new ExecutionException( Messages.get( "NewFeatureOperation_errorMsg" ), e );
        }
    }

    public boolean canRedo() {
        return true;
    }

    public IStatus redo( IProgressMonitor monitor, IAdaptable info )
            throws ExecutionException {
        return execute( monitor, info );
    }

}
