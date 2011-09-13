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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;

import org.polymap.core.model.ConcurrentModificationException;
import org.polymap.core.model.event.ModelStoreEvent.EventType;
import org.polymap.core.operation.OperationSupport;
import org.polymap.core.runtime.ConcurrentReferenceHashMap;
import org.polymap.core.runtime.ListenerList;
import org.polymap.core.runtime.SessionSingleton;
import org.polymap.core.runtime.UIJob;

/**
 * Provides information about modifications of a given entity or feature that might
 * have triggered by another session within this JVM.
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
    
    /** The global map of stored entities and their tracked. */
    private static Map<ModelHandle,Long>    stored = new HashMap( 1024 );

    private static ReadWriteLock            storedLock = new ReentrantReadWriteLock();

    private static EventJob                 lastJob;
    
    private static Map<ModelChangeTracker,Object> instances = 
            new ConcurrentReferenceHashMap( 32, 0.75f, 4 );


    public static ModelChangeTracker instance() {
        return instance( ModelChangeTracker.class );
    }

    
    // instance *******************************************
    
    private ListenerList<IModelStoreListener>   listeners = new ListenerList();

    /** The tracked timestamps of this session. */
    private ConcurrentHashMap<ModelHandle,Long> tracked = new ConcurrentHashMap( 1024, 0.75f, 4 );

    
    public ModelChangeTracker() {
        instances.put( this, new Object() );
    }


    public boolean addListener( IModelStoreListener l ) {
        return listeners.add( l );
    }

    public boolean removeListener( IModelStoreListener l ) {
        return listeners.remove( l );
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
        try {
            storedLock.readLock().lock();

            check = check != null ? check : tracked.get( key );
            if (check == null) {
                throw new IllegalArgumentException( "No timestamp given and no tracked timestamp." );
            }

            Long storedTs = stored.get( key );
            log.info( "key= " + key + ", timestamp= " + check );
            
            log.debug( "CHECK: " + check + " -- " + storedTs );
            return storedTs != null  
                    && storedTs.compareTo( check ) > 0;
        }
        finally {
            storedLock.readLock().unlock();
        }
    }


    /**
     * Only one Updater at a given time. Otherwise this method blocks.
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
            storedLock.readLock().lock();
        }
        
        
        public void done() {
            storedLock.readLock().unlock();
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
            log.info( "check(): key= " + key /*+ ", timestamp= " + entry.getValue()*/ );
            
            check = check != null ? check : tracked.get( key );
            if (check == null) {
                throw new IllegalArgumentException( "No timestamp given and no tracked timestamp." );
            }
            
            if (isConflicting( key, check )) {
                throw new ConcurrentModificationException( "Objekt wurde von einem anderen Nutzer gleichzeitig verändert: " + key.id );
            }
            checked.put( key, set != null ? set : startTime );
            monitor.worked( 1 );
        }


        /**
         * 
         */
        public void apply( Object eventSource, boolean omitMySession ) {
            try {
                storedLock.readLock().unlock();
                storedLock.writeLock().lock();
                
                stored.putAll( checked );
            }
            finally {
                storedLock.readLock().lock();
                storedLock.writeLock().unlock();
            }

            ModelChangeTracker src = omitMySession ? null : ModelChangeTracker.this;
            ModelStoreEvent ev = new ModelStoreEvent( eventSource, checked.keySet(), EventType.COMMIT );
            new EventJob( src, ev ).schedule();
        }


        public int size() {
            return checked.size();
        }
        
    }

    
    /**
     * 
     */
    static class EventJob
            extends UIJob {

        /** Schedule many jobs but let run only one EventJob at a given time. */
        static final ISchedulingRule        exclusiv = new ISchedulingRule() {
            
            public boolean isConflicting( ISchedulingRule other ) {
                return other == this;
            }
            
            public boolean contains( ISchedulingRule other ) {
                return other == this;
            }
        };
        
        private ModelChangeTracker    src;
        
        private ModelStoreEvent      ev;
        
        
        EventJob( ModelChangeTracker src, ModelStoreEvent ev ) {
            super( "GlobalEventJob" );
            this.src = src;
            this.ev = ev;
            setPriority( Job.LONG );
            setRule( exclusiv );
        }

        
        protected void runWithException( IProgressMonitor monitor )
        throws Exception {
            monitor.beginTask( "Globale Events", IProgressMonitor.UNKNOWN );
            for (ModelChangeTracker instance : instances.keySet()) {
                if (!instance.equals( src )) {
                    for (IModelStoreListener listener : instance.listeners) {
                        try {
                            if (monitor.isCanceled()) {
                                return;
                            }
                            listener.modelChanged( ev );
                            monitor.worked( 1 );
                        }
                        catch (Throwable e) {
                            log.warn( "Error while processing ModelStoreEvent: " + ev, e );
                        }
                    }
                }
            }
        }
        
    }

}
