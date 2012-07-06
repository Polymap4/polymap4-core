/* 
 * polymap.org
 * Copyright 2009-2012, Polymap GmbH. All rights reserved.
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
package org.polymap.service.model;

import java.util.List;

import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.project.IMap;
import org.polymap.core.project.ProjectRepository;
import org.polymap.core.qi4j.QiEntity;
import org.polymap.core.qi4j.event.ModelChangeSupport;
import org.polymap.core.qi4j.event.PropertyChangeSupport;
import org.polymap.service.IProvidedService;
import org.polymap.service.ServicesPlugin;
import org.polymap.service.http.MapHttpServletFactory;
import org.polymap.service.http.MapHttpServer;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @since 3.0
 */
@Concerns( {
    PropertyChangeSupport.Concern.class
})
@Mixins( {
    ProvidedServiceComposite.Mixin.class,
    PropertyChangeSupport.Mixin.class,
    ModelChangeSupport.Mixin.class,
    QiEntity.Mixin.class
} )
public interface ProvidedServiceComposite
        extends QiEntity, IProvidedService, PropertyChangeSupport, ModelChangeSupport, EntityComposite {

    @Optional
    @UseDefaults
    Property<Boolean>                   enabled();

    @Optional
    Property<String>                    pathSpec();
  
    /** One of the <code>SERVICE_TYPE_xxx</code> constants in {@link ServicesPlugin}. */
    @Optional
    Property<String>                    serviceType();
    
    @Optional
    @UseDefaults
    Property<String>                    mapId();

    @Optional
    @UseDefaults
    Property<List<String>>              srs();


    /**
     * Transient fields and methods. 
     * <p>
     * Impl. note: property change events are handled by the
     * {@link ChangeEventSideEffect}.
     */
    public static abstract class Mixin
            implements ProvidedServiceComposite {
        
        private static Log log = LogFactory.getLog( Mixin.class );

        @This ProvidedServiceComposite      composite;

        private transient MapHttpServer    wms;
        
        
        public boolean isEnabled() {
            return enabled().get();
        }
        
        public void setEnabled( Boolean enabled ) {
            enabled().set( enabled );
        }
        
        public String getMapId() {
            return mapId().get();
        }
        
        public IMap getMap() {
            return ProjectRepository.instance().findEntity( IMap.class, mapId().get() );
        }

        public void setMap( IMap map ) {
            mapId().set( map.id() );
        }

        public String getPathSpec() {
            return pathSpec().get();
        }

        public void setPathSpec( String url ) {
            assert ServicesPlugin.validPathSpec( url ).equals( url );
            pathSpec().set( url );
        }

        public String getServiceType() {
            return serviceType().get();
        }

        public boolean isServiceType( String serviceType ) {
            assert serviceType != null;
            return serviceType().get().equals( serviceType );
        }

        public void setServiceType( Class cl ) {
            serviceType().set( cl.getName() );
        }

        public List<String> getSRS() {
            return srs().get();
        }
        
        public void setSRS( List<String> srs ) {
            srs().set( srs );
        }

        
        public void start() 
        throws Exception {
//            assert wms == null : "Service is started already.";
            // services are started outside a request
            IMap map = ProjectRepository.instance().findEntity( IMap.class, mapId().get() );
            log.info( "   Starting service for map: " + map.getLabel() + " ..." );
            
            String pathSpec = getPathSpec();
            if (pathSpec == null || pathSpec.length() == 0) {
                // XXX why is this default needed?
                pathSpec = ServicesPlugin.validPathSpec( map.getLabel() );
            }

            wms = MapHttpServletFactory.createWMS( map, pathSpec, false );
            log.info( "        service URL: " + wms.getPathSpec() );
        }

        
        public void stop() 
        throws Exception {
            if (wms == null) {
                log.info( "No service started: " + pathSpec().get() );
            }
            else {
                IMap map = ProjectRepository.instance().findEntity( IMap.class, mapId().get() );
                log.info( "   Stopping service for map: " + map.getLabel() + " ..." );

                try {
                    MapHttpServletFactory.destroyServer( wms );
                }
                catch (Exception e) {
                    log.warn( "", e );
                }
                wms = null;
            }
        }
        
        public boolean isStarted() {
            return wms != null;
        }

    }
    
}
