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

import com.google.common.base.Function;
import com.google.common.collect.Iterators;

import org.polymap.core.model2.Entity;
import org.polymap.core.model2.engine.QueryImpl;
import org.polymap.core.model2.runtime.ConcurrentEntityModificationException;
import org.polymap.core.model2.runtime.EntityRuntimeContext.EntityStatus;
import org.polymap.core.model2.runtime.ModelRuntimeException;
import org.polymap.core.model2.store.CompositeState;
import org.polymap.core.model2.store.StoreRuntimeContext;
import org.polymap.core.model2.store.StoreUnitOfWork;
import org.polymap.core.runtime.recordstore.IRecordState;
import org.polymap.core.runtime.recordstore.IRecordStore;
import org.polymap.core.runtime.recordstore.IRecordStore.Updater;
import org.polymap.core.runtime.recordstore.ResultSet;
import org.polymap.core.runtime.recordstore.SimpleQuery;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class RecordStoreUnitOfWork
        implements StoreUnitOfWork {

    public static final String          TYPE_KEY = "_type_";
    
    private final IRecordStore          store;

    private Updater                     tx;

    private boolean                     prepareFailed;
    
    
    public RecordStoreUnitOfWork( StoreRuntimeContext context, RecordStoreAdapter rsa ) {
        this.store = rsa.store;
    }

    
    @Override
    public <T extends Entity> CompositeState loadEntityState( Object id, Class<T> entityClass ) {
        try {
            IRecordState state = store.get( id );
            return new RecordCompositeState( state );
        }
        catch (Exception e) {
            throw new ModelRuntimeException( e );
        }
    }


    @Override
    public <T extends Entity> CompositeState newEntityState( Object id, Class<T> entityClass ) {
        if (id != null) {
            throw new UnsupportedOperationException( "Not supported: preset id in newly created entity" );
        }
        IRecordState state = store.newRecord();
        state.put( TYPE_KEY, entityClass.getName() );
        return new RecordCompositeState( state );
    }


    @Override
    public <T extends Entity> CompositeState adoptEntityState( Object state, Class<T> entityClass ) {
        return new RecordCompositeState( (IRecordState)state );
    }


    @Override
    public <T extends Entity> Collection find( QueryImpl query ) {
        assert query.expression == null : "Query expressions not yet supported: " + query.expression;
        try {
            // XXX cache result for subsequent loadEntityState() (?)
            final ResultSet results = store.find( 
                    new SimpleQuery().eq( TYPE_KEY, query.resultType().getName() ).setMaxResults( Integer.MAX_VALUE ) );
            
            return new AbstractCollection() {

                public Iterator iterator() {
                    return Iterators.transform( results.iterator(), new Function<IRecordState,Object>() {
                        public Object apply( IRecordState input ) {
                            return input.id();
                        }
                    });
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


    @Override
    public void prepareCommit( Iterable<Entity> loaded )
            throws IOException, ConcurrentEntityModificationException {
        assert tx == null;
        prepareFailed = false;
        tx = store.prepareUpdate();
        
        try {
            for (Entity entity : loaded) {
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
            
            if (e instanceof IOException) { 
                throw (IOException)e; 
            }
            else if (e instanceof ConcurrentEntityModificationException) { 
                throw (ConcurrentEntityModificationException)e; 
            }
            else if (e instanceof RuntimeException) {
                throw (RuntimeException)e;
            }
            else {
                throw new RuntimeException( e );
            }
        }
    }

    
    @Override
    public void commit() {
        assert tx != null;
        assert !prepareFailed : "Previous prepareCommit() failed.";

        tx.apply();
        tx = null;
    }


    @Override
    public void close() {
        if (tx != null && ! prepareFailed) {
            tx.discard();
        }
    }

}