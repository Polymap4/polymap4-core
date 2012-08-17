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
import java.util.PriorityQueue;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.MapMaker;

import org.polymap.core.runtime.Timer;
import org.polymap.core.runtime.cache.LUCache.CacheEntry;

/**
 * Memory usage of all caches is periodically checked by the {@link MemoryChecker}
 * thread. If memory is low then the LRU entries ({@link #DEFAULT_EVICTION_SIZE})
 * from all caches are evicted. The check interval is calculated from the amount of
 * free memory.
 * 
 * @see LUCache
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class LUCacheManager
        extends CacheManager {

    private static Log log = LogFactory.getLog( LUCacheManager.class );
    
    private static final int                DEFAULT_EVICTION_SIZE = 1000;
    private static final int                DEFAULT_EVICTION_MEM_PERCENT = 20;
    
    private static final LUCacheManager     instance = new LUCacheManager();
    
    
    public static LUCacheManager instance() {
        return instance;
    }
    
    
    // instance *******************************************
    
    private Thread                          memoryCheckerThread;
    
    private Map<String,LUCache>             caches;
    

    protected LUCacheManager() {
        // start thread
        memoryCheckerThread = new Thread( new MemoryChecker(), "CacheMemoryChecker" );
        memoryCheckerThread.setPriority( Thread.MAX_PRIORITY );
        memoryCheckerThread.start();
    
        caches = new MapMaker().initialCapacity( 256 ).weakValues().makeMap();
    }
    

    public <K, V> Cache<K, V> newCache( CacheConfig config ) {
        return add( new LUCache( this, null, config ) );
    }

    
    public <K, V> Cache<K, V> getOrCreateCache( String name, CacheConfig config ) {
        return add( new LUCache( this, name, config ) );
    }


    private <K, V> Cache<K, V> add( LUCache cache ) {
        LUCache elm = caches.put( cache.getName(), cache );
        if (elm != null) {
            caches.put( cache.getName(), elm );
            throw new IllegalArgumentException( "Cache name already exists: " + cache.getName() );
        }
        return cache;
    }

    
    void disposeCache( LUCache cache ) {
        LUCache elm = caches.remove( cache.getName() );
        if (elm == null) {
            throw new IllegalArgumentException( "Cache name does not exists: " + cache.getName() );
        }
    }

    
    /*
     * 
     */
    class MemoryChecker
            implements Runnable {

        private MemoryMXBean            memBean = ManagementFactory.getMemoryMXBean();
        
        private int                     lastEvictionCount = 1000;
        

        public void run() {
            while (true) {
                long nextSleep = 1000;
                try {
                    nextSleep = checkMemory();
                    checkMaxHeapFreeRatio();
                }
                catch (Exception e) {
                    log.warn( "", e );
                }
                
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
            long sleep = (long)Math.min( 50, 50*ratio );

            if (sleep < 10) {
                System.gc();
                sleep = 0;
            }
            
            if (heap.getUsed() > memUsedGoal) {
                log.debug( "Eviction..." );
                log.debug( String.format( "    Heap: used: %dMB, max: %dMB", heap.getUsed()/1024/1024, heap.getMax()/1024/1024 ) );
                
                timer.start();
                
                // simple eviction algorithm that sorts *all* entries in a fixed size TreeSet;
                // for a fast, incremental O(1)? algorithm, see the develop-cache-evict branch
                
                // XXX memory allocation!?
                PriorityQueue<EvictionCandidate> evictionQueue = new PriorityQueue( (int)(lastEvictionCount * 1.1) );
                int count = 0;
                int accessThreshold = 0;
                int evictionMemSize = 0;
                int memSizeTarget = (int)(heap.getUsed() / 100 * DEFAULT_EVICTION_MEM_PERCENT );
                log.debug( String.format( "    Eviction target size: %dMB", memSizeTarget/1024/1024 ) );

                // all caches
                for (LUCache cache : caches.values()) {
                    
                    Iterable<Map.Entry<Object,CacheEntry>> entries = cache.entries();
                    for (Map.Entry<Object,CacheEntry> entry : entries) {
                        count++;

                        if (evictionMemSize < memSizeTarget
                                || entry.getValue().accessed() < accessThreshold) {
                            
                            // find last entry and remove
                            EvictionCandidate last = null;
                            if (evictionMemSize > memSizeTarget) {
                                while (evictionMemSize > memSizeTarget) {
                                    last = evictionQueue.remove();

                                    accessThreshold = last.entry.accessed();
                                    evictionMemSize -= last.entry.size();
                                }
                            }
                            else {
                                accessThreshold = Math.max( accessThreshold, entry.getValue().accessed() );
                            }

                            evictionQueue.add( last != null
                                    ? last.set( cache, entry.getValue(), entry.getKey() )
                                    : new EvictionCandidate( cache, entry.getValue(), entry.getKey() ) );
                            evictionMemSize += entry.getValue().size();
                        }
                    }
                }
                
                for (EvictionCandidate candidate : evictionQueue) {
                    // remove from cache
                    Object elm = candidate.cache.remove( candidate.key );
                    assert elm != null : "Unable to remove element from cache: " + candidate.key;
                    // fire eviction event
                    candidate.cache.fireEvictionEvent( candidate.key, candidate.entry.value() );
                    candidate.entry.dispose();
                }
            
                //if (evictionQueue.isEmpty()) {
                    System.gc();
                //}
                
                lastEvictionCount = Math.max( 1000, evictionQueue.size() );
                log.info( "    Checked: " + count
                        + " - Evicted: " + lastEvictionCount
                        + " / " + evictionMemSize + " bytes"
                        + ", accessThreshold: " + accessThreshold + " (" + timer.elapsedTime() + "ms)" );
            }
            
            return sleep;
        }

        
        Timer heapFreeTimer = new Timer().stop();

        /**
         * Force full GC if more thean 30% heap are free for more than 180s. When
         * using G1GC this helps shrinking heap, which is done on full GC only.
         * Otherwise even with <code>-XX:MaxHeapFreeRatio=30</code> the heap never
         * shrinks as no full GC is triggered.
         */
        public void checkMaxHeapFreeRatio() {
            long maxHeapFreeRatio = 30;
//            EnvironmentInfo env;
//            for (String arg : args) {
//                if (arg.startsWith( "-XX:MaxHeapFreeRatio" )) {
//                    maxHeapFreeRatio = Long.parseLong( StringUtils.substringAfterLast( arg, "=" ) );
//                }
//            }
            
            MemoryUsage heap = memBean.getHeapMemoryUsage();
            // check if JDK supports memBean
            long free = heap.getCommitted() != 0
                    ? heap.getCommitted() - heap.getUsed()
                    : Runtime.getRuntime().freeMemory();
            long committed = heap.getCommitted() != 0
                    ? heap.getCommitted()
                    : Runtime.getRuntime().totalMemory();

            long heapFreeRatio = (free * 100) / committed;
        
            log.trace( "Heap free: " + heapFreeRatio + "%" );
            if (heapFreeRatio > maxHeapFreeRatio) {
                if (!heapFreeTimer.isStarted()) {
                    heapFreeTimer.start();
                }
            }
            else {
                heapFreeTimer.stop();
            }
            
            if (heapFreeTimer.elapsedTime() >= 180000) {
                log.debug( "checkMaxHeapFreeRatio(): forcing full GC ..." );
                System.gc();
                heapFreeTimer.stop();
            }
        }
    }
    
    
    /*
     * 
     */
    class EvictionCandidate
            implements Comparable {
       
        LUCache                     cache;
        
        CacheEntry                  entry;
        
        Object                      key;
        
        /**
         * Copy of the {@link CacheEntry#accessed} field, keeping the value stable
         * during one eviction run.
         */
        int                         lastAccessed;            

        
        EvictionCandidate( LUCache cache, CacheEntry entry, Object key ) {
            this.cache = cache;
            this.entry = entry;
            this.key = key;
            this.lastAccessed = entry.accessed();
        }

        public EvictionCandidate set( LUCache cache, CacheEntry entry, Object key ) {
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
                    // early/small accessTime -> high prio in eviction queue
                    ? -(lastAccessed - other.lastAccessed)
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
