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

import java.util.Collection;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.model2.runtime.EntityRepository;
import org.polymap.core.model2.runtime.ModelRuntimeException;
import org.polymap.core.model2.runtime.UnitOfWork;
import org.polymap.core.runtime.Timer;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class SimpleModelTest
        extends TestCase {

    private static final Log log = LogFactory.getLog( SimpleModelTest.class );
    
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


    public void testCreateEmployee() throws Exception {    
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
            employee.nonNullable.get(); } catch (Exception e) { thrown = e; }        
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
        Collection<Employee> results = uow2.find( Employee.class );
        assertEquals( 11, results.size() );

        int previousJap = -1;
        for (Employee employee : results) {
            int jap = employee.jap.get();
            assertTrue( jap >= 0 && jap <= results.size() && previousJap != jap );
        }
    }

    
    public void tstPerformance() throws Exception {
        logHeap();
        Timer timer = new Timer();
        int loops = 50000;
        for (int i=0; i<loops; i++) {
            Employee employee = uow.createEntity( Employee.class, null, null );
            employee.jap.set( i );
        }
        log.info( "Employees created: " + loops + " in " + timer.elapsedTime() + "ms" );
        logHeap();

        // commit
        timer.start();
        uow.commit();
        log.info( "Commit time: " + timer.elapsedTime() + "ms" );
        logHeap();
        
        // load
        timer.start();
        UnitOfWork uow2 = repo.newUnitOfWork();
        Collection<Employee> results = uow2.find( Employee.class );

        for (Employee employee : results) {
            int jap = employee.jap.get();
            assert jap >= 0 && jap <= results.size() : "jap = " + jap + ", result.size() = " + results.size();
        }
        log.info( "Load time: " + timer.elapsedTime() + "ms" );
        logHeap();
    }

    
    protected void logHeap() {
        System.gc();
        Runtime rt = Runtime.getRuntime();
        log.info( "HEAP: free/total: " + 
                FileUtils.byteCountToDisplaySize( rt.freeMemory() ) + " / " + 
                FileUtils.byteCountToDisplaySize( rt.totalMemory() ) );
    }

}
