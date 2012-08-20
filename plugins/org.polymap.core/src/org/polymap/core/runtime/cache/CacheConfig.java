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
public class CacheConfig {
    
    public static final int         DEFAULT_ELEMENT_SIZE = 1024 * 10;
    
    public static final int         DEFAULT_CONCURRENCY_LEVEL = Runtime.getRuntime().availableProcessors() * 2;
    
    public static final int         DEFAULT_INIT_SIZE = 1024;
    
    /**
     * The {@link CacheConfig} with all values set to default.
     */
    public static final CacheConfig DEFAULT = new CacheConfig();

    
    // instance *******************************************
    
    protected int                   elementMemSize = DEFAULT_ELEMENT_SIZE;
    
    protected int                   concurrencyLevel = DEFAULT_CONCURRENCY_LEVEL;
    
    protected int                   initSize = DEFAULT_INIT_SIZE;

    
    /**
     * Creates a new cache config with default value:
     * <ul>
     * <li>{@link #elementMemSize}: 10Kb
     * <li>{@link #concurrencyLevel}: 2 x available processors
     * <li>{@link #initSize}: 1024
     * </ul>
     */
    protected CacheConfig() {
    }
    
    protected CacheConfig( CacheConfig other ) {
        this.concurrencyLevel = other.concurrencyLevel;
        this.initSize = other.initSize;
        this.elementMemSize = other.elementMemSize;
        
    }
    
    public CacheConfig defaultElementSize( int value ) {
        CacheConfig result = new CacheConfig( this );
        result.elementMemSize = value;
        return result;
    }

    public CacheConfig concurrencyLevel( int value ) {
        CacheConfig result = new CacheConfig( this );
        result.concurrencyLevel = value;
        return result;
    }
    
    public CacheConfig initSize( int value ) {
        CacheConfig result = new CacheConfig( this );
        this.initSize = value;
        return result;
    }

}
