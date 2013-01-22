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

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import java.io.IOException;

import com.google.common.base.Function;
import static com.google.common.collect.Collections2.*;

import org.polymap.core.model2.Entity;
import org.polymap.core.model2.runtime.ConcurrentEntityModificationException;
import org.polymap.core.model2.runtime.ModelRuntimeException;
import org.polymap.core.model2.runtime.UnitOfWork;
import org.polymap.core.model2.runtime.ValueInitializer;
import org.polymap.core.model2.runtime.EntityRuntimeContext.EntityStatus;
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
    
    private static volatile long            idCount = 0;
    
    protected EntityRepositoryImpl          repo;
    
    protected StoreUnitOfWork               underlying;

    protected Cache<Object,Entity>          loaded;
    
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
        this.underlying = suow;
        this.repo = repo;
        
        // cache
        this.loaded = repo.getConfig().newCache();
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


    protected void raiseStatus( Entity entity) {
        if (entity.status() == EntityStatus.MODIFIED
                || entity.status() == EntityStatus.REMOVED) {
            modified.putIfAbsent( entity.id(), entity );
        }        
    }


    @Override
    public <T extends Entity> T createEntity( Class<T> entityClass, Object id, ValueInitializer<T> initializer ) {
        if (initializer != null) {
            throw new RuntimeException( "CompositeCreator is not yet supported." );
        }
        CompositeState state = underlying.newEntityState( id, entityClass );
        assert id == null || state.id().equals( id );

        // build fake id; don't depend on store's ability to deliver
        // id for newly created state
        id = id != null ? id : entityClass.getSimpleName() + "." + idCount++;
        
        T result = repo.buildEntity( state, entityClass, this );
        repo.contextOfEntity( result ).raiseStatus( EntityStatus.CREATED );

        Entity old = loaded.putIfAbsent( id, result );
        if (old != null) {
            throw new ModelRuntimeException( "ID of newly created Entity already exists." );
        }
        modified.put( id, result );
        return result;
    }


    @Override
    public <T extends Entity> T entity( final Class<T> entityClass, final Object id ) {
        checkOpen();
        return (T)loaded.get( id, new EntityCacheLoader() {
            public Entity load( Object key ) throws RuntimeException {
                CompositeState state = underlying.loadEntityState( id, entityClass );
                result = repo.buildEntity( state, entityClass, UnitOfWorkImpl.this );
                return result;
            }
        });
    }


    @Override
    public <T extends Entity> T entityForState( final Class<T> entityClass, Object state ) {
        checkOpen();
        
        final CompositeState compositeState = underlying.adoptEntityState( state, entityClass );
        final Object id = compositeState.id();
        
        return (T)loaded.get( id, new EntityCacheLoader() {
            public Entity load( Object key ) throws RuntimeException {
                result = repo.buildEntity( compositeState, entityClass, UnitOfWorkImpl.this );
                return result;
            }
        });
    }

    
    @Override
    public void removeEntity( Entity entity ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    @Override
    public <T extends Entity> Collection<T> find( final Class<T> entityClass ) {
        return transform( underlying.find( entityClass ), new Function<Object,T>() {
            public T apply( Object key ) {
                return entity( entityClass, key ); 
            }
        });
    }

    
    @Override
    public void prepare()
    throws IOException, ConcurrentEntityModificationException {
        try {
            prepareResult = null;
            underlying.prepareCommit( modified.values() );
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
        underlying.commit();
        prepareResult = null;
        
        // reset Entity status
        for (Entity entity : loaded.values()) {
            repo.contextOfEntity( entity ).resetStatus( EntityStatus.LOADED );
        }
        modified.clear();
    }


    public void close() {
        if (isOpen()) {
            underlying.close();
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
            return Math.max( 1024, result.info().getProperties().size() * 100 );
        }
    }
}