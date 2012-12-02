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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import org.eclipse.rwt.lifecycle.UICallBack;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;

import org.polymap.core.Messages;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.runtime.SessionSingleton;
import org.polymap.core.runtime.UIJob;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
class DeferringListener
        extends DecoratingListener {

    private static Log log = LogFactory.getLog( DeferringListener.class );

    /**
     * 
     */
    static class SessionUICallbackCounter
            extends SessionSingleton {

        public static SessionUICallbackCounter instance() {
            return instance( SessionUICallbackCounter.class );
        }
        
        private volatile int        jobCount = 0;
        
        private int                 maxJobCount = 0;
        
        public synchronized void jobStarted() {
            maxJobCount ++;
            if (jobCount++ == 0) {
                Polymap.getSessionDisplay().asyncExec( new Runnable() {
                    public void run() {
                        UICallBack.activate( "DeferredEvents" );
                        log.debug( "UI Callback activated." );
                    }
                });
            }
        }
        
        public synchronized void jobFinished() {
            if (--jobCount == 0) {
                final int temp = maxJobCount;
                maxJobCount = 0;
                Polymap.getSessionDisplay().asyncExec( new Runnable() {
                    public void run() {
                        UICallBack.deactivate( "DeferredEvents" );
                        log.debug( "UI Callback deactivated. (maxJobCount=" + temp + ")" );
                    }
                });
            }
        }
    }
    
    // instance *******************************************
    
    private int                     delay = 3000;
    
    private int                     maxEvents = 10000;
    
    private Job                     job;
    
    private List<EventObject>       events = new ArrayList( 128 );
    

    public DeferringListener( EventListener delegate, int delay, int maxEvents ) {
        super( delegate );
        this.delay = delay;
        this.maxEvents = maxEvents;
    }

    @Override
    public void handleEvent( EventObject ev ) throws Exception {
        events.add( ev );
        
        if (job == null) {
            job = new UIJob( Messages.get( "DeferringListener_jobTitle" ) ) {
                protected void runWithException( IProgressMonitor monitor ) throws Exception {
                    job = null;

                    final DeferredEvent dev = new DeferredEvent( DeferringListener.this, events );
                    events = new ArrayList( 128 );

                    delegate.handleEvent( dev );
                    SessionUICallbackCounter.instance().jobFinished();
                }
            };
            job.setSystem( true );
            job.schedule( delay );
            SessionUICallbackCounter.instance().jobStarted();
        }
        else {
            job.cancel();
            job.schedule( delay );
        }
    }

    
    /**
     * 
     */
    public static class DeferredEvent
            extends Event {
    
        private List<EventObject>       events;
        
        DeferredEvent( Object source, List<EventObject> events ) {
            super( source );
            this.events = new ArrayList( events );
        }
        
        public List<EventObject> events() {
            return events;
        }
        
        public List<EventObject> events( EventFilter filter ) {
            return ImmutableList.copyOf( Iterables.filter( events, filter ) );
        }
    }
    
}
