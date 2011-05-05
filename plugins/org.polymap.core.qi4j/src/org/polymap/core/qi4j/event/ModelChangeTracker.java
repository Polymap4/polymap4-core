/* 
 * polymap.org
 * Copyright 2011, Falko Bräutigam, and other contributors as
 * indicated by the @authors tag. All rights reserved.
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
package org.polymap.core.qi4j.event;

import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.unitofwork.NoSuchEntityException;

import org.eclipse.rwt.SessionSingletonBase;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import org.polymap.core.model.Entity;
import org.polymap.core.model.event.GlobalModelChangeEvent;
import org.polymap.core.model.event.GlobalModelChangeListener;
import org.polymap.core.model.event.ModelChangeEvent;
import org.polymap.core.model.event.ModelChangeListener;
import org.polymap.core.model.event.PropertyEventFilter;
import org.polymap.core.model.event.GlobalModelChangeEvent.EventType;
import org.polymap.core.qi4j.Qi4jPlugin;
import org.polymap.core.runtime.ListenerList;
import org.polymap.core.workbench.PolymapWorkbench;

/**
 * Provides most of the implementation to support entity event handling. {@link QiModule}
 * delegates handling of property and model listeners to the ModelChangeTracker.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @since 3.1
 */
public class ModelChangeTracker
        extends SessionSingletonBase
        implements PropertyChangeListener {

    private static Log log = LogFactory.getLog( ModelChangeTracker.class );
 
    private static ListenerList<GlobalModelChangeListener> globalModelListeners = 
            new ListenerList( ListenerList.IDENTITY, ListenerList.WEAK );
    
    private static Map<String,Long>                entityVersions = new HashMap( 1024 );
    
    private static ReadWriteLock                   entityVersionsLock = new ReentrantReadWriteLock();
    
    private static GlobalEventJob                  globalEventJob;

    private static PropertyChangeListener          staticPropertyListener;

    
    static {
        staticPropertyListener = new PropertyChangeListener() {
            public void propertyChange( PropertyChangeEvent ev ) {
                ModelChangeTracker instance = instance();
                if (instance != null) {
                    instance.propertyChange( ev );
                }
                else {
                    log.info( "propertyChange(): No ModelChangeTracker for this thread!" );
                }
            }
        };
        PropertyChangeSupport.globalListeners.add( staticPropertyListener );
    }
    
    
    public static void addGlobalModelChangeListener( GlobalModelChangeListener l ) {
        globalModelListeners.add( l );
    }

    public static void removeGlobalModelChangeListener( GlobalModelChangeListener l ) {
        globalModelListeners.remove( l );
    }


    /**
     * The instance of the current session, or null if the current thread is not
     * bound to any user session.
     */
    public static ModelChangeTracker instance() {
        return (ModelChangeTracker)getInstance( ModelChangeTracker.class );
    }
    
    
    // instance *******************************************
 
    private ListenerList<ModelChangeListener>       modelListeners = new ListenerList( ListenerList.EQUALITY );
    
    private ListenerList<PropertyChangeListener>    propertyListeners = new ListenerList( ListenerList.EQUALITY );


    /**
     * Add the given model listener. Has no effect if the same listener is already
     * registered. The given listener might be stored in a {@link WeakReference}, the
     * caller has to make sure that a strong reference exists as long as the listener
     * should receive events.
     * <p/>
     * The listener receives events from all entities of all modules of the current
     * session.
     */
    public void addModelChangeListener( ModelChangeListener l, PropertyEventFilter f ) {
        modelListeners.add( new FilteredModelChangeListener( l, f ) );
    }

    
    public void removeModelChangeListener( ModelChangeListener l ) {
        modelListeners.remove( new FilteredModelChangeListener( l, null ) );
    }

    /*
     * 
     */
    static class FilteredModelChangeListener
            implements ModelChangeListener {
        
        private ModelChangeListener         delegate;
        private PropertyEventFilter         filter;
        
        protected FilteredModelChangeListener( ModelChangeListener delegate, PropertyEventFilter filter ) {
            this.delegate = delegate;
            this.filter = filter;
        }

        public void modelChanged( ModelChangeEvent ev ) {
            ModelChangeEvent filterd = new ModelChangeEvent( ev.getSource() );
            int count = 0;
            for (PropertyChangeEvent pev : ev.events( filter )) {
                filterd.propertyChange( pev );
                count++;
            }
            if (count > 0) {
                delegate.modelChanged( ev );
            }
        }

        public boolean equals( Object obj ) {
            if (obj == this) {
                return true;
            }
            else if (obj instanceof FilteredModelChangeListener) {
                return delegate == ((FilteredModelChangeListener)obj).delegate;
            }
            return false;
        }
    }

    
    /**
     * 
     */
    protected void fireModelChangeEvent( ModelChangeEvent ev ) {
        assert ev != null;
        
        // fire local event
        for (ModelChangeListener listener : modelListeners) {
            try {
                listener.modelChanged( ev );
            }
            catch (Throwable e) {
                PolymapWorkbench.handleError( Qi4jPlugin.PLUGIN_ID, listener, "Error while firing operation event.", e );
            }
        }
        // fire global event
        Set<String> ids = new HashSet( 128 );
        for (PropertyChangeEvent pev : ev.events()) {
            Entity entity = (Entity)pev.getSource();
            try {
                ids.add( entity.id() );
            }
            catch (NoSuchEntityException e) {
                // XXX
                log.warn( "Skipping removed entity in ModelChangeEvent!" );
                //ev.addRemoved( ... );
            }
        }
        fireGlobalEvent( this, ids, EventType.CHANGE );
    }

    
    public void propertyChange( PropertyChangeEvent pev ) {
        for (PropertyChangeListener listener : propertyListeners) {
            listener.propertyChange( pev );
        }
    }


    /**
     * Add the given property listener. Has no effect if the same listener is
     * already registered. The given listener might be stored in a
     * {@link WeakReference}, so the caller has to make sure that a strong
     * reference exists as long as the listener should receive events.
     * <p/>
     * The listener receives events from all entities of all modules of the
     * current session.
     */
    public void addPropertyChangeListener( PropertyChangeListener l, PropertyEventFilter f ) {
        propertyListeners.add( new FilteredPropertyChangeListener( l, f ) );
    }
    
    public void removePropertyChangeListener( PropertyChangeListener l ) {
        propertyListeners.remove( new FilteredPropertyChangeListener( l, null ) );
    }
    
    /*
     * 
     */
    static class FilteredPropertyChangeListener
            implements PropertyChangeListener {
        
        private PropertyChangeListener      delegate;
        private PropertyEventFilter         filter;
        
        protected FilteredPropertyChangeListener( PropertyChangeListener delegate, PropertyEventFilter filter ) {
            this.delegate = delegate;
            this.filter = filter;
        }

        public void propertyChange( PropertyChangeEvent ev ) {
            if (filter.accept( ev )) {
                delegate.propertyChange( ev );
            }
        }

        public boolean equals( Object obj ) {
            if (obj == this) {
                return true;
            }
            else if (obj instanceof FilteredModelChangeListener) {
                return delegate == ((FilteredModelChangeListener)obj).delegate;
            }
            return false;
        }
    }
    

    // global events **************************************

    public Long storedEntityVersion( String id, Class<?> type ) {
        String key = id + "_" + type.getName();
        log.info( "storedEntityVersion(): key= " + key );
        return entityVersions.get( key );
    }

    public boolean isConcurrentlyChanged( ModelChangeSupport entity ) {
        // XXX implementation
        return false;
    }
    

    public boolean isConcurrentlyCommitted( ModelChangeSupport entity ) {
        return isConcurrentlyCommitted( entity.id(), entity.type(), entity.lastModified() );
    }

    /**
     *
     * @param id The entity identity.
     * @param type The entity composite type.
     * @param entityLastModified The 
     * @return True if the given entity has been changed/committed by another session. 
     */
    public boolean isConcurrentlyCommitted( String id, Class type, Long lastModified ) {
        assert id != null;
        assert type != null;
        long entityLastModified = lastModified != null ? lastModified : 0L; 
        try {
            entityVersionsLock.readLock().lock();

            String key = id + "_" + type.getName();
            log.debug( "isConcurrentlyCommitted(): key= " + key );
            Long storeLastModified = entityVersions.get( key );

            log.debug( "CHECK: entityLastModified=" + entityLastModified + ", storeLastModified=" + storeLastModified );
            return storeLastModified != null && entityLastModified < storeLastModified;
        }
        finally {
            entityVersionsLock.readLock().unlock();
        }
    }

    
    void updateVersions( List<ModelChangeSupport> entities ) {
        Set<String> ids = new HashSet( entities.size() * 2 );
        try {
            entityVersionsLock.writeLock().lock();

            for (ModelChangeSupport entity : entities) {
                ids.add( entity.id() );
                
                long entityLastModified = entity.lastModified();

                String key = entity.id() + "_" + entity.type().getName();
                log.info( "updateVersions(): key= " + key );
                Long storeLastModified = entityVersions.put( key, entityLastModified );

                log.debug( "CHECK: entityLastModified=" + entityLastModified + ", storeLastModified=" + storeLastModified );
                if (storeLastModified != null && entityLastModified < storeLastModified) {
                    entityVersions.put( key, storeLastModified );
                    throw new RuntimeException( "" );
                }
            }
        }
        finally {
            entityVersionsLock.writeLock().unlock();
        }
        
        // XXX fire also to local modelListeners?
        
        fireGlobalEvent( this, ids, EventType.COMMIT );
    }
    
    
    protected void fireGlobalEvent( final Object source, final Set<String> ids, final EventType type ) {
        Runnable runnable = new Runnable() {
            public void run() {
                GlobalModelChangeEvent ev = new GlobalModelChangeEvent( source, ids, type );
                for (GlobalModelChangeListener listener : globalModelListeners) {
                    try {
                        listener.modelChanged( ev );
                    }
                    catch (Throwable e) {
                        log.warn( "Error while processing GlobalModelChangeEvent: " + ev, e );
                    }
                }
            }
        };
        if (globalEventJob == null || globalEventJob.getResult() != null) {
            globalEventJob = new GlobalEventJob( runnable );
            globalEventJob.schedule();
        }
        else {
            globalEventJob.add( runnable );
        }
    }
    
    
    /**
     * 
     */
    class GlobalEventJob
            extends Job {
        
        Deque<Runnable>         stack = new LinkedList();  

        public GlobalEventJob( Runnable runnable ) {
            super( "GlobalEventJob" );
            setPriority( Job.LONG );
            stack.addLast( runnable );
        }
        
        public void add( Runnable runnable ) {
            synchronized (stack) {
                stack.addLast( runnable );
            }
        }
        
        protected IStatus run( IProgressMonitor monitor ) {
            while (true) {
                Runnable next = null;
                synchronized (stack) {
                    if (stack.isEmpty()) {
                        return Status.OK_STATUS;
                    }
                    next = stack.removeFirst();
                }
                next.run();
            }
        }
        
    }

}
