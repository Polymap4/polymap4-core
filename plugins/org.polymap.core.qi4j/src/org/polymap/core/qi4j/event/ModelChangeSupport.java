/* 
 * polymap.org
 * Copyright 2011, Falko Bräutigam, and other contributors as
 * indicated by the @authors tag. All rights reserved.
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
package org.polymap.core.qi4j.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.property.Property;
import org.qi4j.api.unitofwork.UnitOfWorkCallback;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.runtime.entity.EntityInstance;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entitystore.ConcurrentEntityStateModificationException;

import org.polymap.core.model.Entity;
import org.polymap.core.runtime.Polymap;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @since 3.1
 */
public interface ModelChangeSupport
        extends EntityComposite, Entity, UnitOfWorkCallback {

    static final Log log = LogFactory.getLog( ModelChangeSupport.class );

    @Optional
    Property<Long>          _lastModified();
    
    @Optional
    Property<String>        _lastModifiedBy();


    /**
     * True if this entity has been locally changed but not yet committed or
     * discarded.
     */
    boolean isDirty();

    
    /**
     * Implementation and transient members.
     */
    abstract static class Mixin
            implements ModelChangeSupport {

        @This 
        private ModelChangeSupport          composite;
        
        private static ThreadLocal<List<ModelChangeSupport>> updated = new ThreadLocal();
    
        
        public boolean isDirty() {
            // avoid the injected reference to save memory
            EntityState entityState = EntityInstance.getEntityInstance( composite ).entityState();
            return entityState.status() == EntityStatus.UPDATED
                    || entityState.status() == EntityStatus.NEW
                    || entityState.status() == EntityStatus.REMOVED;
        }
        
        
        public void beforeCompletion()
        throws UnitOfWorkCompletionException {
            // avoid the injected reference to save memory
            EntityState entityState = EntityInstance.getEntityInstance( composite ).entityState();
            //return qi4j.getEntityState( composite ).lastModified();
            
            // hope that Qi4J lets run just one UoW completion at once; otherwise we have
            // race consitions between check and set of lastModified property between the
            // threads
            
            switch (entityState.status()) {
                case UPDATED:
                case REMOVED: {
                    log.info( "UOW -- beforeCompletion(): updated/removed id=" + id() );

                    List<ModelChangeSupport> updatedEntities = updated.get();
                    if (updatedEntities == null) {
                        updatedEntities = new ArrayList( 128 );
                    }
                    updatedEntities.add( composite );
                    
                    // check concurrent modification
                    if (ModelChangeTracker.instance().isConcurrentlyCommitted( composite )) {
                        throw new ConcurrentEntityStateModificationException(
                                Collections.singletonList( EntityReference.getEntityReference( composite ) ) );
                    }
                    // update lastModified
                    _lastModified().set( System.currentTimeMillis() );
                    _lastModifiedBy().set( Polymap.instance().getUser().getName() );
                }
            }
        }

        
        public void afterCompletion( UnitOfWorkStatus status ) {
            List<ModelChangeSupport> updatedEntities = updated.get();
            log.info( "UOW -- afterCompletion(): updatedEntities= " + updatedEntities != null ? updatedEntities.size() : "null" );

            if (updatedEntities != null) {
                updated.set( null );
                
                // completed
                if (status == UnitOfWorkStatus.COMPLETED) {
                    ModelChangeTracker.instance().updateVersions( updatedEntities );
                }
                // discarded
                else {
                    // restore lastModified properties?
                }
            }
        }
        
        
        public long lastModified() {
            Number result = _lastModified().get();
            return result != null ? result.longValue() : 0L;
        }
        
        
        public String lastModifiedBy() {
            String result = _lastModifiedBy().get();
            return result;
        }
        
    }
    
}
