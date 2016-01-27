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

import java.util.Set;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import org.polymap.core.runtime.ConcurrentReferenceHashMap;
import org.polymap.core.runtime.ConcurrentReferenceHashMap.ReferenceType;

/**
 * Cache implementation backed by a {@link ConcurrentReferenceHashMap}.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
final class SoftCache<K,V>
        implements Cache<K,V> {

    private static Log log = LogFactory.getLog( SoftCache.class );
    
    private volatile static int         cacheCounter = 0;
    
    private String                      name;
    
    private SoftCacheManager            manager;

    private ConcurrentReferenceHashMap<K,V> entries;

//    private ListenerList<CacheEvictionListener> listeners;
    
    private CacheConfig                 config;
    

    SoftCache( SoftCacheManager manager, String name, CacheConfig config ) {
        this.manager = manager;
        this.name = name != null ? name : String.valueOf( cacheCounter++ );
        this.config = config;
        
        this.entries = new ConcurrentReferenceHashMap( config.initSize.get(), 0.75f, config.concurrencyLevel.get(),
                ReferenceType.STRONG, ReferenceType.SOFT, null );
    }

    
    @Override
    public String getName() {
        return name;
    }

    
    @Override
    public void dispose() {
        if (!isDisposed()) {
            clear();
            entries = null;
            manager.disposeCache( this );
        }
    }

    
    @Override
    public boolean isDisposed() {
        return entries == null;
    }
    
    
    protected V checkResult( V result ) {
        assert result != null : "";
        return result;
    }
    
    
    @Override
    public V get( K key ) throws CacheException {
        assert key != null : "Null keys are not allowed.";
        assert !isDisposed() : "Cache is disposed.";

        return entries.get( key );
    }

    
    @Override
    public <E extends Exception> V get( K key, CacheLoader<K,V,E> loader ) throws E {
        assert key != null : "Null keys are not allowed.";
        assert !isDisposed() : "Cache is disposed.";
        
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

    @Override
    public V putIfAbsent( K key, V value ) throws CacheException {
        V result = putIfAbsent( key, value, config.elementMemSize.get() );
        assert result != null;
        return result;
    }
    
    
    public V putIfAbsent( K key, V value, int elementMemSize ) throws CacheException {
        assert key != null : "Null keys are not allowed.";
        assert !isDisposed() : "Cache is disposed.";
        assert elementMemSize > 0;

        return entries.putIfAbsent( key, value );
    }
    
    
    @Override
    public V remove( K key ) throws CacheException {
        assert key != null : "Null keys are not allowed.";
        assert !isDisposed() : "Cache is disposed.";

        return entries.remove( key );
    }

    
    @Override
    public int size() {
        assert !isDisposed() : "Cache is disposed.";
        return entries.size();
    }

    
    @Override
    public void clear() {
        assert !isDisposed() : "Cache is disposed.";
        entries.clear();
    }

    
    @Override
    public Iterable<V> values() {
        assert !isDisposed() : "Cache is disposed.";
        return entries.values();
    }

    
    @Override
    public Set<K> keySet() {
        assert !isDisposed() : "Cache is disposed.";
        return entries.keySet();
    }


    public boolean addEvictionListener( EvictionListener listener ) {
        throw new UnsupportedOperationException( "CacheEvictionListener is not supported." );
//        if (listeners == null) {
//            listeners = new ListenerList();
//        }
//        return listeners.add( listener );
    }

    
    public boolean removeEvictionListener( EvictionListener listener ) {
        throw new UnsupportedOperationException( "CacheEvictionListener is not supported." );
//        return listeners != null ? listeners.remove( listener ) : false;
    }
    
    
//    void fireEvictionEvent( K key, V value ) {
//        if (listeners != null) {
//            for (CacheEvictionListener l : listeners.getListeners()) {
//                l.onEviction( key, value );
//            }
//        }
//    }

}
