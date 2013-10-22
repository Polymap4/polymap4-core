/* 
 * polymap.org
 * Copyright 2011-2013, Polymap GmbH. All rights reserved.
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
package org.polymap.core.data.image.cache304;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import java.io.File;
import java.io.IOException;
import java.lang.ref.SoftReference;

import org.geotools.geometry.jts.ReferencedEnvelope;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Supplier;
import com.vividsolutions.jts.geom.Geometry;

import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.InstanceScope;

import org.polymap.core.data.DataPlugin;
import org.polymap.core.data.image.GetMapRequest;
import org.polymap.core.project.ILayer;
import org.polymap.core.runtime.CachedLazyInit;
import org.polymap.core.runtime.LazyInit;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.runtime.Timer;
import org.polymap.core.runtime.recordstore.IRecordFieldSelector;
import org.polymap.core.runtime.recordstore.IRecordState;
import org.polymap.core.runtime.recordstore.IRecordStore;
import org.polymap.core.runtime.recordstore.RecordQuery;
import org.polymap.core.runtime.recordstore.ResultSet;
import org.polymap.core.runtime.recordstore.SimpleQuery;
import org.polymap.core.runtime.recordstore.lucene.LuceneRecordStore;

/**
 * The central API and mediator of the module.
 * <p/>
 * The cache uses the {@link org.polymap.core.runtime.recordstore} package to provide
 * a fast, persistent, plugable persistent backend store. By default the based Lucene
 * engine is used. The structure of the records in the store are defined by
 * {@link CachedTile}.
 * <p/>
 * Updating the backend store is done by the {@link CacheUpdateQueue} only. It
 * bufferes updates as queue of {@link CacheUpdateQueue#Command}s.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @since 3.1
 */
public class Cache304 {

    private static Log log = LogFactory.getLog( Cache304.class );
    
    /** The unit of livetime values: hours */
    public static final TimeUnit        liveTimeUnit = TimeUnit.HOURS;

    public static final String          PROP_MAX_TILE_LIVETIME = "maxTileLivetime";
    
    public static final String          PREF_TOTAL_STORE_SIZE = "totalStoreSize";
    
    public static final int             DEFAULT_MAX_TILE_LIVETIME = 8;
    public static final int             DEFAULT_MAX_STORE_SIZE = 100 * 1024 * 1024;
    
    private static CacheStatistics      statistics = new CacheStatistics();
    
    /**
     * No one should hold a permanent ref to the cache as every access uses {@link #instance()}.
     * If the {@link #updater} job is scheduled, then it holds a strong ref and keeps it from GC.
     * <p/>
     * XXX This does not use {@link CachedLazyInit} as it allows multiple instances to be created.
     */
    private static LazyInit<Cache304>   instance = new LazyInit() {
        private SoftReference<Cache304> ref;
        @Override
        public Cache304 get() {
            Cache304 result = null;
            if (ref == null || (result = ref.get()) == null) {
                synchronized (this) {
                    if (ref == null || (result = ref.get()) == null) {
                        ref = new SoftReference( result = new Cache304() );
                    }
                }
            }
            return result;
        }
        @Override
        public Object get( Supplier _supplier ) {
            throw new RuntimeException( "not yet implemented." );
        }
        @Override
        public void clear() {
            ref = null;
        }
        @Override
        public boolean isInitialized() {
            return ref != null && ref.get() != null;
        }
    };
    
    public static CacheStatistics statistics() {
        return statistics;
    }
    
    public static final Cache304 instance() {
        return instance.get();
    }

    
    // instance *******************************************
    
    IRecordStore                store;
    
    private CacheUpdateQueue    updateQueue = new CacheUpdateQueue( this );
    
    /**
     * Synchronizes store and updateQueue. After a real lock is aquired the store and
     * the queue are stable. Updating both is done with write locked.
     */
    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private Updater             updater = new Updater();
    
    /** Defaults to {@link #DEFAULT_MAX_STORE_SIZE}. */
    private long                maxStoreSizeInByte = DEFAULT_MAX_STORE_SIZE;
    
    /** Update lastAccessed() time (for LRU), only if it is older then this time (30min.) */
    private long                accessTimeRasterMillis = 30 * 60 * 1000;

    
    private IPersistentPreferenceStore    prefs = new ScopedPreferenceStore( 
            new InstanceScope(), DataPlugin.getDefault().getBundle().getSymbolicName() );
    
    
    protected Cache304() {
        try {
            store = new LuceneRecordStore( new File( Polymap.getCacheDir(), "tiles" ), false );
            
            store.setIndexFieldSelector( new IRecordFieldSelector() {
                public boolean accept( String key ) {
                    return !key.equals( CachedTile.TYPE.data );
                }
            });
            
            prefs.setDefault( PREF_TOTAL_STORE_SIZE, DEFAULT_MAX_STORE_SIZE );
            maxStoreSizeInByte = prefs.getInt( PREF_TOTAL_STORE_SIZE );
        }
        catch (Exception e) {
            log.error( "Error starting Cache304.", e );
        }
    }
    
    
    @Override
    protected void finalize() throws Throwable {
        log.info( "FINALIZE..." );
        try {
            // write pending changes and cleanup
            updater.run( new NullProgressMonitor() );
            store.close();
        }
        finally {
        }
    }


    public long getMaxTotalSize() {
        return maxStoreSizeInByte;    
    }
    
    public void setMaxTotalSize( long size ) {
        this.maxStoreSizeInByte = size;
        try {
            prefs.setValue( PREF_TOTAL_STORE_SIZE, size );
            prefs.save();
        }
        catch (IOException e) {
            throw new RuntimeException( e );
        }
    }


    /**
     *
     * @param request
     * @param layers
     * @param props The processor properties for this layer.
     * @return The cached tile or null.
     */
    public CachedTile get( GetMapRequest request, Set<ILayer> layers, Properties props ) {
        try {
            // keep store and queue stable; prevent race cond between removing
            // Command from queue and writing to store
            lock.readLock().lock();
            
            // search the store
            RecordQuery query = buildQuery( request, layers );
            query.setMaxResults( 2 );
            ResultSet resultSet = store.find( query );
            if (resultSet.count() > 1) {
                log.warn( "More than one tile for query: " + request ); 
            }

            List<CachedTile> result = new ArrayList();
            for (IRecordState state : resultSet) {
                CachedTile cachedTile = new CachedTile( state );
                result.add( cachedTile );
            }
            
            // search the queue
            updateQueue.adaptCacheResult( result, query );
            
            if (result.size() > 1) {
                log.warn( "More than one tile in result: " + result.size() ); 
            }
            
            if (!result.isEmpty()) {
                CachedTile cachedTile = result.get( 0 );
                long now = System.currentTimeMillis();
                
                // update lastAccessed() time, only if it was not already
                // done in the last accessTimeRasterMillis
                if ((cachedTile.lastAccessed.get() + accessTimeRasterMillis) < now) {
                    cachedTile.lastAccessed.put( now );
                    updateQueue.push( new CacheUpdateQueue.TouchCommand( cachedTile ) );
                    updater.reSchedule();
                }
                
                statistics.incLayerHitCounter( layers, false );
                return cachedTile;
            }
            else {
                statistics.incLayerHitCounter( layers, true );
                return null;
            }
        }
        catch (Exception e) {
            log.error( "", e );
            return null;
        }
        finally {
            lock.readLock().unlock();
        }
    }


    /**
     * 
     * @param request
     * @param layers
     * @param data
     * @param created
     * @param props The processor properties for this layer.
     * @return The cached tile if the given request maps to a cached tile, or a newly
     *         created tile for the given request.
     */
    public CachedTile put( GetMapRequest request, Set<ILayer> layers, byte[] data, long created, Properties props ) {
        try {
            CachedTile cachedTile = get( request, layers, props );
            if (cachedTile == null) {
                cachedTile = new CachedTile( store.newRecord() );
                cachedTile.created.put( created );
                cachedTile.lastModified.put( created );
                cachedTile.lastAccessed.put( created );

                assert layers.size() == 1 : "put(): more than one layer in request.";
                ILayer layer = layers.iterator().next();

                int maxLivetime = Integer.parseInt( props.getProperty( 
                        PROP_MAX_TILE_LIVETIME, 
                        String.valueOf (DEFAULT_MAX_TILE_LIVETIME ) ) );
                cachedTile.expires.put( created + liveTimeUnit.toMillis( maxLivetime ) );
                
                cachedTile.width.put( request.getWidth() );
                cachedTile.height.put( request.getHeight() );

                String styleHash = "hash" + layer.getStyle().createSLD( new NullProgressMonitor() ).hashCode();
                cachedTile.style.put( styleHash );

                cachedTile.layerId.put( layer.id() );
                
                ReferencedEnvelope bbox = request.getBoundingBox();
                cachedTile.minx.put( bbox.getMinX() );
                cachedTile.miny.put( bbox.getMinY() );
                cachedTile.maxx.put( bbox.getMaxX() );
                cachedTile.maxy.put( bbox.getMaxY() );
                
                cachedTile.data.put( data );
                
                // XXX there is a race cond between threads of different user sessions
                // that request/update the same tile; so this push should have semantics
                // of "pufIfAbsent"
                updateQueue.push( new CacheUpdateQueue.StoreCommand( cachedTile ) );
                updater.reSchedule();
            }
            return cachedTile;
        }
        catch (Exception e) {
            log.error( "", e );
            return null;
        }
    }
    
    
    public void updateLayer( ILayer layer, Geometry changed ) {
        // flush queue
        if (!updateQueue.isEmpty()) {
            log.warn( "Queue is not empty before updateLayer()!" );
        }

        // remove all tiles for layer
        IRecordStore.Updater storeUpdater = null;
        try {
            lock.writeLock().tryLock( 3, TimeUnit.SECONDS );
            
            SimpleQuery query = new SimpleQuery();
            query.eq( CachedTile.TYPE.layerId.name(), layer.id() );
            query.setMaxResults( 100000 );
            ResultSet resultSet = store.find( query );
            log.debug( "Removing tiles: " + resultSet.count() );
            
            Timer timer = new Timer();
            storeUpdater = store.prepareUpdate();
            for (IRecordState record : resultSet) {
                storeUpdater.remove( record );
            }
            storeUpdater.apply( true );
            log.debug( "done. (" + timer.elapsedTime() + "ms)" );
        }
        catch (Exception e) {
            if (storeUpdater != null) {
                storeUpdater.discard();
            }
        }
        finally {
            lock.writeLock().unlock();
        }
    }


    protected RecordQuery buildQuery( GetMapRequest request, Set<ILayer> layers ) {
        SimpleQuery query = new SimpleQuery();

        if (request.getWidth() != -1) {
            query.eq( CachedTile.TYPE.width.name(), request.getWidth() );
        }
        if (request.getHeight() != -1) {
            query.eq( CachedTile.TYPE.height.name(), request.getHeight() );
        }
        
        if (request.getBoundingBox() != null) {
            ReferencedEnvelope bbox = request.getBoundingBox();            
            query.eq( CachedTile.TYPE.maxx.name(), bbox.getMaxX() );
            query.eq( CachedTile.TYPE.minx.name(), bbox.getMinX() );
            query.eq( CachedTile.TYPE.maxy.name(), bbox.getMaxY() );
            query.eq( CachedTile.TYPE.miny.name(), bbox.getMinY() );
            
//            // maxx > bbox.getMinX
//            query.greater( CachedTile.TYPE.maxx.name(), bbox.getMinX() );
//            // minx < bbox.getMaxX
//            query.less( CachedTile.TYPE.minx.name(), bbox.getMaxX() );
//            // maxy > bbox.getMinY
//            query.greater( CachedTile.TYPE.maxy.name(), bbox.getMinY() );
//            // miny < bbox.getMaxY
//            query.less( CachedTile.TYPE.miny.name(), bbox.getMaxY() );
        }

        if (layers != null && !layers.isEmpty()) {
            assert layers.size() == 1 : "put(): more than one layer in request: " + layers;
            ILayer layer = layers.iterator().next();

            // layerId
            query.eq( CachedTile.TYPE.layerId.name(), layer.id() );

            // style
            String styleHash = "hash" + layer.getStyle().createSLD( new NullProgressMonitor() ).hashCode();
            query.eq( CachedTile.TYPE.style.name(), styleHash );
            
            // not expired
            query.greater( CachedTile.TYPE.expires.name(), System.currentTimeMillis() );
        }
        return query;
    }
    
    
    /**
     * The Updater triggers the {@link CacheUpdateQueue} to flush its queue
     * and it prunes cache store afterwards.
     */
    class Updater
            extends Job {

        private long            normDelay = 3000;
        
        /** normDelay / queueCountDelayFactor * queue.count */
        private double          queueCountDelayFactor = 0.02;
        
        private long            lastAccess = System.currentTimeMillis(); 
        
        
        public Updater() {
            super( "Cache304 Updater" );
            setSystem( true );
        }

        
        protected IStatus run( IProgressMonitor monitor ) {
            // flushing updateQueue
            IRecordStore.Updater tx = store.prepareUpdate();
            List<CacheUpdateQueue.Command> queueState = null; 
            try {
                Timer timer = new Timer();
                queueState = updateQueue.state(); 
                log.debug( "Updater: flushing elements in queue: " + queueState.size() );
                for (CacheUpdateQueue.Command command: queueState) {
                    try {
                        command.apply( tx );
                    }
                    catch (Exception ee) {
                        log.error( "Error while flushing command queue: ", ee );
                    }
                }
                log.debug( "writing commands done. (" + timer.elapsedTime() + "ms)" );
                
                // external synchronization of Lucene is not a good idea in general;
                // I don't see another way to make apply() and remove() one atomar
                // operation; but use tryLock() instead of block-forever lock()
                lock.writeLock().tryLock( 5, TimeUnit.SECONDS );
                if (!lock.isWriteLockedByCurrentThread()) {
                    log.warn( "Unable to aquire write lock! (3 seconds)" );
                }
                timer.start();
                tx.apply( false );                
                log.debug( "commit done. (" + timer.elapsedTime() + "ms)" );
            }
            catch (Exception e) {
                tx.discard();
                log.error( "Error while flushing queue:", e );
            }
            finally {
                // remove command from queue no matter if tx failed to avoid
                // overflow if somethinf is wrong with backend
                if (queueState != null && !updateQueue.remove( queueState )) {
                    log.warn( "!!! UNABLE TO REMOVE COMMAND FROM QUEUE: " + queueState.size() );
                }
                if (lock.writeLock().isHeldByCurrentThread()) {
                    lock.writeLock().unlock();
                }
            }

            // check max livetime *************************
            log.debug( "Updater: pruning expired tiles..." );
            tx = store.prepareUpdate();
            try {
                long deadline = System.currentTimeMillis();
                SimpleQuery query = new SimpleQuery();
                query.setMaxResults( 100 );
                query.less( CachedTile.TYPE.expires.name(), deadline );
                query.sort( CachedTile.TYPE.lastAccessed.name(), SimpleQuery.ASC, String.class );
                
                ResultSet expiredTiles = store.find( query );
                
                for (IRecordState state : expiredTiles) {
                    log.debug( "    expired tile: " + new CachedTile( state ).created.get() );
                    tx.remove( state );
                }
                lock.writeLock().lock();
                tx.apply( false );
            }
            catch (Exception e) {
                tx.discard();
                log.error( "Error while pruning cache:", e );
            }
            finally {
                if (lock.writeLock().isHeldByCurrentThread()) {
                    lock.writeLock().unlock();
                }
            }

            // check store size ***************************
            long storeSize = store.storeSizeInByte();
            if (storeSize > maxStoreSizeInByte) {
                log.debug( "Updater: checking maxStoreSize... (" + storeSize + "/" + maxStoreSizeInByte + ")" );
                tx = store.prepareUpdate();
                try {
                    // check max livetime
                    SimpleQuery query = new SimpleQuery();
                    query.setMaxResults( 100 );
                    query.less( CachedTile.TYPE.lastAccessed.name(), System.currentTimeMillis() );
                    query.sort( CachedTile.TYPE.lastAccessed.name(), SimpleQuery.ASC, String.class );
                    
                    ResultSet expiredTiles = store.find( query );

                    for (IRecordState state : expiredTiles) {
                        log.debug( "    oldest tile: " + new CachedTile( state ).lastAccessed.get() );
                        tx.remove( state );
                    }
                    lock.writeLock().lock();
                    tx.apply( true );
                }
                catch (Exception e) {
                    tx.discard();
                    log.error( "Error while pruning cache:", e );
                }
                finally {
                    if (lock.writeLock().isHeldByCurrentThread()) {
                        lock.writeLock().unlock();
                    }
                }
            }
            return Status.OK_STATUS;
        }
        
        
        public boolean shouldRun() {
            if (lastAccess <= (System.currentTimeMillis() - normDelay)) {
                return true;
            }
            else {
                reSchedule();
                return false;
            }
        }


        public void reSchedule() {
            lastAccess = System.currentTimeMillis();
            schedule( normDelay );
        }
        
    }

}
