/* 
 * polymap.org
 * Copyright 2009, Polymap GmbH, and individual contributors as indicated
 * by the @authors tag.
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
import org.polymap.service.http.HttpServiceFactory;
import org.polymap.service.http.WmsService;

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
  
    /** Fully qualified name of the service class. */
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

        private transient WmsService        wms;
        
        
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
            pathSpec().set( url );
        }

        public Class getServiceType() {
            try {
                return Thread.currentThread().getContextClassLoader().loadClass( 
                        serviceType().get() );
            }
            catch (ClassNotFoundException e) {
                throw new IllegalStateException( e );
            }
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
            
            String pathSpec = pathSpec().get();
            if (pathSpec == null || pathSpec.length() == 0) {
                pathSpec = ServicesPlugin.simpleName( map.getLabel() );
                // FIXME
                //pathSpec().set( pathSpec );
            }
            pathSpec = !pathSpec.startsWith( "/" ) ? ("/"+pathSpec) : pathSpec;

            wms = HttpServiceFactory.createWMS( map, ServicesPlugin.SERVICES_PATHSPEC + pathSpec, false );
            log.info( "        service URL: " + wms.getURL() );
        }

        public void stop() 
        throws Exception {
//            assert wms != null : "Service is not yet started.";
            IMap map = ProjectRepository.instance().findEntity( IMap.class, mapId().get() );
            log.info( "   Stopping service for map: " + map.getLabel() + " ..." );
            
            try {
                HttpServiceFactory.unregisterServer( wms, true );
            }
            catch (Exception e) {
                log.warn( "", e );
            }
            wms = null;            
        }
        
        public boolean isStarted() {
            return wms != null;
        }

    }
    
}
