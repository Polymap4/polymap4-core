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
package org.polymap.core.runtime;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.widgets.Display;

import org.eclipse.rwt.RWT;
import org.eclipse.rwt.SessionSingletonBase;
import org.eclipse.rwt.internal.lifecycle.RWTLifeCycle;
import org.eclipse.rwt.internal.service.ContextProvider;
import org.eclipse.rwt.internal.service.ServiceContext;
import org.eclipse.rwt.lifecycle.UICallBack;
import org.eclipse.rwt.service.ISessionStore;
import org.eclipse.rwt.service.SessionStoreEvent;
import org.eclipse.rwt.service.SessionStoreListener;


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
    class RapSessionContext
            extends SessionContext {

        private ServiceContext              serviceContext;
        
        private Display                     display;

        private ISessionStore               sessionStore;


        RapSessionContext( ServiceContext serviceContext ) {
            assert !serviceContext.isDisposed();
            this.serviceContext = serviceContext;
            this.display = RWTLifeCycle.getSessionDisplay();
            this.sessionStore = serviceContext.getSessionStore();
            assert this.sessionStore != null;
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
        }


        public boolean isDestroyed() {
            return serviceContext == null /*|| serviceContext.isDisposed()*/ || display.isDisposed();
        }


        public String getSessionKey() {
            return serviceContext.getSessionStore().getId();
        }

        
        public <T> T sessionSingleton( Class<T> type ) {
            return (T)SessionSingletonBase.getInstance( type );
        }


        public void execute( Runnable task ) {
//            assert !isDestroyed();
            UICallBack.runNonUIThreadWithFakeContext( display, task );
        }


        public <T> T execute( final Callable<T> task ) 
        throws Exception {
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


        public boolean addSessionListener( final ISessionListener l ) {
            return RWT.getSessionStore().addSessionStoreListener( new SessionStoreListener() {
                public void beforeDestroy( SessionStoreEvent event ) {
                    log.info( "beforeDestroy(): ..." );
                    l.beforeDestroy();
                }
            });
        }


        public boolean removeSessionListener( ISessionListener l ) {
            // XXX Auto-generated method stub
            throw new RuntimeException( "not yet implemented." );
        }


        public Object getAttribute( String key ) {
//            assert !isDestroyed();
            return sessionStore.getAttribute( key );
        }


        public void setAttribute( String key, Object value ) {
//            assert !isDestroyed();
            sessionStore.setAttribute( key, value );
        }


        private RapSessionContextProvider getOuterType() {
            return RapSessionContextProvider.this;
        }
     
        @Override
        public String toString() {
            return "RapSessionContext[serviceContext=" + serviceContext + ", attributes=" + serviceContext.getSessionStore() + "]";
        }

    }
    
}
