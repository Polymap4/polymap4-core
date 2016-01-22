/* 
 * polymap.org
 * Copyright (C) 2012-2013, Polymap GmbH. All rights reserved.
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

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface Cache<K,V> {

    /** 
     * Indicates an unknown cache element size. 
     */
    public static final int             ELEMENT_SIZE_UNKNOW = -1;
    
    
    public String getName();


    /**
     * Get the element for the given key. If the given key is not yet in the cache
     * then call the given loader to get the element value.
     * <p/>
     * This is the default way to add new elements to the cache. This method does
     * both: check for a given key <b>and</b> add if not yet registered. It prevents
     * race conditions that would occur between {@link #get(Object)} and put() if the
     * check would be done in the client code. The given loader should 'create' the
     * cache element right in the {@link CacheLoader#load(Object)} method and not
     * beforehand.
     * <p/>
     * This API allows to create the <b>CacheLoader as inner class</b> right within
     * the call. However, consider re-using an existing CacheLoader in order to avoid
     * the overhead of object creation - even if this may lead to slightly more
     * uncommon code.
     * 
     * @param key
     * @param loader
     * @return The already added element for the given key, or the newly created
     *         element if the key was not yet in the cache.
     * @throws Exception
     */
    public <E extends Exception> V get( K key, CacheLoader<K,V,E> loader ) throws E;

    
//    public default V get( K key, Callable<V> loader ) throws Exception {
//        return get( key, new CacheLoader<K,V,Exception>() {
//            @Override
//            public V load( @SuppressWarnings("hiding") K key ) throws Exception {
//                return loader.call();
//            }
//        });
//    }

    
    /**
     * The cache element for the given key, or null if there is no such element in
     * the cache.
     * 
     * @param key
     * @return The element for the given key, or null.
     * @throws CacheException
     */
    public V get( K key ) throws CacheException;


    /**
     * Add the given element to cache, if it is <b>not</b> yet in the cache. This is
     * equivalent of calling <code>putIfAbsent(key, value, ELEMENT_SIZE_UNKNOWN)</code>.
     * <p/>
     * Consider using {@link #get(Object, CacheLoader)} instead.
     * 
     * @see EvictionAware
     * @see #putIfAbsent(Object, Object, int)
     * @return The element for the given key, or null if the key was <b>not</b> yet
     *         in the cache.
     */
    public V putIfAbsent( K key, V value ) throws CacheException;

    
    /**
     * Add the given element to cache, if it is <b>not</b> yet in the cache.
     * <p/>
     * Consider using {@link #get(Object, CacheLoader)} instead.
     * 
     * @see #get(Object, CacheLoader)
     * @param elementMemSize The size of the given element in memory.
     * @return The element for the given key, or null if the key was <b>not</b> yet
     *         in the cache.
     */
    public V putIfAbsent( K key, V value, int elementMemSize ) throws CacheException;
    
    
    public V remove( K key ) throws CacheException;
    
    public int size();
    
    public void dispose();

    public boolean isDisposed();

    public void clear();

    public Iterable<V> values();

    public Set<K> keySet();
    
//    public boolean addEvictionListener( CacheEvictionListener listener );
//
//    public boolean removeEvictionListener( CacheEvictionListener listener );
    
}
