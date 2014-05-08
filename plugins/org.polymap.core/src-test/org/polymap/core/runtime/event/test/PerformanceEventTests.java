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

import java.util.List;

import java.lang.reflect.Method;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.internal.jobs.JobManager;
import org.eclipse.core.runtime.jobs.IJobManager;

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
@SuppressWarnings("restriction")
public class PerformanceEventTests
        extends TestCase {

    private static Log log = LogFactory.getLog( PerformanceEventTests.class );
    
    static {
        System.setProperty( "org.apache.commons.logging.simplelog.defaultlog", "info" );
        System.setProperty( "org.apache.commons.logging.simplelog.log.org.polymap.core.runtime.event", "debug" );
        
        try {
            Method m = JobManager.class.getDeclaredMethod( "getInstance", new Class[0] );
            m.setAccessible( true );
            IJobManager jobManager = (IJobManager)m.invoke( null, new Object[0] );
            log.info( "JobManager: " + jobManager );            
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }
    }

    private int         count, target;
    
    
    @Override
    protected void setUp() throws Exception {
    }
    
    
    @Override
    protected void tearDown() throws Exception {
    }

    
    public void testPublishNoDelay() throws InterruptedException {
        count = 0;
        target = 1000000;
        
        // handler
        Object handler = new Object() {
            @EventHandler(scope=Scope.JVM)
            public void countEvent( PerformanceTestEvent ev ) {
                if (++count == target-1) {
                    synchronized (this) { notifyAll(); }
                }
            }
        };
        EventManager.instance().subscribe( handler );

        // loop: publish event
        Timer timer = new Timer();
        for (int i=0; i<target; i++) {
            EventManager.instance().publish( new PerformanceTestEvent( this ) );
        }
        log.info( "published events: " +  target + " - " + timer.elapsedTime() + "ms" );
        
        // wait for results
        while (count < target-1) {
            synchronized (this) { wait( 100 ); }
        }
        log.info( "count: " + count + " - " + timer.elapsedTime() + "ms" );
        EventManager.instance().unsubscribe( handler );
    }
    
    
    public void testPublishDelay() throws InterruptedException {
        count = 0;
        target = 1000000;
        
        // handler
        Object handler = new Object() {
            @EventHandler(scope=Scope.JVM,delay=100)
            public void countEvent( List<PerformanceTestEvent> evs ) {
                log.info( "handle delayed (100ms): " + evs.size() );
                count += evs.size();
                if (count >= target-1) {
                    synchronized (this) { notifyAll(); }
                }
            }
        };
        EventManager.instance().subscribe( handler );

        // loop: publish event
        Timer timer = new Timer();
        for (int i=0; i<target; i++) {
            EventManager.instance().publish( new PerformanceTestEvent( this ) );
            //Thread.sleep( 10 );
        }
        log.info( "published events: " +  target + " - " + timer.elapsedTime() + "ms" );
        
        // wait for results
        while (count < target-1) {
            synchronized (this) { wait( 100 ); }
        }
        log.info( "count: " + count + " - " + timer.elapsedTime() + "ms" );
        EventManager.instance().unsubscribe( handler );
    }
    
    
    public void testSyncPublishNoDelay() throws InterruptedException {
        count = 0;
        target = 100000;
        
        // handler
        Object handler = new Object() {
            @EventHandler(scope=Scope.JVM)
            public void countEvent( PerformanceTestEvent ev ) {
                if (++count == target-1) {
                    synchronized (this) { notifyAll(); }
                }
            }
        };
        EventManager.instance().subscribe( handler );

        // loop: publish event
        Timer timer = new Timer();
        for (int i=0; i<target; i++) {
            EventManager.instance().syncPublish( new PerformanceTestEvent( this ) );
        }
        log.info( "published events: " +  target + " - " + timer.elapsedTime() + "ms" );
        Assert.assertEquals( count, target );

        EventManager.instance().unsubscribe( handler );
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
