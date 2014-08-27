/* 
 * polymap.org
 * Copyright (C) 2013, Falko Bräutigam. All rights reserved.
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
import org.polymap.core.model2.test.Employee;
import org.polymap.core.model2.test.Person;
import org.polymap.core.model2.test.SimpleModelTest;
import org.polymap.core.runtime.recordstore.lucene.LuceneRecordStore;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class RFeatureSimpleModelTest
        extends SimpleModelTest {

    private static Log log = LogFactory.getLog( RFeatureSimpleModelTest.class );

    static {
        System.setProperty( "org.apache.commons.logging.simplelog.defaultlog", "debug" );
        System.setProperty( "org.apache.commons.logging.simplelog.log.org.polymap.core.model2", "debug" );
        System.setProperty( "org.apache.commons.logging.simplelog.log.org.polymap.core.model2.store.feature", "debug" );
    }
    
    protected RDataStore                ds;

    private FeatureStoreAdapter         store;
    
    
    public RFeatureSimpleModelTest( String name ) {
        super( name );
    }


    protected void setUp() throws Exception {
        super.setUp();
        
        LuceneRecordStore lucenestore = new LuceneRecordStore();
        ds = new RDataStore( lucenestore, new LuceneQueryDialect() );
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
        FeatureType schema = store.featureType( Person.class );
        log.info( "FeatureType: " + schema );
        assertEquals( schema.getName().getLocalPart(), "Person" );
        assertEquals( schema.getDescriptor( "name" ).getType().getBinding(), String.class );
        assertEquals( ((AttributeDescriptor)schema.getDescriptor( "firstname" ))
                    .getDefaultValue(), "Ulli" );
        assertTrue( schema.getDescriptor( "birthday" ).isNillable() );
        GeometryDescriptor geom = schema.getGeometryDescriptor();
        assertNotNull( geom );
        assertEquals( geom.getLocalName(), "geom" );
        assertEquals( geom.getType().getBinding(), Point.class );
        assertEquals( geom.getCoordinateReferenceSystem(), CRS.decode( "EPSG:31468" ) );
    }
    
}
