/* 
 * polymap.org
 * Copyright (C) 2014, Falko Bräutigam. All rights reserved.
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
import java.util.Timer;
import java.util.TimerTask;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import org.polymap.core.Messages;
import org.polymap.core.runtime.SessionContext;

/**
 * Handles delayed event publishing. {@link Timer} is used to implement the delayed
 * execution.
 * 
 * @see JobDeferringListener
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
class TimerDeferringListener
        extends DeferringListener {

    private static Log log = LogFactory.getLog( TimerDeferringListener.class );

    private static Timer                scheduler = new Timer( "DeferringListener.Scheduler", true );

    private SchedulerTask               task;
    
    private volatile List<EventObject>  events;
    
    private volatile long               lastScheduled;
    
    private SessionContext              sessionContext = SessionContext.current();
    

    public  TimerDeferringListener( EventListener delegate, int delay, int maxEvents ) {
        super( delegate, delay, maxEvents );
    }

    
    @Override
    public void handleEvent( EventObject ev ) throws Exception {
        if (events == null) {
            SessionUICallbackCounter.jobStarted( delegate );
            events = new ArrayList( 512 );
            lastScheduled = 0;
        }
        events.add( ev );
        //assert events.size() > maxEvents;
        
        // re-schedule every XX ms at most; keep task queue small to reduce overhead
        // XXX events seem to get lost when task is executed inside this delay
        long now = System.currentTimeMillis();
        if (lastScheduled + 10 < now) {
            if (task != null) {
                task.cancel();
            }
            task = new SchedulerTask().schedule();
            lastScheduled = now;
        }
    }

    
    /**
     * 
     */
    class SchedulerTask
            extends TimerTask {
        
        public SchedulerTask schedule() {
            scheduler.schedule( this, delay );
            return this;
        }
        

        @Override
        public void run() {
            // primarily for testing; Eclipse's JobManager < 3.7.x does schedule in JUnit
            if (sessionContext == null) {
                doRun();
            }
            // avoid overhead of a Job if events are handled in in UI thread anyway
            else if (delegate instanceof DisplayingListener) {
                sessionContext.execute( new Runnable() {
                    public void run() { doRun(); }
                });
            }
            else {
                Job job = new Job( Messages.get( "DeferringListener_jobTitle" ) ) {
                    @Override
                    protected IStatus run( IProgressMonitor monitor ) {
                        sessionContext.execute( new Runnable() {
                            public void run() { doRun(); }
                        });
                        return Status.OK_STATUS;
                    }
                };
                job.setSystem( true );
                job.schedule();
            }

            // use the TimerThread to cleanup the task queue
            //scheduler.purge();
        }

        
        protected void doRun() {
            try {
                DeferredEvent dev = new DeferredEvent( TimerDeferringListener.this, events );
                events = null;
                delegate.handleEvent( dev );
            }
            catch (Exception e) {
                log.warn( "Error while handling defered events.", e );
            }
            finally {
                SessionUICallbackCounter.jobFinished( delegate );
            }
        }
    };

}
