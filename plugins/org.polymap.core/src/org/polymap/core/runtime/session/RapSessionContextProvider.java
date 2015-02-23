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
package org.polymap.core.runtime.session;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.widgets.Display;

import org.eclipse.rap.rwt.SessionSingletonBase;
import org.eclipse.rap.rwt.internal.lifecycle.LifeCycleUtil;
import org.eclipse.rap.rwt.internal.service.ContextProvider;
import org.eclipse.rap.rwt.internal.service.ServiceContext;
import org.eclipse.rap.rwt.lifecycle.UICallBack;
import org.eclipse.rap.rwt.service.UISession;
import org.eclipse.rap.rwt.service.UISessionEvent;
import org.eclipse.rap.rwt.service.UISessionListener;


/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
@SuppressWarnings("restriction")
public class RapSessionContextProvider
        implements ISessionContextProvider {

    private static Log log = LogFactory.getLog( RapSessionContextProvider.class );

    
    public SessionContext currentContext() {
        if (ContextProvider.hasContext()) {
            ServiceContext serviceContext = ContextProvider.getContext();
            // FIXME always returning new instances raises issues
            return new RapSessionContext( serviceContext );
        }
        return null;
    }
    
    
    /**
     * 
     */
    public class RapSessionContext
            extends SessionContext {

        private ServiceContext              serviceContext;
        
        private Display                     display;

        private UISession                   sessionStore;
        
        private Map<ISessionListener,UISessionListener> listenerMap = new HashMap();


        RapSessionContext( ServiceContext serviceContext ) {
            assert !serviceContext.isDisposed();
            this.serviceContext = serviceContext;
            this.display = LifeCycleUtil.getSessionDisplay();
            this.sessionStore = serviceContext.getUISession();
            assert this.sessionStore != null;
        }


        public Display getDisplay() {
            return display;
        }


        @Override
        public int hashCode() {
            return display.hashCode();
        }

        @Override
        public boolean equals( Object obj ) {
            if (this == obj) {
                return true;
            }
            if (obj instanceof RapSessionContext) {
                return display == ((RapSessionContext)obj).display;
            }
            return false;
        }


        public void destroy() {
            serviceContext = null;
            listenerMap = null;
        }


        @Override
        public boolean isDestroyed() {
            return serviceContext == null /*|| serviceContext.isDisposed()*/ || display.isDisposed();
        }


        @Override
        public String getSessionKey() {
            return sessionStore.getId();
        }

        
        @Override
        public <T> T sessionSingleton( Class<T> type ) {
            return SessionSingletonBase.getInstance( type );
        }


        @Override
        public void execute( Runnable task ) {
//            assert !isDestroyed();
            UICallBack.runNonUIThreadWithFakeContext( display, task );
        }


        @Override
        public <T> T execute( final Callable<T> task ) throws Exception {
//            assert !isDestroyed();
            final AtomicReference<Exception> ee = new AtomicReference();
            final AtomicReference<T> result = new AtomicReference();
            
            UICallBack.runNonUIThreadWithFakeContext( display, new Runnable() {
                public void run() {
                    try {
                        result.set( task.call() );
                    }
                    catch (Exception e) {
                        ee.set( e );
                    }
                }
            });
            if (ee.get() != null) {
                throw ee.get();
            }
            else {
                return result.get();
            }
        }


        @Override
        public boolean addSessionListener( final ISessionListener l ) {
            UISessionListener l2 = new UISessionListener() {
                public void beforeDestroy( UISessionEvent event ) {
                    log.info( "beforeDestroy(): ..." );
                    l.beforeDestroy();
                }
            };
            listenerMap.put( l, l2 );
            return sessionStore.addUISessionListener( l2 );
        }


        @Override
        public boolean removeSessionListener( ISessionListener l ) {
            UISessionListener l2 = listenerMap.remove( l );
            return sessionStore.removeUISessionListener( l2 );
        }


        @Override
        public <T> T getAttribute( String key ) {
//            assert !isDestroyed();
            return (T)sessionStore.getAttribute( key );
        }


        @Override
        public void setAttribute( String key, Object value ) {
//            assert !isDestroyed();
            sessionStore.setAttribute( key, value );
        }


        private RapSessionContextProvider getOuterType() {
            return RapSessionContextProvider.this;
        }
     
        @Override
        public String toString() {
            return "RapSessionContext[serviceContext=" + serviceContext + ", attributes=" + serviceContext.getUISession() + "]";
        }

    }
    
}
