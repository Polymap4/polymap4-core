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
package org.polymap.core.runtime.cache.test;

import org.polymap.core.runtime.cache.Cache;
import org.polymap.core.runtime.cache.CacheManager;

import junit.framework.TestCase;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ConcurrentMapTest
        extends TestCase {

    public void testEviction() throws InterruptedException {
        Cache<Object,byte[]> cache = CacheManager.instance().newCache( "Test" );
        
        while (true) {
            System.out.println( "adding 1000 to " + cache.size() );
            for (int i=0; i<1000; i++) {
                cache.putIfAbsent( new Object(), new byte[1024] );
            }
            Thread.sleep( 100 );
        }        
    
    }
    
}
