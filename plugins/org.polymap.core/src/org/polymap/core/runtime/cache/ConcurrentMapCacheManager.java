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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.runtime.Timer;
import org.polymap.core.runtime.cache.ConcurrentMapCache.CacheEntry;

/**
 * In-memory cache manager. The caches are backed by {@link ConcurrentHashMap}s.
 * Memory usage is periodically checked by the {@link MemoryChecker} thread. If
 * memory is low then the LRU entries ({@link #DEFAULT_EVICTION_SIZE}) from all
 * caches are evicted. The check interval is calculated from the amount of free
 * memory.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
final class ConcurrentMapCacheManager
        extends CacheManager {

    private static Log log = LogFactory.getLog( ConcurrentMapCacheManager.class );
    
    private static final int                        DEFAULT_EVICTION_SIZE = 1000;
    
    private static final ConcurrentMapCacheManager  instance = new ConcurrentMapCacheManager();
    
    
    public static ConcurrentMapCacheManager instance() {
        return instance;
    }
    
    
    // instance *******************************************
    
    private Thread                          memoryCheckerThread;
    
    private List<ConcurrentMapCache>        caches;
    

    protected ConcurrentMapCacheManager() {
        // start thread
        memoryCheckerThread = new Thread( new MemoryChecker(), "CacheMemoryChecker" );
        memoryCheckerThread.setPriority( Thread.MAX_PRIORITY );
        memoryCheckerThread.start();
    
        caches = Collections.synchronizedList( new ArrayList( 64 ) );
    }
    

    public <K, V> Cache<K, V> newCache( String name ) {
        ConcurrentMapCache result = new ConcurrentMapCache( this, name );
        caches.add( result );
        return result;
    }

    
    void disposeCache( ConcurrentMapCache cache ) {
        caches.remove( cache );
    }

    
    /*
     * 
     */
    class MemoryChecker
            implements Runnable {

        private MemoryMXBean                    memBean;
        
        /** Holds unsorted eviction elements between evictions runs so that
         * the elements are not allocated/GCed when memory is low. */
//        private List<EvictionCandidate>         evictionElements;

        
        public MemoryChecker() {
            memBean = ManagementFactory.getMemoryMXBean() ;
            
//            // alocate eviction candidates and fill the set
//            evictionElements = new ArrayList( DEFAULT_EVICTION_SIZE );
//            for (int i=0; i<DEFAULT_EVICTION_SIZE; i++) {
//                evictionElements.add( new EvictionCandidate() );
//            }
        }

        public void run() {
            while (true) {
                long nextSleep = checkMemory();

                try {
                    //log.info( "sleeping: " + nextSleep + " ..." );
                    if (nextSleep > 0) {
                        Thread.sleep( nextSleep );
                    }
                }
                catch (InterruptedException e) {
                }
            }
        }
        
        public long checkMemory() {
            Timer timer = new Timer();
            MemoryUsage heap = memBean.getHeapMemoryUsage();
            MemoryUsage nonHeap = memBean.getNonHeapMemoryUsage();

            long memUsedGoal = (long)(heap.getMax() * 0.80);
            long maxFree = heap.getMax() - memUsedGoal;
            long free = heap.getMax() - heap.getUsed();
            float ratio = (float)free / (float)maxFree;
            //log.info( "    memory free ratio: " + ratio );
            // sleep no longer than 100ms
            long sleep = (long)Math.min( 1000, 1000*ratio );

            if (sleep < 500) {
                System.gc();
                sleep = 0;
            }
            
            if (heap.getUsed() > memUsedGoal) {
                log.info( "Starting eviction..." );
                log.info( String.format( "    Heap: used: %d, max: %d", heap.getUsed(), heap.getMax() ) );
                
                timer.start();
                
                // XXX memory allocation!?
                SortedSet<EvictionCandidate> evictionSet = new TreeSet();
                int accessThreshold = 0;  //Integer.MAX_VALUE;
                
                for (ConcurrentMapCache cache : caches) {
                    
                    Iterable<Map.Entry<Object,CacheEntry>> entries = cache.entries();
                    for (Map.Entry<Object,CacheEntry> entry : entries) {

                        if (evictionSet.size() < DEFAULT_EVICTION_SIZE
                                || entry.getValue().accessed() < accessThreshold) {
                            
                            // find last entry and remove
                            EvictionCandidate last = null;
                            if (evictionSet.size() >= DEFAULT_EVICTION_SIZE) {
                                last = evictionSet.last();
                                evictionSet.remove( last );
                                
                                accessThreshold = last.entry.accessed();
                            }
                            else {
                                accessThreshold = Math.max( accessThreshold, entry.getValue().accessed() );
                            }
                            
                            evictionSet.add( last != null
                                    ? last.set( cache, entry.getValue(), entry.getKey() )
                                    : new EvictionCandidate( cache, entry.getValue(), entry.getKey() ) );
                        }
                    }
                }
                
                for (EvictionCandidate candidate : evictionSet) {
                    // remove from cache
                    candidate.cache.remove( candidate.key );
                    // fire eviction event
                    candidate.cache.fireEvictionEvent( candidate.key, candidate.entry.value() );
                }
                
                //System.gc();
                
                log.info( "    Evicted: " + evictionSet.size() + ", accessThreshold: " + accessThreshold + " (" + timer.elapsedTime() + "ms)" );
            }
            
            return sleep;
        }

    }
    
    
    /*
     * 
     */
    class EvictionCandidate
            implements Comparable {
       
        ConcurrentMapCache          cache;
        
        CacheEntry                  entry;
        
        Object                      key;

        
        EvictionCandidate( ConcurrentMapCache cache, CacheEntry entry, Object key ) {
            this.cache = cache;
            this.entry = entry;
            this.key = key;
        }

        public EvictionCandidate set( ConcurrentMapCache cache, CacheEntry entry, Object key ) {
            this.cache = cache;
            this.entry = entry;
            this.key = key;
            return this;
        }

        public void clear() {
            cache = null;
            entry = null;
            key = null;
        }
        
        public int compareTo( Object obj ) {
            EvictionCandidate other = (EvictionCandidate)obj;
            return other.entry != null
                    ? entry.accessed() - other.entry.accessed()
                    : 0;
        }

//        public int hashCode() {
//            final int prime = 31;
//            int result = 1;
//            result = prime * result + ((cache == null) ? 0 : cache.hashCode());
//            result = prime * result + ((key == null) ? 0 : key.hashCode());
//            return result;
//        }

        public boolean equals( Object obj ) {
            if (obj == this) {
                return true;
            }
            if (obj instanceof EvictionCandidate) {
                EvictionCandidate other = (EvictionCandidate)obj;
                return cache == other.cache
                        && key.equals( other.key );
            }
            return false;
        }

    }

}
