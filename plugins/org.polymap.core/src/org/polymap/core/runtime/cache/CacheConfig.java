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

import org.polymap.core.runtime.config.Config2;
import org.polymap.core.runtime.config.Configurable;
import org.polymap.core.runtime.config.DefaultInt;
import org.polymap.core.runtime.config.Mandatory;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class CacheConfig
        extends Configurable
        implements Cloneable {
    
    /**
     * The {@link CacheConfig} with all values set to default.
     */
    public static final CacheConfig defaults() {
        return new CacheConfig();
    }

    
    // instance *******************************************
    
    @Mandatory
    @DefaultInt( 1024 )
    public Config2<CacheConfig,Integer> elementMemSize;
    
    @Mandatory
    @DefaultInt( 4 )
    public Config2<CacheConfig,Integer> concurrencyLevel;
    
    @Mandatory
    @DefaultInt( 1024 )
    public Config2<CacheConfig,Integer> initSize;
    
    
    @Override
    public Object clone() throws CloneNotSupportedException {
        return defaults()
                .concurrencyLevel.put( concurrencyLevel.get() )
                .initSize.put( initSize.get() )
                .elementMemSize.put( elementMemSize.get() );
        
    }

    
    /**
     * See {@link #concurrencyLevel}.
     * @see #concurrencyLevel
     */
    public CacheConfig concurrencyLevel( int value ) {
        concurrencyLevel.set( value );
        return this;
    }
    
    
    /**
     * See {@link #concurrencyLevel}.
     * @see #concurrencyLevel
     */
    public CacheConfig initSize( int value ) {
        initSize.set( value );
        return this;
    }

    
    /**
     * Same as calling <code>CacheManager.instance().newCache( this )</code>.
     *
     * @return Newly created cache instance.
     */
    public <K,V> Cache<K,V> createCache() {
        return CacheManager.instance().newCache( this );
    }
    
}
