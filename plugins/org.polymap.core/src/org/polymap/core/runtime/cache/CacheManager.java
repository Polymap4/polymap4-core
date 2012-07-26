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

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class CacheManager {

    public static CacheManager instance() {
        // XXX make this configurable
        //return SoftReferenceCacheManager.instance();
        return ConcurrentMapCacheManager.instance();
    }
    
    // instance *******************************************

    /**
     * Creates a new cache instance.
     * 
     * @param <K> The type of the keys in the cache.
     * @param <V> The type of the elements/values in the cache.
     * @param config The configuration of the new cache. {@link CacheConfig#DEFAULT}
     *        indicates that default values should be used.
     * @return The newly created cache.
     */
    public abstract <K,V> Cache<K,V> newCache( CacheConfig config );    
    
    /**
     * Creates a new cache instance.
     * 
     * @param <K> The type of the keys in the cache.
     * @param <V> The type of the elements/values in the cache.
     * @param name The name of the new cache instance.
     * @param config The configuration of the new cache. {@link CacheConfig#DEFAULT}
     *        indicates that default values should be used.
     * @return The newly created cache.
     */
    public abstract <K,V> Cache<K,V> getOrCreateCache( String name, CacheConfig config );    
    
}
