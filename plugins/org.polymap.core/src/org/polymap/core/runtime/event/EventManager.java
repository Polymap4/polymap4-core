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

import static com.google.common.collect.Iterables.transform;

import java.util.EventObject;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;

import org.eclipse.rwt.RWT;
import org.eclipse.rwt.lifecycle.PhaseEvent;
import org.eclipse.rwt.lifecycle.PhaseId;
import org.eclipse.rwt.lifecycle.PhaseListener;
import org.eclipse.rwt.lifecycle.UICallBack;
import org.eclipse.rwt.service.ISessionStore;
import org.eclipse.rwt.service.SessionStoreEvent;
import org.eclipse.rwt.service.SessionStoreListener;

import org.eclipse.osgi.framework.eventmgr.EventDispatcher;
import org.eclipse.osgi.framework.eventmgr.ListenerQueue;

import org.polymap.core.runtime.SessionContext;
import org.polymap.core.runtime.Timer;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class EventManager {

    private static Log log = LogFactory.getLog( EventManager.class );

    /**
     * The session that the currently dispatched event is published from. The
     * {@link SessionEventDispatcher event dispatcher} sets this for every dispatched
     * event. No {@link ThreadLocal} needed as there si just one thread dispatching
     * events.
     */
    private static SessionContext           threadPublishSession;
    
    private static final EventManager       instance = new EventManager();
    
    public static final EventManager instance() {
       return instance;    
    }
    
    static SessionContext publishSession() {
        assert threadPublishSession != null;
        return threadPublishSession; 
    }
    
    // instance *******************************************
    
    private org.eclipse.osgi.framework.eventmgr.EventManager manager;
    
    private ConcurrentMap<Integer,EventListener>    listeners = new ConcurrentHashMap( 1024, 0.75f, 8 );
    
    private volatile int                            statCount;
    
    private Timer                                   statTimer;
    
    private volatile int                            pendingEvents;
    
    /** The global {@link PhaseListener} installed by the {@link SessionEventDispatcher}. */
    private UICallbackPhaseListener                 phaseListener;
    

    protected EventManager() {
        // always keep one listener in the list so that SessionEventDispatcher
        // propery counts #pendingEvents
        subscribe( this, new EventFilter<EventObject>() {
            public boolean apply( EventObject input ) {
                return false;
            }
        });
        
        ThreadGroup tg = new ThreadGroup( "EventManager" );
        manager = new org.eclipse.osgi.framework.eventmgr.EventManager( "EventManager.Dispatcher", tg );

//        // force the manager to create the thread
//        publish( new EventObject( this ) );
//        
//        Thread[] threads = new Thread[1];
//        tg.enumerate( threads );
//        for (Thread t : threads) {
//            t.setPriority( Thread.MAX_PRIORITY );
//            log.info( "thread: " + t.getName() + ", prio: " + t.getPriority() );
//        }
    }

    
    public void dispose() {
        manager.close();        
    }


    @EventHandler
    protected void handleEvent( EventObject ev ) {
    }

    
    /**
     * Asynchronously publish the given event. An event dispatch thread maintained by
     * the associated EventManager is used to deliver the events. This method may
     * immediatelly return to the caller.
     * 
     * @param ev The event to dispatch.
     */
    public void publish( EventObject ev, Object... omitHandlers ) {
        assert ev != null;
        ListenerQueue listenerQueue = new ListenerQueue( manager );        
        Set<Map.Entry> queueableListeners = queueableListeners();
        listenerQueue.queueListeners( queueableListeners, 
                new SessionEventDispatcher( queueableListeners.size(), omitHandlers ) );
        listenerQueue.dispatchEventAsynchronous( 0, ev );
    }


    /**
     * Synchronously publish the given event. This method will not return to the
     * caller until the event is dispatched to all listeners.
     * <p>
     * Using this method is discouraged. For normal event dispatch use teh
     * asynchronous {@link #publish(EventObject)}.
     * 
     * @see #publish(Event)
     * @param ev The event to dispatch.
     */
    public void syncPublish( EventObject ev, Object... omitHandlers ) {
        assert ev != null;
        ListenerQueue listenerQueue = new ListenerQueue( manager );
        Set<Map.Entry> queueableListeners = queueableListeners();
        listenerQueue.queueListeners( queueableListeners, 
                new SessionEventDispatcher( queueableListeners.size(), omitHandlers ) );
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

    
//    /**
//     * <p/>
//     * Listeners are weakly referenced by the EventManager. A listener is reclaimed
//     * by the GC and removed from the EventManager as soon as there is no strong
//     * reference to it. An anonymous inner class can not be used as event listener.
//     * 
//     * @param scope
//     * @param type
//     * @param listener
//     * @throws IllegalArgumentException If the given listener is registered already.
//     */
//    public void subscribe( Event.Scope scope, Class<? extends EventObject> type, EventListener listener, EventFilter... filters ) {
//        // weak reference
//        Integer key = System.identityHashCode( listener );
//        WeakListener chained = new WeakListener( listener, key );
//        
//        // scope/type filter
//        TypeEventFilter typeFilter = new TypeEventFilter( type );
//        ScopeEventFilter scopeFilter = ScopeEventFilter.forScope( scope );
//        EventListener tweaked = new FilteringListener( chained, typeFilter, scopeFilter );
//        
//        EventListener found = listeners.putIfAbsent( key, tweaked );
//        if (found != null) {
//            throw new IllegalArgumentException( "EventListener already registered: " + listener ); 
//        }
//    }


    /**
     * Registeres the given {@link EventHandler annotated} handler as event listener.
     * <p/>
     * Listeners are weakly referenced by the EventManager. A listener is reclaimed
     * by the GC and removed from the EventManager as soon as there is no strong
     * reference to it. An anonymous inner class can not be used as event listener.
     * 
     * @see EventHandler
     * @param annotated
     * @throws IllegalStateException If the handler is subscribed already.
     */
    public void subscribe( Object annotated, EventFilter... filters ) {
        EventListener listener = new AnnotatedEventListener( annotated, filters ); 
        Integer key = System.identityHashCode( annotated );
        if (listeners.putIfAbsent( key, listener ) != null) {
            throw new IllegalStateException( "Event handler already registered: " + annotated );        
        }
    }

    
    /**
     *
     * @param listenerOrHandler
     * @throws True if the given handler actually was removed.
     */
    public boolean unsubscribe( Object listenerOrHandler ) {
        assert listenerOrHandler != null;
        Integer key = System.identityHashCode( listenerOrHandler );
        return listeners.remove( key ) != null;
    }
    
    
    EventListener removeKey( Object key ) {
        return listeners.remove( key );
    }


    /**
     * Checks if there are pending events after the render page of an request. If
     * yes, then UICallback is activated - until there are no pending events after
     * any subsequent request.
     * <p/>
     * XXX Currently #pendingEvents counts ALL events from all sessions! So a foreign
     * session might force a UICallback even if we don't have anything to render.
     */
    private class UICallbackPhaseListener
            implements PhaseListener, SessionStoreListener {

        public PhaseId getPhaseId() {
            return PhaseId.ANY;
        }
        
        public void beforePhase( PhaseEvent ev ) {
            //log.debug( "Before " + ev.getPhaseId() + ": pending=" + pendingEvents );
        }
        
        public void afterPhase( PhaseEvent ev ) {
            if (ev.getPhaseId() != PhaseId.PROCESS_ACTION) {
                return;
            }
            ISessionStore session = RWT.getSessionStore();
            boolean uiCallbackActive = session.getAttribute( "uiCallbackActive" ) != null;
            
            //log.debug( "After " + getPhaseId() + ": pending=" + pendingEvents + ", UICallbackActive=" + uiCallbackActive );
            
            if (pendingEvents > 0) {
                if (pendingEvents > 0 && !uiCallbackActive) {
                    log.debug( "UICallback: ON (pending: " + pendingEvents + ")" );
                    UICallBack.activate( "EventManager.pendingEvents" );
                    session.setAttribute( "uiCallbackActive", true );
                }
            }
            else {
                if (uiCallbackActive) {
                    log.debug( "UICallback: OFF" );
                    UICallBack.deactivate( "EventManager.pendingEvents" );
                    session.removeAttribute( "uiCallbackActive" );
                }
            }
        }

        @Override
        public void beforeDestroy( SessionStoreEvent ev ) {
            RWT.getLifeCycle().removePhaseListener( this );
            ev.getSessionStore().removeSessionStoreListener( this );
        }
        
    }
    

    /**
     * 
     */
    private class SessionEventDispatcher
            implements EventDispatcher {
        
        private SessionContext      publishSession;
        
        private Object[]            omitHandlers;
        
        private final int           numOfListeners;
        
        private int                 dispatched;

        
        SessionEventDispatcher( int numOfListeners, Object[] omitHandlers ) {
            this.numOfListeners = numOfListeners;
            this.publishSession = SessionContext.current();
            this.omitHandlers = omitHandlers;
            assert publishSession != null;
            assert omitHandlers != null;
            
            // XXX should never happen
            if (pendingEvents < 0) {
                log.warn( "pendingEvents < 0 : " + pendingEvents, new Exception() );
                pendingEvents = 0;
            }
            pendingEvents ++;
            
            // install UICallbackPhaseListener
            try {
                // seems that a PhaseListener is installed just once for all sessions
                if (phaseListener == null) {
                    phaseListener = new UICallbackPhaseListener();
                    RWT.getLifeCycle().addPhaseListener( phaseListener );
                }
//                ISessionStore session = RWT.getSessionStore();
//                if (session.getAttribute( "EventManager.PhaseListener" ) == null) {
//                    session.setAttribute( "EventManager.PhaseListener", true );
//                    UICallbackPhaseListener rpl = new UICallbackPhaseListener();
//                    RWT.getLifeCycle().addPhaseListener( rpl );
//                    session.addSessionStoreListener( rpl );
//                }
            }
            catch (IllegalStateException e) {
                phaseListener = null;
                // outside request lifecycle -> no UICallback handling
                log.warn( e.toString() );
            }
        }
    
        public void dispatchEvent( Object listener, Object listenerObject, int action, Object event ) {
            assert threadPublishSession == null;
            threadPublishSession = publishSession;
            
            try {
                if (!ArrayUtils.contains( omitHandlers, listener )) {
                    ((EventListener)listener).handleEvent( (EventObject)event );
                }
            } 
            catch (Throwable e) {
                log.warn( "Error during event dispatch: " + e );
                log.debug( "", e );
            }
            finally {
                threadPublishSession = null;
            }

            // decrement pendingEvents
            if (++dispatched >= numOfListeners) {
                pendingEvents --;
            }
            
            // statistics
            if (log.isDebugEnabled()) {
                statCount++;
                if (statTimer == null) {
                    statTimer = new Timer();
                }
                long elapsed = statTimer.elapsedTime();
                if (elapsed > 1000) {
                    log.debug( "********************************************** STATISTICS: " + statCount + " handlers/events in " + elapsed + "ms" );
                    statCount = 0;
                    statTimer = null;
                }
            }
        }
    }
    
}
