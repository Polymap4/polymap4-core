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

import static org.polymap.core.model2.store.recordstore.RecordCompositeState.TYPE_KEY;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import java.io.IOException;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;

import org.polymap.core.model2.Entity;
import org.polymap.core.model2.query.Query;
import org.polymap.core.model2.query.grammar.BooleanExpression;
import org.polymap.core.model2.runtime.ConcurrentEntityModificationException;
import org.polymap.core.model2.runtime.EntityRuntimeContext.EntityStatus;
import org.polymap.core.model2.runtime.ModelRuntimeException;
import org.polymap.core.model2.store.CloneCompositeStateSupport;
import org.polymap.core.model2.store.CompositeState;
import org.polymap.core.model2.store.StoreRuntimeContext;
import org.polymap.core.model2.store.StoreUnitOfWork;
import org.polymap.core.runtime.recordstore.IRecordState;
import org.polymap.core.runtime.recordstore.IRecordStore;
import org.polymap.core.runtime.recordstore.IRecordStore.Updater;
import org.polymap.core.runtime.recordstore.RecordQuery;
import org.polymap.core.runtime.recordstore.ResultSet;
import org.polymap.core.runtime.recordstore.SimpleQuery;
import org.polymap.core.runtime.recordstore.lucene.LuceneRecordStore;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class RecordStoreUnitOfWork
        implements StoreUnitOfWork, CloneCompositeStateSupport {

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
//        if (id != null) {
//            throw new UnsupportedOperationException( "Not supported: preset id in newly created entity" );
//        }
        IRecordState state = id != null ? store.newRecord( id ) : store.newRecord();
        state.put( TYPE_KEY, entityClass.getName() );
        return new RecordCompositeState( state );
    }


    @Override
    public CompositeState cloneEntityState( CompositeState state ) {
        IRecordState clonedState = store.newRecord();
        for (Map.Entry<String,Object> entry : ((RecordCompositeState)state).state) {
            clonedState.put( entry.getKey(), entry.getValue() );
        }
        return new RecordCompositeState( clonedState );
    }


    @Override
    public void reincorparateEntityState( CompositeState state, CompositeState clonedState ) {
        // just replacing the IRecordState is not possible out-of-the-box as it was newly created (wrong id) ??
        
        Set<String> keys = new HashSet( 128 );
        // cloned -> state
        for (Map.Entry<String,Object> entry : ((RecordCompositeState)clonedState).state) {
            ((RecordCompositeState)state).state.put( entry.getKey(), entry.getValue() );
            keys.add( entry.getKey() );
        }
        // check removed
        Iterator<Map.Entry<String,Object>> it = ((RecordCompositeState)state).state.iterator();
        while (it.hasNext()) {
            Entry<String,Object> entry = it.next();
            if (!keys.contains( entry.getKey() )) {
                it.remove();
            }
        }
    }


    @Override
    public <T extends Entity> CompositeState adoptEntityState( Object state, Class<T> entityClass ) {
        return new RecordCompositeState( (IRecordState)state );
    }


    @Override
    public Collection executeQuery( Query query ) {
        try {
            RecordQuery recordQuery = null;
            if (query.expression == null) {
                recordQuery = new SimpleQuery().eq( TYPE_KEY, query.resultType().getName() );
            }
            else if (query.expression instanceof BooleanExpression) {
                // FIXME
                recordQuery = new LuceneQueryBuilder( (LuceneRecordStore)store )
                        .createQuery( query.resultType, (BooleanExpression)query.expression );
            }
            else {
                throw new UnsupportedOperationException( "Query expression type is not supported: " 
                        + query.expression.getClass().getSimpleName() );
            }

            recordQuery.setFirstResult( query.firstResult );
            recordQuery.setMaxResults( query.maxResults );
            final ResultSet results = store.find( recordQuery );
            
            return new AbstractCollection() {
                @Override
                public Iterator iterator() {
                    return Iterators.transform( results.iterator(), new Function<IRecordState,Object>() {
                        public Object apply( IRecordState input ) {
                            return input.id();
                        }
                    });
                }
                @Override
                public int size() {
                    return results.count();
                }
                @Override
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
    public boolean evaluate( Object entityState, Object expression ) {
        throw new RuntimeException( "Query expressions not yet supported: " + expression );
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
    public void rollback() {
        assert tx != null;

        tx.discard();
        tx = null;
    }


    @Override
    public void close() {
        if (tx != null && ! prepareFailed) {
            tx.discard();
        }
    }

}