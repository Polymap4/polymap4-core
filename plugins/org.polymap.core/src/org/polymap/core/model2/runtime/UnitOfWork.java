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

import java.io.IOException;

import javax.annotation.Nullable;

import org.polymap.core.model2.Entity;
import org.polymap.core.model2.engine.UnitOfWorkImpl;
import org.polymap.core.model2.runtime.EntityRuntimeContext.EntityStatus;

/**
 * A UnitOfWork helps to track entity modifications. A {@link UnitOfWork} should be
 * used to logically group operations that modify a set of entities. These
 * modifications can then be written down to the underlying store in one atomic
 * transaction.
 * <p/>
 * The UnitOfWork does not provide a notion of 'rolling back' aka rollback(). Before
 * {@link #prepare()}/{@link #commit()} all modifications are local to the
 * UnitOfWork. After {@link #commit()} all modifications are persitently stored. By
 * calling {@link #close()} the UnitOfWork can be *discarded* at any point in time.
 * There is no way to revert or 'taking back' modifications.
 * <p/>
 * Implementations should be thread-safe. In order to achieve this
 * {@link UnitOfWorkImpl} provides basic functions to track loaded entities.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface UnitOfWork {

    /**
     * Builds an {@link Entity} representation for the given state and assigns it
     * to this {@link UnitOfWork}.
     * 
     * @param entityClass The type of the entity to build.
     * @param state The state of the entity
     * @param <T> The type of the entity to build.
     * @return A newly created entity or a previously created instance.
     */
    public <T extends Entity> T entityForState( Class<T> entityClass, Object state );


    /**
     * Finds the {@link Entity} with the given type and identity.
     * 
     * @param entityClass The type of the entity to find.
     * @param id The identity of the entity to find.
     * @param <T> The type of the entity to build.
     * @return A newly created entity or a previously created instance. Returns null
     *         if no Entity exists for the given id.
     */
    public <T extends Entity> T entity( Class<T> entityClass, Object id );

//    public <T extends Composite> T mixin( Class<T> entityClass, Entity entity );


    /**
     * Creates a new state in the underlying store and builds an {@link Entity}
     * representation for it.
     * 
     * @param <T>
     * @param entityClass
     * @param id The identifier of the newly created entity, or null if a new
     *        identifier is to be created by the store automatically. Avoid using this
     *        as most backend stores do not support this!
     * @param initializer Allows to init properties, especially properties that must
     *        have a value because they are non-{@link Nullable} and does not a
     *        default value.
     * @return Newly created {@link Entity}.
     */
    public <T extends Entity> T createEntity( Class<T> entityClass, Object id, ValueInitializer<T> initializer );


    /**
     * Removes the given {@link Entity} from the underlying store. The
     * {@link Entity#status()} is set to {@link EntityStatus#REMOVED}.
     * 
     * @param entity The entity to remove.
     */
    public void removeEntity( Entity entity );


    /**
     * This methods allows a UnitOfWork to take part on a 2-phase commit protocol.
     * Calling this method sends all changes down to the underlying store but does
     * not commit the transaction. Client code does not have to use this method but
     * call {@link #commit()} directly.
     * <p/>
     * Client code must not call any other method than {@link #commit()} or
     * {@link #close()} after {@link #prepare()}.
     * <p/>
     * Backend stores probably open an external transaction between
     * {@link #prepare()} and {@link #commit()}. This probably aquires resources and
     * locks. It is important that {@link #commit()} and/or {@link #close()} is
     * called with a reasonable timeframe. Consider using some kind of transaction
     * monitor for this.
     * <p/>
     * If {@link #prepare()} fails and throws an exception then it has to clean any
     * transaction specific resources and locks before returning. Client code may use
     * this UnitOfWork after prepare failed to cure problems and try prepare/commit
     * again.
     * 
     * @throws IOException If the underlying store was not able to store all changes
     *         properly.
     * @throws ConcurrentEntityModificationException
     */
    public void prepare() throws IOException, ConcurrentEntityModificationException;


    /**
     * Persistently stores all modifications that were made within this UnitOfWork.
     * If {@link #prepare()} has not been called yet then it is done by this method.
     * <p/>
     * This does not invalidate this {@link UnitOfWork} but may flush internal
     * caches.
     * 
     * @throws ModelRuntimeException If {@link #prepare()} was called by this method
     *         and a exception occured.
     */
    public void commit() throws ModelRuntimeException;

    
    /**
     * Rollback any uncommitted changes but does not close this UnitOfWork.
     *
     * @throws ModelRuntimeException
     */
    public void rollback() throws ModelRuntimeException;


    /**
     * Closes this UnitOfWork by releasing all resources associated with this
     * UnitOfWork. All uncommitted modifications are discarded.
     * <p/>
     * No method should be called after closing the UnitOfWork except for
     * {@link #isOpen()}.
     */
    public void close();
    
    public boolean isOpen();

    
    /**
     * 
     * 
     * @param entityClass
     * @param expression Null indicates that all entities of the given type should be
     *        returned when executing the query.
     * @return Newly created {@link Query} instance that first allows to set
     *         ordering, indexes and then executing the query against the data store.
     */
    public <T extends Entity> Query<T> query( Class<T> entityClass, Object expression );

}
