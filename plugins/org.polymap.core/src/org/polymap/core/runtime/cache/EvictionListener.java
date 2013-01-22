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

/**
 * Listen to eviction events in a {@link Cache}. Instances are created by
 * {@link EvictionAware} cache entries or by an {@link EvictionAwareCacheLoader}.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface EvictionListener {

    /**
     * This method is called if the corresponding cache entry is evicted.
     * <p/>
     * This method must not block and return quickly. Extensive operations should
     * be executed asynchronously in a separate Thread or Job.
     * <p/> 
     * Implementation note: This method also might be called if the cache entry was
     * marked for eviction and be removed from the cache by client code.
     */
    public void onEviction( Object key );
    
}
