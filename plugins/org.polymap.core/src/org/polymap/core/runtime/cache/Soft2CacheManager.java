/* 
 * polymap.org
 * Copyright 2012, Polymap GmbH. All rights reserved.
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
package org.polymap.core.runtime.cache;

import java.util.Map;

import java.lang.ref.ReferenceQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.MapMaker;

import org.polymap.core.runtime.cache.Soft2Cache.CacheEntry;

/**
 * 
 * @see Soft2Cache
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class Soft2CacheManager
        extends CacheManager {

    private static final Log log = LogFactory.getLog( Soft2CacheManager.class );
    
    private static final Soft2CacheManager  instance = new Soft2CacheManager();
    
    
    public static Soft2CacheManager instance() {
        return instance;
    }
    
    
    // instance *******************************************
    
    private QueueChecker                    thread;
    
    private Map<String,Soft2Cache>          caches;

    protected ReferenceQueue                refQueue;
    

    protected Soft2CacheManager() {
        caches = new MapMaker().initialCapacity( 256 ).weakValues().makeMap();
        refQueue = new ReferenceQueue();

        // start thread
        thread = new QueueChecker();
        thread.start();
    }
    

    public <K, V> Cache<K, V> newCache( CacheConfig config ) {
        return add( new Soft2Cache( this, null, config ) );
    }

    
    public <K, V> Cache<K, V> getOrCreateCache( String name, CacheConfig config ) {
        return add( new Soft2Cache( this, name, config ) );
    }


    private <K, V> Cache<K, V> add( Soft2Cache cache ) {
        Soft2Cache elm = caches.put( cache.getName(), cache );
        if (elm != null) {
            caches.put( cache.getName(), elm );
            throw new IllegalArgumentException( "Cache name already exists: " + cache.getName() );
        }
        return cache;
    }

    
    void disposeCache( Soft2Cache cache ) {
        Soft2Cache elm = caches.remove( cache.getName() );
        if (elm == null) {
            throw new IllegalArgumentException( "Cache name does not exists: " + cache.getName() );
        }
    }

    
    /**
     * 
     */
    class QueueChecker
            extends Thread {

        private int         count = 0;
        
        public QueueChecker() {
            super( "Soft2Cache.QueueChecker" );
            setPriority( Thread.MIN_PRIORITY );
            setDaemon( true );
        }

        public void run() {
            while (true) {
                try {
                    count ++;
                    
                    CacheEntry entry = (CacheEntry)refQueue.remove( 5*60*1000 );
                    
                    // timeout? -> periodic GC
                    if (entry == null) {
                        // use spare time to reclaim heap and system memory (G1GC); 
                        // plus check SoftReferences -XX:SoftRefLRUPolicyMSPerMB
                       // logMemory( "Before GC: " );
                        System.gc();
                       // logMemory( "After GC: " );
                    }
                    else {
                        entry.cache().removeEntry( entry.key(), entry );

                        //log.debug( "reclaimed: " + entry.key() );
                        if (count % 1000 == 0) {
                            log.info( "1000 entries reclaimed by GC; total: " + count + ", cache size: " + entry.cache().size() );
                        }

                        // XXX If it could not be removed then there are 2 cases:
                        //   1. user has explictly remove meanwhile
                        //   2. it was removed because get() == null
                        // for 1. the following eviction event is not expected by client code                    
                        entry.fireEvictionEvent();
                    }
                }
                catch (InterruptedException e) {
                }
                catch (Throwable e) {
                    log.warn( "", e );
                }
            }
        }
        
        protected void logMemory( String prefix ) {
            System.gc();
            long total = Runtime.getRuntime().totalMemory();
            long free = Runtime.getRuntime().freeMemory();            
            log.info( prefix + (total-free) / (1024*1024) + "MB");
        }

    }

}
