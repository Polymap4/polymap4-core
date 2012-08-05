/* 
 * polymap.org
 * Copyright 2012, Falko Bräutigam. All rights reserved.
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
package org.polymap.core.model2.runtime;

import java.util.ArrayList;
import java.util.List;

import java.lang.reflect.Field;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.model2.Entity;
import org.polymap.core.model2.engine.InstanceBuilder;
import org.polymap.core.model2.runtime.EntityRuntimeContext.EntityStatus;
import org.polymap.core.model2.store.StoreRuntimeContext;
import org.polymap.core.model2.store.StoreSPI;

/**
 * 
 * <p/>
 * One repository is backed by exactly one underlying store. Client may decide to
 * work with different repositories and their {@link UnitOfWork} instances. It is
 * responsible of synchronizing commit/rollback between those instances.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class EntityRepository {

    private static Log log = LogFactory.getLog( EntityRepository.class );

    // config factory *************************************
    
    public static EntityRepositoryConfiguration newConfiguration() {
        return new EntityRepositoryConfiguration();
    }
    
    // instance *******************************************
    
    private EntityRepositoryConfiguration   config;
    
    private List<Class>                     entityClasses = new ArrayList();
    
    
    public EntityRepository( final EntityRepositoryConfiguration config ) {
        this.config = config;
        
        getStore().init( new StoreRuntimeContext() {
            
            public EntityRepository getRepository() {
                return EntityRepository.this;
            }
            
            public EntityRuntimeContext contextForEntity( Entity entity ) {
                try {
                    Field f = Entity.class.getDeclaredField( "context" );
                    f.setAccessible( true );
                    return (EntityRuntimeContext)f.get( entity );
                }
                catch (Exception e) {
                    throw new RuntimeException( e );
                }
            }
            
            public <T extends Entity> T buildEntity( Object id, Object state, Class<T> entityClass, UnitOfWork uow ) {
                try {
                    return new InstanceBuilder( new EntityRuntimeContextImpl( 
                            state, EntityStatus.LOADED, uow ), config.getStore() )
                            .newEntity( entityClass );
                }
                catch (Exception e) {
                    throw new ModelRuntimeException( e );
                }
            }
        });
    }

    
    public StoreSPI getStore() {
        return config.getStore();
    }

    
    public EntityRepositoryConfiguration getConfig() {
        return config;
    }

    
    public void close() {
    }
    
    
//    /**
//     * Builds an {@link Entity} that is not assigned to any {@link UnitOfWork}. The
//     * resulting entity can be used for reading only. The state can be assigned to an
//     * UnitOfWork via {@link UnitOfWork#entity(Object, Class)} and afterwards make
//     * changes in order to persistently save changes.
//     * 
//     * @param <T>
//     * @param state
//     * @param entityClass
//     * @return A newly created {@link Entity} for the given state.
//     */
//    public <T extends Entity> T entity( Object id, Object state, Class<T> entityClass ) {
//        try {
//            return new InstanceBuilder( 
//                    new EntityRuntimeContextImpl( id, state, EntityStatus.LOADED ), config.getStore() )
//                    .newEntity( entityClass );
//        }
//        catch (Exception e) {
//            throw new ModelRuntimeException( e );
//        }
//    }
    
    
    public UnitOfWork newUnitOfWork() {
        return config.store.createUnitOfWork();
    }
    
    
    /**
     * 
     */
    final class EntityRuntimeContextImpl
            implements EntityRuntimeContext {

        private Object              id;
        
        private Object              state;
        
        private EntityStatus        status;
        
        private UnitOfWork          uow;

        
        EntityRuntimeContextImpl( Object state, EntityStatus status, UnitOfWork uow ) {
            assert state != null;
            assert uow != null;
            assert status != null;
            
            this.state = state;
            this.status = status;
            this.uow = uow;
        }

        public Object id() {
            return config.getStore().stateId( state );
        }

        public UnitOfWork unitOfWork() {
            return uow;
        }

        public Object state() {
            return state;
        }

        public EntityStatus status() {
            return status;
        }

        public void raiseStatus( EntityStatus newStatus ) {
            assert newStatus.status >= status.status;
            // keep created if modified
            if (status != EntityStatus.CREATED) {
                status = newStatus;
            }
        }

        public <T> T createMixin( Class<T> mixinClass ) {
            try {
                return new InstanceBuilder( this, config.getStore() )
                        .newMixin( mixinClass );
            }
            catch (Exception e) {
                throw new ModelRuntimeException( e );
            }
        }

        public void methodProlog( String methodName, Object[] args ) {
            // XXX Auto-generated method stub
            throw new RuntimeException( "not yet implemented." );
        }
    }
    
}
