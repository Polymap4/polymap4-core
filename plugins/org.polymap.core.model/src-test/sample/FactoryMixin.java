/* 
 * polymap.org
 * Copyright 2009, Polymap GmbH, and individual contributors as indicated
 * by the @authors tag.
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
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 * $Id$
 */

package org.polymap.core.qi4j.sample;

import java.util.Iterator;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.query.QueryBuilderFactory;
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;


/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class FactoryMixin
        implements Factory {
    
    @Structure UnitOfWorkFactory    uowf;
    
    @Structure Module               module;
    
    
    public Person createPerson( String id, String label ) {
        UnitOfWork uow = uowf.currentUnitOfWork();
        EntityBuilder<Person> builder = uow.newEntityBuilder( Person.class, id );

        Person prototype = builder.instance();
        prototype.setLabel( label );
        //prototype.model().set( model );

        Person result = builder.newInstance();
        
//        Person proxy = (Person)Proxy.newProxyInstance( 
//                Person.class.getClassLoader(), 
//                new Class[] { Person.class },
//                new ToStringHandler( result ) );
        return result;    
    }

    class ToStringHandler
            implements InvocationHandler {

        Person delegate;
        
        public ToStringHandler( Person delegate ) {
            super();
            this.delegate = delegate;
        }

        public Object invoke( Object proxy, Method method, Object[] args )
                throws Throwable {
            System.out.println( "Proxy: method=" + method.getName() );
            if (method.getName().equals( "toString" )) {
                return "Person[";
            }
            else {
                return method.invoke( delegate, args );
            }
        }
        
    }
    
    public Person findPersonByLabel( String label ) {
        UnitOfWork uow = uowf.currentUnitOfWork();
        
        QueryBuilderFactory qbf = module.queryBuilderFactory();
        QueryBuilder<PersonComposite> builder = qbf.newQueryBuilder( PersonComposite.class );
        
        Person template = QueryExpressions.templateFor( Person.class );
        builder.where( QueryExpressions.isNull( template.label() ) );
        Query<PersonComposite> query = builder.newQuery( uow );
        
        Iterator<PersonComposite> it = query.iterator();
        if (it.hasNext()) {
            return query.iterator().next();
        }
        return null;
    }

    public Person findPersonById( String id ) {
        UnitOfWork uow = uowf.currentUnitOfWork();

        QueryBuilderFactory qbf = module.queryBuilderFactory();
        QueryBuilder<PersonComposite> builder = qbf.newQueryBuilder( PersonComposite.class );

        PersonComposite template = QueryExpressions.templateFor( PersonComposite.class );
        builder.where( QueryExpressions.eq( template.identity(), id ) );
        Query<PersonComposite> query = builder.newQuery( uow );

        Iterator<PersonComposite> it = query.iterator();
        if (it.hasNext()) {
            return query.iterator().next();
        }
        return null;
    }

}
