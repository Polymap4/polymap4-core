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

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import static com.google.common.collect.Iterables.transform;
import org.eclipse.osgi.framework.eventmgr.EventDispatcher;
import org.eclipse.osgi.framework.eventmgr.ListenerQueue;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class EventManager {

    private static Log log = LogFactory.getLog( EventManager.class );
    
    private static final EventManager   instance = new EventManager();
    
    public static final EventManager instance() {
       return instance;    
    }
    
    
    // instance *******************************************
    
    private org.eclipse.osgi.framework.eventmgr.EventManager manager;
    
    private ConcurrentMap<Integer,EventListener>    listeners = new ConcurrentHashMap( 1024, 0.75f, 8 );
    
    private EventDispatcher                         dispatcher;
    
    
    protected EventManager() {
        manager = new org.eclipse.osgi.framework.eventmgr.EventManager( "EventManager-Dispatcher" );

        dispatcher = new EventDispatcher() {
            public void dispatchEvent( Object listener, Object listenerObject, int action, Object event ) {
                try {
                    ((EventListener)listener).handleEvent( (Event)event );
                } 
                catch (Throwable e) {
                    log.warn( "Error during event dispatch: " + e );
                    log.debug( "", e );
                }
            }
        };
    }

    
    public void dispose() {
        manager.close();        
    }


    /**
     * Asynchronously publish the given event. An event dispatch thread maintained by
     * the associated EventManager is used to deliver the events. This method may
     * immediatelly return to the caller.
     * 
     * @param ev The event to dispatch.
     */
    public void publish( Event ev ) {
        ListenerQueue listenerQueue = new ListenerQueue( manager );        
        listenerQueue.queueListeners( queueableListeners(), dispatcher );
        listenerQueue.dispatchEventAsynchronous( 0, ev );
    }


    /**
     * Synchronously publish the given event. This method will not return to the
     * caller until the event is dispatched to all listeners.
     * <p>
     * Using this method is discouraged. For normal event dispatch use teh
     * asynchronous {@link #publish(Event)}.
     * 
     * @see #publish(Event)
     * @param ev The event to dispatch.
     */
    public void syncPublish( Event ev ) {
        ListenerQueue listenerQueue = new ListenerQueue( manager );
        listenerQueue.queueListeners( queueableListeners(), dispatcher );
        listenerQueue.dispatchEventSynchronous( 0, ev );
    }

    
    /**
     * Transforms listeners into Set<Map.Entry> for {@link ListenerQueue}.
     */
    protected Set<Map.Entry> queueableListeners() {
        Iterable<Map.Entry> transformed = transform( listeners.values(), new Function<EventListener,Map.Entry>() {
            public Map.Entry apply( final EventListener input ) {
                return new Map.Entry() {
                    public Object getKey() {
                        return input;
                    }
                    public Object getValue() {
                        // XXX ???
                        return input;
                    }
                    public Object setValue( Object value ) {
                        throw new RuntimeException( "not implemented." );
                    }
                };
            }            
        });
        return ImmutableSet.copyOf( transformed );
    }

    
    /**
     * <p/>
     * Listeners are weakly referenced by the EventManager. A listener is reclaimed
     * by the GC and removed from the EventManager as soon as there is no strong
     * reference to it. An anonymous inner class can not be used as event listener.
     * 
     * @param scope
     * @param type
     * @param listener
     * @throws IllegalArgumentException If the given listener is registered already.
     */
    public void subscribe( Event.Scope scope, Class<? extends Event> type, EventListener listener, EventFilter... filters ) {
        // weak reference
        Integer key = System.identityHashCode( listener );
        WeakListener chained = new WeakListener( listener, key );
        
        // scope/type filter
        TypeEventFilter typeFilter = new TypeEventFilter( type );
        ScopeEventFilter scopeFilter = ScopeEventFilter.forScope( scope );
        EventListener tweaked = new FilteringListener( chained, typeFilter, scopeFilter );
        
        EventListener found = listeners.putIfAbsent( key, tweaked );
        if (found != null) {
            throw new IllegalArgumentException( "EventListener already registered: " + listener ); 
        }
    }


    /**
     * Registeres the given {@link EventHandler annotated} handler as event listener.
     * <p/>
     * Listeners are weakly referenced by the EventManager. A listener is reclaimed
     * by the GC and removed from the EventManager as soon as there is no strong
     * reference to it. An anonymous inner class can not be used as event listener.
     * 
     * @see EventHandler
     * @param annotatedEventHandler
     */
    public void subscribe( Object annotatedEventHandler ) {
        EventListener listener = new AnnotatedEventListener( annotatedEventHandler ); 
        
        Integer key = System.identityHashCode( annotatedEventHandler );
//        listener = new WeakListener( listener, key );
        
        listeners.put( key, listener );        
    }

    
    /**
     *
     * @param listenerOrHandler
     */
    public boolean remove( Object listenerOrHandler ) {
        assert listenerOrHandler != null;
        Integer key = System.identityHashCode( listenerOrHandler );
        return listeners.remove( key ) != null;
    }
    
    
    EventListener removeKey( Object key ) {
        return listeners.remove( key );
    }
    
}
