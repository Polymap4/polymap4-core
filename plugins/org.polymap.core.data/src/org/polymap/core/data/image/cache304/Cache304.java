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
package org.polymap.core.data.image.cache304;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import java.io.File;

import org.geotools.geometry.jts.ReferencedEnvelope;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.vividsolutions.jts.geom.Geometry;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import org.polymap.core.data.image.GetMapRequest;
import org.polymap.core.project.ILayer;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.runtime.Timer;
import org.polymap.core.runtime.recordstore.IRecordFieldSelector;
import org.polymap.core.runtime.recordstore.IRecordState;
import org.polymap.core.runtime.recordstore.IRecordStore;
import org.polymap.core.runtime.recordstore.RecordQuery;
import org.polymap.core.runtime.recordstore.SimpleQuery;
import org.polymap.core.runtime.recordstore.IRecordStore.ResultSet;
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
    
    private static final Cache304       instance;
    
    
    static {
        instance = new Cache304();
    }
    
    public static final Cache304 instance() {
        return instance;
    }
    
    
    // instance *******************************************
    
    IRecordStore                store;
    
    private CacheUpdateQueue    updateQueue = new CacheUpdateQueue( this );
    
    /**
     * Synchronizes store and updateQueue. After a real lock is aquired the store and
     * the queue are stable. Updating both is done with write locked.
     */
    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private TimeUnit            liveTimeUnit = TimeUnit.HOURS;

    /**
     * The default livetime of all tiles in cache. Tiles are considered invalid if
     * their creation time is before current time - {@link #maxTileLiveTime}.
     */
    private int                 maxTileLiveTime = 24;
    
    private Updater             updater = new Updater();
    
    /** Defaults to 100MB. */
    private int                 maxStoreSizeInByte = 100 * 1024 * 1024;
    
    /** Update lastAccessed() time, only if it is older then this time. */
    private long                accessTimeRasterMillis = 3 * 60 * 1000;
    
    
    protected Cache304() {
        try {
            store = new LuceneRecordStore( new File( Polymap.getCacheDir(), "tiles" ), false );
            
            store.setIndexFieldSelector( new IRecordFieldSelector() {
                public boolean accept( String key ) {
                    return !key.equals( CachedTile.TYPE.data );
                }
            });
        }
        catch (Exception e) {
            log.error( "Error starting Cache304.", e );
        }
    }
    
    
    public CachedTile get( GetMapRequest request, Set<ILayer> layers ) {
        try {
            // keep store and queue stable during method run
            lock.readLock().lock();
            
            // search the store
            RecordQuery query = buildQuery( request, layers );
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
                if (cachedTile.lastAccessed.get() + accessTimeRasterMillis < now) {
                    cachedTile.lastAccessed.put( now );
                    updateQueue.push( new CacheUpdateQueue.TouchCommand( cachedTile ) );
                    updater.reSchedule();
                }
                return cachedTile;
            }
            else {
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
    
    
    public CachedTile put( GetMapRequest request, Set<ILayer> layers, byte[] data ) {
        try {
            CachedTile cachedTile = get( request, layers );
            if (cachedTile == null) {
                cachedTile = new CachedTile( store.newRecord() );
                long now = System.currentTimeMillis();
                cachedTile.created.put( now );
                cachedTile.lastModified.put( now );
                cachedTile.lastAccessed.put( now );

                cachedTile.width.put( request.getWidth() );
                cachedTile.height.put( request.getHeight() );

                assert layers.size() == 1 : "put(): more than one layer in request.";
                ILayer layer = layers.iterator().next();
                String styleHash = "hash" + layer.getStyle().createSLD( new NullProgressMonitor() ).hashCode();
                cachedTile.style.put( styleHash );

                cachedTile.layerId.put( layer.id() );
                
                ReferencedEnvelope bbox = request.getBoundingBox();
                cachedTile.minx.put( bbox.getMinX() );
                cachedTile.miny.put( bbox.getMinY() );
                cachedTile.maxx.put( bbox.getMaxX() );
                cachedTile.maxy.put( bbox.getMaxY() );
                
                cachedTile.data.put( data );
                
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
            lock.writeLock().lock();
            
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
            storeUpdater.apply();
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
            try {
                Timer timer = new Timer();
                List<CacheUpdateQueue.Command> queueState = updateQueue.state(); 
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
                
                lock.writeLock().lock();
                timer.start();
                tx.apply();
                updateQueue.remove( queueState );
                log.debug( "commit done. (" + timer.elapsedTime() + "ms)" );
            }
            catch (Exception e) {
                tx.discard();
                log.error( "Error while flushing queue:", e );
            }
            finally {
                if (lock.writeLock().isHeldByCurrentThread()) {
                    lock.writeLock().unlock();
                }
            }

            // check max livetime *************************
            log.debug( "Updater: pruning expired tiles..." );
            tx = store.prepareUpdate();
            try {
                long deadline = System.currentTimeMillis() - liveTimeUnit.toMillis( maxTileLiveTime );
                SimpleQuery query = new SimpleQuery();
                query.setMaxResults( 100 );
                query.less( CachedTile.TYPE.created.name(), deadline );
                
                ResultSet expiredTiles = store.find( query );
                
                for (IRecordState state : expiredTiles) {
                    log.debug( "    expired tile: " + new CachedTile( state ).created.get() );
                    tx.remove( state );
                }
                lock.writeLock().lock();
                tx.apply();
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
                    query.setMaxResults( 50 );
                    query.less( CachedTile.TYPE.lastAccessed.name(), System.currentTimeMillis() );
                    query.sort( CachedTile.TYPE.lastAccessed.name(), SimpleQuery.ASC );
                    
                    ResultSet expiredTiles = store.find( query );

                    for (IRecordState state : expiredTiles) {
                        log.debug( "    oldest tile: " + new CachedTile( state ).lastAccessed.get() );
                        tx.remove( state );
                    }
                    lock.writeLock().lock();
                    tx.apply();
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
