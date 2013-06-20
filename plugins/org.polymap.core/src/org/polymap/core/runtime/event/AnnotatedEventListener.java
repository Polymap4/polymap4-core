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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventObject;
import java.util.List;
import java.util.Queue;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.ObjectArrays;

import org.polymap.core.runtime.SessionContext;
import org.polymap.core.runtime.event.DeferringListener.DeferredEvent;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
class AnnotatedEventListener
        implements EventListener {

    private static Log log = LogFactory.getLog( AnnotatedEventListener.class );
    
    private WeakReference<Object>   handlerRef;
    
    private Class                   handlerClass;
    
    private Integer                 mapKey;
    
    private List<EventListener>     methods = new ArrayList( 2 );

    
    /**
     * 
     */
    public AnnotatedEventListener( Object handler, Integer mapKey, EventFilter... filters ) {
        assert handler != null;
        assert filters != null;
        this.handlerRef = new WeakReference( handler );
        this.handlerClass = handler.getClass();
        this.mapKey = mapKey;
        
        // find annotated methods
        Queue<Class> types = new ArrayDeque( 16 );
        types.add( handler.getClass() );
        while (!types.isEmpty()) {
            Class type = types.remove();
            if (type.getSuperclass() != null) {
                types.add( type.getSuperclass() );
            }
            types.addAll( Arrays.asList( type.getInterfaces() ) );
            
            for (Method m : type.getDeclaredMethods()) {
                EventHandler annotation = m.getAnnotation( EventHandler.class );
                if (annotation != null) {
                    m.setAccessible( true );
                    
                    // annotated method
                    AnnotatedMethod am = annotation.delay() > 0
                            ? new DeferredAnnotatedMethod( m, annotation )
                            : new AnnotatedMethod( m, annotation );

                    // display thread
                    EventListener listener = am;
                    if (annotation.display()) {
                        listener = new DisplayingListener( listener );
                    }
                    
                    // deferred
                    // XXX There is a race cond. between the UIThread and the event thread; if the UIThread
                    // completes the current request before all display events are handled then the UI
                    // is not updated until next user request; DeferringListener handles this by activating
                    // UICallBack, improving behaviour - but not really solving in all cases; after 500ms we
                    // are quite sure that no more events are pending and UI callback can turned off
                    
                    // currenty COMMENTED OUT! see EventManger#SessionEventDispatcher
                    if (annotation.delay() > 0 /*|| annotation.display()*/) {
                        int delay = annotation.delay() > 0 ? annotation.delay() : 500;
                        listener = new DeferringListener( listener, delay, 10000 );
                    }
                    // filters
                    listener = new FilteringListener( listener, 
                            ObjectArrays.concat( am.filters, filters, EventFilter.class ) );
                    
                    // session context; first in chain so that all listener/filters
                    // get the proper context
                    SessionContext session = SessionContext.current();
                    if (session != null) {
                        listener = new SessioningListener( listener, mapKey, session, handlerClass );
                    }
                    methods.add( listener );
                }
            }
        }
        if (methods.isEmpty()) {
            throw new IllegalArgumentException( "No EventHandler annotation found in: " + handler.getClass() );
        }
    }


    @Override
    public void handleEvent( EventObject ev ) throws Exception {
        if (handlerRef != null) {
            Object handler = handlerRef.get();
            if (handler != null) {
                for (EventListener m : methods) {
                    m.handleEvent( ev );
                }
            }
            else {
                log.info( "Removing reclaimed handler: " + handlerClass );
                handlerRef = null;
                EventManager.instance().removeKey( mapKey );
            }
        }
    }

    
    public Integer getMapKey() {
        return mapKey;
    }


    /**
     * 
     */
    class AnnotatedMethod
             implements EventListener {
    
        protected Method                handlerMethod;
        
        protected EventFilter[]         filters;
        
        
        protected AnnotatedMethod() {
        }
        
        
        protected AnnotatedMethod( Method m, EventHandler annotation ) {
            // check param type
            Type[] params = m.getGenericParameterTypes();
            if (params.length != 1) {
                throw new IllegalArgumentException( "EventHandler method must have one param: " + m );                    
            }
            if (!EventObject.class.isAssignableFrom( (Class<?>)params[0] )) {
                throw new IllegalArgumentException( "EventHandler method param must be assignable from EventObject: " + params[0] );                    
            }
            
            // type and scope filter
            filters = new EventFilter[2];
            filters[0] = new TypeEventFilter( (Class<? extends EventObject>)params[0] );
            filters[1] = ScopeEventFilter.forScope( annotation.scope() );
            
            handlerMethod = m;
        }

        
        @Override
        public void handleEvent( final EventObject ev ) throws Exception {
            // when DisplayingListener is used then handlerRef might got reclaimed
            // between #handleEvent() and here
            Object handler = handlerRef != null  ? handlerRef.get() : null;
            
            if (handler != null) {
                // display events are always wrapped into DeferredListener (see above)
                if (ev instanceof DeferredEvent) {
                    for (EventObject ev2 : ((DeferredEvent)ev).events()) {
                        handlerMethod.invoke( handler, new Object[] {ev2} );
                    }
                }
                else {
                    handlerMethod.invoke( handler, new Object[] {ev} );
                }
            }
        }
    }
    
    
    /**
     * 
     */
    class DeferredAnnotatedMethod
             extends AnnotatedMethod {
    
        protected DeferredAnnotatedMethod( Method m, EventHandler annotation ) {
            // check param type
            Type[] params = m.getGenericParameterTypes();
            if (params.length != 1) {
                throw new IllegalArgumentException( "EventHandler method must have one param: " + m );                    
            }
            if (!(params[0] instanceof ParameterizedType)) {
                throw new IllegalArgumentException( "EventHandler param must be of type: List<? extends EventObject>" + m );                    
            }
            ParameterizedType param = (ParameterizedType)params[0];
            if (!List.class.isAssignableFrom( (Class)param.getRawType() )) {
                throw new IllegalArgumentException( "EventHandler method param must be assignable from List: " + params[0] );                    
            }
            Type[] typeParams = param.getActualTypeArguments();
            if (typeParams.length != 1) {
                throw new IllegalArgumentException( "EventHandler param must be of type: List<? extends EventObject>" + m );                    
            }
            Class<?> typeParam = (Class<?>)typeParams[0];
            if (!EventObject.class.isAssignableFrom( typeParam )) {
                throw new IllegalArgumentException( "EventHandler param must be of type: List<? extends EventObject>" + m );                    
            }
            
            // type and scope filter
            filters = new EventFilter[2];
            filters[0] = new TypeEventFilter( typeParam );
            filters[1] = ScopeEventFilter.forScope( annotation.scope() );
            
            handlerMethod = m;
        }

        
        @Override
        public void handleEvent( final EventObject ev ) throws Exception {
            Object[] params = new Object[] { ((DeferredEvent)ev).events() };
            handlerMethod.invoke( handlerRef.get(), params );
        }
    }
    
}
