/* 
 * polymap.org
 * Copyright 2013, Falko Bräutigam. All rights reserved.
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
package org.polymap.service.fs.providers;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import java.beans.PropertyChangeEvent;
import java.lang.ref.WeakReference;
import java.security.Principal;

import org.geotools.feature.FeatureCollection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import org.polymap.core.geohub.FeatureSelectionEvent;
import org.polymap.core.geohub.LayerFeatureSelectionManager;
import org.polymap.core.project.IMap;
import org.polymap.core.runtime.SessionContext;
import org.polymap.core.runtime.event.Event;
import org.polymap.core.runtime.event.EventFilter;
import org.polymap.core.runtime.event.EventHandler;
import org.polymap.core.runtime.event.EventManager;

/**
 * Tracks the state (map, extent, selected features, etc.) of each user Workbench
 * session.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class WorkbenchState {

    private static Log log = LogFactory.getLog( WorkbenchState.class );
 
    private static ConcurrentMap<Principal,WorkbenchState> states = new ConcurrentHashMap();
    
    private static Object mapListener;
    
    private static Object featureListener;
    
    
    public static WorkbenchState instance( SessionContext session ) {
        Principal user = (Principal)session.getAttribute( "user" );
        assert user != null;
        WorkbenchState result = states.get( user );
        if (result == null) {
            result = new WorkbenchState();
            WorkbenchState previous = states.putIfAbsent( user, result );
            // handle concurrent calls to this method
            result = previous != null ? previous : result;
        }
        return result;
    }

    
    public static void startup() {
        new Job( "WorkbenchState.startup" ) {
            protected IStatus run( IProgressMonitor monitor ) {
                log.info( "Installing listener..." );
                // mapListener
                EventManager.instance().subscribe(
                        // listener
                        mapListener = new Object() {
                            @EventHandler(scope=Event.Scope.JVM)
                            protected void handleEvent( PropertyChangeEvent ev ) {
                                SessionContext session = EventManager.publishSession();
                                if (!session.isDestroyed()) {
                                    WorkbenchState state = WorkbenchState.instance( session );
                                    state.handleEvent( ev );
                                }
                            }                            
                        }, 
                        // filter
                        new EventFilter<PropertyChangeEvent>() {
                            public boolean apply( PropertyChangeEvent ev ) {
                                return ev.getSource() instanceof IMap;
                            }
                        }
                );
                // featureListener
                EventManager.instance().subscribe(
                        // listener
                        featureListener = new Object() {
                            @EventHandler(scope=Event.Scope.JVM)
                            protected void handleEvent( FeatureSelectionEvent ev ) {
                                WorkbenchState state = WorkbenchState.instance( EventManager.publishSession() );
                                state.handleEvent( ev );
                            }                            
                        }, 
                        // filter
                        new EventFilter<FeatureSelectionEvent>() {
                            public boolean apply( FeatureSelectionEvent ev ) {
                                return true;
                            }
                        }
                );
                return Status.OK_STATUS;
            }            
        }.schedule( 10000 );
    }

    
    // instance *******************************************
    
    private WeakReference<IMap>     map;
    
    private Date                    mapModified;
    
    private FeatureCollection       selectedFeatures;
    
    private Date                    featuresModified;
    

    protected void handleEvent( PropertyChangeEvent ev ) {
        log.debug( "Event: " + ev );
        log.debug( "my session: " + SessionContext.current() );
        log.debug( "publish session: " + EventManager.publishSession() );
        
        map = new WeakReference( ev.getSource() );
        mapModified = new Date();
        
        //getSite().invalidateFolder( getSite().getFolder( new Path( "/Workbench" ) ) );
    }

    protected void handleEvent( FeatureSelectionEvent ev ) {
        LayerFeatureSelectionManager fsm = ev.getSource();
        selectedFeatures = fsm.getFeatureCollection();
        featuresModified = new Date();
    }

    public IMap getMap() {
        return map != null ? map.get() : null;
    }
    
    public Date getMapModified() {
        return mapModified;
    }

    public FeatureCollection getSelectedFeatures() {
        return selectedFeatures;
    }

    public Date getFeaturesModified() {
        return featuresModified;
    }
    
}
