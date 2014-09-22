/* 
 * polymap.org
 * Copyright (C) 2014, Falko Bräutigam. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.core.model2.store;

import static java.util.Collections.singletonList;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.model2.Entity;
import org.polymap.core.model2.query.Query;
import org.polymap.core.model2.runtime.ConcurrentEntityModificationException;
import org.polymap.core.model2.runtime.EntityRuntimeContext.EntityStatus;

/**
 * Provides a decorator for an underlying store. This decorator implements a simple
 * check for concurrent modifications. The check fails on
 * {@link StoreUnitOfWork#prepareCommit(Iterable)}.
 * <p/>
 * This implementation holds all versions in memory and never checks the underlying
 * store for concurrent modifications. So the check is fast but the table of versions
 * grows with the number of modified entities. This implementation does not detect
 * modification of the underlying store by a second party.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class OptimisticLocking
        implements StoreSPI {

    private static Log log = LogFactory.getLog( OptimisticLocking.class );
    
    private StoreRuntimeContext             context;
    
    private StoreSPI                        store;
    
    private ConcurrentMap<Object,Integer>   storeVersions = new ConcurrentHashMap( 256, 0.75f, 4 );

    
    public OptimisticLocking( StoreSPI store ) {
        this.store = store;
    }

    @Override
    public void init( @SuppressWarnings("hiding") StoreRuntimeContext context ) {
        store.init( context );
        this.context = context;
    }

    @Override
    public void close() {
        store.close();
    }

    @Override
    public Object stateId( Object state ) {
        return store.stateId( state );
    }

    @Override
    public StoreUnitOfWork createUnitOfWork() {
        StoreUnitOfWork suow = store.createUnitOfWork();
        return suow instanceof CloneCompositeStateSupport
                ? new OptimisticLockingSuow2( suow )
                : new OptimisticLockingSuow( suow );
    }


    /**
     * 
     */
    class OptimisticLockingSuow
            implements StoreUnitOfWork {
    
        protected StoreUnitOfWork               suow;

        protected ConcurrentMap<Object,Integer> loadedVersions = new ConcurrentHashMap( 256, 0.75f, 2 );

        
        public OptimisticLockingSuow( StoreUnitOfWork suow ) {
            this.suow = suow;
        }

        @Override
        public void prepareCommit( Iterable<Entity> loaded ) throws Exception {
            // check versions
            for (Entity entity : loaded) {
                if (entity.status() == EntityStatus.MODIFIED || entity.status() == EntityStatus.REMOVED) {
                    Integer loadedVersion = loadedVersions.get( entity.id() );
                    Integer newVersion = loadedVersion == null 
                            ? new Integer( 1 ) 
                            : new Integer( loadedVersion.intValue() + 1 );
                            
                    Integer storeVersion = storeVersions.put( entity.id(), newVersion );
                    if (storeVersion != loadedVersion) {
                        storeVersions.put( entity.id(), storeVersion );
                        throw new ConcurrentEntityModificationException( "Entity has been modified by another UnitOfWork: " + entity.id(), singletonList( entity ) );
                    }
                }
            }
            //
            suow.prepareCommit( loaded );
        }

        @Override
        public <T extends Entity> CompositeState loadEntityState( Object id, Class<T> entityClass ) {
            CompositeState result = suow.loadEntityState( id, entityClass );
            Integer version = storeVersions.get( id );
            if (version != null) {
                loadedVersions.put( id, version );
            }
            return result;
        }

        @Override
        public <T extends Entity> CompositeState adoptEntityState( Object state, Class<T> entityClass ) {
            CompositeState result = suow.adoptEntityState( state, entityClass );
            Integer version = storeVersions.get( result.id() );
            if (version != null) {
                loadedVersions.put( result.id(), version );
            }
            return result;
        }

        @Override
        public <T extends Entity> CompositeState newEntityState( Object id, Class<T> entityClass ) {
            return suow.newEntityState( id, entityClass );
        }

        @Override
        public Collection<Object> executeQuery( Query query ) {
            return suow.executeQuery( query );
        }

        @Override
        public boolean evaluate( Object entityState, Object expression ) {
            return suow.evaluate( entityState, expression );
        }

        @Override
        public void commit() {
            suow.commit();
        }

        @Override
        public void close() {
            suow.close();
        }

        @Override
        public void rollback() {
            suow.rollback();
        }
    }
        
    
    /**
     * 
     */
    class OptimisticLockingSuow2
            extends OptimisticLockingSuow
            implements CloneCompositeStateSupport {

        public OptimisticLockingSuow2( StoreUnitOfWork suow ) {
            super( suow );
        }

        protected CloneCompositeStateSupport suow() {
            return (CloneCompositeStateSupport)suow;
        }
        
        @Override
        public CompositeState cloneEntityState( CompositeState state ) {
            return suow().cloneEntityState( state ); 
        }

        @Override
        public void reincorparateEntityState( CompositeState state, CompositeState clonedState ) {
            suow().reincorparateEntityState( state, clonedState );
        }
        
    }
    
}
