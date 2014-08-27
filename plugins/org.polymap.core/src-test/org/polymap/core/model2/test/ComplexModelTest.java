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

import org.polymap.core.model2.runtime.EntityRepository;
import org.polymap.core.model2.runtime.UnitOfWork;

/**
 * Test for complex models: associations, Composite properties
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
        Company company = uow.createEntity( Company.class, null, null );
        Employee employee = uow.createEntity( Employee.class, null, null );
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
    
}
