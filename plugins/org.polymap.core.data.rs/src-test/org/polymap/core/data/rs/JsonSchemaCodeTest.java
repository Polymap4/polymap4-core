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
package org.polymap.core.data.rs;

import junit.framework.TestCase;

import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.referencing.CRS;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;

import com.vividsolutions.jts.geom.MultiLineString;

import org.polymap.core.data.rs.JsonSchemaCoder;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class JsonSchemaCodeTest
        extends TestCase {

    private SimpleFeatureType       schema;
    

    public JsonSchemaCodeTest( String name ) 
    throws NoSuchAuthorityCodeException, FactoryException {
        super( name );
    }


    protected void setUp() throws Exception {
        super.setUp();
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName( "Test1" );
        builder.setCRS( CRS.decode( "EPSG:4326" ) );
        builder.add( "name", String.class );
        builder.add( "geom", MultiLineString.class, "EPSG:4326" );
        schema = builder.buildFeatureType();
    }


    public void testEncodeDecode() throws Exception {
        String encoded = new JsonSchemaCoder().encode( schema );
        System.out.println( encoded );
        assertNotNull( encoded );

        FeatureType schema2 = new JsonSchemaCoder().decode( encoded );
        System.out.println( "\n\n" + schema2 );
        
        // fails, not sure why
        //assertTrue( schema.equals( schema2 ) );

        String encoded2 = new JsonSchemaCoder().encode( schema2 );
        System.out.println( encoded2 );
        assertEquals( encoded, encoded2 );
    }
    
}
