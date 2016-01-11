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

import java.util.Random;

import java.io.PrintStream;

import junit.framework.TestCase;

import org.polymap.core.runtime.Timer;
import org.polymap.core.runtime.cache.Cache;
import org.polymap.core.runtime.cache.CacheConfig;
import org.polymap.core.runtime.cache.CacheLoader;
import org.polymap.core.runtime.cache.Soft2CacheManager;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class PerformanceCacheTest
        extends TestCase {

    Cache<Object,byte[]>        cache;
    
    PrintStream                 log = System.err;
    
    static {
        System.setProperty( "org.apache.commons.logging.simplelog.defaultlog", "info" );
        System.setProperty( "org.apache.commons.logging.simplelog.log.org.polymap.cache", "debug" );
    }

    protected void setUp() throws Exception {
        //cache = CacheManager.instance().newCache( CacheConfig.DEFAULT );
        cache = Soft2CacheManager.instance().newCache( CacheConfig.defaults()
                .initSize( 1000000 ).concurrencyLevel( 8 ) );
    }

    
    protected void tearDown() throws Exception {
        cache.dispose();
    }

    
    public void tstEviction() throws InterruptedException {
        while (true) {
            System.out.println( "adding 1000 to " + cache.size() );
            for (int i=0; i<1000; i++) {
                cache.putIfAbsent( new Object(), new byte[1024] );
            }
            Thread.sleep( 100 );
        }            
    }
    
    
    public void tstLoader() throws Exception {
        Timer timer = new Timer();
        int count = 0;
        while (timer.elapsedTime() < 10000) {
            log.println( "adding 1000 to " + cache.size() );
            for (int i=0; i<1000; i++) {
                cache.get( new Object(), key -> new byte[1024] );
                count++;
            }
            Thread.sleep( 100 );
        }
        long time = timer.elapsedTime();
        log.println( "Loops: " + count + " in " + time + "ms -> "
                + (1000f*count/time) + "/s");
    }


    public void testRandom() throws Exception {
        Timer timer = new Timer();
        int count = 0;
        Random random = new Random();
        ByteArrayLoader loader = new ByteArrayLoader();
        while (timer.elapsedTime() < 30000) {
            log.println( "adding 1000 to " + cache.size() );
            for (int i=0; i<10000; i++) {
                Integer key = new Integer( (int)(Math.abs( 
                        random.nextGaussian() ) * 400000) );
                cache.get( key, loader );
                count++;
            }
        }
        long time = timer.elapsedTime();
        log.println( "Loops: " + count + " in " + time + "ms -> "
                + (1000f*count/time) + "/s");
    }

    
    class ByteArrayLoader 
            implements CacheLoader<Object,byte[],Exception> {

        public byte[] load( Object key ) throws Exception {
            byte[] result = new byte[1024];
            //Random rand = new Random();
            for (int i=0; i<result.length; i++) {
                //result[i] = (byte)rand.nextInt();
            }
            return result;
        }
        
        public int size() throws Exception {
            return 1024;
        }
    }
    
}
