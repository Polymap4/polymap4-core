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
public interface Cache<K,V> {

    public String getName();
    
    public V get( K key ) throws CacheException;
    
    public V put( K key, CacheLoader<K,V> loader ) throws Exception;
    
    public V putIfAbsent( K key, V value ) throws CacheException;
    
    /**
     * @deprecated Use {@link #putIfAbsent(Object, Object)} instead.
     */
    public V put( K key, V value ) throws CacheException;
    
    public V remove( K key ) throws CacheException;
    
    public int size();
    
    public void dispose();

    public void clear();

    public Iterable<V> values();

    public boolean addEvictionListener( CacheEvictionListener listener );

    public boolean removeEvictionListener( CacheEvictionListener listener );
    
}
