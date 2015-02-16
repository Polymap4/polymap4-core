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

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.configuration.CacheEntryListenerConfiguration;
import javax.cache.configuration.CompleteConfiguration;
import javax.cache.configuration.Configuration;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.integration.CacheLoader;
import javax.cache.integration.CacheLoaderException;
import javax.cache.integration.CompletionListener;
import javax.cache.processor.EntryProcessor;
import javax.cache.processor.EntryProcessorException;
import javax.cache.processor.EntryProcessorResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class LoadingCacheWrapper<K,V>
        implements LoadingCache<K,V> {

    private static Log log = LogFactory.getLog( LoadingCacheWrapper.class );
    
    // instance *******************************************

    private Cache<K,V>                  delegate;
    
    private ThreadLocal<Loader<K,V>>    threadLoader = new ThreadLocal();
    
    
    public LoadingCacheWrapper( CacheManager cacheManager, CompleteConfiguration config ) {
        MutableConfiguration newConfig = config instanceof MutableConfiguration 
                ? (MutableConfiguration)config : new MutableConfiguration( config );
                
        newConfig.setCacheLoaderFactory( () -> new CacheLoader<K,V>() {
            @Override
            public V load( K key ) throws CacheLoaderException {
                return threadLoader.get().load( key );
            }
            @Override
            public Map<K,V> loadAll( Iterable<? extends K> keys ) throws CacheLoaderException {
                throw new RuntimeException( "not yet implemented." );
            }
        });
        
        delegate = cacheManager.createCache( "LoadingCache-" + hashCode(), newConfig );
    }


    public V get( K key, Loader<K,V> loader ) {
        threadLoader.set( loader );
        try {
            return get( key );
        }
        finally {
            threadLoader.remove();
        }
    }

    // delegate *******************************************
    
    public V get( K key ) {
        return delegate.get( key );
    }


    public Map<K,V> getAll( Set<? extends K> keys ) {
        return delegate.getAll( keys );
    }


    public boolean containsKey( K key ) {
        return delegate.containsKey( key );
    }


    public void loadAll( Set<? extends K> keys, boolean replaceExistingValues, CompletionListener completionListener ) {
        delegate.loadAll( keys, replaceExistingValues, completionListener );
    }


    public void put( K key, V value ) {
        delegate.put( key, value );
    }


    public V getAndPut( K key, V value ) {
        return delegate.getAndPut( key, value );
    }


    public void putAll( Map<? extends K,? extends V> map ) {
        delegate.putAll( map );
    }


    public boolean putIfAbsent( K key, V value ) {
        return delegate.putIfAbsent( key, value );
    }


    public boolean remove( K key ) {
        return delegate.remove( key );
    }


    public boolean remove( K key, V oldValue ) {
        return delegate.remove( key, oldValue );
    }


    public V getAndRemove( K key ) {
        return delegate.getAndRemove( key );
    }


    public boolean replace( K key, V oldValue, V newValue ) {
        return delegate.replace( key, oldValue, newValue );
    }


    public boolean replace( K key, V value ) {
        return delegate.replace( key, value );
    }


    public V getAndReplace( K key, V value ) {
        return delegate.getAndReplace( key, value );
    }


    public void removeAll( Set<? extends K> keys ) {
        delegate.removeAll( keys );
    }


    public void removeAll() {
        delegate.removeAll();
    }


    public void clear() {
        delegate.clear();
    }


    public <C extends Configuration<K,V>> C getConfiguration( Class<C> clazz ) {
        return delegate.getConfiguration( clazz );
    }


    public <T> T invoke( K key, EntryProcessor<K,V,T> entryProcessor, Object... arguments )
            throws EntryProcessorException {
        return delegate.invoke( key, entryProcessor, arguments );
    }


    public <T> Map<K,EntryProcessorResult<T>> invokeAll( Set<? extends K> keys, EntryProcessor<K,V,T> entryProcessor,
            Object... arguments ) {
        return delegate.invokeAll( keys, entryProcessor, arguments );
    }


    public String getName() {
        return delegate.getName();
    }


    public CacheManager getCacheManager() {
        return delegate.getCacheManager();
    }


    public void close() {
        delegate.close();
    }


    public boolean isClosed() {
        return delegate.isClosed();
    }


    public <T> T unwrap( Class<T> clazz ) {
        return delegate.unwrap( clazz );
    }


    public void registerCacheEntryListener( CacheEntryListenerConfiguration<K,V> cacheEntryListenerConfiguration ) {
        delegate.registerCacheEntryListener( cacheEntryListenerConfiguration );
    }


    public void deregisterCacheEntryListener( CacheEntryListenerConfiguration<K,V> cacheEntryListenerConfiguration ) {
        delegate.deregisterCacheEntryListener( cacheEntryListenerConfiguration );
    }


    public Iterator<javax.cache.Cache.Entry<K,V>> iterator() {
        return delegate.iterator();
    }
    
}
