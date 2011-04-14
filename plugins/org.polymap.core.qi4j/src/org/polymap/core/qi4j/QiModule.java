/* 
 * polymap.org
 * Copyright 2009, Polymap GmbH, and individual contributors as indicated
 * by the @authors tag.
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
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 * $Id$
 */

package org.polymap.core.qi4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.collections.SetUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.entity.Identity;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.query.grammar.BooleanExpression;
import org.qi4j.api.unitofwork.ConcurrentEntityModificationException;
import org.qi4j.api.unitofwork.EntityTypeNotFoundException;
import org.qi4j.api.unitofwork.NoSuchEntityException;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;

import org.eclipse.rwt.RWT;
import org.eclipse.rwt.internal.service.ContextProvider;
import org.eclipse.rwt.service.SessionStoreEvent;
import org.eclipse.rwt.service.SessionStoreListener;

import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.ListenerList;

import org.polymap.core.model.ACL;
import org.polymap.core.model.ACLUtils;
import org.polymap.core.model.AclPermission;
import org.polymap.core.model.CompletionException;
import org.polymap.core.model.ConcurrentModificationException;
import org.polymap.core.model.Entity;
import org.polymap.core.model.EntityType;
import org.polymap.core.model.GlobalModelChangeListener;
import org.polymap.core.model.ModelChangeEvent;
import org.polymap.core.model.ModelChangeListener;
import org.polymap.core.model.ModelChangeSet;
import org.polymap.core.model.Module;
import org.polymap.core.model.GlobalModelChangeEvent.EventType;
import org.polymap.core.workbench.PolymapWorkbench;

/**
 * Provides the implementation of a module of the Qi4J model. There is one module
 * instance per user session.
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
@SuppressWarnings("restriction")
public abstract class QiModule
        implements Module {

    private static Log log = LogFactory.getLog( QiModule.class );
    
    public static final int             DEFAULT_MAX_RESULTS = 10000000;  

    private static GlobalEntityVersions  globalEntityVersions = new GlobalEntityVersions(); 

    private static GlobalEntityChangeSets globalEntityChangeSets = new GlobalEntityChangeSets(); 

    private static GlobalEntityListeners globalEntityListeners = new GlobalEntityListeners();


    protected QiModuleAssembler         assembler;
    
    protected UnitOfWork                uow;

    protected ListenerList              propChangeListeners = new ListenerList( ListenerList.IDENTITY );
    
    private ListenerList                modelChangeListeners = new ListenerList( ListenerList.IDENTITY );
    
    private Map<Thread,ModelChangeEvent> operationEvents = new HashMap();

    private LinkedList<NestedChangeSet> changeSets = new LinkedList();
    
    
    protected QiModule( QiModuleAssembler assembler ) {
        this.assembler = assembler;
        this.uow = assembler.getModule().unitOfWorkFactory().newUnitOfWork();
        
        //log.warn( "Qi4JPlugin is not automatically removed from Global." );
        globalEntityChangeSets.registerModule( this );

        // for the global instance of the module (Qi4jPlugin.Session.globalInstance()) there
        // is no request context
        if (ContextProvider.hasContext()) {
            RWT.getSessionStore().addSessionStoreListener( new SessionStoreListener() {
                public void beforeDestroy( SessionStoreEvent ev ) {
                    log.info( "Session closed: removing module..."  );
                    done();
                }
            });
        }
        else {
            log.debug( "Module instantiated outside request context." );
        }
    }

    protected void done() {
        if (uow != null) {
            globalEntityChangeSets.unregisterModule( this );
            uow = null;
        }
    }
    
    public ModelChangeSet newChangeSet() {
        synchronized (changeSets) {
            changeSets.addLast( new NestedChangeSet( uow ) );
            return currentChangeSet();
        }
    }
    
    public ModelChangeSet currentChangeSet() {
        assert !changeSets.isEmpty() : "No current changeSet.";
        return changeSets.getLast();
    }


    /**
     * Discard the current change set. Call {@link #startOperation()} /
     * {@link #endOperation(boolean)} before/after this to ensure events are
     * fired.
     */
    public void discardChangeSet() {
        assert !changeSets.isEmpty();
        synchronized (changeSets) {
            NestedChangeSet changeSet = changeSets.removeLast();
            changeSet.discard();
        }        
    }
    
    public Iterable<ModelChangeSet> changeSets() {
        // for empty changes (likely) avoid synchronizing and copy
        if (changeSets.isEmpty()) {
            return ListUtils.EMPTY_LIST;
        }
        else {
            synchronized (changeSets) {
                return new ArrayList( changeSets );
            }
        }
    }
    
    
    public EntityState entityState( Entity entity ) {
        return new EntityState( entity );
    }

    /**
     * 
     *
     */
    public class EntityState {
        private Entity      entity;
        
        protected EntityState( Entity entity ) {
            this.entity = entity;
        }

        public boolean isLocallyChanged() {
            for (ModelChangeSet cs : changeSets()) {
                if (cs.hasChanges( entity.id() )) {
                    return true;
                }
            }
            return false;
        }
        
        public boolean isGloballyChanged() {
            return globalEntityChangeSets.isConcurrentlyChanged( entity, QiModule.this );            
        }
        
        public boolean isGloballyCommited() {
            return globalEntityVersions.isConcurrentlyCommited( entity );
        }
    }
    

    // commit / rollback ***

    public void commitChanges()
    throws ConcurrentModificationException, CompletionException {
        // check/update global entity versions
        Map<String,Entity> entities = null;
        try {
            entities = new HashMap();
            for (ModelChangeSet cs : changeSets()) {
                NestedChangeSet ncs = (NestedChangeSet)cs;
                entities.putAll( ncs.entities() );
            }
            globalEntityVersions.checkSetEntityVersions( entities.values() );
        }
        catch (Exception e) {
            if (e instanceof ConcurrentModificationException) {
                throw (ConcurrentModificationException)e;
            }
            else {
                // don't fail entire commit
                log.warn( e.getLocalizedMessage(), e );
                PolymapWorkbench.handleError( Qi4jPlugin.PLUGIN_ID, this, e.getLocalizedMessage(), e );
            }
        }
        
        try {
            // save changes
            uow.apply();

            // wipe change sets
            synchronized (changeSets) {
                changeSets.clear();
            }        

            // notify listeners
            if (entities != null) {
                globalEntityListeners.fireEvent( this, entities.keySet(), EventType.commit );
            }
        }
        catch (ConcurrentEntityModificationException e) {
            throw new ConcurrentModificationException( e );
        }
        catch (UnitOfWorkCompletionException e) {
            throw new CompletionException( e );
        }
    }
    

    public void discardChanges() {
//        // get all changed entities
//        Map<String,Entity> entities = new HashMap();
//        for (ModelChangeSet cs : changeSets()) {
//            NestedChangeSet ncs = (NestedChangeSet)cs;
//            entities.putAll( ncs.entities() );
//        }

        // discard change sets
        startOperation();
        synchronized (changeSets) {
            while (!changeSets.isEmpty()) {
                discardChangeSet();
            }
        }
        endOperation( true );
    }

    
    // events ***
    
    public void addPropertyChangeListener( PropertyChangeListener l ) {
        propChangeListeners.add( l );    
    }

    public void removePropertyChangeListener( PropertyChangeListener l ) {
        propChangeListeners.remove( l );
    }

    public void addModelChangeListener( ModelChangeListener l ) {
        modelChangeListeners.add( l );
    }

    public void removeModelChangeListener( ModelChangeListener l ) {
        modelChangeListeners.remove( l );
    }
    
    public void addGlobalModelChangeListener( GlobalModelChangeListener l ) {
        globalEntityListeners.add( l );
    }

    public void removeGlobalModelChangeListener( GlobalModelChangeListener l ) {
        globalEntityListeners.remove( l );
    }
    
    
    protected boolean appliesTo( org.qi4j.api.structure.Module rhs ) {
        return assembler.getModule().equals( rhs );
    }
    
    protected void fireChangeEvent( Object obj, String featureName, Object oldValue, Object newValue ) {
        // property change
        PropertyChangeEvent event = new PropertyChangeEvent( obj, featureName, oldValue, newValue ); 
        for (Object l : propChangeListeners.getListeners()) {
            ((PropertyChangeListener)l).propertyChange( event );
        }
        // model change
        ModelChangeEvent modelEvent = operationEvents.get( Thread.currentThread() );
        if (modelEvent == null) {
            // XXX
            //throw new IllegalStateException( "" );
        }
        else {
            modelEvent.addEvent( event );
        }
    }    

    /**
     * The method is called from {@link IUndoableOperation operations} before they start
     * to change the domain model. An operation is associated to the calling thread.
     */
    public synchronized void startOperation() {
        if (operationEvents.containsKey( Thread.currentThread() )) {
            throw new IllegalStateException( "Thread has started an operation already." );
        }
        ModelChangeEvent event = new ModelChangeEvent( this );
        operationEvents.put( Thread.currentThread(), event );
    }

    /**
     * The method is called from {@link IUndoableOperation operations} to complete an
     * operation. A {@link ModelChangeEvent} is fired to all registered listeners.
     * <p>
     * The changes are not persisted to the underlying store. This has to be triggered
     * via {@link #commitChanges()}.
     */
    public synchronized void endOperation( boolean fireEvents ) {
        ModelChangeEvent event = operationEvents.remove( Thread.currentThread() );
        if (event == null && fireEvents) {
            throw new IllegalStateException( "Thread has no operation associated." );
        }
        else if (event == null && !fireEvents) {
            log.warn( "Thread has no operation associated." );
        }
        else if (fireEvents) {
            // local
            for (Object l : modelChangeListeners.getListeners()) {
                ((ModelChangeListener)l).modelChanged( event );
            }
            // global
            if (!changeSets.isEmpty()) {
                NestedChangeSet ncs = (NestedChangeSet)currentChangeSet();
                Set<String> ids = ncs.ids();
                globalEntityListeners.fireEvent( this, ids, EventType.change );
            }
            else {
                globalEntityListeners.fireEvent( this, SetUtils.EMPTY_SET, EventType.change );
            }
        }
    }

    /**
     * Checks if the calling thread has an operation associated with it.
     */
    public boolean hasOperation() {
        return operationEvents.get( Thread.currentThread() ) != null;
    }
    
    /**
     *
     */
    protected void bindThread() {
        log.info( "no bindThread() currently." );
//        uow.pause();
//        uow.resume();
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
        ((NestedChangeSet)currentChangeSet()).compositeCreate( (Identity)result );
        return result;
    }

    /**
     *
     * @param entity
     */
    public void removeEntity( Entity entity ) {
        if (entity instanceof ACL) {
            ACLUtils.checkPermission( (ACL)entity, AclPermission.DELETE, true );
        }
        
        ModelChangeEvent ev = operationEvents.get( Thread.currentThread() );
        if (ev != null) {
            ev.addRemoved( entity.id() );
        }

        ((NestedChangeSet)currentChangeSet()).compositeRemove( (Identity)entity );
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
    public EntityType entityType( Class<? extends Entity> type ) {
        return new EntityTypeImpl( type );
    }
    
}
