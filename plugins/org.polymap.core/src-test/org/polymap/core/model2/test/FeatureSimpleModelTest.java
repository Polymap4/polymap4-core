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
package org.polymap.core.model2.test;

import java.util.HashMap;
import java.util.Map;

import java.io.File;
import java.io.Serializable;

import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.referencing.CRS;
import org.opengis.feature.Feature;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.GeometryDescriptor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.vividsolutions.jts.geom.Point;

import org.polymap.core.model2.runtime.EntityRepository;
import org.polymap.core.model2.store.feature.FeatureStoreAdapter;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class FeatureSimpleModelTest
        extends SimpleModelTest {

    private static Log log = LogFactory.getLog( FeatureSimpleModelTest.class );

    protected ShapefileDataStore        ds;

    private FeatureStoreAdapter         store;
    
    
    public FeatureSimpleModelTest( String name ) {
        super( name );
    }


    protected void setUp() throws Exception {
        super.setUp();
        
        //File f = new File( "/home/falko/Data/WGN_SAX_INFO/Datenuebergabe_Behoerden_Stand_1001/Shapedateien/Chem_Zustand_Fliessgew_WK_Liste_CHEM_0912.shp" );
        File dir = new File( "/tmp/" + getClass().getSimpleName() );
        dir.mkdir();
        File f = new File( dir, "employee.shp" );
        log.debug( "opening shapefile: " + f );
        ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();

        Map<String,Serializable> params = new HashMap<String, Serializable>();
        params.put( "url", f.toURI().toURL() );
        params.put( "create spatial index", Boolean.TRUE );

        ds = (ShapefileDataStore) dataStoreFactory.createNewDataStore( params );
        store = new FeatureStoreAdapter( ds );
        repo = EntityRepository.newConfiguration()
                .setStore( store )
                .setEntities( new Class[] {Employee.class} )
                .create();
        uow = repo.newUnitOfWork();
    }

    
    protected Object stateId( Object state ) {
        return ((Feature)state).getIdentifier().getID();    
    }

    
    public void testFeatureTypeBuilder() throws Exception {
        FeatureType schema = store.featureType( Employee.class );
        log.info( "FeatureType: " + schema );
        assertEquals( "Employee", schema.getName().getLocalPart() );
        assertEquals( String.class, schema.getDescriptor( "name" ).getType().getBinding() );
        assertEquals( "Ulli", ((AttributeDescriptor)schema.getDescriptor( "firstname" )).getDefaultValue() );
        assertTrue( schema.getDescriptor( "birthday" ).isNillable() );

        // mixin: Trackable
        assertEquals( Integer.class, schema.getDescriptor( "track" ).getType().getBinding() );
        
        GeometryDescriptor geom = schema.getGeometryDescriptor();
        assertNotNull( geom );
        assertEquals( geom.getLocalName(), "geom" );
        assertEquals( geom.getType().getBinding(), Point.class );
        assertEquals( geom.getCoordinateReferenceSystem(), CRS.decode( "EPSG:31468" ) );
    }
    
}
