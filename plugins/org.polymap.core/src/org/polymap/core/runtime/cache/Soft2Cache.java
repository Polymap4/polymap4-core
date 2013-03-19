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

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Cache backed by a {@link ConcurrentHashMap} with separate thread
 * for handling {@link ReferenceQueue}.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
final class Soft2Cache<K,V>
        implements Cache<K,V> {

    private static Log log = LogFactory.getLog( Soft2Cache.class );
    
    private volatile static int         accessCounter = 0;
    
    private volatile static int         cacheCounter = 0;
    
    private String                      name;
    
    private Soft2CacheManager           manager;

    private ConcurrentMap<K,CacheEntry<K,V>> entries;

    private CacheConfig                 config;
    

    Soft2Cache( Soft2CacheManager manager, String name, CacheConfig config ) {
        this.manager = manager;
        this.name = name != null ? name : String.valueOf( cacheCounter++ );
        this.config = config;
        
        this.entries = new ConcurrentHashMap( config.initSize, 0.75f, config.concurrencyLevel );
    }

    
    public String getName() {
        return name;
    }

    
    public void dispose() {
        if (entries != null) {
            clear();
            entries = null;
            manager.disposeCache( this );
        }
    }

    
    public boolean isDisposed() {
        return entries == null;
    }
    
    
    public V get( K key ) throws CacheException {
        assert key != null : "Null keys are not allowed.";
        assert entries != null : "Cache is closed.";

        CacheEntry<K,V> entry = entries.get( key );
        return entry != null ? entry.value() : null;
    }

    
    public <E extends Throwable> V get( K key, CacheLoader<K,V,E> loader ) throws E {
        assert key != null : "Null keys are not allowed.";
        assert entries != null : "Cache is closed.";
                
        CacheEntry<K,V> entry = entries.get( key );
        V value = entry != null ? entry.value() : null;
        if (value != null) {
            return value;
        }
        else {
            // we do not prevent threads from concurrently creating a value for the
            // same key! but we make sure that just one value is retuned to all threads
            value = loader.load( key );
            int memSize = loader.size();
            EvictionListener listener = loader instanceof EvictionAwareCacheLoader ? 
                    ((EvictionAwareCacheLoader)loader).evictionListener() : null;

            if (value != null) {
                V previous = putIfAbsent( key, value, memSize, listener );
                value = previous != null ? previous : value;
            }
            return value;
        }
    }

    
    public V putIfAbsent( K key, V value ) throws CacheException {
        return putIfAbsent( key, value, config.elementMemSize, null );
    }
    
    
    public V putIfAbsent( K key, V value, int memSize ) throws CacheException {
        return putIfAbsent( key, value, memSize, null );
    }
    
    
    V putIfAbsent( K key, V value, int memSize, EvictionListener l ) 
    throws CacheException {
        assert key != null : "Null keys are not allowed.";
        assert entries != null : "Cache is closed.";
        
        memSize = memSize > 0 ? memSize : config.elementMemSize;

        CacheEntry<K,V> entry = new CacheEntry<K,V>( this, key, value, memSize, l );
        CacheEntry<K,V> previous = entries.putIfAbsent( key, entry );

        // previous entry reclaimed?
        if (previous != null) {
            value = previous.value();
            // and was not reclaimed -> return it
            if (value != null) {
                return value;
            }
            // if reclaimed -> insert new entry; return null                    
            else {
                entries.put( key, entry );
            }
        }
        return null;
    }
    
    
    public V remove( K key ) throws CacheException {
        assert key != null : "Null keys are not allowed.";
        assert entries != null : "Cache is closed.";

        CacheEntry<K,V> entry = entries.remove( key );
        return entry != null ? entry.value() : null;
    }

    
    boolean removeEntry( K key, CacheEntry entry ) throws CacheException {
        assert key != null : "Null keys are not allowed.";
        assert entries != null : "Cache is closed.";

        return entries.remove( key, entry );
    }

    
    public int size() {
        assert entries != null : "Cache is closed.";
        return entries.size();
    }

    
    public void clear() {
        assert entries != null : "Cache is closed.";
        entries.clear();
    }

    
    public Iterable<V> values() {
        assert entries != null : "Cache is closed.";
        
        // the returned iterator should be aware off reclaimed entries
        // and needs to support remove()
        return new Iterable<V>() {
            public Iterator<V> iterator() {
                return new Iterator<V>() {
                    
                    private Iterator<CacheEntry<K,V>>   it = entries.values().iterator();
                    
                    private CacheEntry<K,V>             next;
                    
                    public boolean hasNext() {
                        // find next, un-reclaimed entry
                        while ((next == null || next.value() == null) && it.hasNext()) {
                            next = it.next();
                        }
                        return next != null;
                    }
                    
                    public V next() {
                        assert next != null;
                        try { return next.value(); } finally { next = null; }
                    }
                    
                    public void remove() {
                        it.remove();
                    }
                };
            }
        };
    }

    
    /**
     * 
     */
    static class CacheEntry<K,V>
            extends SoftReference<V> {

        private K                   key;
        
        private EvictionListener    evictionListener;
        
        private Soft2Cache          cache;
        
        /** Use short instead of int, saving 2 bytes of memory. */
//        private short               sizeInKB = -1;
        
//        private volatile int        accessed = accessCounter++;
        
        
        CacheEntry( Soft2Cache cache, K key, V value, int elementSize, EvictionListener l ) {
            super( value, cache.manager.refQueue );
            assert value != null : "Null values are not allowed.";
            assert cache != null;
            assert elementSize <= 0 || (elementSize / 1024) <= 256*256;

            this.cache = cache;
            this.key = key;
            if (value instanceof EvictionAware) {
                evictionListener = ((EvictionAware)value).newListener();
            }
            if (l != null) {
                assert evictionListener == null;
                evictionListener = l;
            }
//            this.sizeInKB = (short)(elementSize / 1024);
//            assert sizeInKB > 0 : "elementSize=" + elementSize + " -> sizeInKB=" + sizeInKB;
        }

        void dispose() {
//            accessed = -1;
        }
        
        void fireEvictionEvent() {
            if (evictionListener != null) {
                evictionListener.onEviction( key );
            }
        }
        
        public Soft2Cache cache() {
            return cache;
        }
        
        public K key() {
            return key;
        }
        
        public V value() {
            accessCounter++;
//            accessed = accessCounter++;
//            if (accessed <= 0) {
//                throw new CacheException( "Access counter exceeded!" );
//            }
            return get();
        }

//        public int accessed() {
//            return accessed;
//        }
        
//        public int size() {
//            assert sizeInKB != -1;
//            return 1024*sizeInKB;
//        }
    }

}
