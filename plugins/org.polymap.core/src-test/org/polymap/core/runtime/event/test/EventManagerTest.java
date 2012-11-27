/* 
 * polymap.org
 * Copyright 2012, Falko Bräutigam. All rights reserved.
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

import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.runtime.Timer;
import org.polymap.core.runtime.event.Event;
import org.polymap.core.runtime.event.EventHandler;
import org.polymap.core.runtime.event.EventManager;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class EventManagerTest
        extends TestCase {

    private static Log log = LogFactory.getLog( EventManagerTest.class );
    
    static {
        System.setProperty( "org.apache.commons.logging.simplelog.defaultlog", "info" );
        System.setProperty( "org.apache.commons.logging.simplelog.log.org.polymap.core.runtime.event", "debug" );
    }

    private volatile int        count, target;
    
    @Override
    protected void setUp() throws Exception {
        count = 0;
    }

    @Override
    protected void tearDown() throws Exception {
        EventManager.instance().dispose();
    }

    
    public void tstSimple() {
        EventManager.instance().subscribe( this );
        EventManager.instance().publish( new TestEvent( this ) );
    }
    
    @EventHandler
    public void printEvent( TestEvent ev ) {
        log.info( "Session scope: " + ev );        
    }

    @EventHandler(scope=Event.Scope.JVM)
    public void failOnSessionEvent( TestEvent ev ) {
        log.info( "JVM scope: " + ev );
    }

    
    public synchronized void testPerformance() throws InterruptedException {
        EventManager.instance().subscribe( this );
        
        Timer timer = new Timer();
        target = 1000000;
        for (int i=0; i<target; i++) {
            EventManager.instance().publish( new PerformanceTestEvent( this ) );
        }
        
        while (count < target-1) {
            synchronized (this) { wait( 100 ); }
        }
        log.info( "count: " + count + " - " + timer.elapsedTime() + "ms" );
    }
    
    @EventHandler()
    public void countEvent( PerformanceTestEvent ev ) {
        if (++count == target-1) {
            synchronized (this) { notifyAll(); }
        }
    }

    @EventHandler(delay=3000)
    public void countEvent( List<TestEvent> events ) {
        log.info( "got some events: " + events.size() );
        count += events.size();
        if (count == target-1) {
            synchronized (this) { notifyAll(); }
        }
    }

    
    /**
     * 
     */
    class TestEvent
            extends Event {

        public TestEvent( Object source ) {
            super( source );
        }
    }
    
    /**
     * 
     */
    class PerformanceTestEvent
            extends Event {

        public PerformanceTestEvent( Object source ) {
            super( source );
        }
    }
    
}
