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
package org.polymap.core.runtime.event;

import java.util.EventObject;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

/**
 * 
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
abstract class DeferringListener
        extends DecoratingListener {

    private static Log log = LogFactory.getLog( DeferringListener.class );

    protected int                       delay;
    
    protected int                       maxEvents = 10000;

    
    public DeferringListener( EventListener delegate, int delay, int maxEvents ) {
        super( delegate );
        assert delay > 0;
        this.delay = delay;
        assert maxEvents > 100;
        this.maxEvents = maxEvents;
    }

    
    /**
     * 
     */
    public static class DeferredEvent
            extends Event {
    
        private List<EventObject>       events;
        
        DeferredEvent( Object source, List<EventObject> events ) {
            super( source );
            assert events != null;
            this.events = events;
        }
        
        public List<EventObject> events() {
            return events;
        }
        
        public List<EventObject> events( EventFilter filter ) {
            return events.stream()
                    .filter( ev -> filter.apply( ev ) )
                    .collect( Collectors.toList() );
        }
    }
    
    
//    /**
//     * Keep a callback request open while there are pending delayed, display events.
//     */
//    static class SessionUICallbackCounter
//    extends SessionSingleton {
//
//        protected static SessionUICallbackCounter instance() {
//            //return SingletonUtil.getSessionInstance( ServerPushManager.class );
//            return instance( SessionUICallbackCounter.class );
//        }
//
//        public static void jobStarted( EventListener delegate ) {
//            if (delegate instanceof DisplayingListener && SessionContext.current() != null) {
//                String id = String.valueOf( delegate.hashCode() );
//                instance().doJobStarted( id );
//            }
//        }
//
//        public static void jobFinished( EventListener delegate ) {
//            if (delegate instanceof DisplayingListener && SessionContext.current() != null) {
//                String id = String.valueOf( delegate.hashCode() );
//                instance().doJobFinished( id );
//            }
//        }
//
//
//        // instance ***************************************
//
//        private AtomicInteger       jobCount = new AtomicInteger( 0 );
//
//        private AtomicInteger       maxJobCount = new AtomicInteger( 0 );
//
//        protected void doJobStarted( String id ) {
//            log.debug( "Delayed events: job started for: " + id + ". counter: " + jobCount.incrementAndGet() );            
//            UIThreadExecutor.asyncFast( () -> UIUtils.activateCallback( id ) );
//        }
//
//        protected void doJobFinished( String id ) {
//            log.debug( "Delayed events: job finished for: " + id + ". counter: " + jobCount.decrementAndGet() );
//            UIThreadExecutor.asyncFast( () -> UIUtils.deactivateCallback( id ) );
//        }
//    }
    
}
