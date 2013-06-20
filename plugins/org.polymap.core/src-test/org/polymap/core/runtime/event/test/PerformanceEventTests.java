/* 
 * polymap.org
 * Copyright 2013, Falko Bräutigam. All rights reserved.
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
package org.polymap.core.runtime.event.test;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.runtime.Timer;
import org.polymap.core.runtime.event.Event;
import org.polymap.core.runtime.event.Event.Scope;
import org.polymap.core.runtime.event.EventHandler;
import org.polymap.core.runtime.event.EventManager;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class PerformanceEventTests
        extends TestCase {

    private static Log log = LogFactory.getLog( PerformanceEventTests.class );
    
    static {
        System.setProperty( "org.apache.commons.logging.simplelog.defaultlog", "info" );
        System.setProperty( "org.apache.commons.logging.simplelog.log.org.polymap.core.runtime.event", "debug" );
    }

    private int         count, target = 1000000;
    
    
    @Override
    protected void setUp() throws Exception {
        EventManager.instance().subscribe( this );
    }


    @Override
    protected void tearDown() throws Exception {
        EventManager.instance().unsubscribe( this );
    }

    
    public synchronized void testPerformance() throws InterruptedException {
        Timer timer = new Timer();
        for (int i=0; i<target; i++) {
            EventManager.instance().publish( new PerformanceTestEvent( this ) );
        }
        log.info( "published events: " +  target + " - " + timer.elapsedTime() + "ms" );
        
        while (count < target-1) {
            synchronized (this) { wait( 100 ); }
        }
        log.info( "count: " + count + " - " + timer.elapsedTime() + "ms" );
    }
    
    
    @EventHandler(scope=Scope.JVM)
    public void countEvent( PerformanceTestEvent ev ) {
        if (++count == target-1) {
            synchronized (this) { notifyAll(); }
        }
    }

    
    /**
     * 
     */
    static class PerformanceTestEvent
            extends Event {

        public PerformanceTestEvent( Object source ) {
            super( source );
        }
    }

}
