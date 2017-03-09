/* 
 * polymap.org
 * Copyright 2012-2016, Falko Bräutigam. All rights reserved.
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
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.atomic.AtomicInteger;

import java.lang.ref.WeakReference;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.MapMaker;

import org.polymap.core.runtime.Timer;
import org.polymap.core.runtime.session.SessionContext;
import org.polymap.core.ui.UIUtils;

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

    private static final Log log = LogFactory.getLog( EventManager.class );
    
    private static final Optional<AtomicInteger> ONE = Optional.of( new AtomicInteger( 1 ) );

    /**
     * The session that the currently dispatched event is published from. The
     * {@link SessionEventDispatcher event dispatcher} sets this for every dispatched
     * event. No {@link ThreadLocal} needed as there is just one thread dispatching
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
     * @param ev 
     * @result The session, or null if published outside session.
     * @throws AssertionError If the method was called from outside an event handler
     *         or filter method.
     */
    public static SessionContext publishSession() {
        return threadPublishSession; 
    }
    
    // instance *******************************************
    
    private DispatcherThread            dispatcher = new DispatcherThread();
    
    /**
     * XXX check https://lmax-exchange.github.io/disruptor/
     * just to save the link somewhere: https://github.com/npgall/cqengine
     */
    private CopyOnWriteArraySet<AnnotatedEventListener> listeners = new CopyOnWriteArraySet();
    
    private volatile int                statCount;
    
    private Timer                       statTimer;
    
    /** 
     * The threads with pending events and {@link UIUtils#activateCallback(String)}. Entries
     * are never removed, we rely on {@link WeakReference}. 
     */
    private Map<Thread,Optional<AtomicInteger>> uiThreads = new MapMaker().weakKeys().makeMap();


    protected EventManager() {
        dispatcher.start();
    }

    
    public void dispose() {
        if (dispatcher != null) {
            dispatcher.dispose();
            dispatcher = null;
        }
    }


    /**
     * Asynchronously publish the given event. An event dispatch thread actually
     * delivers the events. This method may immediatelly return to the caller.
     * 
     * @param ev The event to dispatch.
     */
    public void publish( EventObject ev, Object... omitHandlers ) {
        doPublish( ev, omitHandlers );
    }


    /**
     * Synchronously publish the given event. This method will not return to the
     * caller until the event is dispatched to all listeners.
     * <p/>
     * Using this method is discouraged. For normal event dispatch use the
     * asynchronous {@link #publish(EventObject)}.
     * <p/>
     * Beware that handlers that are invoked inside the <b>display thread</b> (see
     * {@link EventHandler}) are not actually called before this method returnes. In
     * other words, display handlers are not guaranteed to be called synchronously.
     * 
     * @see #publish(Event)
     * @param ev The event to dispatch.
     */
    public void syncPublish( EventObject ev, Object... omitHandlers ) {
        SessionEventDispatcher d = doPublish( ev, omitHandlers );
        
        synchronized (d) {
            Timer timer = new Timer();
            while (!d.isDone()) {
                try { d.wait( 200 ); } catch (InterruptedException e) {}
                
                if (timer.elapsedTime() >= 5000) {
                    throw new RuntimeException( "Timeout exceeded for synch event: " + ev );
                }
            }
        }
    }

    
    protected SessionEventDispatcher doPublish( EventObject ev, Object... omitHandlers ) {
        assert ev != null;
        
        Thread thread = Thread.currentThread();
        activateUICallback( thread );
        
        Iterator<AnnotatedEventListener> snapshot = listeners.iterator();
        SessionEventDispatcher d = new SessionEventDispatcher( ev, snapshot, omitHandlers, thread );
        dispatcher.dispatch( d );
        return d;
    }

    
    /**
     *
     */
    protected void activateUICallback( Thread thread ) {
        // commented out in favour of a simple timeout handled by BatikApplication 
        
//        // activate UI callback if not already done for this thread;
//        // do it only if we are in UIThread; that is, we expect UI updates happens just for
//        // events that are originated from UIThread; 
//        Optional<AtomicInteger> callbackCount = uiThreads.computeIfAbsent( thread, key -> {
//            return Optional.ofNullable( Display.getCurrent() != null ? new AtomicInteger( 0 ) : null );
//        });
//        callbackCount.ifPresent( count -> {
//            if (count.getAndIncrement() == 0) {
//                UIUtils.activateCallback( "EventManager" );
//                log.warn( "ACTIVATED (actually): " + count.get() );
//            }
//            else {
//                log.warn( "COUNT: " + count.get() );
//            }
//        });
    }

    
    /**
     * Check/deactivate UI callback.
     */
    protected void deactivateUICallback( Thread publishThread, SessionContext publishSession ) {
//        uiThreads.get( publishThread ).ifPresent( count -> {
//            // XXX entries are not removed as this would cause race cond between check
//            // of the counter and the remove() call; I tried Map.remove(key,value) but no luck 
//            if (count.decrementAndGet() == 0) {
//                publishSession.execute( () -> {
//                    UIThreadExecutor.async( () -> {
//                        UIUtils.deactivateCallback( "EventManager" );
//                        log.warn( "DEACTIVATED (actually): " + count.get() );
//                    });
//                });
//                log.warn( "DEACTIVATED: " + count.get() + ", threads=" + uiThreads.size() );
//            }
//        });
    }
    
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
        for (AnnotatedEventListener l : listeners) {
            if (l.getMapKey().equals( key )) {
                if (!listeners.remove( l )) {
                    log.warn( "Unable to remove key: " + key + " (EventManager: " + EventManager.instance().size() + ")" );                    
                }
                return l;
            }
        }
        log.warn( "Key not found: " + key + " (EventManager: " + EventManager.instance().size() + ")" );
        return null;
    }

    
    int size() {
        return listeners.size();
    }

    
    /**
     * 
     */
    protected class SessionEventDispatcher
            implements Runnable {
        
        private SessionContext      publishSession;
        
        private Object[]            omitHandlers;
        
        private Iterator<? extends EventListener> snapshot;
        
        private int                 dispatched;

        private EventObject         event;
        
        private volatile boolean    done;

        private Thread              publishThread;
        
        
        SessionEventDispatcher( EventObject event, Iterator<? extends EventListener> snapshot, 
                Object[] omitHandlers, Thread thread ) {
//          assert publishSession != null;
            assert omitHandlers != null;
            this.event = event;
            this.snapshot = snapshot;
            this.omitHandlers = omitHandlers;
            this.publishSession = SessionContext.current();
            this.publishThread = thread;
        }
    
        
        @Override
        public void run() {
            try {
                assert threadPublishSession == null;
                threadPublishSession = publishSession;

                // dispatch event
                while (snapshot.hasNext()) {
                    try {
                        EventListener listener = snapshot.next();
                        if (omitHandlers.length == 0 || !ArrayUtils.contains( omitHandlers, listener )) {
                            listener.handleEvent( event );
                        }
                    } 
                    catch (Throwable e) {
                        log.warn( "Error during event dispatch: " + e, e );
                    }
                }
                //
                deactivateUICallback( publishThread, publishSession );
            } 
            finally {
                threadPublishSession = null;

                synchronized (this) {  // XXX better sync method???
                    done = true;
                    notifyAll();
                }
            }
        }

        
        public boolean isDone() {
            return done;
        }
        
        
//        @SuppressWarnings("hiding")
//        protected void dispatchEvent( Object listener, Object listenerObject, int action, Object event ) {
//
////            // statistics
////            if (log.isDebugEnabled()) {
////                statCount++;
////                if (statTimer == null) {
////                    statTimer = new Timer();
////                }
////                long elapsed = statTimer.elapsedTime();
////                if (elapsed > 1000) {
////                    log.debug( "********************************************** STATISTICS: " + statCount + " handlers/events in " + elapsed + "ms" );
////                    statCount = 0;
////                    statTimer = null;
////                }
////            }
//        }
    }

    
    /**
     * 
     */
    protected static class DispatcherThread
            extends Thread {

        public static final int         MAX_QUEUE_SIZE = 10000;
        
        /**
         * XXX check https://lmax-exchange.github.io/disruptor/
         * just to save the link somewhere: https://github.com/npgall/cqengine
         */
        private BlockingQueue<Runnable> queue;

        /** 
         * Non synchronized "assumption" about size of the {@link #queue}. 
         * XXX on IA32 it seems to work ok without "volatile"; not sure about other platforms; 
         * see http://brooker.co.za/blog/2012/09/10/volatile.html
         */
        private volatile int            queueSize;
        
        private int                     queueReadCount;

        private boolean                 stopped;
        
        
        public DispatcherThread() {
            super( "EventManager.Dispatcher" );
            try {
                // faster but available in JDK 1.7 only
                queue = new LinkedTransferQueue();
            }
            catch (Throwable e) {
                log.warn( e.toString() + " -> falling back to JDK1.6 ArrayBlockingQueue" );
                queue = new ArrayBlockingQueue( MAX_QUEUE_SIZE );
            }
            //setPriority( Thread.MAX_PRIORITY );
            setDaemon( true );
        }

        public void dispose() {
            stopped = true;    
        }
        
        public void dispatch( Runnable work ) {
            try {
                for (int i=0; queueSize >= MAX_QUEUE_SIZE; i++) {
                    log.trace( "Waiting on dispatch... queue: " + queueSize );
                    try { Thread.sleep( Math.min( 10*i, 1000 ) ); } catch (InterruptedException e) {};
                    log.trace( "    queue: " + queueSize );
                }
                ++queueSize;

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
                    if (queueReadCount > MAX_QUEUE_SIZE) {
                        // synchronize the assumption with real value
                        queueSize = queue.size();
                        queueReadCount = 0;
                    }
                    ++queueReadCount;
                    
                    Runnable work = queue.take();
                    work.run();
                    --queueSize;
                }
                catch (InterruptedException e) {
                }
            }
        }
    }
    
}
