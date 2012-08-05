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

import com.google.common.base.Function;
import static com.google.common.collect.Collections2.*;

import org.polymap.core.model2.Entity;
import org.polymap.core.model2.runtime.EntityCreator;
import org.polymap.core.model2.runtime.EntityRepository;
import org.polymap.core.model2.runtime.ModelRuntimeException;
import org.polymap.core.model2.runtime.UnitOfWork;
import org.polymap.core.model2.runtime.EntityRuntimeContext.EntityStatus;
import org.polymap.core.model2.store.StoreRuntimeContext;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class UnitOfWorkImpl
        implements UnitOfWork {

    protected StoreRuntimeContext           context;
    
    protected EntityRepository              repo;

    protected ConcurrentMap<Object,Entity>  loaded = new ConcurrentHashMap( 1024, 0.75f, 4 );

    
    protected UnitOfWorkImpl( StoreRuntimeContext context ) {
        this.context = context;
        this.repo = context.getRepository();
    }


    protected abstract Object stateId( Object state );

    protected abstract <T extends Entity> Object loadState( Object id, Class<T> entityClass );

    protected abstract <T extends Entity> Object newState( Object id, Class<T> entityClass );

    protected abstract <T extends Entity> Collection findStates( Class<T> entityClass );
    
    
    protected <T extends Entity> T getOrCreateEntity( Class<T> entityClass, Object state, Object id ) {
        assert id != null;
        Entity result = loaded.get( id );
        if (result == null) {
            result = context.buildEntity( id, state, entityClass, this );
            Entity old = loaded.putIfAbsent( id, result );
            result = old != null ? old : result;
        }
        return (T)result;
    }


    public <T extends Entity> T newEntity( Class<T> entityClass, Object id, EntityCreator<T> creator ) {
        if (creator != null) {
            throw new RuntimeException( "EntityCreator is not yet supported." );
        }
        Object state = newState( id, entityClass );
        
        // build fake id; don't depend on store's ability to deliver
        // id for newly created state
        id = id != null ? id : entityClass.getSimpleName() + "." + state.hashCode();
        
        T result = getOrCreateEntity( entityClass, state, id );
        context.contextForEntity( result ).raiseStatus( EntityStatus.CREATED );
        return result;
    }


    public <T extends Entity> T entity( Class<T> entityClass, Object id ) {
        checkOpen();
        Entity result = loaded.get( id );
        if (result == null) {
            Object state = loadState( id, entityClass );
            if (state != null) {
                result = context.buildEntity( id, state, entityClass, this );
                Entity old = loaded.putIfAbsent( id, result );
                result = old != null ? old : result;
            }
        }
        return (T)result;
    }


    @Override
    public <T extends Entity> T entityForState( Class<T> entityClass, Object state ) {
        checkOpen();
        Object id = stateId( state );
        return getOrCreateEntity( entityClass, state, id );
    }

    
    public <T extends Entity> Collection<T> find( final Class<T> entityClass ) {
        return transform( findStates( entityClass ), new Function<Object,Entity>() {
            public Entity apply( Object state ) {
                return entityForState( entityClass, state );
            }
        });
    }

    
    public void close() {
        if (isOpen()) {
            repo = null;
            loaded.clear();
            loaded = null;
        }
    }


    public boolean isOpen() {
        return repo != null;
    }

    
    protected final void checkOpen() throws ModelRuntimeException {
        assert isOpen() : "UnitOfWork is closed.";
    }
    
}