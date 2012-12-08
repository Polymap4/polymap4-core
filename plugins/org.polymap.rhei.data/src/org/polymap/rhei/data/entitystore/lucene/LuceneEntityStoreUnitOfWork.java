/* 
 * polymap.org
 * Copyright 2011, Polymap GmbH. All rights reserved.
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
package org.polymap.rhei.data.entitystore.lucene;

import java.util.concurrent.ConcurrentMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.structure.Module;
import org.qi4j.spi.entity.EntityDescriptor;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entitystore.EntityNotFoundException;
import org.qi4j.spi.entitystore.EntityStoreException;
import org.qi4j.spi.entitystore.EntityStoreSPI;
import org.qi4j.spi.entitystore.EntityStoreUnitOfWork;
import org.qi4j.spi.entitystore.StateCommitter;

import com.google.common.collect.MapMaker;

/**
 * This {@link EntityStoreUnitOfWork} works with {@link LuceneEntityStoreMixin}.
 * 
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 */
public final class LuceneEntityStoreUnitOfWork
        implements EntityStoreUnitOfWork {

    private static Log log = LogFactory.getLog( LuceneEntityStoreUnitOfWork.class );

    private EntityStoreSPI                  entityStoreSPI;

    private String                          identity;

    private Module                          module;

    private ConcurrentMap<String,EntityState> modified;
    
    
    public LuceneEntityStoreUnitOfWork( EntityStoreSPI entityStoreSPI, String identity,
            Module module ) {
        this.entityStoreSPI = entityStoreSPI;
        this.identity = identity;
        this.module = module;

        modified = new MapMaker().initialCapacity( 256 ).concurrencyLevel( 8 ).makeMap();
        
        // allow to EntityState instances to be reclaimed; UnitOfWorkInstance is repsonsible
        // for caching, therefore we hold just weak references and listen to eviction
        
//        states = new MapMaker().weakValues().initialCapacity( 1024 ).concurrencyLevel( 8 )
//                .evictionListener( new MapEvictionListener<String,EntityState>() {
//                    public void onEviction( String key, EntityState state ) {
//                        //log.info( "EVICTION: key=" + key + ", states=" + states.size() );
//                        if (state != null && state.status() != EntityStatus.LOADED) {
//                            log.info( "    -> MODIFIED!" );
//                            EntityState old = modified.put( key, state );
//                            if (old != null) {
//                                log.warn( "Evicted EntityState has been seen already." );
//                            }
//                        }
//                    }
//                })
//                .makeMap();
    }


    public void discard() {
        modified.clear();
    }


    public String identity() {
        return identity;
    }


    public Module module() {
        return module;
    }


    // EntityStore

    public EntityState newEntityState( EntityReference ref, EntityDescriptor descriptor )
    throws EntityStoreException {
        EntityState result = entityStoreSPI.newEntityState( this, ref, descriptor );
        EntityState previous = modified.put( result.identity().identity(), result );
        assert previous == null;
        return result;
    }


    public EntityState getEntityState( EntityReference ref )
    throws EntityStoreException, EntityNotFoundException {
        EntityState entityState = modified.get( ref.identity() );
        if (entityState != null) {
            return entityState;
        }
        else {       
            entityState = entityStoreSPI.getEntityState( this, ref );
            assert entityState != null;
            return entityState;
        }
    }


    public StateCommitter apply()
    throws EntityStoreException {
        return entityStoreSPI.apply( modified.values(), identity );
    }


    void statusChanged( LuceneEntityState entityState ) {
        if (entityState.status() != EntityStatus.LOADED) {
            modified.putIfAbsent( entityState.identity().identity(), entityState );
        }
        else {
            modified.remove( entityState.identity().identity() );
        }
    }

}
