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
package org.polymap.core.model2.engine;

import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.Collections2.transform;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import java.io.IOException;

import org.apache.commons.collections.collection.CompositeCollection;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;

import org.polymap.core.model2.Composite;
import org.polymap.core.model2.Entity;
import org.polymap.core.model2.runtime.ConcurrentEntityModificationException;
import org.polymap.core.model2.runtime.EntityRuntimeContext.EntityStatus;
import org.polymap.core.model2.runtime.ModelRuntimeException;
import org.polymap.core.model2.runtime.Query;
import org.polymap.core.model2.runtime.UnitOfWork;
import org.polymap.core.model2.runtime.ValueInitializer;
import org.polymap.core.model2.store.CompositeState;
import org.polymap.core.model2.store.StoreUnitOfWork;
import org.polymap.core.runtime.cache.Cache;
import org.polymap.core.runtime.cache.CacheLoader;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class UnitOfWorkImpl
        implements UnitOfWork {

    private static final Exception          PREPARED = new Exception( "Successfully prepared for commit." );
    
    private static AtomicInteger            idCount = new AtomicInteger( (int)System.currentTimeMillis() );
    
    protected EntityRepositoryImpl          repo;
    
    protected StoreUnitOfWork               delegate;

    protected Cache<Object,Entity>          loaded;
    
    protected Cache<String,Composite>       loadedMixins;
    
    /** Hard reference to Entities that must not be GCed from {@link #loaded} cache. */
    protected ConcurrentMap<Object,Entity>  modified;
    
    private volatile Exception              prepareResult;

    
//    /**
//     * 
//     */
//    static class CachedEntity
//            implements EvictionAware {
//        
//        Entity          entity;
//        
//        Object          evictableDummy = new Object();
//
//        public CachedEntity( Entity entity ) {
//            this.entity = entity;
//        }
//
//        public EvictionListener newListener() {
//            CachedEntityEvictionListener result = new CachedEntityEvictionListener();
//            result.entity = entity;
//            return result;
//        }
//        
//        static class CachedEntityEvictionListener
//                implements EvictionListener {
//            
//            Entity      entity;   
//
//            public void onEviction() {
//            }
//        }
//    }
    
    
    protected UnitOfWorkImpl( EntityRepositoryImpl repo, StoreUnitOfWork suow ) {
        this.delegate = suow;
        this.repo = repo;
        
        this.loaded = repo.getConfig().newCache();
        this.loadedMixins = repo.getConfig().newCache();
        this.modified = new ConcurrentHashMap( 1024, 0.75f, 4 );
        
//        // check evicted entries and re-insert if modified
//        this.loaded.addEvictionListener( new CacheEvictionListener<Object,Entity>() {
//            public void onEviction( Object key, Entity entity ) {
//                // re-insert if modified
//                if (entity.status() != EntityStatus.LOADED) {
//                    loaded.putIfAbsent( key, entity );
//                }
//                // mark entity as evicted otherwise
//                else {
//                    EntityRuntimeContext entityContext = UnitOfWorkImpl.this.repo.contextOfEntity( entity );
//                    entityContext.raiseStatus( EntityStatus.EVICTED );
//                }
//            }
//        });
    }

    
    /**
     * Raises the status of the given Entity. Called by {@link ConstraintsPropertyInterceptor}.
     */
    protected void raiseStatus( Entity entity) {
        if (entity.status() == EntityStatus.MODIFIED
                || entity.status() == EntityStatus.REMOVED) {
            modified.putIfAbsent( entity.id(), entity );
        }        
    }


    @Override
    public <T extends Entity> T createEntity( Class<T> entityClass, Object id, ValueInitializer<T> initializer ) {
        // build id; don't depend on store's ability to deliver id for newly created state
        id = id != null ? id : entityClass.getSimpleName() + "." + idCount.getAndIncrement();

        CompositeState state = delegate.newEntityState( id, entityClass );
        assert id == null || state.id().equals( id );
        
        T result = repo.buildEntity( state, entityClass, this );
        repo.contextOfEntity( result ).raiseStatus( EntityStatus.CREATED );

        Entity old = loaded.putIfAbsent( id, result );
        if (old != null) {
            throw new ModelRuntimeException( "ID of newly created Entity already exists." );
        }
        modified.put( id, result );
        
        // initializer
        try {
            if (initializer != null) {
                initializer.initialize( result );
            }
        }
        catch (Exception e) {
            throw new IllegalStateException( "Error while initializing.", e );
        }
        
        return result;
    }


    @Override
    public <T extends Entity> T entity( final Class<T> entityClass, final Object id ) {
        assert entityClass != null;
        assert id != null;
        checkOpen();
        return (T)loaded.get( id, new EntityCacheLoader() {
            public Entity load( Object key ) throws RuntimeException {
                CompositeState state = delegate.loadEntityState( id, entityClass );
                return repo.buildEntity( state, entityClass, UnitOfWorkImpl.this );
            }
        });
    }


    @Override
    public <T extends Entity> T entityForState( final Class<T> entityClass, Object state ) {
        checkOpen();
        
        final CompositeState compositeState = delegate.adoptEntityState( state, entityClass );
        final Object id = compositeState.id();
        
        return (T)loaded.get( id, new EntityCacheLoader() {
            public Entity load( Object key ) throws RuntimeException {
                result = repo.buildEntity( compositeState, entityClass, UnitOfWorkImpl.this );
                return result;
            }
        });
    }

    
    public <T extends Composite> T mixin( final Class<T> mixinClass, final Entity entity ) {
        assert mixinClass != null : "mixinClass must not be null.";
        assert entity != null : "entity must not be null.";
        checkOpen();
        
        String key = Joiner.on( '_' ).join( entity.id().toString(), mixinClass.getName() );
        return (T)loadedMixins.get( key, new MixinCacheLoader() {
            public Composite load( String _key ) throws RuntimeException {
                return repo.buildMixin( entity, mixinClass, UnitOfWorkImpl.this );
            }
        });
    }


    @Override
    public void removeEntity( Entity entity ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    @Override
    public <T extends Entity> Query<T> query( final Class<T> entityClass, Object expression ) {
        return new QueryImpl( entityClass, expression ) {
            public Collection execute() {
                // transform id -> Entity
                // XXX without this copy the collection is empty on second access
                // making a copy might be not that bad either as it avoid querying store for every iterator
                Collection ids = new ArrayList( delegate.find( this ) );
                Collection<T> found = transform( ids, new Function<Object,T>() {
                    public T apply( Object id ) {
                        return entity( entityClass, id ); 
                    }
                });
                // filter out modified/removed (avoid loading everything in memory)
                Collection<T> filtered = filter( found, new Predicate<T>() {
                    public boolean apply( T input ) {
                        EntityStatus status = input.status();
                        assert status != EntityStatus.CREATED; 
                        return status == EntityStatus.LOADED;
                    }
                });
                
                // new/updated states -> pre-process
                List<T> updated = new ArrayList();
                for (Entity entity : modified.values()) {
                    if (entity.getClass().equals( entityClass ) &&
                            (entity.status() == EntityStatus.CREATED || entity.status() == EntityStatus.MODIFIED )) {
                        if (expression == null || delegate.eval( entity.state(), expression )) {
                            updated.add( (T)entity );
                        }
                    }
                }

                // result: queried + created
                return new CompositeCollection( new Collection[] {filtered, updated} );
            }
        };
    }


    @Override
    public void prepare()
    throws IOException, ConcurrentEntityModificationException {
        try {
            prepareResult = null;
            delegate.prepareCommit( modified.values() );
            prepareResult = PREPARED;
        }
        catch (ModelRuntimeException e) {
            prepareResult = e;
            throw e;
        }
        catch (IOException e) {
            prepareResult = e;
            throw e;
        }
        catch (Exception e) {
            prepareResult = e;
            throw new ModelRuntimeException( e );
        }
    }


    @Override
    public void commit() throws ModelRuntimeException {
        // prepare if not yet done
        if (prepareResult == null) {
            try {
                prepare();
            }
            catch (ModelRuntimeException e) {
                throw e;
            }
            catch (Exception e) {
                throw new ModelRuntimeException( e );
            }
        }
        if (prepareResult != PREPARED) {
            throw new ModelRuntimeException( "UnitOfWork was not successfully prepared for commit." );
        }
        // commit store
        delegate.commit();
        prepareResult = null;
        
        // reset Entity status
        for (Entity entity : loaded.values()) {
            repo.contextOfEntity( entity ).resetStatus( EntityStatus.LOADED );
        }
        modified.clear();
    }


    @Override
    public void rollback() throws ModelRuntimeException {
        // commit store
        delegate.rollback();
        prepareResult = null;
        
        // reset Entity status
        loaded.clear();
        modified.clear();
    }


    public void close() {
        if (isOpen()) {
            delegate.close();
            repo = null;
            loaded.clear();
            loaded = null;
            modified.clear();
            modified = null;
        }
    }


    protected void finalize() throws Throwable {
        close();
    }


    public boolean isOpen() {
        return repo != null;
    }

    
    protected final void checkOpen() throws ModelRuntimeException {
        assert isOpen() : "UnitOfWork is closed.";
    }
    
    
    /**
     * 
     */
    abstract class EntityCacheLoader
            implements CacheLoader<Object,Entity,RuntimeException> {

        protected Entity        result;
        
        public int size() throws RuntimeException {
            // XXX rough approximation (count Composite props)
            return 1024;
//            return Math.max( 1024, result.info().getProperties().size() * 100 );
        }
    }

    
    /**
     * 
     */
    abstract class MixinCacheLoader
            implements CacheLoader<String,Composite,RuntimeException> {

        protected Composite     result;
        
        public int size() throws RuntimeException {
            // XXX rough approximation (count Composite props)
            return 1024;
//            return Math.max( 1024, result.info().getProperties().size() * 100 );
        }
    }
    
}