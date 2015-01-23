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
import javax.cache.configuration.Factory;
import javax.cache.expiry.EternalExpiryPolicy;
import javax.cache.integration.CacheLoader;
import javax.cache.integration.CompletionListener;
import javax.cache.processor.EntryProcessor;
import javax.cache.processor.EntryProcessorException;
import javax.cache.processor.EntryProcessorResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.cache.CacheBuilder;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class GuavaCache<K,V>
        implements Cache<K,V> {

    private static Log log = LogFactory.getLog( GuavaCache.class );
    
    private String                                      name;
    
    private CacheManager                                manager;
    
    private Configuration<K,V>                          configuration;
    
    private com.google.common.cache.Cache<K,V>          delegate;


    protected GuavaCache( String name, CacheManager manager, Configuration<K,V> configuration ) {
        this.name = name;
        this.manager = manager;
        this.configuration = configuration;
        
        if (configuration instanceof CompleteConfiguration) {
            CacheBuilder builder = CacheBuilder.newBuilder().concurrencyLevel( 4 );
            
            // listeners
            CompleteConfiguration config = (CompleteConfiguration)configuration;
            if (config.getCacheEntryListenerConfigurations().iterator().hasNext()) {
                throw new IllegalArgumentException( "Registering listeners via configuration is not supported yet." );
            }
            // expiry
            if (!(config.getExpiryPolicyFactory().create() instanceof EternalExpiryPolicy)) {
                throw new IllegalArgumentException( "No other the EternalExpiryPolicy is supported yet." );                
            }
            // loader
            final Factory<CacheLoader<K,V>> loaderFactory = config.getCacheLoaderFactory();
            if (loaderFactory != null) {
                delegate = builder.build( new com.google.common.cache.CacheLoader<K,V>() {
                    private CacheLoader<K,V> loader = loaderFactory.create();
                    @Override
                    public V load( K key ) throws Exception {
                        return loader.load( key );
                    }
                    @Override
                    public Map<K,V> loadAll( Iterable<? extends K> keys ) throws Exception {
                        return loader.loadAll( keys );
                    }
                });
            }
            // no loader
            else {
                delegate = builder.build();
            }
        }
    }

    protected void checkOpen() {
        assert !isClosed() : "Cache is closed.";
    }

    @Override
    public V get( K key ) {
        checkOpen();
        return ((com.google.common.cache.LoadingCache)delegate).getUnchecked( key );
    }

    @Override
    public Map<K,V> getAll( Set<? extends K> keys ) {
        checkOpen();
        return delegate.getAllPresent( keys );
    }

    @Override
    public boolean containsKey( K key ) {
        checkOpen();
        return delegate.getIfPresent( key ) != null;
    }

    @Override
    public void loadAll( Set<? extends K> keys, boolean replaceExistingValues, CompletionListener completionListener ) {
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public void put( K key, V value ) {
        checkOpen();
        delegate.put( key, value );
    }

    @Override
    public V getAndPut( K key, V value ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public void putAll( Map<? extends K,? extends V> map ) {
        checkOpen();
        delegate.putAll( map );
    }

    @Override
    public boolean putIfAbsent( K key, V value ) {
        checkOpen();
        return delegate.asMap().putIfAbsent( key, value ) != null;
    }

    @Override
    public boolean remove( K key ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public boolean remove( K key, V oldValue ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public V getAndRemove( K key ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public boolean replace( K key, V oldValue, V newValue ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public boolean replace( K key, V value ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public V getAndReplace( K key, V value ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public void removeAll( Set<? extends K> keys ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public void removeAll() {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public void clear() {
        checkOpen();
        delegate.asMap().clear();
    }

    @Override
    public <T> T invoke( K key, EntryProcessor<K,V,T> entryProcessor, Object... arguments )
            throws EntryProcessorException {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public <T> Map<K,EntryProcessorResult<T>> invokeAll( Set<? extends K> keys, EntryProcessor<K,V,T> entryProcessor,
            Object... arguments ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public Iterator<Cache.Entry<K,V>> iterator() {
        return delegate.asMap().entrySet().stream()
                .<Cache.Entry<K,V>>map( entry -> new Cache.Entry<K,V>() {
                    @Override
                    public K getKey() { return entry.getKey(); }
                    @Override
                    public V getValue() { return entry.getValue(); }
                    @Override
                    public <T> T unwrap( Class<T> clazz ) { throw new IllegalArgumentException( "Guava does not provide an Entry type." ); } 
                })
                .iterator();
        
        //Iterables.transform( delegate.asMap().entrySet().iterator(), (Map.Entry<K,V> entry) -> { return null;} );
    }

    @Override
    public <C extends Configuration<K,V>> C getConfiguration( Class<C> clazz ) {
        return (C)configuration;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public CacheManager getCacheManager() {
        return manager;
    }

    @Override
    public void close() {
        
        delegate.cleanUp();
        delegate = null;
    }

    @Override
    public boolean isClosed() {
        return delegate != null;
    }

    @Override
    public <T> T unwrap( Class<T> clazz ) {
        return clazz.cast( delegate );
    }

    @Override
    public void registerCacheEntryListener( CacheEntryListenerConfiguration<K,V> cacheEntryListenerConfiguration ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public void deregisterCacheEntryListener( CacheEntryListenerConfiguration<K,V> cacheEntryListenerConfiguration ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }
    
}
