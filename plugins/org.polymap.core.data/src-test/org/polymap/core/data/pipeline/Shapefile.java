/* 
 * polymap.org
 * Copyright (C) 2018, the @authors. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.core.data.pipeline;

import java.util.HashMap;
import java.util.Map;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.nio.file.Files;

import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.simple.SimpleFeatureType;

import org.polymap.core.data.pipeline.SimpleReadTest.Expected;
import org.polymap.core.data.util.Geometries;

/**
 * 
 *
 * @author Falko Bräutigam
 */
public class Shapefile {

    public static final ShapefileDataStoreFactory factory = new ShapefileDataStoreFactory();

    
    public static Expected testfileExpectations() throws Exception {
        Expected _expected = new Expected();
        _expected.SCHEMA_NAME = "GemeindenMittelsachsen";    
        _expected.COUNT = 61;
        _expected.CRS = Geometries.crs( "EPSG:31468" );
        _expected.BOUNDS = new ReferencedEnvelope( 4543463.93, 4616738.24, 5611984.558, 5678565.71, _expected.CRS );
        return _expected;
    }
    
    public static ShapefileDataStore openTestfile() {
        URL url = Thread.currentThread().getContextClassLoader().getResource( "GemeindenMittelsachsen.shp" );
        return open( url );
        //return open( "/home/falko/Data/WGN_SAX_INFO/Datenuebergabe_Behoerden_Stand_1001/Shapedateien/Chem_Zustand_Fliessgew_WK_Liste_CHEM_0912.shp" );
    }

    
    public static ShapefileDataStore open( URL file ) {
        try {
            Map<String,Serializable> params = new HashMap();
            params.put( ShapefileDataStoreFactory.URLP.key, file );
            return (ShapefileDataStore)factory.createDataStore( params );
        }
        catch (IOException e) {
            throw new RuntimeException( e );
        }
    }

    
    public static ShapefileDataStore createTemp( SimpleFeatureType schema ) {
        try {
            File dir = Files.createTempDirectory( Shapefile.class.getPackage().getName() + ".test" ).toFile();
            File shp = new File( dir, schema.getName().getLocalPart() + ".shp" );
            ShapefileDataStore ds = open( shp.toURI().toURL() );
            
            ds.createSchema( schema );
            return ds;
        }
        catch (IOException e) {
            throw new RuntimeException( e );
        }
    }
    
}
