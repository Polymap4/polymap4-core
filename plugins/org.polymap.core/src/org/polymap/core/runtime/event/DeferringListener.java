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

import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import org.polymap.core.runtime.session.SessionContext;
import org.polymap.core.runtime.session.SessionSingleton;
import org.polymap.core.ui.UIUtils;

/**
 * 
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
abstract class DeferringListener
        extends DecoratingListener {

    private static Log log = LogFactory.getLog( DeferringListener.class );

    /**
     * Counter for pending events per session.
     */
    static class SessionUICallbackCounter
            extends SessionSingleton {
        
        public static int jobStarted( EventListener delegate ) {
            return delegate instanceof DisplayingListener && SessionContext.current() != null ? 
                    instance( SessionUICallbackCounter.class ).doJobStarted() : -1;
        }
        
        public static int jobFinished( EventListener delegate ) {
            return delegate instanceof DisplayingListener && SessionContext.current() != null ? 
                    instance( SessionUICallbackCounter.class ).doJobFinished() : -1;
        }

        // instance ***************************************
        
        private AtomicInteger       jobCount = new AtomicInteger( 0 );
        
        private AtomicInteger       maxJobCount = new AtomicInteger( 0 );
        
        protected synchronized int doJobStarted() {
            maxJobCount.incrementAndGet();
            log.debug( "UICallback: job started. counter=" + jobCount.get() );
            if (jobCount.getAndIncrement() == 0) {
                UIUtils.sessionDisplay().asyncExec( new Runnable() {
                    public void run() {
                        UIUtils.activateCallback( "DeferredEvents" );
                        log.debug( "UICallback: ON (counter=" + jobCount.get() + ")" );
                    }
                });
            }
            return jobCount.get();
        }
        
        protected synchronized int doJobFinished() {
            log.debug( "UICallback: job finished. counter=" + jobCount.get() );
            if (jobCount.decrementAndGet() == 0) {
                final int max = maxJobCount.getAndSet( 0 );
                UIUtils.sessionDisplay().asyncExec( new Runnable() {
                    public void run() {
                        UIUtils.deactivateCallback( "DeferredEvents" );
                        log.debug( "UICallback: OFF (counter=" + jobCount.get() + ", max=" + max + ")" );
                    }
                });
            }
            return jobCount.get();
        }
    }
    

    // instance *******************************************
    
    protected int                       delay = 3000;
    
    protected int                       maxEvents = 10000;

    
    public DeferringListener( EventListener delegate, int delay, int maxEvents ) {
        super( delegate );
        this.delay = delay;
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
            this.events = new ArrayList( events );
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
    
}
