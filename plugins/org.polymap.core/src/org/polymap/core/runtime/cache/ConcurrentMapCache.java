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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

import org.polymap.core.runtime.ListenerList;

/**
 * In-memory cache backed by a {@link ConcurrentHashMap}.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
final class ConcurrentMapCache<K,V>
        implements Cache<K,V> {

    private static Log log = LogFactory.getLog( ConcurrentMapCache.class );
    
    private volatile static int             accessCounter = 0;
    
    private String                          name;
    
    private ConcurrentMapCacheManager       manager;

    private ConcurrentMap<K,CacheEntry>     entries;

    private ListenerList<CacheEvictionListener> listeners;
    

    ConcurrentMapCache( ConcurrentMapCacheManager manager, String name ) {
        this.manager = manager;
        this.name = name;
        this.entries = new ConcurrentHashMap( 1024, 0.75f, 16 );
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

    
    public V get( K key ) throws CacheException {
        CacheEntry entry = entries.get( key );
        return entry != null ? entry.value() : null;
    }

    
    public V put( K key, CacheLoader<K, V> loader ) throws Exception {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

    
    public V putIfAbsent( K key, V value ) throws CacheException {
        CacheEntry entry = entries.putIfAbsent( key, new CacheEntry( value ) );
        return entry != null ? entry.value() : null;
    }
    
    
    public V put( K key, V value ) throws CacheException {
        return putIfAbsent( key, value );
    }

    
    public V remove( K key ) throws CacheException {
        CacheEntry entry = entries.remove( key );
        return entry != null ? entry.value() : null;
    }

    
    public int size() {
        return entries.size();
    }

    
    public Iterable<Map.Entry<K,CacheEntry>> entries() {
        return entries.entrySet();
    }

    
    public void clear() {
        entries.clear();
    }

    
    public Iterable<V> values() {
        return Iterables.transform( entries.values(), new Function<CacheEntry,V>() {
            public V apply( CacheEntry input ) {
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
    class CacheEntry {

        private V                  value;
        
        private int                accessed = accessCounter++;
        
        
        CacheEntry( V data ) {
            this.value = data;
        }

        public V value() {
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
