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

import java.util.concurrent.ExecutionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.cache.CacheBuilder;

/**
 * Experimental cache implementation backed by {@link CacheBuilder}.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
final class GuavaCache<K,V>
        implements Cache<K,V> {

    private static Log log = LogFactory.getLog( GuavaCache.class );
    
    private String                                  name;
    
    private GuavaCacheManager                       manager;

    private CacheConfig                             config;

    private com.google.common.cache.CacheLoader<K,V> loaderWapper;
    
    /** FIXME this works for single threads only; ThreadLocal seem to big overhead(?) */
    private CacheLoader<K,V,? extends Throwable>    currentLoader;

    private com.google.common.cache.Cache<K,V>      cache;
    

    GuavaCache( GuavaCacheManager manager, String name, CacheConfig config ) {
        this.manager = manager;
        this.name = name != null ? name : String.valueOf( hashCode() );
        this.config = config;
        
        loaderWapper = new com.google.common.cache.CacheLoader<K,V>() {
            public V load( K key ) throws Exception {
                try {
                    return currentLoader.load( key );
                }
                catch (Exception e) {
                    throw e;
                }
                catch (Throwable e) {
                    throw new RuntimeException( e );
                }
            }
        };
//        cache = new MapMaker()
//                .concurrencyLevel( config.concurrencyLevel )
//                .initialCapacity( config.initSize )
//                .softValues()
//                .makeMap().g
        cache = CacheBuilder.newBuilder()
                .concurrencyLevel( config.concurrencyLevel )
                .initialCapacity( config.initSize )
                .softValues()
                .build( loaderWapper );
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
        
        return cache.asMap().get( key );
    }

    
    public <E extends Throwable> V get( K key, CacheLoader<K,V,E> loader ) throws E {
        assert key != null : "Null keys are not allowed.";
        assert cache != null : "Cache is closed.";
        
        assert currentLoader == null || currentLoader == loader;
        try {
            currentLoader = loader;
            return cache.get( key );
        }
        // XXX for the google cache the loade *always* returns a value
        catch (NullPointerException e) {
            return null;
        }
        catch (ExecutionException e) {
            throw new CacheException( e );
        }
    }

    
    public V putIfAbsent( K key, V value ) throws CacheException {
        return putIfAbsent( key, value, config.elementMemSize );
    }
    
    
    public V putIfAbsent( K key, V value, int elementMemSize ) throws CacheException {
        assert key != null : "Null keys are not allowed.";
        assert cache != null : "Cache is closed.";
        assert elementMemSize > 0;

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

    
    public boolean addEvictionListener( CacheEvictionListener listener ) {
        throw new RuntimeException( "not yet implemented" );
    }

    
    public boolean removeEvictionListener( CacheEvictionListener listener ) {
        throw new RuntimeException( "not yet implemented" );
    }
    
}
