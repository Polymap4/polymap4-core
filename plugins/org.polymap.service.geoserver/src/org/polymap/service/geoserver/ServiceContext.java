/* 
 * polymap.org
 * Copyright (C) 2011-2013, Polymap GmbH. All rights reserved.
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
package org.polymap.service.geoserver;

import java.beans.PropertyChangeEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.InstanceScope;

import org.polymap.core.runtime.Polymap;
import org.polymap.core.runtime.entity.EntityStateEvent;
import org.polymap.core.runtime.entity.EntityStateEvent.EventType;
import org.polymap.core.runtime.entity.EntityStateTracker;
import org.polymap.core.runtime.entity.IEntityStateListener;
import org.polymap.core.runtime.session.DefaultSessionContextProvider;
import org.polymap.core.runtime.session.SessionContext;
import org.polymap.core.runtime.session.SessionSingleton;

/**
 * Provides a {@link SessionContext session context} for a service (
 * {@link IProvidedService}) or other services that access a {@link SessionSingleton}
 * (entity repositories for example) and need a session context for that. It
 * maps/unmaps session context. Also handles service start/stop/restart via
 * {@link IModelChangeListener} and {@link EntityStateEvent}.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class ServiceContext
        implements IEntityStateListener/*, IPropertyChangeListener*/ {

    private static Log log = LogFactory.getLog( ServiceContext.class );
    
    private static final DefaultSessionContextProvider contextProvider;
    
    
    static {
        contextProvider = new DefaultSessionContextProvider();
        SessionContext.addProvider( contextProvider );
    }
    
    public static void mapContext( String sessionKey ) {
        contextProvider.mapContext( sessionKey, false );    
    }

    public static void unmapContext( boolean failFast ) {
        try {
            contextProvider.unmapContext();
        }
        catch (RuntimeException e) {
            log.debug( "", e );
            if (failFast) {
                throw e;
            }
        }
    }
    
    
    // instance *******************************************
    
    protected String                serviceId;

    protected String                sessionKey;


    public ServiceContext( String serviceId ) {
        this.serviceId = serviceId;
        this.sessionKey = "service-" + serviceId;
//        try {
////            log.info( "mapping sessionKey: " + sessionKey );
//            boolean mapped = contextProvider.mapContext( sessionKey, true );
//            assert mapped : "sessionKey already mapped: " + sessionKey;
//            Polymap.instance().addPrincipal( new ServicesPlugin.AdminPrincipal() );
//        }
//        finally {
//            contextProvider.unmapContext();
//        }
        startService();
    }
    

    protected abstract void start() throws Exception;
    
    protected abstract void stop() throws Exception;
    
    /**
     * Does this context needs a restart after the given event?
     */
    protected abstract boolean needsRestart( EntityStateEvent ev ) throws Exception;
    
    
    /**
     * Starts this service context. Starts listening to model changes. If the
     * service is currently enabled, then start the service.
     */
    public void startService() {
        // "clean" thread to be able to map a new session context
        new Job( "Re-starting Service: " + serviceId ) {
            protected IStatus run( IProgressMonitor monitor ) {
                try {
                    boolean newContext = contextProvider.mapContext( sessionKey, true );
                    Polymap.instance().addPrincipal( new ServicesPlugin.AdminPrincipal() );

                    if (contextProvider.currentContext().isDestroyed()) {
                        log.warn( "Context already destroyed: " + sessionKey );
                        return Status.OK_STATUS;
                    }

                    // start service
                    start();

                    // listen to global change events of the map and layers (in the new context)
                    EntityStateTracker.instance().addListener( ServiceContext.this );

                    // listen to preference changes
                    final ScopedPreferenceStore prefStore = new ScopedPreferenceStore( 
                            new InstanceScope(), ServicesPlugin.getDefault().getBundle().getSymbolicName() );
                    prefStore.addPropertyChangeListener( ServiceContext.this );
                }
                catch (Exception e) {
                    log.error( "Error while starting services: " + serviceId, e );
                }
                finally {
                    contextProvider.unmapContext();
                }
                return Status.OK_STATUS;
            }
        }.schedule();
    }


    /**
     * Stops this service context. Remove model change listener. Stops the service if
     * it is running currently.
     */
    public void stopService() {
        try {
            contextProvider.mapContext( sessionKey, false );
            if (contextProvider.currentContext().isDestroyed()) {
                log.warn( "Context already destroyed: " + sessionKey );
                return;
            }
            
            // stop service
            stop();
            
            // unregister listener
            try {
                EntityStateTracker.instance().removeListener( ServiceContext.this );
            }
            catch (Throwable e) {
                log.warn( "", e );
                // FIXME hack to 
//                service = null;
            }
        }
        catch (Exception e) {
            log.error( "Error while stopping services: " + serviceId, e );
        }
        finally {
            contextProvider.unmapContext();
        }
        // destroy session
        contextProvider.destroyContext( sessionKey );
    }

    
    // event handling *************************************

    public void modelChanged( final EntityStateEvent ev ) {
        if (ev.getEventType() == EventType.COMMIT) {
            
            // the event comes within a Job but with RAP session context (in most cases)
            // so we need a "clean" Job to be able to map a new session context
            new Job( "Re-starting Service: " + serviceId ) {
                protected IStatus run( IProgressMonitor monitor ) {
                    boolean needsUnmap = contextProvider.mapContext( sessionKey, false );
                    assert needsUnmap;
                    if (contextProvider.currentContext().isDestroyed()) {
                        log.warn( "Context is already destroyed -> stopping." );
                        stopService();
                        return Status.OK_STATUS;
                    }

                    boolean needsRestart = false;
                    try {
                        needsRestart = needsRestart( ev );
                    }
                    // primary NoSuchEntityException
                    catch (Exception e) {
                        needsRestart = true;
                    }
                    finally {
                        contextProvider.unmapContext();
                    }

                    if (needsRestart) {
                        stopService();
                        startService();
                    }
                    return Status.OK_STATUS;
                }
            }.schedule();
        }
    }
    
    public boolean isValid() {
        return true;
    }
    

    public void propertyChange( PropertyChangeEvent ev ) {
        log.debug( "Preferences changed: " + ev.getProperty() );
        if (ev.getProperty().equals( ServicesPlugin.PREF_PROXY_URL )) {

            // the event comes within a Job but with RAP session context (in most cases)
            // so we nee a "clean" Job to be able to map a new session context
            new Job( "Re-starting Service: " + serviceId ) {
                protected IStatus run( IProgressMonitor monitor ) {
                    try {
                        boolean needsUnmap = contextProvider.mapContext( sessionKey, true );
                        assert needsUnmap;
                        
                        stopService();
                        startService();
                        return Status.OK_STATUS;
                    }
                    finally {
                        contextProvider.unmapContext();
                    }
                }
            }.schedule();
        }
    }

}
