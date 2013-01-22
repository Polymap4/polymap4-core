/* 
 * polymap.org
 * Copyright 2012, Falko Br�utigam. All rights reserved.
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
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
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
        log.info( "Initialializing Composite types:" );
        Queue<Class<? extends Composite>> queue = new LinkedList();
        queue.addAll( Arrays.asList( config.getEntities() ) );
        
        while (!queue.isEmpty()) {
            log.info( "    Composite type: " + queue.peek() );
            CompositeInfoImpl info = new CompositeInfoImpl( queue.poll() );
            infos.put( info.getType(), info );
            
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

    
    protected EntityRuntimeContext contextOfEntity( Entity entity ) {
        try {
            Field f = Composite.class.getDeclaredField( "context" );
            f.setAccessible( true );
            return (EntityRuntimeContext)f.get( entity );
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

//        private Class<? extends Entity> entityClass;
        
        private Entity                  entity;
        
        private CompositeState          state;
        
        private EntityStatus            status;
        
        private UnitOfWork              uow;

        
        EntityRuntimeContextImpl( CompositeState state, EntityStatus status, UnitOfWork uow ) {
            assert state != null;
            assert uow != null;
            assert status != null;
            
//            this.entityClass = entityClass;
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
        
        public CompositeInfo getInfo() {
            return getRepository().infoOf( entity.getClass() );
        }


        public UnitOfWork getUnitOfWork() {
            checkEviction();
            return uow;
        }

        @Override
        public StoreUnitOfWork getStoreUnitOfWork() {
            checkEviction();
            // XXX :( ???
            return ((UnitOfWorkImpl)uow).underlying;
        }


        public EntityRepository getRepository() {
            checkEviction();
            return EntityRepositoryImpl.this;
        }
        
        public CompositeState getState() {
            checkEviction();
            return state;
        }

        public EntityStatus getStatus() {
            checkEviction();
            return status;
        }

        public void raiseStatus( EntityStatus newStatus ) {
            assert newStatus.status >= status.status;
            // keep created if modified after creation
            if (status != EntityStatus.CREATED) {
                status = newStatus;
            }
            ((UnitOfWorkImpl)uow).raiseStatus( entity );
        }

        public void resetStatus( EntityStatus newStatus ) {
            checkEviction();
            this.status = newStatus;
        }


        public <T> T createMixin( Class<T> mixinClass ) {
            checkEviction();
            try {
                return new InstanceBuilder( this ).newMixin( mixinClass );
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
