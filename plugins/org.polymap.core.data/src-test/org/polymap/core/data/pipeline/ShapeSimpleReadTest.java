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
import java.io.Serializable;

import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;

/**
 * Test against {@link ShapefileDataStore}.
 *
 * @author Falko Bräutigam
 */
public class ShapeSimpleReadTest
        extends SimpleReadTest {

    public void setUp() throws Exception {
        // DataStore
        File f = new File( "/home/falko/Data/WGN_SAX_INFO/Datenuebergabe_Behoerden_Stand_1001/Shapedateien/Chem_Zustand_Fliessgew_WK_Liste_CHEM_0912.shp" );
        ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();

        Map<String, Serializable> params = new HashMap<String, Serializable>();
        params.put( "url", f.toURI().toURL() );
        params.put( "create spatial index", Boolean.TRUE );

        ds = (ShapefileDataStore)dataStoreFactory.createNewDataStore( params );
        fs = ds.getFeatureSource();
        
        // Pipeline
        new Pipeline();
    }
    

    public void tearDown() throws Exception {
        ds.dispose();
    }

}
