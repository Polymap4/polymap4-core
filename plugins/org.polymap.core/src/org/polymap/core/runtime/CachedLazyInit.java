/* 
 * polymap.org
 * Copyright 2012, Falko Bräutigam. All rights reserved.
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
package org.polymap.core.runtime;

import com.google.common.base.Supplier;

import org.polymap.core.runtime.cache.Cache;
import org.polymap.core.runtime.cache.CacheConfig;
import org.polymap.core.runtime.cache.CacheManager;
import org.polymap.core.runtime.cache.EvictionAwareCacheLoader;
import org.polymap.core.runtime.cache.EvictionListener;

/**
 * Provides a pseudo-persistent lazily initialized variable. Ones initialized the
 * value is stored in a global {@link Cache}. The cache may decide to evict this value
 * at any time if memory is low (or for any other reason). In this case the next access
 * triggers the supplier again.
 * <p/>
 * Concurrent access is synchronized by the cache's internal mechanism.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class CachedLazyInit<T>
        extends LazyInit<T> {

    /**
     * The global cache. Use hashCode()/Integer as key to allow the instance to be
     * GC'ed - and {@link #clear()} the value from cache.
     */
    private static final Cache<Integer,Object>   cache = CacheManager.instance().newCache( CacheConfig.DEFAULT );

    /**
     * Store the hashKey to prevent call to hashCode and auto-boxing on each access.
     */
    private Integer                 cacheKey = hashCode();
    
    private int                     elementSize;
    
    
    /**
     * 
     * @param elementSize The size of the value in the cache in bytes.
     */
    public CachedLazyInit( int elementSize ) {
        super();
        this.elementSize = elementSize;
    }

    /**
     * 
     * @param elementSize The size of the value in the cache in bytes.
     * @param supplier
     */
    public CachedLazyInit( int elementSize, Supplier<T> supplier ) {
        super( supplier );
        this.elementSize = elementSize;
    }

    /**
     * This ctor allows to preset the value. This can be used if the value is
     * available when initialized but may be reclaimed during processing.
     * 
     * @param elementSize The size of the value in the cache in bytes.
     * @param supplier
     */
    public CachedLazyInit( T value, int elementSize, Supplier<T> supplier ) {
        super( supplier );
        cache.putIfAbsent( cacheKey, value, elementSize );
        this.elementSize = elementSize;
    }

    @Override
    @SuppressWarnings("hiding")
    public T get( final Supplier<T> supplier ) {
        return (T)cache.get( cacheKey, new EvictionAwareCacheLoader<Integer,Object,RuntimeException>() {
            @Override
            public Object load( Integer key ) throws RuntimeException {
                return supplier.get();
            }
            @Override
            public int size() throws RuntimeException {
                return elementSize;
            }
            @Override
            public EvictionListener evictionListener() {
                return supplier instanceof EvictionSupplier
                        ? ((EvictionSupplier)supplier).evictionListener() 
                        : null;
            }
        });
    }

    @Override
    public void clear() {
        cache.remove( cacheKey );
    }

    @Override
    public boolean isInitialized() {
        return cache.get( cacheKey ) != null;
    }

    @Override
    protected void finalize() throws Throwable {
        clear();
    }
    
    
    /**
     * 
     */
    public static interface EvictionSupplier<T>
            extends Supplier<T> {
        
        EvictionListener evictionListener();
        
    }
    
}
