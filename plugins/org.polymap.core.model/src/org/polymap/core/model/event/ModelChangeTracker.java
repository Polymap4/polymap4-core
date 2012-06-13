/* 
 * polymap.org
 * Copyright 2011, Polymap GmbH. All rights reserved.
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
package org.polymap.core.model.event;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import java.lang.ref.WeakReference;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.MapMaker;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;

import org.polymap.core.model.ConcurrentModificationException;
import org.polymap.core.model.Messages;
import org.polymap.core.model.event.ModelStoreEvent.EventType;
import org.polymap.core.operation.OperationSupport;
import org.polymap.core.runtime.ListenerList;
import org.polymap.core.runtime.SessionContext;
import org.polymap.core.runtime.SessionSingleton;
import org.polymap.core.runtime.UIJob;

/**
 * Provides information about modifications of a given entity or feature that might
 * have triggered by the session of the caller or another(!) session within this JVM.
 * <p/>
 * This class provides a general API and implementation to track the
 * version/timestamp of all kind of model entities, features, objects. It helps to
 * find {@link ConcurrentModificationException}s between the sessions of an JVM.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ModelChangeTracker
        extends SessionSingleton {

    private static Log log = LogFactory.getLog( ModelChangeTracker.class );
    
    /** 
     * The global map of stored entities and their tracked.
     * <p/>
     * XXX access should be read/write locked; however, I don't seem to be able to release
     * locks in all cases; so I'm ignoring race cond between prepare and apply in Updater 
     */
    private static Map<ModelHandle,Long>    stored = new MapMaker()
            .concurrencyLevel( 8 ).initialCapacity( 1024 ).makeMap();

    private static EventJob                 lastJob;
    
    private static Map<ModelChangeTracker,Object> instances = new MapMaker()
            .concurrencyLevel( 4 ).initialCapacity( 32 ).weakKeys().makeMap();


    public static ModelChangeTracker instance() {
        return instance( ModelChangeTracker.class );
    }

    
    // instance *******************************************
    
    private ListenerList<SessionListener>   listeners = new ListenerList( ListenerList.EQUALITY );

    /** The tracked timestamps of this session. */
    private ConcurrentMap<ModelHandle,Long>     tracked = new MapMaker()
            .concurrencyLevel( 8 ).initialCapacity( 1024 ).makeMap();

    
    public ModelChangeTracker() {
        instances.put( this, new Object() );
    }


    protected void finalize() throws Throwable {
        log.info( "FINALIZED." );
        listeners.clear();
        tracked.clear();
    }


    public boolean addListener( IModelStoreListener l ) {
        SessionContext context = SessionContext.current();
        assert context != null : "No context when registering ImodelStoreListener!";
        return listeners.add( new SessionListener( l, context ) );
    }

    public boolean removeListener( IModelStoreListener l ) {
        return listeners.remove( new SessionListener( l, null ) );
    }

    
    /**
     * Register the given surrogate (object, feature, entity) with the given
     * timestamp.
     * <p/>
     * This can be used by data sources that do not have the ability to track tracked
     * of locally read/changed entities. On next session save the tracked tracked can
     * be checked against global timestamp of the respective surrogate.
     * <p/>
     * The semantics is copy-on-write ...
     * 
     * @param src
     * @param key The surrogate of an entity, feature, object, etc.
     * @param timestamp The timestamp to track for the given object.
     * @param upgrade True signals that an existing timestamp is replaced be the
     *        given one.
     */
    public void track( Object src, ModelHandle key, long timestamp, boolean upgrade ) {
        Long old = tracked.put( key, timestamp );
        
        if (!upgrade && old != null && !old.equals( timestamp )) {
            tracked.put( key, old );
            throw new IllegalArgumentException( "Entity already tracked: " + key );
        }
        
//        EventJob job = new EventJob( this, 
//                new ModelStoreEvent( src, keys, EventType.CHANGE ) );
//        job.schedule();
    }

    
    public void forget( ModelHandle key ) {
        tracked.remove( key );    
    }


    public void revert( OperationSupport os, IProgressMonitor monitor ) {
        tracked.clear();
    }


    public boolean isConcurrentlyTracked( ModelHandle key ) {
        // XXX implementation
        return false;
    }

    
    /**
     *
     * @param key
     * @param check The timestamp to check for the given entity. Null signals
     *        that the tracked timestamp of the session is to be used.
     */
    public boolean isConflicting( ModelHandle key, Long check ) {
        check = check != null ? check : tracked.get( key );
        if (check == null) {
            throw new IllegalArgumentException( "No timestamp given and no tracked timestamp." );
        }

        Long storedTs = stored.get( key );
        log.debug( "key= " + key + ", timestamp= " + check );

        log.debug( "CHECK: " + check + " -- " + storedTs );
        return storedTs != null && storedTs.compareTo( check ) > 0;
    }


    /**
     * 
     * @return Newly created Updater.
     */
    public Updater newUpdater() {
        return new Updater( new NullProgressMonitor() );
    }


    /**
     * 
     * <p/>
     * The Updater aquires the read lock of the global table in ctor and holds it
     * until {@link #done()}. This makes sure that the global table does not change
     * while the Updater is working.
     * <p/>
     * The Updater is not synchronized, it must be called from a single thread only.
     */
    public class Updater {
        
        private IProgressMonitor        monitor;
        
        private long                    startTime;
        
        private Map<ModelHandle,Long>   checked = new HashMap();
        
        
        Updater( IProgressMonitor monitor ) {
            this.monitor = monitor;
            this.startTime = System.currentTimeMillis();
            
            monitor.beginTask( "ModelChangeTracker", IProgressMonitor.UNKNOWN );
        }
        
        
        public void done() {
            monitor.done();
        }


        public long getStartTime() {
            return startTime;
        }


        /**
         * Checks if the given surrogate has a conflicting global timestamp.
         * <p/>
         * This method can be called as part of the {@link OperationSupport} 2-phase
         * commit.
         * 
         * @param key The entity to check.
         * @param check The timestamp to check for the given entity. Null signals
         *        that the tracked timestamp of the session is to be used.
         * @param set The timestamp to set for the given entity. Null signals
         *        that the {@link #startTime} of the Updater is to be used.
         * @throws ConcurrentModificationException If the global timestamp of an
         *         object has changed.
         */
        public void checkSet( ModelHandle key, Long check, Long set )
        throws ConcurrentModificationException {
            log.debug( "check(): key= " + key /*+ ", timestamp= " + entry.getValue()*/ );
            
            // check/set should be atomic; however, its unlikely that different threads of the
            // same session modify the same object at the same time (isn't it?)
            
            check = check != null ? check : tracked.get( key );
            if (check == null) {
                throw new IllegalArgumentException( "No timestamp given and no tracked timestamp." );
            }
            
            if (isConflicting( key, check )) {
                throw new ConcurrentModificationException( "Objekt wurde von einem anderen Nutzer gleichzeitig verändert: " + key.id +
                        " (check=" + check + ", stored=" + stored.get( key ) + ")");
            }
            checked.put( key, set != null ? set : startTime );
            monitor.worked( 1 );
        }


        /**
         * 
         */
        public void apply( Object eventSource ) {
            stored.putAll( checked );

            ModelStoreEvent ev = new ModelStoreEvent( 
                    SessionContext.current(), eventSource, 
                    checked.keySet(), EventType.COMMIT );
            new EventJob( ev ).schedule();
        }


        public int size() {
            return checked.size();
        }
        
    }


    /**
     * Fire the given {@link ModelStoreEvent} from within a separate job/thread.
     * <p/>
     * This must not be an {@link UIJob} in order to have no {@link SessionContext}
     * mapped to this thread.
     */
    static class EventJob
            extends Job {

        /** Schedule many jobs but let run only one EventJob at a given time. */
        static final ISchedulingRule        exclusiv = new ISchedulingRule() {
            
            public boolean isConflicting( ISchedulingRule other ) {
                return other == this;
            }
            
            public boolean contains( ISchedulingRule other ) {
                return other == this;
            }
        };
        
        private ModelStoreEvent         ev;
        
        
        EventJob( ModelStoreEvent ev ) {
            super( Messages.get( "ModelChangeTracker_EventJob_title" ) );
            this.ev = ev;
            setPriority( Job.LONG );
            setRule( exclusiv );
        }

        
        protected IStatus run( IProgressMonitor monitor ) {
            monitor.beginTask( Messages.get( "ModelChangeTracker_EventJob_title"), IProgressMonitor.UNKNOWN );
            log.info( "EventJob: started..." );
            for (ModelChangeTracker instance : instances.keySet()) {
                for (SessionListener listener : instance.listeners) {
                    // canceled?
                    if (monitor.isCanceled()) {
                        return Status.CANCEL_STATUS;
                    }
                    // remove invalid listener
                    else if (!listener.isValid()) {
                        log.warn( "Removing invalid listener: " + listener );
                        instance.listeners.remove( listener );
                    }
                    // call listener
                    else {
                        listener.modelChanged( ev );
                    }
                    monitor.worked( 1 );
                }
            }
            return Status.OK_STATUS;
        }

    }


    /**
     * Holds a {@link IModelStoreListener} and the corresponding
     * {@link SessionContext}.
     */
    class SessionListener
            implements IModelStoreListener {
        
        private WeakReference<SessionContext>   contextRef;
        
        private IModelStoreListener             listener;

        
        public SessionListener( IModelStoreListener listener, SessionContext context ) {
            this.listener = listener;
            this.contextRef = context != null ? new WeakReference( context ) : null;
        }

        public boolean isValid() {
            SessionContext context = null;
            return contextRef != null 
                    && (context = contextRef.get()) != null
                    && !context.isDestroyed()
                    && listener != null
                    && listener.isValid();
        }

        public void modelChanged( final ModelStoreEvent ev ) {
            SessionContext context = contextRef.get();
            if (context != null ) {
                if (context.isDestroyed()) {
                    log.warn( "SessionContext was destroyed without removing the listener: " + listener );
                    listener = null;
                }
                else {
                    context.execute( new Runnable() {
                        public void run() {
                            try {
                                listener.modelChanged( ev );
                            }
                            catch (Throwable e) {
                                log.warn( "Error while processing ModelStoreEvent: " + ev, e );
                            }
                        }
                    });
                }
            }
            else {
                log.warn( "Listener has no context: " + listener );
                listener = null;
            }
        }

        public boolean equals( Object obj ) {
            if (obj == this) {
                return true;
            }
            else if (obj instanceof SessionListener) {
                return listener == ((SessionListener)obj).listener;
            }
            else {
                throw new IllegalStateException( "obj is not an instance of SessionListener: " + obj);
            }
        }
        
    }
    
}
