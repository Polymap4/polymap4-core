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
package org.polymap.core;

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
import org.eclipse.rwt.service.SessionStoreEvent;
import org.eclipse.rwt.service.SessionStoreListener;

import org.polymap.core.runtime.ISessionContextProvider;
import org.polymap.core.runtime.ISessionListener;
import org.polymap.core.runtime.SessionContext;

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
            return new RapSessionContext( serviceContext );
        }
        return null;
    }
    
    
    /*
     * 
     */
    class RapSessionContext
            extends SessionContext {

        private ServiceContext      serviceContext;
        
        private Display             display;


        RapSessionContext( ServiceContext serviceContext ) {
            this.serviceContext = serviceContext;
            this.display = RWTLifeCycle.getSessionDisplay();
        }


        public void destroy() {
        }


        public String getSessionKey() {
            return serviceContext.getSessionStore().getId();
        }

        
        public <T> T sessionSingleton( Class<T> type ) {
            return (T)SessionSingletonBase.getInstance( type );
        }


        public void execute( Runnable task ) {
            UICallBack.runNonUIThreadWithFakeContext( display, task );
        }


        public <T> T execute( final Callable<T> task ) 
        throws Exception {
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
            // XXX Auto-generated method stub
            throw new RuntimeException( "not yet implemented." );
        }


        public void setAttribute( String key, Object value ) {
            // XXX Auto-generated method stub
            throw new RuntimeException( "not yet implemented." );
        }
        
    }
    
}
