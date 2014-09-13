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

import static org.polymap.core.model2.query.Expressions.and;
import static org.polymap.core.model2.query.Expressions.eq;
import static org.polymap.core.model2.query.Expressions.matches;
import static org.polymap.core.model2.query.Expressions.not;
import static org.polymap.core.model2.query.Expressions.notEq;
import static org.polymap.core.model2.query.Expressions.or;
import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Iterables;

import org.polymap.core.model2.query.Expressions;
import org.polymap.core.model2.query.ResultSet;
import org.polymap.core.model2.query.grammar.BooleanExpression;
import org.polymap.core.model2.runtime.EntityRepository;
import org.polymap.core.model2.runtime.UnitOfWork;
import org.polymap.core.model2.runtime.ValueInitializer;
import org.polymap.core.model2.test.Employee.Rating;

/**
 * Test for simple model queries.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class SimpleQueryTest
        extends TestCase {

    private static final Log log = LogFactory.getLog( SimpleQueryTest.class );

    protected EntityRepository      repo;

    protected UnitOfWork            uow;
    

    public SimpleQueryTest( String name ) {
        super( name );
    }

    protected void setUp() throws Exception {
        log.info( " --------------------------------------- " + getClass().getSimpleName() + " : " + getName() );
    }

    protected void tearDown() throws Exception {
        uow.close();
        repo.close();
    }


    public void testCommitted() throws Exception {
        createEntities();
        uow.commit();
        doQueries();
    }

    
    /**
     * Query uncommitted changes which used the {@link BooleanExpression} implementations.
     */
    public void testUncommitted() throws Exception {
        createEntities();
        doQueries();
    }

    
    protected void createEntities() {
        uow.createEntity( Employee.class, null, new ValueInitializer<Employee>() {
            public Employee initialize( Employee proto ) throws Exception {
                proto.firstname.set( "Ulli" );
                proto.name.set( "Philipp" );
                proto.rating.set( Rating.good );
                return proto;
            }
        });
        uow.createEntity( Employee.class, null, new ValueInitializer<Employee>() {
            public Employee initialize( Employee proto ) throws Exception {
                proto.firstname.set( "AZ" );
                proto.name.set( "Zimmermann" );
                return proto;
            }
        });        
    }
    
    
    protected void doQueries() {
        // all
        ResultSet<Employee> rs = uow.query( Employee.class ).execute();
        assertEquals( 2, rs.size() );
        assertEquals( 2, Iterables.size( rs ) );

        // String property
        Employee wanted = Expressions.template( Employee.class, repo );
        rs = uow.query( Employee.class ).where( eq( wanted.firstname, "Ulli" ) ).execute();
        assertEquals( 1, rs.size() );
        assertEquals( 1, Iterables.size( rs ) );

        // matches
        rs = uow.query( Employee.class ).where( matches( wanted.firstname, "Ul*" ) ).execute();
        assertEquals( 1, rs.size() );
        assertEquals( 1, Iterables.size( rs ) );

        // matches
        rs = uow.query( Employee.class ).where( matches( wanted.firstname, "Ull?" ) ).execute();
        assertEquals( 1, rs.size() );
        assertEquals( 1, Iterables.size( rs ) );

        // Enum property
        rs = uow.query( Employee.class ).where( eq( wanted.rating, Rating.good ) ).execute();
        assertEquals( 1, rs.size() );
        assertEquals( 1, Iterables.size( rs ) );

        // and
        rs = uow.query( Employee.class )
                .where( and( eq( wanted.firstname, "Ulli" ), eq( wanted.name, "Philipp" ) ) )
                .execute();
        assertEquals( 1, rs.size() );
        assertEquals( 1, Iterables.size( rs ) );

        // or
        rs = uow.query( Employee.class )
                .where( or( eq( wanted.firstname, "Ulli" ), eq( wanted.name, "Zimmermann" ) ) )
                .execute();
        assertEquals( 2, rs.size() );
        assertEquals( 2, Iterables.size( rs ) );

        // and notEq
        rs = uow.query( Employee.class )
                .where( and( eq( wanted.firstname, "Ulli" ), notEq( wanted.name, "Zimmermann" ) ) )
                .execute();
        assertEquals( 1, rs.size() );
        assertEquals( 1, Iterables.size( rs ) );
        
        // and not eq
        rs = uow.query( Employee.class )
                .where( and( eq( wanted.firstname, "Ulli" ), not( eq( wanted.name, "Zimmermann" ) ) ) )
                .execute();
        assertEquals( 1, rs.size() );
        assertEquals( 1, Iterables.size( rs ) );
    }
    
}
