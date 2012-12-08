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

import java.util.UUID;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.EntityTypeNotFoundException;
import org.qi4j.api.unitofwork.NoSuchEntityException;
import org.qi4j.api.usecase.Usecase;
import org.qi4j.spi.entity.EntityDescriptor;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entitystore.EntityStore;
import org.qi4j.spi.entitystore.EntityStoreException;
import org.qi4j.spi.entitystore.EntityStoreSPI;
import org.qi4j.spi.entitystore.EntityStoreUnitOfWork;
import org.qi4j.spi.entitystore.StateCommitter;
import org.qi4j.spi.service.ServiceDescriptor;
import org.qi4j.spi.structure.ModuleSPI;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import org.polymap.core.runtime.Timer;
import org.polymap.core.runtime.UIJob;
import org.polymap.core.runtime.cache.Cache;
import org.polymap.core.runtime.cache.CacheConfig;
import org.polymap.core.runtime.cache.CacheManager;
import org.polymap.core.runtime.recordstore.IRecordState;
import org.polymap.core.runtime.recordstore.IRecordStore.Updater;
import org.polymap.core.runtime.recordstore.lucene.GeometryValueCoder;
import org.polymap.core.runtime.recordstore.lucene.LuceneRecordStore;

/**
 * Implementation of EntityStore backed by Apache Lucene.
 * 
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 */
public class LuceneEntityStoreMixin
    implements LuceneSearcher, Activatable, EntityStore, EntityStoreSPI {

    private static Log log = LogFactory.getLog( LuceneEntityStoreMixin.class );
    
    private static final int        MAX_FIELD_SIZE = 1024*1024;

    
    private @This EntityStoreSPI    entityStoreSpi;

    private @Uses ServiceDescriptor descriptor;
    
    protected String                uuid;
    
    private int                     count;
    
    private LuceneRecordStore       store;


    public LuceneRecordStore getStore() {
        return store;
    }
    
    
    public void activate()
    throws Exception {
        File indexDir = getApplicationRoot();
        log.debug( "Lucene store: " + indexDir.getAbsolutePath() );
        uuid = UUID.randomUUID().toString() + "-";

        store = new LuceneRecordStore( indexDir, false );
        
        Cache<Object,Document> documentCache = CacheManager.instance().newCache( CacheConfig.DEFAULT );
        store.setDocumentCache( documentCache );
        
        store.getValueCoders().addValueCoder( new GeometryValueCoder() );
    }


    private File getApplicationRoot() {
        LuceneEntityStoreInfo storeInfo = descriptor.metaInfo( LuceneEntityStoreInfo.class );
        if (storeInfo == null) {
            throw new IllegalStateException( "No dir for LuceneEntityStore" );
        }
        else {
            return storeInfo.getDir();
        }
    }


    public void passivate()
    throws Exception {
        store.close();
    }


    public EntityStoreUnitOfWork newUnitOfWork( Usecase usecase, Module module ) {
        return new LuceneEntityStoreUnitOfWork( entityStoreSpi, newUnitOfWorkId(), module );
    }


    public EntityStoreUnitOfWork visitEntityStates( EntityStateVisitor visitor, 
            Module moduleInstance ) {
        throw new RuntimeException( "not yet implemented." );
//        final DefaultEntityStoreUnitOfWork uow = new DefaultEntityStoreUnitOfWork( entityStoreSpi,
//                newUnitOfWorkId(), moduleInstance );
//
//        try {
//            String[] identities = dir.list();
//            for (String identity : identities) {
//                EntityState entityState = uow.getEntityState( EntityReference
//                        .parseEntityReference( identity ) );
//                visitor.visitEntityState( entityState );
//            }
//        }
//        catch (/*BackingStore*/Exception e) {
//            throw new EntityStoreException( e );
//        }
//        
//        return uow;
    }


    public EntityState newEntityState( EntityStoreUnitOfWork unitOfWork, EntityReference identity,
            EntityDescriptor entityDescriptor ) {
        return new LuceneEntityState( (LuceneEntityStoreUnitOfWork)unitOfWork, 
                identity, entityDescriptor, store.newRecord() );
    }


    public EntityState getEntityState( EntityStoreUnitOfWork unitOfWork, EntityReference identity ) {
        try {
            LuceneEntityStoreUnitOfWork uow = (LuceneEntityStoreUnitOfWork)unitOfWork;
            ModuleSPI module = (ModuleSPI)uow.module();

            IRecordState record = null;
            
//            // use docnum in EntityReference
//            if (identity instanceof LuceneEntityReference
//                    && ((LuceneEntityReference)identity).docnum() != -1) {
//                int docnum = ((LuceneEntityReference)identity).docnum();
//                record = store.get( docnum );
//            }
//            // use identity (no docnum in EntityReference)
//            else {
                record = store.get( identity.identity() );
                if (record == null) {
                    throw new NoSuchEntityException( identity );
                }
//            }
            String typeName = record.get( "type" );
            if (typeName != null) {
                EntityDescriptor entityDescriptor = module.entityDescriptor( typeName );
                if (entityDescriptor != null) {
                    return new LuceneEntityState( uow, identity, entityDescriptor, record );
                }
            }
            throw new EntityTypeNotFoundException( typeName );
        }
        catch (Exception e) {
            throw new EntityStoreException( e );
        }
    }


    public StateCommitter apply( final Iterable<EntityState> states, final String version ) {

        return new StateCommitter() {

            public void commit() {
                log.info( "Committing..." );
                Timer timer = new Timer();

//                EntityState[] array = Iterables.toArray( states, EntityState.class );
                IProgressMonitor monitor = UIJob.monitorForThread();
                SubProgressMonitor sub = new SubProgressMonitor( monitor, 9 );
                sub.beginTask( "Lucene prepare", IProgressMonitor.UNKNOWN );
                
                final Updater updater = store.prepareUpdate();
                try {
                    
//                    ForEach.in( states )
//                        .doFirst( new Parallel<EntityState,EntityState>() {
//                            public EntityState process( EntityState entityState )
//                            throws Exception {
//                                LuceneEntityState state = (LuceneEntityState)entityState;
//
//                                switch (state.status()) {
//                                    case NEW : {
//                                        state.writeBack( version );
//                                        updater.store( state.state() );
//                                        //log.debug( "    added: " + doc );
//                                        break;
//                                    }
//                                    case UPDATED : {
//                                        state.writeBack( version );
//                                        updater.store( state.state() );
//                                        log.debug( "    updated: " + state );
//                                        break;
//                                    }
//                                    case REMOVED : {
//                                        updater.remove( state.state() );
//                                        log.debug( "    deleted: " + state );
//                                        break;
//                                    }
//                                    default : {
//                                        //log.debug( "    ommitting: " + state.identity().identity() + ", Status= " + state.status() + ", Doc= " + state.writeBack( version ) );
//                                    }
//                                }
//                                return state;
//                            }
//                    }).start();
                    
                    for (EntityState entityState : states) {
                        LuceneEntityState state = (LuceneEntityState)entityState;
                        
                        switch (state.status()) {
                            case NEW : {
                                state.writeBack( version );
                                updater.store( state.state() );
                                //log.debug( "    added: " + doc );
                                break;
                            }
                            case UPDATED : {
                                state.writeBack( version );
                                updater.store( state.state() );
                                log.debug( "    updated: " + state );
                                break;
                            }
                            case REMOVED : {
                                updater.remove( state.state() );
                                log.debug( "    deleted: " + state );
                                break;
                            }
                            default : {
                                //log.debug( "    ommitting: " + state.identity().identity() + ", Status= " + state.status() + ", Doc= " + state.writeBack( version ) );
                            }
                        }
                        sub.worked( 1 );
                    }
                    sub.done();

                    sub = new SubProgressMonitor( monitor, 1 );
                    sub.beginTask( "Lucene apply", 1 );

                    updater.apply();
                    
                    sub.done();
                    log.info( "...done. (" + timer.elapsedTime() + "ms)" );
                }
                catch (Exception e) {
                    updater.discard();
                    throw new RuntimeException( e );
                }
            }

            public void cancel() {
            }
        };
    }

    
    protected String newUnitOfWorkId() {
        return uuid + Integer.toHexString( count++ );
    }

}
