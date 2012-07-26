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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.MapMaker;

/**
 * Experimental manager for {@link SoftReferenceCache}s.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
final class SoftReferenceCacheManager
        extends CacheManager {

    private static Log log = LogFactory.getLog( SoftReferenceCacheManager.class );
    
    private static final SoftReferenceCacheManager  instance = new SoftReferenceCacheManager();
    
    
    public static SoftReferenceCacheManager instance() {
        return instance;
    }
    
    
    // instance *******************************************
    
    private Map<String,SoftReferenceCache>  caches;
    

    protected SoftReferenceCacheManager() {
        caches = new MapMaker().initialCapacity( 256 ).weakValues().makeMap();
    }
    

    public <K, V> Cache<K, V> newCache( CacheConfig config ) {
        return add( new SoftReferenceCache( this, null, config ) );
    }

    
    public <K, V> Cache<K, V> getOrCreateCache( String name, CacheConfig config ) {
        return add( new SoftReferenceCache( this, name, config ) );
    }


    private <K, V> Cache<K, V> add( SoftReferenceCache cache ) {
        SoftReferenceCache elm = caches.put( cache.getName(), cache );
        if (elm != null) {
            caches.put( cache.getName(), elm );
            throw new IllegalArgumentException( "Cache name already exists: " + cache.getName() );
        }
        return cache;
    }

    
    void disposeCache( ConcurrentMapCache cache ) {
        SoftReferenceCache elm = caches.remove( cache.getName() );
        if (elm == null) {
            throw new IllegalArgumentException( "Cache name does not exists: " + cache.getName() );
        }
    }

}
