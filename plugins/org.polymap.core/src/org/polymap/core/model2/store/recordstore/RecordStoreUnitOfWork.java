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
package org.polymap.core.model2.store.recordstore;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;

import java.io.IOException;

import org.polymap.core.model2.Entity;
import org.polymap.core.model2.engine.UnitOfWorkImpl;
import org.polymap.core.model2.runtime.ConcurrentEntityModificationException;
import org.polymap.core.model2.runtime.ModelRuntimeException;
import org.polymap.core.model2.runtime.EntityRuntimeContext.EntityStatus;
import org.polymap.core.model2.store.StoreRuntimeContext;
import org.polymap.core.runtime.recordstore.IRecordState;
import org.polymap.core.runtime.recordstore.IRecordStore;
import org.polymap.core.runtime.recordstore.ResultSet;
import org.polymap.core.runtime.recordstore.SimpleQuery;
import org.polymap.core.runtime.recordstore.IRecordStore.Updater;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
final class RecordStoreUnitOfWork
        extends UnitOfWorkImpl {

    public static final String          TYPE_KEY = "_type_";
    
    private final IRecordStore          store;

    private Updater                     tx;

    private boolean                     prepareFailed;
    
    
    public RecordStoreUnitOfWork( StoreRuntimeContext context, RecordStoreAdapter rsa ) {
        super( context );
        this.store = rsa.store;
    }


    protected <T extends Entity> Object loadState( Object id, Class<T> entityClass ) {
        try {
            return store.get( id );
        }
        catch (Exception e) {
            throw new ModelRuntimeException( e );
        }
    }


    protected Object stateId( Object state ) {
        return ((IRecordState)state).id();
    }


    protected <T extends Entity> Object newState( Object id, Class<T> entityClass ) {
        if (id != null) {
            throw new UnsupportedOperationException( "Not supported: preset id in newly created entity" );
        }
        IRecordState result = store.newRecord();
        result.put( TYPE_KEY, entityClass.getName() );
        return result;
    }


    protected <T extends Entity> Collection findStates( Class<T> entityClass ) {
        try {
            final ResultSet results = store.find( new SimpleQuery().eq( TYPE_KEY, entityClass.getName() ) );
            
            return new AbstractCollection() {

                public Iterator iterator() {
                    return results.iterator();
                }

                public int size() {
                    return results.count();
                }

                protected void finalize() throws Throwable {
                    results.close();
                }
            };
        }
        catch (Exception e) {
            throw new ModelRuntimeException( e );
        }
    }


    public void prepare()
            throws IOException, ConcurrentEntityModificationException {
        assert tx == null;
        prepareFailed = false;
        tx = store.prepareUpdate();
        
        try {
            for (Entity entity : loaded.values()) {
                IRecordState state = (IRecordState)entity.state();

                if (entity.status() == EntityStatus.CREATED
                        || entity.status() == EntityStatus.MODIFIED) {
                    tx.store( state );
                }
                else if (entity.status() == EntityStatus.REMOVED) {
                    tx.remove( state );
                }
            }
        }
        catch (Exception e) {
            tx.discard();
            prepareFailed = true;
        }
    }


    public void commit() throws ModelRuntimeException {
        assert !prepareFailed : "Previous prepare() failed.";
        if (tx == null) {
            try {
                prepare();
            }
            catch (IOException e) {
                throw new ModelRuntimeException( e );
            }
        }
        tx.apply();
        tx = null;
        
        // clear entities, contexts and their status
        // XXX this also clears cache, ok?
        loaded.clear();
    }


    public void removeEntity( Entity entity ) {
        throw new RuntimeException( "not yet implemented." );
    }
    
}