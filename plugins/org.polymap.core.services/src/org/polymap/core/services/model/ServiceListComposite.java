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
package org.polymap.core.services.model;

import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.model.AssocCollection;
import org.polymap.core.qi4j.AssocCollectionImpl;
import org.polymap.core.qi4j.QiEntity;
import org.polymap.core.qi4j.event.ModelChangeSupport;
import org.polymap.core.services.IProvidedService;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @since 3.0
 */
@Mixins( {
    ServiceListComposite.Mixin.class,
    ModelChangeSupport.Mixin.class,
    QiEntity.Mixin.class
} )
public interface ServiceListComposite
        extends QiEntity, EntityComposite {

    @Optional
    @UseDefaults
    ManyAssociation<IProvidedService>   services();

    
    public AssocCollection<IProvidedService> getServices();

    public boolean addService( IProvidedService service );
    
    public boolean removeService( IProvidedService service );
    

    /**
     * Transient fields and methods. 
     */
    public static abstract class Mixin
            implements ServiceListComposite {
        
        private static Log log = LogFactory.getLog( Mixin.class );

        @This ServiceListComposite          composite;
        

        public AssocCollection<IProvidedService> getServices() {
            return new AssocCollectionImpl( services() );
        }

        public boolean addService( IProvidedService service ) {
            return services().add( service );
        }
        
        public boolean removeService( IProvidedService service ) {
            return services().remove( service );
        }

    }
    
}
