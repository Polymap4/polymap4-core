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
package org.polymap.core.model2.engine;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import java.lang.reflect.Field;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.model2.Composite;
import org.polymap.core.model2.Entity;
import org.polymap.core.model2.runtime.CompositeInfo;
import org.polymap.core.model2.runtime.EntityRepository;
import org.polymap.core.model2.runtime.EntityRepositoryConfiguration;
import org.polymap.core.model2.runtime.EntityRuntimeContext;
import org.polymap.core.model2.runtime.ModelRuntimeException;
import org.polymap.core.model2.runtime.PropertyInfo;
import org.polymap.core.model2.runtime.UnitOfWork;
import org.polymap.core.model2.runtime.EntityRuntimeContext.EntityStatus;
import org.polymap.core.model2.store.CompositeState;
import org.polymap.core.model2.store.StoreRuntimeContext;
import org.polymap.core.model2.store.StoreSPI;
import org.polymap.core.model2.store.StoreUnitOfWork;
import org.polymap.core.runtime.cache.Cache;

/**
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class EntityRepositoryImpl
        extends EntityRepository {

    private static Log log = LogFactory.getLog( EntityRepositoryImpl.class );

    private EntityRepositoryConfiguration               config;
    
    /** Infos of Entities, Mixins, Composite properties. */
    private Map<Class<? extends Composite>,CompositeInfo> infos = new HashMap();
    
    
    public EntityRepositoryImpl( final EntityRepositoryConfiguration config ) {
        this.config = config;
        
        // init store
        getStore().init( new StoreRuntimeContextImpl() );
        
        // init infos
        log.debug( "Initialializing Composite types:" );
        Queue<Class<? extends Composite>> queue = new LinkedList();
        queue.addAll( Arrays.asList( config.getEntities() ) );
        
        while (!queue.isEmpty()) {
            Class<? extends Composite> type = queue.poll();
            if (!infos.containsKey( type )) {
                log.debug( "    Composite type: " + queue.peek() );
                CompositeInfoImpl info = new CompositeInfoImpl( type );
                infos.put( type, info );

                // mixins
                queue.addAll( info.getMixins() );

                // Composite properties
                for (PropertyInfo propInfo : info.getProperties()) {
                    if (Composite.class.isAssignableFrom( propInfo.getType() )) {
                        queue.offer( propInfo.getType() );
                    }
                }
            }
        }
    }

    
    public StoreSPI getStore() {
        return config.getStore();
    }

    
    public EntityRepositoryConfiguration getConfig() {
        return config;
    }

    
    public void close() {
    }

    @Override
    public <T extends Composite> CompositeInfo infoOf( Class<T> compositeClass ) {
        return infos.get( compositeClass );
    }
    
    @Override    
    public UnitOfWork newUnitOfWork() {
        return new UnitOfWorkImpl( this, getStore().createUnitOfWork() );
    }
    
    
    protected <T extends Entity> T buildEntity( CompositeState state, Class<T> entityClass, UnitOfWork uow ) {
        try {
            EntityRuntimeContextImpl entityContext = new EntityRuntimeContextImpl( 
                    state, EntityStatus.LOADED, uow );
            InstanceBuilder builder = new InstanceBuilder( entityContext );
            T result = builder.newComposite( state, entityClass );
            entityContext.entity = result;
            return result;
        }
        catch (RuntimeException e) {
            throw e;
        }
        catch (Exception e) {
            throw new ModelRuntimeException( e );
        }
    }

    
    protected <T extends Composite> T buildMixin( Entity entity, Class<T> mixinClass, UnitOfWork uow ) {
        try {
            EntityRuntimeContextImpl entityContext = contextOfEntity( entity );
            InstanceBuilder builder = new InstanceBuilder( entityContext );
            return builder.newComposite( entityContext.getState(), mixinClass );
        }
        catch (RuntimeException e) {
            throw e;
        }
        catch (Exception e) {
            throw new ModelRuntimeException( e );
        }
    }

    
    protected <T extends EntityRuntimeContext> T contextOfEntity( Entity entity ) {
        try {
            Field f = Composite.class.getDeclaredField( "context" );
            f.setAccessible( true );
            return (T)f.get( entity );
        }
        catch (RuntimeException e) {
            throw e;
        }
        catch (Exception e) {
            throw new ModelRuntimeException( e );
        }
    }
    
    
    /**
     * 
     */
    protected final class StoreRuntimeContextImpl
            implements StoreRuntimeContext {

        public EntityRepositoryImpl getRepository() {
            return EntityRepositoryImpl.this;
        }


        public EntityRuntimeContext contextOfEntity( Entity entity ) {
            return EntityRepositoryImpl.this.contextOfEntity( entity );
        }

    }


    /**
     * 
     */
    protected class EntityRuntimeContextImpl
            implements EntityRuntimeContext {

        private Entity                  entity;
        
        private CompositeState          state;
        
        private EntityStatus            status;
        
        private UnitOfWork              uow;

        
        EntityRuntimeContextImpl( CompositeState state, EntityStatus status, UnitOfWork uow ) {
            assert state != null;
            assert uow != null;
            assert status != null;
            
            this.state = state;
            this.status = status;
            this.uow = uow;
        }


        /**
         * For some {@link Cache} implementations used by {@link UnitOfWorkImpl} it
         * is possible that cache entries are evicted while there are still
         * references on it. This may lead to a situation where modifications are not
         * recognized, hence lost updates. This check makes sure that an Exception is
         * thrown at least.
         */
        protected void checkEviction() {
            if (status == EntityStatus.EVICTED) {
                // XXX I realy don't know what to do here
                throw new IllegalStateException( "Entity is evicted: " + state );
            }
        }
        
        @Override
        public CompositeInfo getInfo() {
            return getRepository().infoOf( entity.getClass() );
        }

        @Override
        public UnitOfWork getUnitOfWork() {
            checkEviction();
            return uow;
        }

        @Override
        public StoreUnitOfWork getStoreUnitOfWork() {
            checkEviction();
            // XXX :( ???
            return ((UnitOfWorkImpl)uow).delegate;
        }

        @Override
        public EntityRepository getRepository() {
            checkEviction();
            return EntityRepositoryImpl.this;
        }
        
        @Override
        public CompositeState getState() {
            checkEviction();
            return state;
        }

        @Override
        public EntityStatus getStatus() {
            checkEviction();
            return status;
        }

        @Override
        public void raiseStatus( EntityStatus newStatus ) {
            assert newStatus.status >= status.status;
            // keep created if modified after creation
            if (status != EntityStatus.CREATED) {
                status = newStatus;
            }
            ((UnitOfWorkImpl)uow).raiseStatus( entity );
        }

        @Override
        public void resetStatus( EntityStatus newStatus ) {
            checkEviction();
            this.status = newStatus;
        }

        @Override
        public <T extends Composite> T getCompositePart( Class<T> type ) {
            if (type.isAssignableFrom( entity.getClass() )) {
                return (T)entity;
            }
            else {
                throw new RuntimeException( "Retrieving mixin parts is not yet implemented." );
            }
        }

        @Override
        public void methodProlog( String methodName, Object[] args ) {
            // XXX Auto-generated method stub
            throw new RuntimeException( "not yet implemented." );
        }
    }
    
}
