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
package org.polymap.core.runtime.cache;

import java.util.concurrent.ConcurrentMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.runtime.ConcurrentReferenceHashMap;
import org.polymap.core.runtime.ConcurrentReferenceHashMap.ReferenceType;

/**
 * 
 * @see SoftCache
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class SoftCacheManager
        extends CacheManager {

    private static Log log = LogFactory.getLog( SoftCacheManager.class );

    // factory ********************************************
    
    private static final SoftCacheManager   instance = new SoftCacheManager(); 
    
    public static SoftCacheManager instance() {
        return instance;
    }

    
    // instance *******************************************

    private ConcurrentMap<String,SoftCache>     caches;
    
    
    protected SoftCacheManager() {
        this.caches = new ConcurrentReferenceHashMap( 256, 0.75f, 4,
                ReferenceType.STRONG, ReferenceType.WEAK, null );
    }

    
    @Override
    public <K, V> Cache<K, V> getOrCreateCache( String name, CacheConfig config ) {
        return add( new SoftCache( this, name, config ) );
    }


    @Override
    public <K, V> Cache<K, V> newCache( CacheConfig config ) {
        return add( new SoftCache( this, null, config ) );
    }
    

    protected <K, V> Cache<K, V> add( SoftCache cache ) {
        SoftCache elm = caches.put( cache.getName(), cache );
        if (elm != null) {
            caches.put( cache.getName(), elm );
            throw new IllegalArgumentException( "Cache name already exists: " + cache.getName() );
        }
        return cache;
    }
    
    
    protected void disposeCache( SoftCache cache ) {
        SoftCache elm = caches.remove( cache.getName() );
        assert elm != null;
    }
    
}
