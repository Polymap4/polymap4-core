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

import java.lang.reflect.Method;

import org.qi4j.api.concern.Concerns;
import org.qi4j.api.concern.GenericConcern;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.SingletonAssembler;
import org.qi4j.test.EntityTestAssembler;


/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class ToString {
    
    public static void main( String[] args) {
       new ToString().testEquals();    
    }
    
    
    public void testEquals() {
        SingletonAssembler assembler = new SingletonAssembler() {

            public void assemble( ModuleAssembly moduleAssembly )
                    throws AssemblyException {
                moduleAssembly.addEntities( EntityTest.class );
                moduleAssembly.addEntities( PersonComposite.class );
                new EntityTestAssembler().assemble( moduleAssembly );
            }
        };

        String id = "123";
        UnitOfWork uow = assembler.unitOfWorkFactory().newUnitOfWork();
        
        EntityTest entity = uow.newEntity( EntityTest.class, id );
        System.out.println( ": " + entity.toString() );

//        Person person = uow.newEntity( Person.class, id );
//        System.out.println( ": " + person.toString() );
        uow.discard();
    }
    

    @Concerns({TestConcern.class})
    interface EntityTest
            extends EntityComposite, MixinInterface {
    }


    @Mixins(MixinInterface.Mixin.class)
    interface MixinInterface {

        void helloWorld();

        class Mixin
                implements MixinInterface {

            public void helloWorld() {
            }

            public String toString() {
                return super.toString();
            }
        }
    }
    
    
    public static class TestConcern
            extends GenericConcern {

        @This EntityComposite   composite;
        
        public Object invoke( Object proxy, Method method, Object[] args )
                throws Throwable {
            System.out.println( "method: " + method.getName() );
            return next.invoke( proxy, method, args );
        }        
    }
}
