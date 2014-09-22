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

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.model2.runtime.ConcurrentEntityModificationException;
import org.polymap.core.model2.runtime.EntityRepository;
import org.polymap.core.model2.runtime.UnitOfWork;
import org.polymap.core.model2.runtime.ValueInitializer;
import org.polymap.core.model2.store.OptimisticLocking;
import org.polymap.core.model2.store.recordstore.RecordStoreAdapter;
import org.polymap.core.runtime.recordstore.IRecordStore;
import org.polymap.core.runtime.recordstore.lucene.LuceneRecordStore;

/**
 * The {@link SimpleModelTest} with {@link IRecordStore}/Lucene backend.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ConcurrentTest
        extends TestCase {

    private static final Log log = LogFactory.getLog( ConcurrentTest.class );

    protected IRecordStore          store;

    
    public ConcurrentTest( String name ) {
        super( name );
    }


    public void testOptimistickLocking() throws Exception {
        store = new LuceneRecordStore();
        EntityRepository repo = EntityRepository.newConfiguration()
                .setStore( new OptimisticLocking( new RecordStoreAdapter( store ) ) )
                .setEntities( new Class[] {Employee.class} )
                .create();
      
        UnitOfWork uow1 = repo.newUnitOfWork();
        Employee employee = uow1.createEntity( Employee.class, null, new ValueInitializer<Employee>() {
            public Employee initialize( Employee prototype ) throws Exception {
                prototype.name.set( "samstag" );
                return prototype;
            }
        });
        uow1.commit();
        
        uow1 = repo.newUnitOfWork();
        Employee employee1 = uow1.entity( Employee.class, employee.id() );

        UnitOfWork uow2 = repo.newUnitOfWork();
        Employee employee2 = uow2.entity( Employee.class, employee.id() );
        
        try {
            employee1.name.set( "changed" );
            uow1.commit();

            employee2.name.set( "changed too" );
            uow2.commit();
            assertTrue( "No exception :(", false );
        }
        catch (ConcurrentEntityModificationException e) {
            // ok
        }
    }

}
