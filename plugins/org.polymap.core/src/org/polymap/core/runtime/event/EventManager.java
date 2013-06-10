/* 
 * polymap.org
 * Copyright 2012-2013, Falko Bräutigam. All rights reserved.
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

import java.util.EventObject;
import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArraySet;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.rwt.RWT;
import org.eclipse.rwt.lifecycle.PhaseEvent;
import org.eclipse.rwt.lifecycle.PhaseId;
import org.eclipse.rwt.lifecycle.PhaseListener;
import org.eclipse.rwt.lifecycle.UICallBack;
import org.eclipse.rwt.service.ISessionStore;
import org.eclipse.rwt.service.SessionStoreEvent;
import org.eclipse.rwt.service.SessionStoreListener;

import org.eclipse.osgi.framework.eventmgr.EventDispatcher;
import org.polymap.core.runtime.SessionContext;
import org.polymap.core.runtime.Timer;

/**
 * Provides the central API and implementation of the event system. Classes
 * interested in receiving events should use {@link EventHandler} to annotated the
 * handler methods. Event handler classes can then be
 * {@link #subscribe(Object, EventFilter...) subscribed} to the manager.
 * 
 * @see EventHandler
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
    
    /**
     * The session this event was published from.
     * <p/>
     * This method can be called from within an event handler or filter method to
     * retrieve the session the current event was published from.
     * 
     * @result The session, or null if published outside session.
     * @throws AssertionError If the method was called from outside an event handler
     *         or filter method.
     */
    public static SessionContext publishSession() {
        assert threadPublishSession != null;
        return threadPublishSession; 
    }
    
    // instance *******************************************
    
    private DispatcherThread                        dispatcher = new DispatcherThread();
    
    private CopyOnWriteArraySet<AnnotatedEventListener> listeners = new CopyOnWriteArraySet();
    
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

        // install UICallbackPhaseListener
        try {
            // seems that a PhaseListener is installed just once for all sessions
            if (phaseListener == null) {
                phaseListener = new UICallbackPhaseListener();
                RWT.getLifeCycle().addPhaseListener( phaseListener );
            }
        }
        catch (IllegalStateException e) {
            phaseListener = null;
            // outside request lifecycle -> no UICallback handling
            log.warn( e.toString() );
        }

        dispatcher.start();
    }

    
    public void dispose() {
        if (dispatcher != null) {
            dispatcher.dispose();
            dispatcher = null;
        }
    }


    @EventHandler
    protected void handleEvent( EventObject ev ) {
    }

    
    /**
     * Asynchronously publish the given event. An event dispatch thread actually
     * delivers the events. This method may immediatelly return to the caller.
     * 
     * @param ev The event to dispatch.
     */
    public void publish( EventObject ev, Object... omitHandlers ) {
        assert ev != null;        
        
        Iterator<AnnotatedEventListener> snapshot = queueableListeners();
        int numOfListeners = listeners.size();
        SessionEventDispatcher d = new SessionEventDispatcher( ev, snapshot, numOfListeners, omitHandlers );

        dispatcher.dispatch( d );
    }


    /**
     * Synchronously publish the given event. This method will not return to the
     * caller until the event is dispatched to all listeners.
     * <p>
     * Using this method is discouraged. For normal event dispatch use the
     * asynchronous {@link #publish(EventObject)}.
     * 
     * @see #publish(Event)
     * @param ev The event to dispatch.
     */
    public void syncPublish( EventObject ev, Object... omitHandlers ) {
        assert ev != null;
        Iterator<AnnotatedEventListener> snapshot = queueableListeners();
        int numOfListeners = listeners.size();
        SessionEventDispatcher d = new SessionEventDispatcher( ev, snapshot, numOfListeners, omitHandlers );
        
        dispatcher.dispatch( d );
        
        synchronized (d) {
            while (!d.isDone()) {
                try { d.wait( 1000 ); } catch (InterruptedException e) {}
            }
        }
    }

    
    /**
     * A snapshot of the current {@link #listeners}.
     */
    protected Iterator<AnnotatedEventListener> queueableListeners() {
        return listeners.iterator();
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
     * Listeners are <b>weakly</b> referenced by the EventManager. A listener is
     * reclaimed by the GC and removed from the EventManager as soon as there is no
     * strong reference to it. An anonymous inner class can not be used as event
     * listener.
     * <p/>
     * The given handler and filters are called within the <b>
     * {@link SessionContext#current() current session}</b>. If the current method
     * call is done outside a session, then the handler is called with no session
     * set. A handler can use {@link EventManager#publishSession()} to retrieve the
     * session the event was published from.
     * 
     * @see EventHandler
     * @param annotated The {@link EventHandler annotated} event handler.
     * @throws IllegalStateException If the handler is subscribed already.
     */
    public void subscribe( Object annotated, EventFilter... filters ) {
        assert annotated != null;
        Integer key = System.identityHashCode( annotated );
        AnnotatedEventListener listener = new AnnotatedEventListener( annotated, key, filters ); 
        if (!listeners.add( listener )) {
            throw new IllegalStateException( "Event handler already registered: " + annotated );        
        }
    }

    
    /**
     *
     * @param listenerOrHandler
     * @throws True if the given handler actually was removed.
     */
    public boolean unsubscribe( Object annotated ) {
        assert annotated != null;
        Integer key = System.identityHashCode( annotated );
        return removeKey( key ) != null;
    }
    
    
    EventListener removeKey( Object key ) {
        assert key instanceof Integer;
        EventListener removed = null;
        for (AnnotatedEventListener l : listeners) {
            if (l.getMapKey().equals( key )) {
                if (!listeners.remove( l )) {
                    log.warn( "Unable to remove key: " + key + " (EventManager: " + EventManager.instance().size() + ")" );                    
                }
                return l;
            }
        }
        log.warn( "Unable to remove key: " + key + " (EventManager: " + EventManager.instance().size() + ")" );
        return null;
    }

    
    int size() {
        return listeners.size();
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
            implements EventDispatcher, Runnable {
        
        private SessionContext      publishSession;
        
        private Object[]            omitHandlers;
        
        private Iterator<? extends EventListener> snapshot;
        
        private final int           numOfListeners;
        
        private int                 dispatched;

        private EventObject         event;
        
        
        SessionEventDispatcher( EventObject event, Iterator<? extends EventListener> snapshot, int numOfListeners, Object[] omitHandlers ) {
            this.event = event;
            this.snapshot = snapshot;
            this.numOfListeners = numOfListeners;
            this.omitHandlers = omitHandlers;
            this.publishSession = SessionContext.current();
//            assert publishSession != null;
            assert omitHandlers != null;
            
            // XXX should never happen
            if (pendingEvents < 0) {
                log.warn( "pendingEvents < 0 : " + pendingEvents, new Exception() );
                pendingEvents = 0;
            }
            pendingEvents ++;
        }
    
        
        @Override
        public void run() {
            while (snapshot.hasNext()) {
                dispatchEvent( snapshot.next(), null, 0, event );
            }
            synchronized (this) {
                notifyAll();
            }
        }

        
        public boolean isDone() {
            return dispatched == numOfListeners;
        }
        
        
        @SuppressWarnings("hiding")
        public void dispatchEvent( Object listener, Object listenerObject, int action, Object event ) {
            assert threadPublishSession == null;
            threadPublishSession = publishSession;
            
            try {
                if (!ArrayUtils.contains( omitHandlers, listener )) {
                    ((EventListener)listener).handleEvent( (EventObject)event );
                }
            } 
            catch (Throwable e) {
                log.warn( "Error during event dispatch: " + e, e );
                log.debug( "", e );
            }
            finally {
                threadPublishSession = null;
            }

            // decrement pendingEvents
            if (++dispatched >= numOfListeners) {
                pendingEvents --;
            }
            
//            // statistics
//            if (log.isDebugEnabled()) {
//                statCount++;
//                if (statTimer == null) {
//                    statTimer = new Timer();
//                }
//                long elapsed = statTimer.elapsedTime();
//                if (elapsed > 1000) {
//                    log.debug( "********************************************** STATISTICS: " + statCount + " handlers/events in " + elapsed + "ms" );
//                    statCount = 0;
//                    statTimer = null;
//                }
//            }
        }
    }

    
    /**
     * 
     */
    class DispatcherThread
            extends Thread {

        private BlockingQueue<Runnable> queue = new ArrayBlockingQueue( 10000 );
        
        private boolean                 stopped;
        
        public DispatcherThread() {
            super( "EventManager.Dispatcher" );
            setPriority( Thread.MAX_PRIORITY );
            setDaemon( true );
        }

        public void dispose() {
            stopped = true;    
        }
        
        public void dispatch( Runnable work ) {
            try {
                //log.debug( "Queue size: " + queue.size() );
                queue.put( work );
            }
            catch (InterruptedException e) {
                throw new RuntimeException( e );
            }    
        }
        
        @Override
        public void run() {
            while (!stopped) {
                try {
                    Runnable work = queue.take();
                    work.run();
                }
                catch (InterruptedException e) {
                }
            }
        }
    }
    
}
