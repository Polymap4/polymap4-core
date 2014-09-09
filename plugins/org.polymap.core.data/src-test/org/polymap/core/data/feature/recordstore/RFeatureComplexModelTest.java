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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.model2.runtime.EntityRepository;
import org.polymap.core.model2.store.feature.FeatureStoreAdapter;
import org.polymap.core.model2.test.Company;
import org.polymap.core.model2.test.ComplexModelTest;
import org.polymap.core.model2.test.Employee;
import org.polymap.core.runtime.recordstore.lucene.LuceneRecordStore;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class RFeatureComplexModelTest
        extends ComplexModelTest {

    private static Log log = LogFactory.getLog( RFeatureComplexModelTest.class );

    protected RDataStore                ds;

    private FeatureStoreAdapter         store;
    
    
    public RFeatureComplexModelTest( String name ) {
        super( name );
    }


    protected void setUp() throws Exception {
        super.setUp();
        
        LuceneRecordStore lucenestore = new LuceneRecordStore();
        ds = new RDataStore( lucenestore, new LuceneQueryDialect() );
        store = new FeatureStoreAdapter( ds );
        repo = EntityRepository.newConfiguration()
                .setStore( store )
                .setEntities( new Class[] {Employee.class, Company.class} )
                .create();
        uow = repo.newUnitOfWork();
    }


    @Override
    public void testAssociation() {
        log.warn( "No Associations yet!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!" );
    }


    @Override
    public void testCompositeProperty() {
        log.warn( "No Composite properties yet!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!" );
    }


    @Override
    public void testPrimitiveCollection() {
        log.warn( "No Collections yet!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!" );
    }


    @Override
    public void testCompositeCollection() {
        log.warn( "No Collections yet!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!" );
    }

    
}
