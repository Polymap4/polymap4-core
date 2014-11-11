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

import java.util.ArrayList;
import java.util.Collection;

import junit.framework.TestCase;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import org.polymap.core.model2.runtime.EntityRepository;
import org.polymap.core.model2.runtime.UnitOfWork;
import org.polymap.core.model2.runtime.ValueInitializer;

/**
 * Test for complex models: associations, Composite properties, collections
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class ComplexModelTest
        extends TestCase {

    private static final Log log = LogFactory.getLog( ComplexModelTest.class );
    
    protected EntityRepository      repo;

    protected UnitOfWork            uow;
    

    public ComplexModelTest( String name ) {
        super( name );
    }

    protected void setUp() throws Exception {
        log.info( " --------------------------------------- " + getClass().getSimpleName() + " : " + getName() );
    }

    protected void tearDown() throws Exception {
        uow.close();
        repo.close();
    }


    public void testAssociation() {
        Company company = uow.createEntity( Company.class, null );
        Employee employee = uow.createEntity( Employee.class, null );
        company.chief.set( employee );
        
        Employee chief = company.chief.get();
        log.info( "chief: " + chief );
        assertNotNull( chief );
        
        uow.commit();

        UnitOfWork uow2 = repo.newUnitOfWork();
        Company company2 = uow2.entity( Company.class, company.id() );
        Employee chief2 = company2.chief.get();
        log.info( "chief2: " + chief2 );
        assertNotNull( chief2 );
    }
    

    public void testCompositeProperty() {
        Company company = uow.createEntity( Company.class, null );
        
        Address address = company.address.get();
        assertNull( address );
        
        address = company.address.createValue( new ValueInitializer<Address>() {
            public Address initialize( Address value ) throws Exception {
                value.street.set( "Jump" );
                value.nr.set( 1 );
                return value;
            }
        } );
        assertNotNull( address );
        log.info( "Address: " + address );
        assertEquals( "Jump", company.address.get().street.get() );
        assertEquals( 1, (int)company.address.get().nr.get() );

        uow.commit();

        UnitOfWork uow2 = repo.newUnitOfWork();
        Company company2 = uow2.entity( Company.class, company.id() );
        Address address2 = company2.address.get();
        assertNotNull( address2 );
        log.info( "Address: " + address2 );
        assertEquals( "Jump", company2.address.get().street.get() );
        assertEquals( 1, (int)company2.address.get().nr.get() );
    }

    
    public void testPrimitiveCollection() {
        Company company = uow.createEntity( Company.class, null );

        company.docs.add( "doc1" );
        log.info( "Company: " + company );
        assertEquals( 1, company.docs.size() );
        assertEquals( "doc1", Iterables.get( company.docs, 0 ) );
        
        // check equal()
        assertTrue( company.docs.equals( company.docs ) );
        ArrayList<String> copy = Lists.newArrayList( company.docs );
        assertTrue( company.docs.equals( copy ) );
        assertTrue( CollectionUtils.isEqualCollection( company.docs, copy ) );

        uow.commit();

        UnitOfWork uow2 = repo.newUnitOfWork();
        Company company2 = uow2.entity( Company.class, company.id() );
        Collection<String> docs = company2.docs;
        assertEquals( 1, docs.size() );
        assertEquals( "doc1", Iterables.get( docs, 0 ) );

        // equal()
        assertTrue( company.docs.equals( company2.docs ) );
//        assertEquals( copy, company2.docs );
//        assertEquals( company2.docs, copy );
    }
    
    
    public void testCompositeCollection() {
        Company company = uow.createEntity( Company.class, null );

        assertEquals( 0, company.moreAddresses.size() );

        Address address = company.moreAddresses.createElement( new ValueInitializer<Address>() {
            public Address initialize( Address value ) throws Exception {
                value.street.set( "Jump" );
                value.nr.set( 1 );
                return value;
            }
        } );
        log.info( "Company: " + company );
        log.info( "Address: " + address );
        assertEquals( 1, company.moreAddresses.size() );
        Address firstAddress = Iterables.get( company.moreAddresses, 0 );
        assertEquals( "Jump", firstAddress.street.get() );
        assertEquals( 1, (int)firstAddress.nr.get() );

        uow.commit();

        UnitOfWork uow2 = repo.newUnitOfWork();
        Company company2 = uow2.entity( Company.class, company.id() );
        assertEquals( 1, company2.moreAddresses.size() );
        Address firstAddress2 = Iterables.get( company2.moreAddresses, 0 );
        assertEquals( "Jump", firstAddress2.street.get() );
        assertEquals( 1, (int)firstAddress2.nr.get() );
    }
    
}
