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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.MapMaker;

import org.polymap.core.runtime.Timer;

/**
 * Experimental manager for {@link GuavaCache}s.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class GuavaCacheManager
        extends CacheManager {

    private static Log log = LogFactory.getLog( GuavaCacheManager.class );
    
    private static final GuavaCacheManager      instance = new GuavaCacheManager();
    
    
    public static GuavaCacheManager instance() {
        return instance;
    }
    
    
    // instance *******************************************
    
    protected static volatile int           accessCounter = 0;
    
    private Map<String,GuavaCache>          caches;
    
    /** Maintained just by the {@link Journaler}. No synch. */
    private Map<CacheEntry,CacheEntry>      hardRefs = new HashMap( 1024 );
    
    /** */
    private BlockingQueue<CacheEntry>       journal = new ArrayBlockingQueue( 1000 );
    
    private Journaler                       journaler;
    
    private volatile int                    globalEntryCount;

    
    protected GuavaCacheManager() {
        this.caches = new MapMaker().initialCapacity( 256 ).weakValues().makeMap();
        
        this.journaler = new Journaler();
        this.journaler.start();
    }
    

    public <K, V> Cache<K, V> newCache( CacheConfig config ) {
        return add( new GuavaCache( this, null, config ) );
    }

    
    public <K, V> Cache<K, V> getOrCreateCache( String name, CacheConfig config ) {
        return add( new GuavaCache( this, name, config ) );
    }


    private <K, V> Cache<K, V> add( GuavaCache cache ) {
        GuavaCache elm = caches.put( cache.getName(), cache );
        if (elm != null) {
            caches.put( cache.getName(), elm );
            throw new IllegalArgumentException( "Cache name already exists: " + cache.getName() );
        }
        return cache;
    }

    
    void disposeCache( GuavaCache cache ) {
        GuavaCache elm = caches.remove( cache.getName() );
        if (elm == null) {
            throw new IllegalArgumentException( "Cache name does not exists: " + cache.getName() );
        }
    }
    
    
    void event( byte cause, GuavaCache cache, Object key, Object value ) {
//        try {
//            journal.put( new CacheEntry( cause, cache, key, value ) );
//        }
//        catch (InterruptedException e) {
//            throw new IllegalStateException( "Must never happen." );
//        }
    }
    
    
    /**
     * 
     */
    class Journaler
            extends Thread {
    
        protected Journaler() {
            super( "SoftCacheManager.Journaler" );
            //setPriority( MAX_PRIORITY );
        }
    
        @Override
        public void run() {
            while (true) {
                try {
                    CacheEntry event = journal.take();
                    
                    if (event.cause == CacheEntry.ACCESSED) {
                        CacheEntry entry = hardRefs.put( event, event );
                        entry = entry != null ? entry : event;
                        entry.accessed = accessCounter++;
                    }
                    else if (event.cause == CacheEntry.ADDED) {
                        globalEntryCount ++;
                        CacheEntry test = hardRefs.put( event, event );
                        assert test == null;
                    }
                    else if (event.cause == CacheEntry.REMOVED) {
                        globalEntryCount --;    
                        hardRefs.remove( event );                        
                    }
                    else {
                        throw new IllegalStateException( "Unhandled CacheEvent cause: " + event.cause );
                    }
                    
                    // trim hardRefs
                    if (hardRefs.size() > 1000 && hardRefs.size() > globalEntryCount*0.75) {
                        Timer timer = new Timer();
                        log.info( "Trimming hardRefs: " + hardRefs.size() + "/" + globalEntryCount );
                        log.info( "Journal size: " + journal.size() );
                        
                        int targetSize = globalEntryCount / 2;
                        log.info( "    target size: " + targetSize );
                        PriorityQueue<CacheEntry> priorities = new PriorityQueue( targetSize );
                        
                        List<CacheEntry> remove = new ArrayList( targetSize );
                        for (CacheEntry entry : hardRefs.values()) {
                            priorities.add( entry );
                            if (priorities.size() > targetSize) {
                                CacheEntry evict = priorities.poll();
                                remove.add( evict );
                            }
                        }
                        
                        for (CacheEntry entry : remove) {
                            hardRefs.remove( entry );
                        }
                        
//                        Iterator<CacheEntry> it = hardRefs.values().iterator();
//                        log.info( "    target size: " + targetSize );
//                        while (it.hasNext() && hardRefs.size() > targetSize) {
//                            it.next();
//                            it.remove();
//                        }
                        log.info( "    trim done: " + timer.elapsedTime() + "ms" );
                    }
                }
                catch (Exception e) {
                    log.warn( "", e );
                }
            }
        }        
    }


    /**
     * 
     */
    static class CacheEntry
            implements Comparable {

        public static final byte    ACCESSED = 0;
        public static final byte    ADDED = 1;
        public static final byte    REMOVED = 2;
        
        /** The cause of the event. */
        protected byte              cause;
        
        protected GuavaCache        cache;
        
        protected Object            key;
        
        protected Object            value;
        
        protected int               accessed = accessCounter++;

        
        protected CacheEntry( byte cause, GuavaCache cache, Object key, Object value ) {
            this.cause = cause;
            this.cache = cache;
            this.key = key;
            this.value = value;
        }

        /** Cache Priority. */
        public int compareTo( Object obj ) {
            return accessed - ((CacheEntry)obj).accessed;
            //return ((CacheEntry)obj).accessed - accessed;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((cache == null) ? 0 : cache.hashCode());
            result = prime * result + ((key == null) ? 0 : key.hashCode());
            return result;
        }

        @Override
        public boolean equals( Object obj ) {
            assert obj instanceof CacheEntry;
            if (this == obj) {
                return true;
            }
            CacheEntry other = (CacheEntry)obj;
            return cache == other.cache && key.equals( other.key );
        }

    }

}
