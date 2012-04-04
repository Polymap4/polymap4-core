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
package org.polymap.core.model.event;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.model.event.ModelChangeEvent;
import org.polymap.core.model.event.IModelChangeListener;
import org.polymap.core.model.event.IEventFilter;
import org.polymap.core.qi4j.Qi4jPlugin;
import org.polymap.core.qi4j.event.AbstractModelChangeOperation;
import org.polymap.core.runtime.ListenerList;
import org.polymap.core.runtime.SessionSingleton;
import org.polymap.core.workbench.PolymapWorkbench;

/**
 * Provides API and most of the implementation of the model event system. The source
 * of events can be entities, features, objects with a backend store.
 * <p/>
 * Sub-systems (like {@link QiModule} or feature store) can delegate handling of
 * property and model listeners to the ModelEventManager.
 * <p/>
 * ModelEventManager relies entirely on {@link AbstractModelChangeOperation} to
 * generate and fire {@link ModelChangeEvent}.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @since 3.1
 */
public class ModelEventManager
        extends SessionSingleton {

    private static Log log = LogFactory.getLog( ModelEventManager.class );
 
    
    /**
     * The instance of the current session, or null if the current thread is not
     * bound to any user session.
     */
    public static ModelEventManager instance() {
        return instance( ModelEventManager.class );
    }
    
    
    // instance *******************************************
 
    /**
     * EQUALITY is mandatory in order to support in-place
     * {@link FilteredPropertyChangeListener} support.
     */
    private ListenerList<IModelChangeListener>       modelListeners = new ListenerList( ListenerList.EQUALITY );
    
    /**
     * EQUALITY is mandatory in order to support in-place
     * {@link FilteredPropertyChangeListener} support.
     */
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
    public void addModelChangeListener( IModelChangeListener l, IEventFilter f ) {
        modelListeners.add( new FilteredModelChangeListener( l, f ) );
    }

    
    public void removeModelChangeListener( IModelChangeListener l ) {
        modelListeners.remove( new FilteredModelChangeListener( l, null ) );
    }

    /*
     * 
     */
    static class FilteredModelChangeListener
            implements IModelChangeListener {
        
        private IModelChangeListener    delegate;
        private IEventFilter            filter;
        
        protected FilteredModelChangeListener( IModelChangeListener delegate, IEventFilter filter ) {
            this.delegate = delegate;
            this.filter = filter;
        }

        public void modelChanged( ModelChangeEvent ev ) {
            ModelChangeEvent filtered = new ModelChangeEvent( ev.getSource() );
            int count = 0;
            for (PropertyChangeEvent pev : ev.events( filter )) {
                filtered.propertyChange( pev );
                count++;
            }
            if (filter.accept( ev ) || count > 0) {
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
    public void fireModelChangeEvent( ModelChangeEvent ev ) {
        assert ev != null;
        
        // fire local event
        for (IModelChangeListener listener : modelListeners) {
            try {
                listener.modelChanged( ev );
            }
            catch (Throwable e) {
                PolymapWorkbench.handleError( Qi4jPlugin.PLUGIN_ID, listener, "Error while firing operation event.", e );
            }
        }
        
//        // fire global event
//        for (PropertyChangeEvent pev : ev.events()) {
//            Entity entity = (Entity)pev.getSource();
//            try {
//                ModelHandle key = ModelHandle.instance( entity.id(), entity.getEntityType().getName() );
//
//                ModelChangeTracker.instance().track( 
//                        this, key, entity.lastModified(), false );
//            }
//            catch (NoSuchEntityException e) {
//                // XXX
//                log.warn( "Skipping removed entity in ModelChangeEvent!" );
//            }
//        }
    }

    
    public void firePropertyChangeEvent( PropertyChangeEvent pev ) {
        for (PropertyChangeListener listener : propertyListeners) {
            try {
                listener.propertyChange( pev );
            }
            catch (Throwable e) {
                PolymapWorkbench.handleError( Qi4jPlugin.PLUGIN_ID, listener, "Error while firing event.", e );
            }
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
    public boolean addPropertyChangeListener( PropertyChangeListener l, IEventFilter f ) {
        return propertyListeners.add( new FilteredPropertyChangeListener( l, f ) );
    }
    
    public boolean removePropertyChangeListener( PropertyChangeListener l ) {
        return propertyListeners.remove( new FilteredPropertyChangeListener( l, null ) );
    }
    
    /*
     * 
     */
    static class FilteredPropertyChangeListener
            implements PropertyChangeListener {
        
        private PropertyChangeListener      delegate;
        private IEventFilter         filter;
        
        protected FilteredPropertyChangeListener( PropertyChangeListener delegate, IEventFilter filter ) {
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
            else if (obj instanceof FilteredPropertyChangeListener) {
                return delegate == ((FilteredPropertyChangeListener)obj).delegate;
            }
            return false;
        }
    }
    

//    // global events **************************************
//
//    public Long storedEntityVersion( String id, Class<?> type ) {
//        String key = id + "_" + type.getName();
//        log.info( "storedEntityVersion(): key= " + key );
//        return entityVersions.get( key );
//    }
//
//    public boolean isConcurrentlyChanged( ModelChangeSupport entity ) {
//        // XXX implementation
//        return false;
//    }
//    
//
//    public boolean isConcurrentlyCommitted( ModelChangeSupport entity ) {
//        return isConcurrentlyCommitted( entity.id(), entity.type(), entity.lastModified() );
//    }
//
//    /**
//     *
//     * @param id The entity identity.
//     * @param type The entity composite type.
//     * @param entityLastModified The 
//     * @return True if the given entity has been changed/committed by another session. 
//     */
//    public boolean isConcurrentlyCommitted( String id, Class type, Long lastModified ) {
//        assert id != null;
//        assert type != null;
//        long entityLastModified = lastModified != null ? lastModified : 0L; 
//        try {
//            entityVersionsLock.readLock().lock();
//
//            String key = id + "_" + type.getName();
//            log.debug( "isConcurrentlyCommitted(): key= " + key );
//            Long storeLastModified = entityVersions.get( key );
//
//            log.debug( "CHECK: entityLastModified=" + entityLastModified + ", storeLastModified=" + storeLastModified );
//            return storeLastModified != null && entityLastModified < storeLastModified;
//        }
//        finally {
//            entityVersionsLock.readLock().unlock();
//        }
//    }
//
//    
//    void updateVersions( List<ModelChangeSupport> entities ) {
//        Set<String> ids = new HashSet( entities.size() * 2 );
//        try {
//            entityVersionsLock.writeLock().lock();
//
//            for (ModelChangeSupport entity : entities) {
//                ids.add( entity.id() );
//                
//                long entityLastModified = entity.lastModified();
//
//                String key = entity.id() + "_" + entity.type().getName();
//                log.info( "updateVersions(): key= " + key );
//                Long storeLastModified = entityVersions.put( key, entityLastModified );
//
//                log.debug( "CHECK: entityLastModified=" + entityLastModified + ", storeLastModified=" + storeLastModified );
//                if (storeLastModified != null && entityLastModified < storeLastModified) {
//                    entityVersions.put( key, storeLastModified );
//                    throw new RuntimeException( "" );
//                }
//            }
//        }
//        finally {
//            entityVersionsLock.writeLock().unlock();
//        }
//        
//        // XXX fire also to local modelListeners?
//        
//        fireGlobalEvent( this, ids, EventType.COMMIT );
//    }
    
}
