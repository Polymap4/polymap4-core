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
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 * $Id$
 */

package org.polymap.core.services.qi4j;

import java.util.List;

import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.project.IMap;
import org.polymap.core.project.ProjectRepository;
import org.polymap.core.qi4j.EntityMixin;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.services.IProvidedService;
import org.polymap.core.services.ServicesPlugin;
import org.polymap.core.services.http.HttpServiceFactory;
import org.polymap.core.services.http.WmsService;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
@Mixins( {
    ProvidedServiceComposite.Mixin.class, 
    EntityMixin.class
} )
public interface ProvidedServiceComposite
        extends IProvidedService, EntityComposite {

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
            return Polymap.getSessionDisplay() != null
                    ? ProjectRepository.instance().findEntity( IMap.class, mapId().get() )
                    : ProjectRepository.globalInstance().findEntity( IMap.class, mapId().get() );
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
            assert wms == null : "Service is started already.";
            // services are started outside a request
            IMap map = ProjectRepository.globalInstance().findEntity( IMap.class, mapId().get() );
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
            assert wms != null : "Service is not yet started.";
            IMap map = ProjectRepository.globalInstance().findEntity( IMap.class, mapId().get() );
            log.info( "   Stopping service for map: " + map.getLabel() + " ..." );
            
            HttpServiceFactory.unregisterServer( wms, false );
            wms = null;            
        }
    }
    
}
