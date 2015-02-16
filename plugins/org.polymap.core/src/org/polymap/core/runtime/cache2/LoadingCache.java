/* 
 * polymap.org
 * Copyright (C) 2015, Falko Bräutigam. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.core.runtime.cache2;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.configuration.CompleteConfiguration;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface LoadingCache<K,V>
        extends Cache<K,V> {

    // Factory ********************************************

    /**
     * 
     */
    public static <K,V> LoadingCache<K,V> create( CacheManager cacheManager, CompleteConfiguration config ) {
        return new LoadingCacheWrapper( cacheManager, config ); 
    }

    // Loader interface ***********************************
    
    public interface Loader<K,V> {
        
        public V load( K key );
    }
    
    // interface ******************************************
    
    public V get( K key, Loader<K,V> supplier );
    
}
