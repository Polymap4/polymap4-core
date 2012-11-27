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
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;

import org.polymap.core.runtime.UIJob;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class DeferringListener
        extends DecoratingListener {

    private static Log log = LogFactory.getLog( DeferringListener.class );

    private int                     delay = 3000;
    
    private int                     maxEvents = 10000;
    
    private Job                     job;
    
    private List<Event>             events = new ArrayList( 128 );
    

    public DeferringListener( EventListener delegate, int delay, int maxEvents ) {
        super( delegate );
        this.delay = delay;
        this.maxEvents = maxEvents;
    }

    @Override
    public void handleEvent( Event ev ) throws Exception {
        events.add( ev );
        
        if (job == null) {
            job = new UIJob( "DeferredEvents" ) {
                protected void runWithException( IProgressMonitor monitor ) throws Exception {
                    job = null;

                    final DeferredEvent dev = new DeferredEvent( DeferringListener.this, events );
                    events = new ArrayList( 128 );

                    delegate.handleEvent( dev );
                }
            };
            job.setSystem( true );
            job.schedule( delay);
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
    
        private List<Event>         events;
        
        DeferredEvent( Object source, List<Event> events ) {
            super( source );
            this.events = new ArrayList( events );
        }
        
        public List<Event> events() {
            return events;
        }
        
        public List<Event> events( EventFilter filter ) {
            return ImmutableList.copyOf( Iterables.filter( events, filter ) );
        }
    }
    
}
