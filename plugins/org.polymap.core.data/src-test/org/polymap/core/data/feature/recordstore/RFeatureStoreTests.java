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
package org.polymap.core.data.feature.recordstore;

import java.util.HashMap;
import java.util.Map;

import java.io.File;
import java.io.Serializable;

import junit.framework.TestCase;

import org.geotools.data.FeatureSource;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.feature.DefaultFeatureCollections;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.referencing.CRS;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureVisitor;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiLineString;

import org.polymap.core.runtime.Timer;
import org.polymap.core.runtime.recordstore.lucene.LuceneRecordStore;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class RFeatureStoreTests
        extends TestCase {

    private static Log log = LogFactory.getLog( RFeatureStoreTests.class );

    private RDataStore              ds;
    

    protected void setUp() throws Exception {
        super.setUp();
        LuceneRecordStore rs = new LuceneRecordStore( new File( "/tmp/LuceneRecordStoreTest" ), true );
        ds = new RDataStore( rs, new LuceneQueryDialect() );
    }


    protected void tearDown() throws Exception {
        super.tearDown();
    }

    protected SimpleFeatureType createSimpleSchema() throws Exception {
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName( "Test1" );
        builder.setCRS( CRS.decode( "EPSG:4326" ) );
        builder.add( "name", String.class );
        builder.add( "geom", MultiLineString.class, "EPSG:4326" );
        return builder.buildFeatureType();        
    }
    
    
    public void tstCreateSimpleSchemaAndFeature() throws Exception {
        log.debug( "creating schema..." );
        SimpleFeatureType schema = createSimpleSchema();
        ds.createSchema( schema );

        SimpleFeatureBuilder fb = new SimpleFeatureBuilder( schema );
        fb.set( "name", "value" );
        fb.set( "geom", null );
        FeatureCollection features = DefaultFeatureCollections.newCollection();
        features.add( fb.buildFeature( null ) );
        
        log.debug( "adding features..." );
        RFeatureStore fs = (RFeatureStore)ds.getFeatureSource( schema.getName() );        
        fs.addFeatures( features );
        
        log.debug( "iterating all features..." );
        fs.getFeatures().accepts( new FeatureVisitor() {
            public void visit( Feature feature ) {
                log.info( "Feature: " + feature );
            }
        }, null );
    }

    
    public void testCopyFluesse() throws Exception {
        File f = new File( "/home/falko/Data/WGN_SAX_INFO/Datenuebergabe_Behoerden_Stand_1001/Shapedateien/Chem_Zustand_Fliessgew_WK_Liste_CHEM_0912.shp" );
        log.debug( "opening shapefile: " + f );
        ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();

        Map<String, Serializable> params = new HashMap<String, Serializable>();
        params.put( "url", f.toURI().toURL() );
        params.put( "create spatial index", Boolean.TRUE );

        ShapefileDataStore shapeDs = (ShapefileDataStore) dataStoreFactory.createNewDataStore( params );
        FeatureSource<SimpleFeatureType, SimpleFeature> shapeFs = shapeDs.getFeatureSource();

        log.debug( "creating schema..." );
        ds.createSchema( shapeFs.getSchema() );

        log.debug( "adding features..." );
        RFeatureStore fs = (RFeatureStore)ds.getFeatureSource( shapeFs.getSchema().getName() );        
        fs.addFeatures( shapeFs.getFeatures() );
        
        Timer timer = new Timer();
        log.debug( "iterating all features..." );
        fs.getFeatures().accepts( new FeatureVisitor() {
            public void visit( Feature feature ) {
                boolean isGeom = feature.getDefaultGeometryProperty().getValue() instanceof Geometry;
                //log.info( "Feature: geom: " + (feature.getDefaultGeometryProperty().getValue() instanceof Geometry) );
            }
        }, null );
        log.info( "Reading all features: " + timer.elapsedTime() + "ms" );
    }
    
}
