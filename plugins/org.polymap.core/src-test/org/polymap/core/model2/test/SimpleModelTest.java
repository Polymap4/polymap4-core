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

import java.util.Collection;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.model2.runtime.EntityRepository;
import org.polymap.core.model2.runtime.ModelRuntimeException;
import org.polymap.core.model2.runtime.UnitOfWork;

/**
 * Test for simple models: no associations, no Composite properties
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class SimpleModelTest
        extends TestCase {

    private static final Log log = LogFactory.getLog( SimpleModelTest.class );

    static {
        System.setProperty( "org.apache.commons.logging.simplelog.defaultlog", "debug" );
//        System.setProperty( "org.apache.commons.logging.simplelog.log.org.polymap.core.model2", "debug" );
//        System.setProperty( "org.apache.commons.logging.simplelog.log.org.polymap.core.model2.store.feature", "debug" );
//        System.setProperty( "org.apache.commons.logging.simplelog.log.org.polymap.core.runtime.recordstore", "debug" );
//        System.setProperty( "org.apache.commons.logging.simplelog.log.org.polymap.core.runtime.recordstore.lucene", "debug" );
//        System.setProperty( "org.apache.commons.logging.simplelog.log.org.polymap.core.data.feature.recordstore", "debug" );
    }

    // instance *******************************************
    
    protected EntityRepository      repo;

    protected UnitOfWork            uow;
    

    public SimpleModelTest( String name ) {
        super( name );
    }

    protected void setUp() throws Exception {
        log.info( " --------------------------------------- " + getClass().getSimpleName() + " : " + getName() );
    }

    protected void tearDown() throws Exception {
        uow.close();
        repo.close();
    }


    public void testProperties() throws Exception {    
        Employee employee = uow.createEntity( Employee.class, null, null );
        log.info( "Employee: id=" + employee.id() );
//        assertEquals( employee.id(), "employee1" );
        log.info( "Employee: name=" + employee.name.get() );
        assertNull( employee.name.get() );
        // default value
        log.info( "Employee: firstname=" + employee.firstname.get() );
        assertEquals( employee.firstname.get(), "Ulli" );
        // set name
        employee.name.set( "Philipp" );
        log.info( "Employee: name=" + employee.name.get() );
        assertEquals( employee.name.get(), "Philipp" );

        // commit
        log.info( "### COMMIT ###" );
        uow.commit();

        // re-read
        log.info( "Employee: id=" + employee.id() );
        Employee employee2 = uow.entityForState( Employee.class, employee.state() );
        log.info( "Employee: name=" + employee2.name.get() );
        assertEquals( "Philipp", employee2.name.get() );
        log.info( "Employee: firstname=" + employee2.firstname.get() );
        assertEquals( "Ulli", employee2.firstname.get() );

        Employee employee3 = uow.entity( Employee.class, employee.id() );
        log.info( "Employee: name=" + employee3.name.get() );

        // modify
        employee2.firstname.set( "Ulrike" );
        log.info( "Employee: firstname=" + employee2.firstname.get() );
        assertEquals( "Ulrike", employee2.firstname.get() );
        employee2.jap.set( 100 );
        
        // commit
        log.info( "### COMMIT ###" );
        uow.commit();
    }
    
    
    public void testDefaults() throws Exception {
        Employee employee = uow.createEntity( Employee.class, null, null );
        assertEquals( "", employee.defaultString.get() );        
        assertEquals( 0, (int)employee.jap.get() );
        assertEquals( "Ulli", employee.firstname.get() );
    }
    

    public void testNullable() throws Exception {
        Employee employee = uow.createEntity( Employee.class, null, null );
        Exception thrown = null;
        try { 
            employee.jap.set( null ); 
        } 
        catch (Exception e) { thrown = e; }        
        assertTrue( thrown instanceof ModelRuntimeException );
        
        try { 
            employee.nonNullable.get(); 
        } 
        catch (Exception e) { thrown = e; }        
        assertTrue( thrown instanceof ModelRuntimeException );
    }
    
    
    public void testCreateEmployees() throws Exception {
        // loop count greater than default query size of RecordStore
        for (int i=0; i<11; i++) {
            Employee employee = uow.createEntity( Employee.class, null, null );
            employee.jap.set( i );
        }
        // commit
        log.info( "### COMMIT ###" );
        uow.commit();
        
        // check
        UnitOfWork uow2 = repo.newUnitOfWork();
        Collection<Employee> results = uow2.query( Employee.class, null ).execute();
        assertEquals( 11, results.size() );

        int previousJap = -1;
        for (Employee employee : results) {
            int jap = employee.jap.get();
            assertTrue( jap >= 0 && jap <= results.size() && previousJap != jap );
        }
    }

    
    public void testMixinComputed() throws Exception {
        Employee employee = uow.createEntity( Employee.class, null, null );
        
        TrackableMixin trackable = employee.as( TrackableMixin.class );
        assertNotNull( trackable );
        assertSame( employee.state(), trackable.state() );

        trackable = employee.as( TrackableMixin.class );
        assertNotNull( trackable );
        assertSame( employee.state(), trackable.state() );
        
        trackable.track.set( 10 );
        log.info( "Computed property: " + trackable.computed.get() );
        assertTrue( trackable.computed.get().endsWith( "computed" ) );
        
        // commit
        uow.commit();
        
        // check
        UnitOfWork uow2 = repo.newUnitOfWork();
        employee = uow2.entityForState( Employee.class, employee.state() );
        trackable = employee.as( TrackableMixin.class );
        assertNotNull( trackable );
        assertEquals( 10, (int)trackable.track.get() );
    }
    
    
    public void testConcern() throws Exception {
        Employee employee = uow.createEntity( Employee.class, null, null );

        int getCount = InvocationCountConcern.getCount.get();
        int setCount = InvocationCountConcern.setCount.get();
        
        employee.name.set( "Mufu" );
        employee.name.get();
        assertEquals( getCount+1, InvocationCountConcern.getCount.get() );        
        assertEquals( setCount+1, InvocationCountConcern.setCount.get() );        

        employee.firstname.set( "Mufu" );
        employee.firstname.get();
        assertEquals( getCount+1, InvocationCountConcern.getCount.get() );        
        assertEquals( setCount+1, InvocationCountConcern.setCount.get() );        

        // Employee does not have LogConcern
        employee.jap.set( 1 );
        employee.jap.get();
        assertEquals( getCount+1, InvocationCountConcern.getCount.get() );        
        assertEquals( setCount+1, InvocationCountConcern.setCount.get() );        
    }
    
}
