/*
 * polymap.org 
 * Copyright 2010-2012, Polymap GmbH, and individual contributors as
 * indicated by the @authors tag.
 * 
 * This is free software; you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 2.1 of the License, or (at your option) any later
 * version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */
package org.polymap.core.mapeditor.services;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import org.apache.commons.io.output.CountingOutputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.opengis.feature.Feature;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.ReferenceIdentifier;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geojson.GeoJSONUtil;
import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.geojson.geom.GeometryJSON;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.geotools.referencing.NamedIdentifier;

import com.vividsolutions.jts.geom.Geometry;

import org.eclipse.jface.dialogs.MessageDialog;

import org.polymap.core.mapeditor.Messages;
import org.polymap.core.workbench.PolymapWorkbench;

/**
 * GeoJSON server based on {@link FeatureJSON} package of GeoTools. The encoder
 * seems to work faster for big geometries compared to to {@link GsJsonEncoder}.
 * The encoder does not encode all decimals of coordinates.
 * <p/>
 * FIXME Does not convert WGS84 geometries correctly.
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @since 3.0
 */
class GtJsonEncoder 
        extends JsonEncoder {

    private static final Log log = LogFactory.getLog( GtJsonEncoder.class );

    
    public void encode( CountingOutputStream out, String encoding )
    throws IOException {
        BufferedWriter writer = new BufferedWriter( 
                new OutputStreamWriter( out, encoding ) );

        boolean encodeFeatureCollectionBounds = false;
        boolean encodeFeatureCollectionCRS = false;

        // XXX this creates a GeometryJSON which has decimals set by default
        FeatureJSON fjson = new FeatureJSON( new GeometryJSON( decimals ) );
        //log.warn( "XXX: fjson.setGjson( new GeometryJSON( decimals ) );" );
        fjson.setEncodeFeatureBounds( false );
        fjson.setEncodeFeatureCRS( false );

        LinkedHashMap obj = new LinkedHashMap();
        obj.put( "type", "FeatureCollection" );
        obj.put( "features", new CollectionEncoder( fjson, out ) );
        obj.put( "crs", new CRSEncoder( fjson, mapCRS ) );
        
        GeoJSONUtil.encode( obj, writer );
        writer.flush();
    }

    
    /**
     * 
     */
    class CRSEncoder
            implements JSONStreamAware {
        
        private FeatureJSON                 fjson;

        private CoordinateReferenceSystem   crs;

        public CRSEncoder( FeatureJSON fjson, CoordinateReferenceSystem crs ) {
            super();
            this.fjson = fjson;
            this.crs = crs;
        }

        public void writeJSONString( Writer out )
                throws IOException {
            // this is code is from the 'old' SimpleJsonServer
            Set<ReferenceIdentifier> ids = mapCRS.getIdentifiers();
            // WKT defined crs might not have identifiers at all
            if (ids != null && ids.size() > 0) {
                NamedIdentifier namedIdent = (NamedIdentifier)ids.iterator().next();
                String csStr = namedIdent.getCodeSpace().toUpperCase();
                
                if (csStr.equals( "EPSG" )) {
                    JSONObject obj = new JSONObject();
                    obj.put( "type", csStr );

                    JSONObject props = new JSONObject();
                    obj.put( "properties", props );
                    props.put( "code", namedIdent.getCode() );
                    
                    obj.writeJSONString( out );
                }
                else {
                    log.warn( "Non-EPSG code not supported: " + csStr );
                }
            }
            else {
                log.warn( "No CRS identifier for CRS: " + mapCRS );
            }

            // this is the original code which throws exception for lookupIdentifier()
//            JSONObject obj = new JSONObject();
//            obj.put("type", "name");
//            
//            Map<String,Object> props = new LinkedHashMap<String, Object>();
//            try {
//                props.put("name", CRS.lookupIdentifier(crs, true));
//            } 
//            catch (FactoryException e) {
//                throw (IOException) new IOException("Error looking up crs identifier").initCause(e);
//            }
//            
//            obj.put("properties", props);
//            return obj;
        }
    }
    

    /**
     * 
     */
    class CollectionEncoder 
            implements JSONStreamAware {

        private FeatureJSON                 fjson;
        
        private MathTransform               transform;
        
        private SimpleFeatureType           transformedSchema;
        
        private CountingOutputStream        byteCounter;
        
        
        public CollectionEncoder( FeatureJSON fjson, CountingOutputStream byteCounter ) {
            this.fjson = fjson;
            this.byteCounter = byteCounter;
        }
        
        public void writeJSONString( Writer out ) 
        throws IOException {
            
            out.write("[");
            
            try {
                int featureCount = 0;
                Iterator<Feature> it = features.iterator();
                while (it.hasNext()) {
                    // check byte limit
                    if (byteCounter.getCount() > maxBytes) {
                        log.warn( "Byte limit reached. Features encoded: " + featureCount );
                        final int encoded = featureCount;
                        display.asyncExec( new Runnable() {
                            public void run() {
                                MessageDialog.openInformation(
                                        PolymapWorkbench.getShellToParentOn(),
                                        Messages.get( "GsJsonEncoder_toManyFeatures_title" ),
                                        Messages.get( "GsJsonEncoder_toManyFeatures_msg", features.size(), encoded ) );
                            }
                        } );
                        break;
                    }
                    // encode feature
                    fjson.writeFeature( transform( (SimpleFeature)it.next() ), out );
                    featureCount++;
                    if (it.hasNext()) { out.write(","); }
                }
            }
            catch (IOException e) {
                throw e;
            }
            catch (Exception e) {
                throw new IOException( e );
            }

            out.write("]");
        }

        private SimpleFeatureType transformedSchema( SimpleFeatureType schema )
        throws FactoryException {
            if (transformedSchema == null) {
                // CRS transform
                CoordinateReferenceSystem featureCRS = schema.getGeometryDescriptor().getCoordinateReferenceSystem();
                if (featureCRS != null && !mapCRS.equals( featureCRS )) {
                    transform = CRS.findMathTransform( featureCRS, mapCRS, true );
                }
                // feature type
                SimpleFeatureTypeBuilder ftb = new SimpleFeatureTypeBuilder();
                ftb.setName( schema.getName() );
                ftb.setCRS( mapCRS );
                ftb.add( schema.getGeometryDescriptor() );
                
                transformedSchema = ftb.buildFeatureType();
            }
            return transformedSchema;
        }
        
        /**
         * Transform the given feature: reproject CRS;
         * 
         * @throws FactoryException 
         * @throws TransformException 
         * @throws MismatchedDimensionException 
         */
        private SimpleFeature transform( SimpleFeature feature ) 
        throws FactoryException, MismatchedDimensionException, TransformException {
            SimpleFeatureType schema = feature.getFeatureType();
            
            SimpleFeatureBuilder builder = new SimpleFeatureBuilder( transformedSchema( schema ) );
            GeometryAttribute geomAttr = feature.getDefaultGeometryProperty();
            Geometry geom = (Geometry)geomAttr.getValue();
            builder.set( geomAttr.getName(), transform != null ? JTS.transform( geom, transform ) : geom );

            return builder.buildFeature( feature.getID() );
        }
    }

}