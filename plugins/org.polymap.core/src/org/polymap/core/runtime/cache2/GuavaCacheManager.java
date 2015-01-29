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

import java.util.Properties;
import java.util.concurrent.ConcurrentMap;

import java.net.URI;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.configuration.Configuration;
import javax.cache.spi.CachingProvider;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.MapMaker;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class GuavaCacheManager
        implements CacheManager {

    private static Log log = LogFactory.getLog( GuavaCacheManager.class );

    private ConcurrentMap<String,Cache>     caches = new MapMaker().concurrencyLevel( 2 ).initialCapacity( 64 ).makeMap();


    @Override
    public CachingProvider getCachingProvider() {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    @Override
    public URI getURI() {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    @Override
    public ClassLoader getClassLoader() {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    @Override
    public Properties getProperties() {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    protected void checkOpen() {
        if (isClosed()) {
            throw new IllegalStateException( "CacheManager is already closed." );
        }
    }
    
    @Override
    public <K, V, C extends Configuration<K,V>> Cache<K,V> createCache( String cacheName, C configuration )
            throws IllegalArgumentException {
        checkOpen();
        GuavaCache result = new GuavaCache( cacheName, this, configuration );
        if (caches.putIfAbsent( cacheName, result ) != null) {
            throw new IllegalArgumentException( "Name is already mapped to a cache: " + cacheName );
        }
        return result;
    }


    @Override
    public <K, V> Cache<K,V> getCache( String cacheName, Class<K> keyType, Class<V> valueType ) {
        checkOpen();
        return caches.get( cacheName );
    }


    @Override
    public <K, V> Cache<K,V> getCache( String cacheName ) {
        checkOpen();
        return caches.get( cacheName );
    }


    @Override
    public Iterable<String> getCacheNames() {
        checkOpen();
        return caches.keySet();
    }


    @Override
    public void destroyCache( String cacheName ) {
        checkOpen();
    }


    @Override
    public void close() {
        if (!isClosed()) {
            caches.clear();
            caches = null;
        }
    }


    @Override
    public boolean isClosed() {
        return caches == null;
    }


    @Override
    public <T> T unwrap( Class<T> clazz ) {
        throw new IllegalArgumentException( "GuavaCacheManager: unknown type: " + clazz );
    }


    @Override
    public void enableManagement( String cacheName, boolean enabled ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    @Override
    public void enableStatistics( String cacheName, boolean enabled ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }
}
