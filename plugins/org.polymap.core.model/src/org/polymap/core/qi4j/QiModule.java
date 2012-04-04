/*
 * polymap.org
 * Copyright 2009-2011, Falko Bräutigam, and individual contributors as
 * indicated by the @authors tag. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */
package org.polymap.core.qi4j;

import java.util.EventObject;

import java.beans.PropertyChangeListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.query.grammar.BooleanExpression;
import org.qi4j.api.unitofwork.ConcurrentEntityModificationException;
import org.qi4j.api.unitofwork.EntityTypeNotFoundException;
import org.qi4j.api.unitofwork.NoSuchEntityException;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.unitofwork.UnitOfWorkException;
import org.qi4j.api.value.ValueBuilder;

import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.model.CompletionException;
import org.polymap.core.model.Composite;
import org.polymap.core.model.ConcurrentModificationException;
import org.polymap.core.model.Entity;
import org.polymap.core.model.EntityType;
import org.polymap.core.model.Module;
import org.polymap.core.model.event.ModelChangeTracker;
import org.polymap.core.model.event.ModelEventManager;
import org.polymap.core.model.event.IModelStoreListener;
import org.polymap.core.model.event.IModelChangeListener;
import org.polymap.core.model.event.IEventFilter;
import org.polymap.core.model.security.ACL;
import org.polymap.core.model.security.ACLUtils;
import org.polymap.core.model.security.AclPermission;
import org.polymap.core.operation.IOperationSaveListener;
import org.polymap.core.operation.OperationSupport;
import org.polymap.core.qi4j.event.PropertyChangeSupport;
import org.polymap.core.runtime.ISessionListener;
import org.polymap.core.runtime.SessionContext;
import org.polymap.core.workbench.PolymapWorkbench;

/**
 * Provides the implementation of a module of the Qi4J model. There is one
 * module instance per user session.
 * <p/>
 * QiModule implements {@link PropertyChangeListener} so that changes to
 * entities can be signaled to the module. This can be done inside an
 * {@link IUndoableOperation} or via the {@link PropertyChangeSupport} provided
 * by an entity.
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @since 3.0
 */
public abstract class QiModule
        implements Module {

    private static Log log = LogFactory.getLog( QiModule.class );

    public static final int                 DEFAULT_MAX_RESULTS = 10000000;


    // instance *******************************************

    protected QiModuleAssembler             assembler;

    protected UnitOfWork                    uow;


    protected QiModule( QiModuleAssembler assembler ) {
        this.assembler = assembler;
        this.uow = assembler.getModule().unitOfWorkFactory().newUnitOfWork();

        SessionContext sessionContext = SessionContext.current();
        if (sessionContext != null) {
            sessionContext.addSessionListener( new ISessionListener() {
                public void beforeDestroy() {
                    log.info( "Session closed: removing module..."  );
                    done();
                }
            });
        }
    }

    protected void done() {
        if (uow != null) {
            uow.discard();
            uow = null;
        }
    }

    /**
     * @see {@link ModelEventManager#addPropertyChangeListener(PropertyChangeListener, IEventFilter)}
     */
    public void addPropertyChangeListener( final PropertyChangeListener l, final IEventFilter f ) {
        ModelEventManager.instance().addPropertyChangeListener( l,
            new IEventFilter() {
                public boolean accept( EventObject ev ) {
                    QiEntity entity = (QiEntity)ev.getSource();
                    try {
                        // check if entity is part of this module
                        findEntity( entity.getCompositeType(), entity.id() );
                        return f.accept( ev );
                    }
                    catch (UnitOfWorkException e) {
                        return false;
                    }
                }
            });
    }

    public void removePropertyChangeListener( PropertyChangeListener l ) {
        ModelEventManager.instance().removePropertyChangeListener( l );
    }

    public void commitChanges()
    throws ConcurrentModificationException, CompletionException {
        try {
            // save changes
            uow.apply();
        }
        catch (ConcurrentEntityModificationException e) {
            throw new ConcurrentModificationException( e );
        }
        catch (UnitOfWorkCompletionException e) {
            throw new CompletionException( e );
        }
    }


    public void revertChanges() {
        throw new RuntimeException( "Not yet implemented." );

        // where do we get the list of changed entities?
        // then we could create a new UoW and get the old state from;
        // just creating new UoW does not send events and nothing gets updated
    }

    
    /**
     * 
     */
    public class OperationSaveListener
    implements IOperationSaveListener {

        public void prepareSave( OperationSupport os, IProgressMonitor monitor )
        throws Exception {
            //
        }

        public void save( OperationSupport os, IProgressMonitor monitor ) {
            try {
                monitor.beginTask( getClass().getSimpleName(), 1 );
                commitChanges();
            }
            catch (Exception e) {
                PolymapWorkbench.handleError( Qi4jPlugin.PLUGIN_ID, this, "Die Änderungen konnten nicht gespeichert werden.\nDie Daten sind möglicherweise in einem inkonsistenten Zustand.\nBitte verständigen Sie den Administrator.", e );
            }
        }

        public void rollback( OperationSupport os, IProgressMonitor monitor ) {
            // no prepare -> no rollback
        }

        public void revert( OperationSupport os, IProgressMonitor monitor ) {
            monitor.beginTask( getClass().getSimpleName(), 1 );
            revertChanges();
        }
    }
    

    // events ***

    /**
     * Register the given listener for the current session.
     * <p/>
     * All listeners are disposed when the session is destroyed.
     */
    public void addModelStoreListener( IModelStoreListener l ) {
        ModelChangeTracker.instance().addListener( l );
    }

    public void removeModelStoreListener( IModelStoreListener l ) {
        ModelChangeTracker.instance().removeListener( l );
    }

    public void addModelChangeListener( IModelChangeListener l, final IEventFilter f ) {
        ModelEventManager.instance().addModelChangeListener( l,
            new IEventFilter() {
                public boolean accept( EventObject ev ) {
                    if (ev.getSource() instanceof QiEntity) {
                        QiEntity entity = (QiEntity)ev.getSource();
                        try {
                            // check if entity is part of this module
                            findEntity( entity.getCompositeType(), entity.id() );
                            return f.accept( ev );
                        }
                        catch (UnitOfWorkException e) {
                            return false;
                        }
                    }
                    return false;
                }
            });
    }

    public void removeModelChangeListener( IModelChangeListener l ) {
        ModelEventManager.instance().removeModelChangeListener( l );
    }

    protected boolean appliesTo( org.qi4j.api.structure.Module rhs ) {
        return assembler.getModule().equals( rhs );
    }


    // factory methods ***

    /**
     *
     * @param <T>
     * @param type
     * @param id The id of the newly created entity; null specifies that the
     *        system creates a unique id.
     * @return The newly created entity.
     */
    public <T> T newEntity( Class<T> type, String id ) {
        T result = uow.newEntity( type, id );
        return result;
    }

    /**
     *
     * @param <T>
     * @param type
     * @param id
     * @param creator
     * @return The newly created entity
     * @throws Exception If an exception occured while executing the creator.
     */
    public <T> T newEntity( Class<T> type, String id, EntityCreator<T> creator )
    throws Exception {
        EntityBuilder<T> builder = uow.newEntityBuilder( type );
        creator.create( builder.instance() );
        return builder.newInstance();
    }

    /**
     * Functor for the {@link QiModule#newEntity(Class, String, EntityCreator)} method.
     */
    public interface EntityCreator<T> {

        public void create( T prototype )
        throws Exception;

    }

    public <T> ValueBuilder<T> newValueBuilder( Class<T> type ) {
        return assembler.getModule().valueBuilderFactory().newValueBuilder( type );
    }

    /**
     *
     * @param entity
     */
    public void removeEntity( Entity entity ) {
        if (entity instanceof ACL) {
            ACLUtils.checkPermission( (ACL)entity, AclPermission.DELETE, true );
        }

        // XXX add removed entities to current operation/ModelChangeEvent?
//        ModelChangeEvent ev = operationEvents.get( Thread.currentThread() );
//        if (ev != null) {
//            ev.addRemoved( entity.id() );
//        }
        uow.remove( entity );
    }

    /**
     * Update the state of the given entity. If the global state has changed
     * (entity was commited by another session), then the entity is loaded from
     * store. If the entity has locally changed, then these changes are merged
     * with the global changes. If one property was concurrently changed, then a
     * {@link ConcurrentModificationException} is thrown.
     */
    public void updateEntity( Entity entity )
            throws ConcurrentModificationException {
        throw new RuntimeException( "not yet implemented." );
    }

    /**
     * Find an Entity of the given mixin type with the give identity. This
     * method verifies that it exists by asking the underlying EntityStore.
     *
     * @return the entity
     * @throws EntityTypeNotFoundException If no entity type could be found
     * @throws NoSuchEntityException
     */
    public <T> T findEntity( Class<T> type, String id ) {
        return uow.get( type, id );
    }

    /**
     *
     * @param <T>
     * @param compositeType
     * @param expression The query, or null if all entities are to be fetched.
     * @param firstResult The first result index, 0 by default.
     * @param maxResults The maximum number of entities in the result; -1
     *        signals that there si no limit.
     * @return The newly created query.
     */
    public <T> Query<T> findEntities( Class<T> compositeType, BooleanExpression expression,
            int firstResult, int maxResults ) {
        if (maxResults < 0) {
            maxResults = DEFAULT_MAX_RESULTS;
        }
        if (maxResults > DEFAULT_MAX_RESULTS) {
            maxResults = DEFAULT_MAX_RESULTS;
        }

        QueryBuilder<T> builder = assembler.getModule()
                .queryBuilderFactory().newQueryBuilder( compositeType );

        builder = expression != null
                ? builder.where( expression )
                : builder;

        Query<T> query = builder.newQuery( uow )
                .maxResults( maxResults )
                .firstResult( firstResult );
        return query;
    }

    /**
     * Creates a new operation of the given type.
     * <p>
     * The caller must ensure to initialize the operation properly.
     *
     * @param type Class that extends {@link IUndoableOperation}.
     */
    public <T> T newOperation( Class<T> type ) {
        T result = assembler.getModule().transientBuilderFactory().newTransient( type );
        return result;
    }

    /**
     * Creates a new {@link EntityType} instance for the given {@link Entity}
     * class. The return value should be cached and reused if possible.
     */
    public <T extends Composite> EntityType<T> entityType( Class<T> type ) {
        return EntityTypeImpl.forClass( type );
    }

}
