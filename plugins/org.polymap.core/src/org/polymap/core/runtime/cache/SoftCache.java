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

import org.polymap.core.runtime.ConcurrentReferenceHashMap;
import org.polymap.core.runtime.ConcurrentReferenceHashMap.ReferenceType;
import org.polymap.core.runtime.ListenerList;

/**
 * Cache backed by a {@link ConcurrentReferenceHashMap}.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
final class SoftCache<K,V>
        implements Cache<K,V> {

    private static Log log = LogFactory.getLog( SoftCache.class );
    
    private volatile static int         cacheCounter = 0;
    
    private String                      name;
    
    private SoftCacheManager            manager;

    private ConcurrentMap<K,V>          entries;

    private ListenerList<CacheEvictionListener> listeners;
    
    private CacheConfig                 config;
    

    SoftCache( SoftCacheManager manager, String name, CacheConfig config ) {
        this.manager = manager;
        this.name = name != null ? name : String.valueOf( cacheCounter++ );
        this.config = config;
        
        this.entries = new ConcurrentReferenceHashMap( config.initSize, 0.75f, config.concurrencyLevel,
                ReferenceType.STRONG, ReferenceType.SOFT, null );

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

        return entries.get( key );
    }

    
    public <E extends Throwable> V get( K key, CacheLoader<K,V,E> loader ) throws E {
        assert key != null : "Null keys are not allowed.";
        assert entries != null : "Cache is closed.";
        
        V entry = entries.get( key );
        if (entry == null) {
            entry = loader.load( key );
            if (entry != null) {
                V previous = entries.putIfAbsent( key, entry );
                entry = previous != null ? previous : entry;
            }
        }
        return entry;
    }

    
    public V putIfAbsent( K key, V value ) throws CacheException {
        return putIfAbsent( key, value, config.elementMemSize );
    }
    
    
    public V putIfAbsent( K key, V value, int elementMemSize ) throws CacheException {
        assert key != null : "Null keys are not allowed.";
        assert entries != null : "Cache is closed.";
        assert elementMemSize > 0;

        return entries.putIfAbsent( key, value );
    }
    
    
    public V remove( K key ) throws CacheException {
        assert key != null : "Null keys are not allowed.";
        assert entries != null : "Cache is closed.";

        return entries.remove( key );
    }

    
    public int size() {
        assert entries != null : "Cache is closed.";
        return entries.size();
    }

    
    public Iterable<Map.Entry<K,V>> entries() {
        assert entries != null : "Cache is closed.";
        return entries.entrySet();
    }

    
    public void clear() {
        assert entries != null : "Cache is closed.";
        entries.clear();
    }

    
    public Iterable<V> values() {
        assert entries != null : "Cache is closed.";
        return entries.values();
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

}
