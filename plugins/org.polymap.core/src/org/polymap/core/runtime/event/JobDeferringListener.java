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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;

import org.polymap.core.Messages;
import org.polymap.core.runtime.UIJob;

/**
 * Handles delayed event publishing. Eclipse {@link Job} system is used to implement
 * the delayed execution.
 * <p/>
 * There is a problem when canceling a job for re-scheduling and the thread has no
 * session context. An exception is thrown in this case because of a missing jon
 * monitor provider. Performance has not been tested.
 * 
 * @see TimerDeferringListener
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
class JobDeferringListener
        extends DeferringListener {

    private static Log log = LogFactory.getLog( JobDeferringListener.class );

    private volatile Job                job;
    
    private volatile List<EventObject>  events;
    

    public JobDeferringListener( EventListener delegate, int delay, int maxEvents ) {
        super( delegate, delay, maxEvents );
    }

    
    @Override
    public void handleEvent( EventObject ev ) throws Exception {
        if (events == null) {
            events = new ArrayList( 128 );
        }
        events.add( ev );
        
        if (job == null) {
            SessionUICallbackCounter.jobStarted();
            
            job = new UIJob( Messages.get( "DeferringListener_jobTitle" ) ) {
                protected void runWithException( IProgressMonitor monitor ) throws Exception {
                    job = null;

                    final DeferredEvent dev = new DeferredEvent( JobDeferringListener.this, events );
                    events = null;
                    delegate.handleEvent( dev );
                    
                    SessionUICallbackCounter.jobFinished();
                }
            };
            job.setSystem( true );
            job.schedule( delay );
        }
        else {
            job.cancel();
            job.schedule( delay );
        }
    }
    
}
