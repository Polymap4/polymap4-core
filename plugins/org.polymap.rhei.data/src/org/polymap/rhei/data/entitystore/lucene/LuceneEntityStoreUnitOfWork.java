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

import static com.google.common.collect.Iterables.concat;
import com.google.common.collect.MapEvictionListener;
import com.google.common.collect.MapMaker;

/**
 * This {@link EntityStoreUnitOfWork} works with {@link LuceneEntityStoreMixin}.
 * <p>
 * This UnitOfWork holds the entity states in a concurrent, weak Map in order to
 * determine what entities have been accessed and/or created already. I'm not sure if
 * this is the intended way to do. I'll give it a try...
 * 
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 */
@SuppressWarnings("deprecation")
public final class LuceneEntityStoreUnitOfWork
        implements EntityStoreUnitOfWork {

    private static Log log = LogFactory.getLog( LuceneEntityStoreUnitOfWork.class );

    private EntityStoreSPI          entityStoreSPI;

    private String                  identity;

    private Module                  module;

    //private List<EntityState>       states = new ArrayList( 128 );

    private ConcurrentMap<String,EntityState> states;

    private ConcurrentMap<String,EntityState> modified;
    
    
    public LuceneEntityStoreUnitOfWork( EntityStoreSPI entityStoreSPI, String identity,
            Module module ) {
        this.entityStoreSPI = entityStoreSPI;
        this.identity = identity;
        this.module = module;

        modified = new MapMaker().initialCapacity( 1024 ).concurrencyLevel( 8 ).makeMap();
        
        // allow to EntityState instances to be reclaimed; UnitOfWorkInstance is repsonsible
        // for caching, therefore we hold just weak references and listen to eviction
        states = new MapMaker().weakValues().initialCapacity( 1024 ).concurrencyLevel( 8 )
                .evictionListener( new MapEvictionListener<String,EntityState>() {
                    public void onEviction( String key, EntityState state ) {
                        //log.info( "EVICTION: key=" + key + ", states=" + states.size() );
                        if (state != null && state.status() != EntityStatus.LOADED) {
                            log.info( "    -> MODIFIED!" );
                            EntityState old = modified.put( key, state );
                            if (old != null) {
                                log.warn( "Evicted EntityState has been seen already." );
                            }
                        }
                    }
                })
                .makeMap();
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
        EntityState entityState = entityStoreSPI.newEntityState( this, ref, descriptor );
        assert entityState != null;
        
        EntityState old = states.put( ref.identity(), entityState );
        if (old != null) {
            log.debug( "Concurrent: EntityState has been seen already: " + old.identity().identity() );
        }
        return entityState;
    }


    public EntityState getEntityState( EntityReference ref )
    throws EntityStoreException, EntityNotFoundException {
        EntityState entityState = entityStoreSPI.getEntityState( this, ref );
        assert entityState != null;
        
        EntityState old = states.put( ref.identity(), entityState );
        if (old != null) {
            log.debug( "Concurrent: EntityState has been seen already: " + old.identity().identity() );
        }
        return entityState;
    }


    public StateCommitter apply()
    throws EntityStoreException {
        return entityStoreSPI.apply( concat( modified.values(), states.values() ), identity );
    }


    public void discard() {
    }

}
