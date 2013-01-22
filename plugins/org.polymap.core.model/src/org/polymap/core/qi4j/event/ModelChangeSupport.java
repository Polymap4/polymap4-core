/* 
 * polymap.org
 * Copyright 2011, Falko Br�utigam, and other contributors as
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

import java.util.Collections;

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

import org.polymap.core.qi4j.QiEntity;
import org.polymap.core.runtime.entity.ConcurrentModificationException;
import org.polymap.core.runtime.entity.IEntityHandleable;
import org.polymap.core.runtime.entity.EntityStateTracker;
import org.polymap.core.runtime.entity.EntityHandle;
import org.polymap.core.runtime.entity.EntityStateTracker.Updater;

/**
 * ...
 * <p/>
 * Implements {@link UnitOfWorkCallback}. This seems to be the only way in Qi4j to
 * get informed about commit/rollback in the client code.
 * 
 * @see EntityStateTracker
 * @see EventManager
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 * @since 3.1
 */
public interface ModelChangeSupport
        extends IEntityHandleable, EntityComposite, QiEntity, UnitOfWorkCallback {

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

        static final Log log = LogFactory.getLog( ModelChangeSupport.Mixin.class );

        private static ThreadLocal<Updater> threadUpdater = new ThreadLocal();
        
        @This 
        private ModelChangeSupport          composite;
        
        private EntityHandle                 handle;
        
        
        public EntityHandle handle() {
            if (handle == null) {
                handle = EntityHandle.instance( id(), getEntityType().getName() );
            }
            return handle;
        }
        
        
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
                case NEW:
                case UPDATED:
                case REMOVED: {
                    log.debug( "UOW -- beforeCompletion(): updated/removed id=" + id() );

                    Updater updater = threadUpdater.get();
                    if (updater == null) {
                        updater = EntityStateTracker.instance().newUpdater();
                        threadUpdater.set( updater );
                    }

                    Long set = System.currentTimeMillis();
                    try {
                        EntityHandle key = EntityHandle.instance( 
                                composite.id(), composite.getEntityType().getName() );
                        // older versions have Integer
                        Number lastModified = composite._lastModified().get();
                        Long timestamp = lastModified != null
                                ? lastModified.longValue()
                                : null;
                        // newly created entities might not have an timestamp
                        timestamp = timestamp != null ? timestamp : set;
                        updater.checkSet( key, timestamp, set );
                    }
                    catch (ConcurrentModificationException e) {
                        // afterCompletion() might not be called after the exception
                        updater.done();
                        threadUpdater.set( null );
                        
                        throw new ConcurrentEntityStateModificationException(
                                Collections.singletonList( EntityReference.getEntityReference( composite ) ) );
                    }
                    catch (RuntimeException e) {
                        // afterCompletion() might not be called after the exception
                        updater.done();
                        threadUpdater.set( null );
                        
                        throw e;
                    }

                    // update lastModified
                    _lastModified().set( set );
                    //_lastModifiedBy().set( Polymap.instance().getUser().getName() );
                }
            }
        }

        
        public void afterCompletion( UnitOfWorkStatus status ) {
            Updater updater = threadUpdater.get();

            if (updater != null) {
                threadUpdater.set( null );
                log.debug( "UOW -- afterCompletion(): updater.size(): " + updater.size() );

                // completed
                if (status == UnitOfWorkStatus.COMPLETED) {
                    updater.apply( updater );
                }
                // discarded
                else {
                    // restore lastModified properties?
                }
                updater.done();
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
