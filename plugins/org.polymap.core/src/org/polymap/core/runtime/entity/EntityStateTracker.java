/* 
 * polymap.org
 * Copyright 2011-2012, Polymap GmbH. All rights reserved.
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
package org.polymap.core.runtime.entity;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import com.google.common.collect.MapMaker;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import org.polymap.core.operation.OperationSupport;
import org.polymap.core.runtime.SessionContext;
import org.polymap.core.runtime.SessionSingleton;
import org.polymap.core.runtime.entity.EntityStateEvent.EventType;
import org.polymap.core.runtime.event.EventFilter;
import org.polymap.core.runtime.event.EventHandler;
import org.polymap.core.runtime.event.EventManager;

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
public class EntityStateTracker
        extends SessionSingleton {

    private static Log log = LogFactory.getLog( EntityStateTracker.class );
    
    /** 
     * The global map of stored entities and their tracked.
     * <p/>
     * XXX access should be read/write locked; however, I don't seem to be able to release
     * locks in all cases; so I'm ignoring race cond between prepare and apply in Updater 
     */
    private static Map<EntityHandle,Long>            stored = new MapMaker()
            .concurrencyLevel( 8 ).initialCapacity( 1024 ).makeMap();

    private static Map<EntityStateTracker,Object>   instances = new MapMaker()
            .concurrencyLevel( 4 ).initialCapacity( 32 ).weakKeys().makeMap();


    public static EntityStateTracker instance() {
        return instance( EntityStateTracker.class );
    }

    
    // instance *******************************************
    
    /** The tracked timestamps of this session. */
    private ConcurrentMap<EntityHandle,Long>         tracked = new MapMaker()
            .concurrencyLevel( 8 ).initialCapacity( 1024 ).makeMap();

    
    public EntityStateTracker() {
        instances.put( this, new Object() );
    }


    protected void finalize() throws Throwable {
        log.info( "FINALIZED." );
        tracked.clear();
    }


    /**
     * 
     * @param handler An {@link IEntityStateListener} or any other
     *        {@link EventHandler annotated} object.
     * @param filters
     */
    public void addListener( Object handler, EventFilter... filters ) {
        SessionContext context = SessionContext.current();
        assert context != null : "No context when registering IEntityStateListener!";

        // if impl changes then check QiModule#addListener too!
        EventManager.instance().subscribe( handler, filters );
    }

    
    public void removeListener( Object handler ) {
        EventManager.instance().unsubscribe( handler );
    }

    
    /**
     * Register the given surrogate (object, feature, entity) with the given
     * timestamp.
     * <p/>
     * This can be used by data sources that do not have the ability to track tracked
     * of locally read/changed entities. On next session save the tracked can be
     * checked against global timestamp of the respective surrogate.
     * <p/>
     * The semantics is copy-on-write ...
     * 
     * @param src
     * @param key The surrogate of an entity, feature, object, etc.
     * @param timestamp The timestamp to track for the given object.
     * @param upgrade True signals that an existing timestamp is replaced be the
     *        given one.
     */
    public void track( Object src, EntityHandle key, long timestamp, boolean upgrade ) {
        Long old = tracked.put( key, timestamp );
        
        if (!upgrade && old != null && !old.equals( timestamp )) {
            tracked.put( key, old );
            throw new IllegalArgumentException( "Entity already tracked: " + key );
        }
        
//        EventJob job = new EventJob( this, 
//                new EntityStateEvent( src, keys, EventType.CHANGE ) );
//        job.schedule();
    }

    
    public void forget( EntityHandle key ) {
        tracked.remove( key );    
    }


    public void revert( OperationSupport os, IProgressMonitor monitor ) {
        tracked.clear();
    }


    public boolean isConcurrentlyTracked( EntityHandle key ) {
        // XXX implementation
        return false;
    }

    
    /**
     *
     * @param key
     * @param check The timestamp to check for the given entity. Null signals
     *        that the tracked timestamp of the session is to be used.
     */
    public boolean isConflicting( EntityHandle key, Long check ) {
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
        
        private Map<EntityHandle,Long>  checked = new HashMap();
        
        
        Updater( IProgressMonitor monitor ) {
            this.monitor = monitor;
            this.startTime = System.currentTimeMillis();
            
            monitor.beginTask( "EntityStateTracker", IProgressMonitor.UNKNOWN );
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
        public void checkSet( EntityHandle key, Long check, Long set )
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

            EventManager.instance().publish( new EntityStateEvent( 
                    SessionContext.current(), eventSource, 
                    checked.keySet(), EventType.COMMIT ) );
        }


        public int size() {
            return checked.size();
        }
        
    }


//    /**
//     * Fire the given {@link EntityStateEvent} from within a separate job/thread.
//     * <p/>
//     * This must not be an {@link UIJob} in order to have no {@link SessionContext}
//     * mapped to this thread.
//     */
//    static class EventJob
//            extends Job {
//
//        /** Schedule many jobs but let run only one EventJob at a given time. */
//        static final ISchedulingRule        exclusiv = new ISchedulingRule() {
//            
//            public boolean isConflicting( ISchedulingRule other ) {
//                return other == this;
//            }
//            
//            public boolean contains( ISchedulingRule other ) {
//                return other == this;
//            }
//        };
//        
//        private EntityStateEvent         ev;
//        
//        
//        EventJob( EntityStateEvent ev ) {
//            super( Messages.get( "ModelChangeTracker_EventJob_title" ) );
//            this.ev = ev;
//            setPriority( Job.LONG );
//            setRule( exclusiv );
//        }
//
//        
//        protected IStatus run( IProgressMonitor monitor ) {
//            monitor.beginTask( Messages.get( "ModelChangeTracker_EventJob_title"), IProgressMonitor.UNKNOWN );
//            log.info( "EventJob: started..." );
//            for (EntityStateTracker instance : instances.keySet()) {
//                for (SessionListener listener : instance.listeners) {
//                    // canceled?
//                    if (monitor.isCanceled()) {
//                        return Status.CANCEL_STATUS;
//                    }
//                    // remove invalid listener
//                    else if (!listener.isValid()) {
//                        log.warn( "Removing invalid listener: " + listener );
//                        instance.listeners.remove( listener );
//                    }
//                    // call listener
//                    else {
//                        listener.modelChanged( ev );
//                    }
//                    monitor.worked( 1 );
//                }
//            }
//            return Status.OK_STATUS;
//        }
//
//    }


//    /**
//     * Holds a {@link IEntityStateListener} and the corresponding
//     * {@link SessionContext}.
//     */
//    class SessionListener
//            implements IEntityStateListener {
//        
//        private WeakReference<SessionContext>   contextRef;
//        
//        private IEntityStateListener             listener;
//
//        
//        public SessionListener( IEntityStateListener listener, SessionContext context ) {
//            this.listener = listener;
//            this.contextRef = context != null ? new WeakReference( context ) : null;
//        }
//
//        public boolean isValid() {
//            SessionContext context = null;
//            return contextRef != null 
//                    && (context = contextRef.get()) != null
//                    && !context.isDestroyed()
//                    && listener != null
//                    && listener.isValid();
//        }
//
//        public void modelChanged( final EntityStateEvent ev ) {
//            SessionContext context = contextRef.get();
//            if (context != null ) {
//                if (context.isDestroyed()) {
//                    log.warn( "SessionContext was destroyed without removing the listener: " + listener );
//                    listener = null;
//                }
//                else {
//                    context.execute( new Runnable() {
//                        public void run() {
//                            try {
//                                listener.modelChanged( ev );
//                            }
//                            catch (Throwable e) {
//                                log.warn( "Error while processing EntityStateEvent: " + ev, e );
//                            }
//                        }
//                    });
//                }
//            }
//            else {
//                log.warn( "Listener has no context: " + listener );
//                listener = null;
//            }
//        }
//
//        public boolean equals( Object obj ) {
//            if (obj == this) {
//                return true;
//            }
//            else if (obj instanceof SessionListener) {
//                return listener == ((SessionListener)obj).listener;
//            }
//            else {
//                throw new IllegalStateException( "obj is not an instance of SessioningListener: " + obj);
//            }
//        }
//        
//    }
    
}
