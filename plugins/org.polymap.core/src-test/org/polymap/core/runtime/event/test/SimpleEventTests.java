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

import java.util.EventObject;
import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.runtime.event.Event;
import org.polymap.core.runtime.event.EventHandler;
import org.polymap.core.runtime.event.EventManager;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class SimpleEventTests
        extends TestCase 
        implements ListenerInterface {

    private static Log log = LogFactory.getLog( SimpleEventTests.class );
    
    static {
        System.setProperty( "org.apache.commons.logging.simplelog.defaultlog", "info" );
        System.setProperty( "org.apache.commons.logging.simplelog.log.org.polymap.core.runtime.event", "debug" );
    }

    private volatile int        count, target;
    
    private EventObject         sessionScopeResult, jvmScopeResult, listenerResult;
    
    
    @Override
    protected void setUp() throws Exception {
        EventManager.instance().subscribe( this );
    }


    @Override
    protected void tearDown() throws Exception {
        if (!EventManager.instance().unsubscribe( this )) {
            throw new RuntimeException( "Unable to remove handler!" );
        }
    }


    public void testSimple() throws InterruptedException {
        count = 0;
        EventManager.instance().syncPublish( new TestEvent( this ) );
        
        //Assert.assertNotNull( sessionScopeResult );
        Assert.assertNotNull( jvmScopeResult );
        Assert.assertNotNull( listenerResult );
        
        Assert.assertEquals( 0, count );
        Thread.sleep( 1500 );
        Assert.assertEquals( target, count );
    }
    
    
    @EventHandler
    public void printEvent( TestEvent ev ) {
        log.info( "Session scope: " + ev );
        sessionScopeResult = ev;
    }

    
    @EventHandler(scope=Event.Scope.JVM)
    public void failOnSessionEvent( TestEvent ev ) {
        log.info( "JVM scope: " + ev );
        jvmScopeResult = ev;
    }
    
    
    @Override
    public void handleEvent( EventObject ev ) {
        log.info( "ListenerInterface: " + ev );
        listenerResult = ev;
    }

    
    @EventHandler(delay=1000)
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
    class TestEvent extends Event {

        public TestEvent( Object source ) {
            super( source );
        }
    }
    
}

/**
 * 
 */
interface ListenerInterface {

    @EventHandler(scope=Event.Scope.JVM)
    void handleEvent( EventObject ev );
    
}

