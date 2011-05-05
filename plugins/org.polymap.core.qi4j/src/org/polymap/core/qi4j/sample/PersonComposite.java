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

import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.runtime.entity.EntityInstance;
import org.qi4j.spi.Qi4jSPI;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 */
//@SideEffects( 
//        ParentSideEffect.class 
//)
//@Concerns( 
//        PropertyChangeConcern.class
//)
@Mixins( {
        PersonComposite.Mixin.class,
        PersonMixin.class, 
        Labeled.Mixin.class, 
        PersonParentMixin.class
//        EntityMixin.class
} )
public interface PersonComposite
        extends EntityComposite, Person, Labeled, PersonParent { //, Entity {

    public long lastModified();
    
//    public String toString();

    abstract class Mixin
            implements PersonComposite {
        
//        @Structure
//        private Qi4jSPI         qi4j;
        
        @This
        private PersonComposite composite;
        
        public long lastModified() {
            return EntityInstance.getEntityInstance( composite ).entityState().lastModified();
//            return qi4j.getEntityState( composite ).lastModified();
        }
    }
    
}
