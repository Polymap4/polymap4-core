/* 
 * polymap.org
 * Copyright (C) 2012-2014, Falko Bräutigam. All rights reserved.
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

import com.google.common.collect.Iterables;
import org.polymap.core.model2.query.ResultSet;
import org.polymap.core.model2.runtime.EntityRepository;
import org.polymap.core.model2.runtime.UnitOfWork;
import org.polymap.core.model2.runtime.ValueInitializer;

/**
 * Test for simple models: no associations, no Composite properties
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class NestedUowTest
        extends TestCase {

    private static final Log log = LogFactory.getLog( NestedUowTest.class );

    // instance *******************************************
    
    protected EntityRepository      repo;

    protected UnitOfWork            uow;
    

    public NestedUowTest( String name ) {
        super( name );
    }

    protected void setUp() throws Exception {
        log.info( " --------------------------------------- " + getClass().getSimpleName() + " : " + getName() );
    }

    protected void tearDown() throws Exception {
        uow.close();
        repo.close();
    }


    public void testModification() throws Exception {
        Employee employee = uow.createEntity( Employee.class, null, new ValueInitializer<Employee>() {
            public Employee initialize( Employee prototype ) throws Exception {
                prototype.name.set( "name" );
                return prototype;
            }
        });
        
        UnitOfWork nested = uow.newUnitOfWork();
        
        // check nested
        Employee employee2 = nested.entity( Employee.class, employee.id() );
        assertEquals( "name", employee2.name.get() );

        // modify nested
        employee2.name.set( "nested" );
        assertEquals( "nested", employee2.name.get() );
        
        // check parent
        assertEquals( "name", employee.name.get() );
        Employee employee3 = uow.entity( Employee.class, employee.id() );
        assertEquals( "name", employee3.name.get() );
        
        // commit nested
        nested.commit();

        // check parent again
        assertEquals( "nested", employee3.name.get() );        
    }
    

    public void testQuery() throws Exception {
        Employee parentEmployee = uow.createEntity( Employee.class, null, new ValueInitializer<Employee>() {
            public Employee initialize( Employee prototype ) throws Exception {
                prototype.name.set( "name" ); return prototype;
            }
        });
        
        UnitOfWork nested = uow.newUnitOfWork();
        
        nested.createEntity( Employee.class, null, new ValueInitializer<Employee>() {
            public Employee initialize( Employee prototype ) throws Exception {
                prototype.name.set( "name2" ); return prototype;
            }
        });

        // check nested
        ResultSet<Employee> rs = nested.query( Employee.class ).execute();
        assertEquals( 2, rs.size() );
        assertEquals( 2, Iterables.size( rs ) );
        
        // check if entities are adopted
        for (Employee found : rs) {
            assertNotSame( found, parentEmployee );
        }
        
        // check parent
        ResultSet<Employee> rs2 = uow.query( Employee.class ).execute();
        assertEquals( 1, rs2.size() );
        assertEquals( 1, Iterables.size( rs2 ) );        
    }
    
}
