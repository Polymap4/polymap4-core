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
    
    /**
     * The {@link CacheConfig} with all values set to default.
     */
    public static final CacheConfig DEFAULT = new CacheConfig();
    
    public static final int         DEFAULT_ELEMENT_SIZE = 1024 * 10;
    
    public static final int         DEFAULT_CONCURRENCY_LEVEL = 8;
    
    public static final int         DEFAULT_INIT_SIZE = 1024;
    
    protected int                   elementSize = DEFAULT_ELEMENT_SIZE;
    
    protected int                   concurrencyLevel = DEFAULT_CONCURRENCY_LEVEL;
    
    protected int                   initSize = DEFAULT_INIT_SIZE;
    
    
    public CacheConfig setDefaultElementSize( int elementSize ) {
        this.elementSize = elementSize;
        return this;
    }

    public CacheConfig setConcurrencyLevel( int concurrencyLevel ) {
        this.concurrencyLevel = concurrencyLevel;
        return this;
    }
    
    public CacheConfig setInitSize( int initSize ) {
        this.initSize = initSize;
        return this;
    }

}
