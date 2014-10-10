/* 
 * polymap.org
 * Copyright (C) 2014, Falko Bräutigam. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.core.model2.store;

import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.model2.Entity;
import org.polymap.core.model2.query.Query;
import org.polymap.core.model2.store.CompositeState;
import org.polymap.core.model2.store.StoreRuntimeContext;
import org.polymap.core.model2.store.StoreSPI;
import org.polymap.core.model2.store.StoreUnitOfWork;

/**
 * Provides a no-op decorator for an underlying store. 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class StoreDecorator
        implements StoreSPI {

    private static Log log = LogFactory.getLog( StoreDecorator.class );
    
    protected StoreSPI                  store;

    
    public StoreDecorator( StoreSPI delegate ) {
        this.store = delegate;
    }

    @Override
    public void init( StoreRuntimeContext context ) {
        store.init( context );
    }

    @Override
    public void close() {
        store.close();
    }

    @Override
    public Object stateId( Object state ) {
        return store.stateId( state );
    }

    @Override
    public StoreUnitOfWork createUnitOfWork() {
        return store.createUnitOfWork();
    }


    /**
     * 
     */
    protected abstract class UnitOfWorkDecorator
            implements StoreUnitOfWork {
        
        protected StoreUnitOfWork       suow;

        public UnitOfWorkDecorator( StoreUnitOfWork suow ) {
            this.suow = suow;
        }

        public void prepareCommit( Iterable<Entity> loaded ) throws Exception {
            suow.prepareCommit( loaded );
        }

        public <T extends Entity> CompositeState loadEntityState( Object id, Class<T> entityClass ) {
            return suow.loadEntityState( id, entityClass );
        }

        public <T extends Entity> CompositeState adoptEntityState( Object state, Class<T> entityClass ) {
            return suow.adoptEntityState( state, entityClass );
        }

        public <T extends Entity> CompositeState newEntityState( Object id, Class<T> entityClass ) {
            return suow.newEntityState( id, entityClass );
        }

        public Collection<Object> executeQuery( Query query ) {
            return suow.executeQuery( query );
        }

        public boolean evaluate( Object entityState, Object expression ) {
            return suow.evaluate( entityState, expression );
        }

        public void commit() {
            suow.commit();
        }

        public void close() {
            suow.close();
        }

        public void rollback() {
            suow.rollback();
        }
        
    }

    
    /**
     * 
     */
    protected abstract class UnitOfWorkDecorator2
            extends UnitOfWorkDecorator
            implements CloneCompositeStateSupport {

        public UnitOfWorkDecorator2( StoreUnitOfWork suow ) {
            super( suow );
        }

        protected CloneCompositeStateSupport suow() {
            return (CloneCompositeStateSupport)suow;
        }
        
        @Override
        public CompositeState cloneEntityState( CompositeState state ) {
            return suow().cloneEntityState( state ); 
        }

        @Override
        public void reincorparateEntityState( CompositeState state, CompositeState clonedState ) {
            suow().reincorparateEntityState( state, clonedState );
        }
        
    }
    
}
