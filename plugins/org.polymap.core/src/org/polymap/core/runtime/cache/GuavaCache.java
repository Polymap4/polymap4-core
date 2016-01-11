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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import com.google.common.cache.CacheBuilder;
import org.polymap.core.runtime.cache.GuavaCacheManager.CacheEntry;

/**
 * Experimental cache implementation backed by Guava's cache implementation
 * {@link CacheBuilder}.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
final class GuavaCache<K,V>
        implements Cache<K,V> {

    private static Log log = LogFactory.getLog( GuavaCache.class );
    
    private String                                  name;
    
    private GuavaCacheManager                       manager;

    private CacheConfig                             config;

    private com.google.common.cache.Cache<K,V>      cache;
    
//    private ListenerList<EvictionListener>          listeners;
    

    GuavaCache( GuavaCacheManager manager, String name, CacheConfig config ) {
        this.manager = manager;
        this.name = name != null ? name : String.valueOf( hashCode() );
        this.config = config;
        
//        RemovalListener<K,V> removalListener = new RemovalListener<K,V>() {
//            public void onRemoval( RemovalNotification<K,V> notification ) {
//                // fire event
//                if (notification.wasEvicted() && listeners != null) {
//                    for (EvictionListener l : listeners.getListeners()) {
//                        l.onEviction( notification.getKey()/*, notification.getValue()*/ );
//                    }
//                }
//                // manager
//                GuavaCache.this.manager.event( CacheEntry.REMOVED, GuavaCache.this, 
//                        notification.getKey(), notification.getValue() );
//            }
//        };
        
        cache = CacheBuilder.newBuilder()
                .concurrencyLevel( config.concurrencyLevel.get() )
                .initialCapacity( config.initSize.get() )
                .softValues()
//                .removalListener( removalListener )
                .build();
        
    }

    
    public String getName() {
        return name;
    }

    
    public void dispose() {
        if (cache != null) {
            clear();
            cache = null;
        }
    }

    
    public boolean isDisposed() {
        return cache == null;
    }
    
    
    public V get( K key ) throws CacheException {
        assert key != null : "Null keys are not allowed.";
        assert cache != null : "Cache is closed.";

        V result = cache.getIfPresent( key );
        
        manager.event( CacheEntry.ACCESSED, this, key, result );
        
        return result;
    }

    
    public <E extends Exception> V get( K key, CacheLoader<K,V,E> loader ) throws E {
        assert key != null : "Null keys are not allowed.";
        assert cache != null : "Cache is closed.";

        try {
            return cache.get( key, new Callable<V>() {
                @Override
                public V call() throws Exception {
                    return loader.load( key );
                }
            });
        }
        catch (ExecutionException e) {
            throw (E)e.getCause(); 
        }
    }

    
    public V putIfAbsent( K key, V value ) throws CacheException {
        manager.event( CacheEntry.ADDED, this, key, value );
        return putIfAbsent( key, value, config.elementMemSize.get() );
    }
    
    
    public V putIfAbsent( K key, V value, int elementMemSize ) throws CacheException {
        assert key != null : "Null keys are not allowed.";
        assert cache != null : "Cache is closed.";
        assert elementMemSize > 0;

        manager.event( CacheEntry.ADDED, this, key, value );
        return cache.asMap().putIfAbsent( key, value );
    }
    
    
    public V remove( K key ) throws CacheException {
        assert key != null : "Null keys are not allowed.";
        assert cache != null : "Cache is closed.";

        return cache.asMap().remove( key );
    }

    
    public int size() {
        assert cache != null : "Cache is closed.";
        return (int)cache.size();
    }

    
    public void clear() {
        assert cache != null : "Cache is closed.";
        cache.invalidateAll();
    }

    
    public Iterable<V> values() {
        assert cache != null : "Cache is closed.";
        return cache.asMap().values();        
    }

    
    @Override
    public Set<K> keySet() {
        assert cache != null : "Cache is closed.";
        return cache.asMap().keySet();        
    }

    
//    public boolean addEvictionListener( EvictionListener listener ) {
//        if (listeners == null) {
//            listeners = new ListenerList();
//        }
//        return listeners.add( listener );
//    }
//
//    
//    public boolean removeEvictionListener( EvictionListener listener ) {
//        return listeners != null ? listeners.remove( listener ) : false;
//    }
    
}
