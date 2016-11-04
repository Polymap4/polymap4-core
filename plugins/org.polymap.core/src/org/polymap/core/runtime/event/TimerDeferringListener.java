/* 
 * polymap.org
 * Copyright (C) 2014-2016, Falko Bräutigam. All rights reserved.
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
import org.polymap.core.runtime.Lazy;
import org.polymap.core.runtime.LockedLazyInit;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.runtime.UIJob;
import org.polymap.core.runtime.session.SessionContext;

/**
 * Handles delayed event publishing. {@link Timer} is used to implement the delayed
 * execution.
 * 
 * @see JobDeferringListener
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
class TimerDeferringListener
        extends DeferringListener {

    private static final Log log = LogFactory.getLog( TimerDeferringListener.class );

    private static Timer                scheduler = new Timer( "TimerDeferringListener.Scheduler", true );

    private Lazy<SchedulerTask>         task = new LockedLazyInit( () -> new SchedulerTask() );
    
    private SessionContext              sessionContext = SessionContext.current();
    

    public TimerDeferringListener( EventListener delegate, int delay, int maxEvents ) {
        super( delegate, delay, maxEvents );
    }

    
    @Override
    public void handleEvent( EventObject ev ) throws Exception {
        task.get().events.add( ev );
    }


    /**
     * Activating our own UI callback; EventManger will deactivate after our
     * handleEvent() returnes.
     */
    protected void activateUICallback( String callbackId ) {
        // commented out in favour of a simple timeout handled by BatikApplication 

//        if (delegate instanceof DisplayingListener) {
//            log.warn( "DEFERRED: " + callbackId );
//            UIThreadExecutor.async( () -> {
//                log.warn( "DEFERRED actually: " + callbackId );
//                UIUtils.activateCallback( callbackId );
//            });
//        }        
    }
    
    
    protected void deactivateUICallback( String callbackId ) {
//        if (delegate instanceof DisplayingListener) {
//            log.warn( "DEFERRED deactivate: " + callbackId );
//            UIThreadExecutor.async( () -> {
//                log.warn( "DEFERRED deactivate (actually): " + callbackId );
//                UIUtils.deactivateCallback( callbackId );
//            });
//        }
    }
    
    
    /**
     * 
     */
    protected class SchedulerTask
            extends TimerTask {
        
        private volatile List<EventObject>  events = new ArrayList( 128 );
        
        private String callbackId = "SchedulerTask-" + hashCode();
        
        public SchedulerTask() {
            try {
                scheduler.schedule( this, delay );

                activateUICallback( callbackId );
            }
            catch (Exception e) {
                // Timer already cancelled (?)
                log.error( "", e );
            }
        }
        

        @Override
        public void run() {
            // don't add events anymore
            TimerDeferringListener.this.task.clear();
            
            // avoid overhead of a Job if events are handled in UI thread anyway
            if (delegate instanceof DisplayingListener) {
                sessionContext.execute( () -> doRun() );
            }
            else {
                inJob( () -> {
                    if (sessionContext != null) {
                        sessionContext.execute( () -> doRun() );
                    }
                    else {
                        doRun();
                    }
                });
            }
            // use the TimerThread to cleanup the task queue
            //scheduler.purge();
        }


        protected void doRun() {
            try {
                DeferredEvent dev = new DeferredEvent( TimerDeferringListener.this, events );
                delegate.handleEvent( dev );
            }
            catch (Exception e) {
                log.warn( "Error while handling deferred events.", e );
            }
            finally {
                // release current request *after* events have been handled in the display thread
                deactivateUICallback( callbackId );
            }
        }


        protected void inThread( Runnable run ) {
            Polymap.executorService().submit( () -> {
                doRun();
            });
        }
        
        
        protected void inJob( Runnable run ) {
            Job job = new Job( Messages.get( "DeferringListener_jobTitle" ) ) {
                @Override
                protected IStatus run( IProgressMonitor monitor ) {
                    run.run();
                    return Status.OK_STATUS;
                }
            };
            job.setSystem( true );
            job.setPriority( UIJob.DEFAULT_PRIORITY );
            job.schedule();            
        }
    }

}
