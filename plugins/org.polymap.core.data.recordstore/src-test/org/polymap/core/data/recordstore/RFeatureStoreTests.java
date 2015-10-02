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
package org.polymap.core.data.recordstore;

import static org.polymap.core.data.Features.iterable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import java.io.File;
import java.io.Serializable;

import junit.framework.TestCase;

import org.geotools.data.FeatureSource;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.referencing.CRS;
import org.geotools.util.Utilities;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureVisitor;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.filter.FilterFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Iterables;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiLineString;

import org.polymap.core.data.recordstore.RDataStore;
import org.polymap.core.data.recordstore.RFeatureStore;
import org.polymap.core.data.recordstore.lucene.LuceneQueryDialect;

import org.polymap.recordstore.lucene.LuceneRecordStore;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class RFeatureStoreTests
        extends TestCase {

    private static Log log = LogFactory.getLog( RFeatureStoreTests.class );

    private static final FilterFactory ff = CommonFactoryFinder.getFilterFactory( null );
    
    private RDataStore              ds;
    

    protected void setUp() throws Exception {
        super.setUp();
        LuceneRecordStore rs = new LuceneRecordStore( /*new File( "/tmp/LuceneRecordStoreTest" ), true*/ );
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
    
    
    public void testCreateSimpleSchemaAndFeature() throws Exception {
        log.debug( "creating schema..." );
        SimpleFeatureType schema = createSimpleSchema();
        ds.createSchema( schema );
        RFeatureStore fs = (RFeatureStore)ds.getFeatureSource( schema.getName() );        

        assertEquals( 0, Iterables.size( iterable( fs.getFeatures() ) ) );

        // add feature
        SimpleFeatureBuilder fb = new SimpleFeatureBuilder( schema );
        fb.set( "name", "value" );
        fb.set( "geom", null );
        DefaultFeatureCollection features = new DefaultFeatureCollection();
        features.add( fb.buildFeature( null ) );
        fs.addFeatures( features );
        
        // check size
        assertEquals( 1, Iterables.size( iterable( fs.getFeatures() ) ) );

        // check properties
        fs.getFeatures().accepts( new FeatureVisitor() {
            public void visit( Feature feature ) {
                log.debug( "Feature: " + feature );
                assertEquals( "value", ((SimpleFeature)feature).getAttribute( "name" ) );
                assertEquals( null, ((SimpleFeature)feature).getAttribute( "geom" ) );
                assertEquals( null, ((SimpleFeature)feature).getDefaultGeometry() );
            }
        }, null );
        
        // modify property
        Feature feature = Iterables.getOnlyElement( iterable( fs.getFeatures() ) );
        fs.modifyFeatures( (AttributeDescriptor)feature.getProperty( "name" ).getDescriptor(), 
                "changed", ff.id( Collections.singleton( feature.getIdentifier() ) )  );

        Feature feature2 = Iterables.getOnlyElement( iterable( fs.getFeatures() ) );
        assertEquals( "changed", ((SimpleFeature)feature2).getAttribute( "name" ) );
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

        // creating schema
        ds.createSchema( shapeFs.getSchema() );

        // adding features
        RFeatureStore fs = (RFeatureStore)ds.getFeatureSource( shapeFs.getSchema().getName() );        
        fs.addFeatures( shapeFs.getFeatures() );
        
        // check size
        assertEquals( 669, fs.getFeatures().size() );
        
        // iterating accept
        fs.getFeatures().accepts( new FeatureVisitor() {
            public void visit( Feature feature ) {
                assertTrue( feature.getDefaultGeometryProperty().getValue() instanceof Geometry );
            }
        }, null );

        // check feature
        FeatureIterator<SimpleFeature> fsIt = fs.getFeatures().features();
        FeatureIterator<SimpleFeature> shapeIt = shapeFs.getFeatures().features();
        int count = 0;
        for (;fsIt.hasNext() && shapeIt.hasNext(); count++) {
            SimpleFeature f1 = fsIt.next();
            SimpleFeature f2 = shapeIt.next();
            
            for (Property prop1 : f1.getProperties()) {
                Property prop2 = f2.getProperty( prop1.getName() );
                if (prop1.getValue() instanceof Geometry) {
                    // skip
                }
                else {
                    assertTrue( "Property don't match: " + prop1.getName() + ": " + prop1.getValue() + "!=" + prop2.getValue(), 
                            Utilities.equals( prop1.getValue(), prop2.getValue() ) );
                }
            }
            //assertTrue( "Features are not equal: \n" + f1 + "\n" + f2, Utilities. );
        }
        assertEquals( 669, count );
    }
    
}
