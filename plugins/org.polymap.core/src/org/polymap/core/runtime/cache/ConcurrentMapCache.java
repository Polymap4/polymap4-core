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
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.MapMaker;

import org.polymap.core.runtime.ListenerList;

/**
 * In-memory cache backed by a {@link ConcurrentHashMap}.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
final class ConcurrentMapCache<K,V>
        implements Cache<K,V> {

    private static Log log = LogFactory.getLog( ConcurrentMapCache.class );
    
    private volatile static int         accessCounter = 0;
    
    private String                      name;
    
    private ConcurrentMapCacheManager   manager;

    private ConcurrentMap<K,CacheEntry<V>> entries;

    private ListenerList<CacheEvictionListener> listeners;
    
    private CacheConfig                 config;
    

    ConcurrentMapCache( ConcurrentMapCacheManager manager, String name, CacheConfig config ) {
        this.manager = manager;
        this.name = name;
        this.config = config;
        
        //this.entries = new ConcurrentHashMap( config.initSize, 0.75f, config.concurrencyLevel );
        
        this.entries = new MapMaker()
                .initialCapacity( config.initSize )
                .concurrencyLevel( config.concurrencyLevel )
                .makeMap();

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

    
    public V get( K key, CacheLoader<K, V> loader ) throws Exception {
        assert key != null : "Null keys are not allowed.";
        assert entries != null : "Cache is closed.";
        
        CacheEntry<V> entry = entries.get( key );
        if (entry != null) {
            return entry.value();
        }
        else {
            entry = new CacheEntry( null, ELEMENT_SIZE_UNKNOW );
            CacheEntry<V> previous = entries.putIfAbsent( key, entry );
            if (previous == null) {
                try {
                    entry.setValue( loader.load( key ), loader.size() );
                    return entry.value();
                }
                catch (Exception e) {
                    entries.remove( key );
                    throw e;
                }
            }
            else {
                return previous.value();
            }
        }
    }

    
    public V putIfAbsent( K key, V value ) throws CacheException {
        return putIfAbsent( key, value, ELEMENT_SIZE_UNKNOW );
    }
    
    
    public V putIfAbsent( K key, V value, int elementSize ) throws CacheException {
        assert key != null : "Null keys are not allowed.";
        assert entries != null : "Cache is closed.";

        CacheEntry<V> entry = entries.putIfAbsent( key, new CacheEntry( value, elementSize ) );
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
        
        private byte            sizeInKB;
        
        private int             accessed = accessCounter++;
        
        
        CacheEntry( V data, int elementSize ) {
            this.value = data;
        }

        void setValue( V value, int elementSize ) {
            this.value = value;
            synchronized (this) {
                notifyAll();
            }
        }
        
        public V value() {
            // wait for the loader to be ready and value is set
            while (value == null) {
                synchronized (this) {
                    try {
                        wait( 1000 );
                    }
                    catch (InterruptedException e) {
                    }
                }
            }
            
            accessed = accessCounter++;
            if (accessed <= 0) {
                throw new CacheException( "Access counter exceeded!" );
            }
            return value;
        }

        public int accessed() {
            return accessed;
        }
        
    }

}
