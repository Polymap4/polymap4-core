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

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;
import static java.util.Collections.singletonList;
import static org.polymap.core.model2.runtime.EntityRuntimeContext.EntityStatus.CREATED;
import static org.polymap.core.model2.runtime.EntityRuntimeContext.EntityStatus.MODIFIED;

import java.util.Iterator;

import java.io.IOException;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;

import org.polymap.core.model2.Entity;
import org.polymap.core.model2.query.Query;
import org.polymap.core.model2.query.ResultSet;
import org.polymap.core.model2.query.grammar.BooleanExpression;
import org.polymap.core.model2.runtime.ConcurrentEntityModificationException;
import org.polymap.core.model2.runtime.EntityRuntimeContext.EntityStatus;
import org.polymap.core.model2.runtime.ModelRuntimeException;
import org.polymap.core.model2.runtime.UnitOfWork;
import org.polymap.core.model2.store.CloneCompositeStateSupport;
import org.polymap.core.model2.store.CompositeState;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class UnitOfWorkNested
        extends UnitOfWorkImpl {

    /** The parent UnitOfWork in case of nested instances, or null for the root UnitOfWork. */
    protected UnitOfWorkImpl        parent;

    
    protected UnitOfWorkNested( EntityRepositoryImpl repo, CloneCompositeStateSupport storeUow, UnitOfWorkImpl parent ) {
        super( repo, storeUow );
        this.parent = parent;
    }
    
    
    CloneCompositeStateSupport storeUow() {
        return (CloneCompositeStateSupport)storeUow;    
    }
    
    
    @Override
    public <T extends Entity> T entity( final Class<T> entityClass, final Object id ) {
        assert entityClass != null;
        assert id != null;
        checkOpen();
        T result = (T)loaded.get( id, new EntityCacheLoader() {
            public Entity load( Object key ) throws RuntimeException {
                // just clone the entire Entity and its state; copy-on-write would probably
                // be faster and less memory consuming but also would introduce a lot more complexity;
                // maybe I will later investigate a global copy-on-write cache for Entities
                T parentEntity = parent.entity( entityClass, id );
                CompositeState parentState = repo.contextOfEntity( parentEntity ).getState();
                
                CompositeState state = storeUow().cloneEntityState( parentState );
                return repo.buildEntity( state, entityClass, UnitOfWorkNested.this );
            }
        });
        return result.status() != EntityStatus.REMOVED ? result : null;
    }


    @Override
    public <T extends Entity> T entityForState( final Class<T> entityClass, Object state ) {
        throw new RuntimeException( "not yet implemented." );
//        checkOpen();
//        
//        final CompositeState compositeState = storeUow.adoptEntityState( state, entityClass );
//        final Object id = compositeState.id();
//        
//        return (T)loaded.get( id, new EntityCacheLoader() {
//            public Entity load( Object key ) throws RuntimeException {
//                return repo.buildEntity( compositeState, entityClass, UnitOfWorkNested.this );
//            }
//        });
    }

    
    @Override
    public void removeEntity( Entity entity ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    @Override
    public <T extends Entity> Query<T> query( final Class<T> entityClass ) {
        return new Query<T>( entityClass ) {
            public ResultSet<T> execute() {
                final ResultSet<T> parentRs = parent.query( entityClass )
                        .where( expression )
                        .maxResults( maxResults )
                        .firstResult( firstResult )
                        .execute();
                
                // adopt Entities
                Iterable<T> found = transform( parentRs, new Function<T,T>() {
                    public T apply( Entity parentEntity ) {
                        return entity( entityClass, parentEntity.id() ); 
                    }
                });

                // filter out modified/removed
                final Iterable<T> notModified = filter( found, new Predicate<T>() {
                    public boolean apply( T input ) {
                        EntityStatus status = input.status();
                        assert status != EntityStatus.CREATED; 
                        return status == EntityStatus.LOADED;
                    }
                });
                
                // new/updated states -> pre-process
                final Iterable<Entity> updated = filter( modified.values(), new Predicate<Entity>() {
                    public boolean apply( Entity entity ) {
                        if (entity.getClass().equals( entityClass ) && (entity.status() == CREATED || entity.status() == MODIFIED )) {
                            if (expression == null) {
                                return true;
                            }
                            else if (expression instanceof BooleanExpression) {
                                return ((BooleanExpression)expression).evaluate( entity );
                            }
                            else {
                                return storeUow.evaluate( entity.state(), expression );
                            }
                        }
                        return false;
                    }                    
                });

                return new ResultSet<T>() {
                    /** The cached size; not synchronized */
                    private int size = -1;

                    @Override
                    public Iterator<T> iterator() {
                        return (Iterator<T>)Iterators.concat( notModified.iterator(), updated.iterator() );
                    }
                    @Override
                    public int size() {
                        if (size == -1) {
                            // avoid iterating if no modifications
                            size = modified.isEmpty() ? parentRs.size() : Iterators.size( iterator() );
                        }
                        return size;
                    }
                };
            }
        };
    }


    @Override
    public UnitOfWork newUnitOfWork() {
        return new UnitOfWorkNested( repo, storeUow(), this );
    }


    @Override
    public void prepare() throws IOException, ConcurrentEntityModificationException {
        prepareResult = null;
        for (Entity entity : modified.values()) {
            // created
            if (entity.status() == EntityStatus.CREATED) {
                Entity previous = parent.modified.putIfAbsent( entity.id(), entity );
                assert previous != null;
            }
            // modified
            if (entity.status() == EntityStatus.MODIFIED
                    || entity.status() == EntityStatus.REMOVED) {
                Entity parentEntity = parent.entity( entity.getClass(), entity.id() );
                
                if (parentEntity == null || parentEntity.status() == EntityStatus.REMOVED) {
                    throw new ConcurrentEntityModificationException( "Entity was removed in parent UnitOfWork.", singletonList( entity ) );
                }

                repo.contextOfEntity( parentEntity ).raiseStatus( entity.status() );

                CompositeState parentState = repo.contextOfEntity( parentEntity ).getState();
                CompositeState clonedState = repo.contextOfEntity( entity ).getState();
                storeUow().reincorparateEntityState( parentState, clonedState );
            }
        }
        prepareResult = PREPARED;
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
        prepareResult = null;
        
        // reset Entity status
        for (Entity entity : loaded.values()) {
            repo.contextOfEntity( entity ).resetStatus( EntityStatus.LOADED );
        }
        modified.clear();
    }


    @Override
    public void rollback() throws ModelRuntimeException {
        prepareResult = null;
        loaded.clear();
        modified.clear();
    }


    public void close() {
        if (isOpen()) {
            parent = null;
        }
        super.close();
    }

}