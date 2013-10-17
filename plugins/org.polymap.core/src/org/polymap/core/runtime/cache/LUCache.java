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
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

import org.polymap.core.runtime.ListenerList;

/**
 * Cache backed by a {@link ConcurrentHashMap} with separate thread
 * evicting entries via LRU policy.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
final class LUCache<K,V>
        implements Cache<K,V> {

    private static Log log = LogFactory.getLog( LUCache.class );
    
    private volatile static int         accessCounter = 0;
    
    private volatile static int         cacheCounter = 0;
    
    private String                      name;
    
    private LUCacheManager              manager;

    private ConcurrentMap<K,CacheEntry<V>> entries;

    private ListenerList<CacheEvictionListener> listeners;
    
    private CacheConfig                 config;
    

    LUCache( LUCacheManager manager, String name, CacheConfig config ) {
        this.manager = manager;
        this.name = name != null ? name : String.valueOf( cacheCounter++ );
        this.config = config;
        
        this.entries = new ConcurrentHashMap( config.initSize, 0.75f, config.concurrencyLevel );

//        this.entries = new MapMaker()
//                .initialCapacity( config.initSize )
//                .concurrencyLevel( config.concurrencyLevel )
//                //.softValues()
//                .makeMap();
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

        CacheEntry<V> entry = entries.get( key );
        return entry != null ? entry.value() : null;
    }

    
    public <E extends Throwable> V get( K key, CacheLoader<K,V,E> loader ) throws E {
        assert key != null : "Null keys are not allowed.";
        assert entries != null : "Cache is closed.";
        
        CacheEntry<V> entry = entries.get( key );
        if (entry != null) {
            return entry.value();
        }
        else {
            V value = loader.load( key );
            int memSize = loader.size();
            if (value != null) {
                entry = new CacheEntry( value, memSize != ELEMENT_SIZE_UNKNOW ? memSize : config.elementMemSize );
                CacheEntry<V> previous = entries.putIfAbsent( key, entry );
                return previous == null
                        ? entry.value()
                        : previous.value();
            }
            else {
                return null;
            }
        }
    }

    
    public V putIfAbsent( K key, V value ) throws CacheException {
        return putIfAbsent( key, value, config.elementMemSize );
    }
    
    
    public V putIfAbsent( K key, V value, int elementMemSize ) throws CacheException {
        assert key != null : "Null keys are not allowed.";
        assert entries != null : "Cache is closed.";
        assert elementMemSize > 0;

        CacheEntry<V> entry = entries.putIfAbsent( key, new CacheEntry( value, elementMemSize ) );
        return entry != null ? entry.value() : null;
    }
    
    
    public V remove( K key ) throws CacheException {
        assert key != null : "Null keys are not allowed.";
        assert entries != null : "Cache is closed.";

        CacheEntry<V> entry = entries.remove( key );
        return entry != null ? entry.value() : null;
    }

    
    public int size() {
        assert entries != null : "Cache is closed.";
        return entries.size();
    }

    
    public Iterable<Map.Entry<K,CacheEntry<V>>> entries() {
        assert entries != null : "Cache is closed.";
        return entries.entrySet();
    }

    
    @Override
    public Set<K> keySet() {
        assert entries != null : "Cache is closed.";
        return entries.keySet();
    }

    
    public void clear() {
        assert entries != null : "Cache is closed.";
        entries.clear();
    }

    
    public Iterable<V> values() {
        assert entries != null : "Cache is closed.";
        
        return Iterables.transform( entries.values(), new Function<CacheEntry<V>,V>() {
            public V apply( CacheEntry<V> input ) {
                return input.value();
            }
        });
    }

    
    public boolean addEvictionListener( CacheEvictionListener listener ) {
        if (listeners == null) {
            listeners = new ListenerList();
        }
        return listeners.add( listener );
    }

    
    public boolean removeEvictionListener( CacheEvictionListener listener ) {
        return listeners != null ? listeners.remove( listener ) : false;
    }
    
    
    void fireEvictionEvent( K key, V value ) {
        if (listeners != null) {
            for (CacheEvictionListener l : listeners.getListeners()) {
                l.onEviction( key, value );
            }
        }
    }

    
    /**
     * 
     */
    static class CacheEntry<V> {

        private V               value;
        
        /** Use short instead of int, saving 2 bytes of memory. */
        private short           sizeInKB = -1;
        
        private volatile int    accessed = accessCounter++;
        
        
        CacheEntry( V value, int elementSize ) {
            assert value != null : "Null values are not allowed.";
            assert elementSize <= 0 || (elementSize / 1024) <= 256*256;
            
            this.value = value;
            this.sizeInKB = (short)(elementSize / 1024);
            assert sizeInKB > 0 : "elementSize=" + elementSize + " -> sizeInKB=" + sizeInKB;
        }

        void dispose() {
            value = null;
            accessed = -1;
        }
        
        public V value() {
            assert value != null;
            accessed = accessCounter++;
            if (accessed <= 0) {
                throw new CacheException( "Access counter exceeded!" );
            }
            return value;
        }

        public int accessed() {
            return accessed;
        }
        
        public int size() {
            assert sizeInKB != -1;
            return 1024*sizeInKB;
        }
    }

}
