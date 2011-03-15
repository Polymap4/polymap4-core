/* 
 * polymap.org
 * Copyright 2009, Polymap GmbH, and individual contributors as indicated
 * by the @authors tag.
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
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 * $Id$
 */

package org.polymap.core.qi4j;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import java.lang.ref.WeakReference;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import org.polymap.core.model.GlobalModelChangeEvent;
import org.polymap.core.model.GlobalModelChangeListener;
import org.polymap.core.model.Module;


/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
class GlobalEntityListeners {

    private static Log log = LogFactory.getLog( GlobalEntityListeners.class );
    
    private List<WeakReference> listeners = new ArrayList();
    
    private AtomicBoolean       working = new AtomicBoolean( false );

    
    public void add( GlobalModelChangeListener l ) {
        synchronized (listeners) {
            listeners.add( new WeakReference( l ) );    
        }
    }
    
    public void remove( GlobalModelChangeListener l ) {
        synchronized (listeners) {
            for (Iterator<WeakReference> it=listeners.iterator(); it.hasNext(); ) {
                GlobalModelChangeListener listener = (GlobalModelChangeListener)it.next().get();
                if (listener == null || listener == l) {
                    it.remove();
                }
            }
        }
    }
    

    void fireEvent( final Module source, final Set<String> ids, final GlobalModelChangeEvent.EventType eventType  ) {
        log.info( "fireEvent: type=" + eventType );
        if (!working.compareAndSet( false, true )) {
            log.warn( "!!! At least one other job is still working..." );
        }
//        else {
            Job job = new Job( "Änderungen aktivieren..." ) {
                protected IStatus run( IProgressMonitor monitor ) {
                    try {
                        GlobalModelChangeEvent gev = new GlobalModelChangeEvent( source, ids, eventType );
                        
                        // copy the list in order to prevent the listeners from
                        // blocking the list
                        List<GlobalModelChangeListener> ls = new ArrayList( listeners.size() );
                        synchronized (listeners) {
                            for (Iterator<WeakReference> it=listeners.iterator(); it.hasNext(); ) {
                                GlobalModelChangeListener listener = (GlobalModelChangeListener)it.next().get();
                                if (listener == null) {
                                    it.remove();
                                }
                                else {
                                    ls.add( listener );
                                }
                            }
                        }

                        // notify the listeners
                        for (Object l : ls) {
                            if (((GlobalModelChangeListener)l).isValid()) {
                                ((GlobalModelChangeListener)l).modelChanged( gev );
                            }
                            else {
                                log.warn( "..." );
                                listeners.remove( l );
                            }
                        }
                        return Status.OK_STATUS;
                    }
                    finally {
                        working.set( false );
                    }
                }
            };
            job.setPriority( Job.DECORATE );
            job.schedule();
//        }
    }

}
