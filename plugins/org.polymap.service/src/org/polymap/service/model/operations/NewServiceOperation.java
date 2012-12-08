/* 
 * polymap.org
 * Copyright 2009-20122, Polymap GmbH. ALl rights reserved.
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
package org.polymap.service.model.operations;

import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.mixin.Mixins;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.polymap.core.project.IMap;
import org.polymap.core.qi4j.event.AbstractModelChangeOperation;
import org.polymap.service.ServiceRepository;
import org.polymap.service.ServicesPlugin;
import org.polymap.service.model.ProvidedServiceComposite;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @since 3.0
 */
@Mixins( NewServiceOperation.Mixin.class )
public interface NewServiceOperation
        extends IUndoableOperation, TransientComposite {

    static Log log = LogFactory.getLog( NewServiceOperation.class );
    

    public void init( IMap _map, String serviceType );
    
    /** Implementation is provided bei {@link AbstractOperation} */ 
    public boolean equals( Object obj );
    
    public int hashCode();

    
    /**
     * Implementation. 
     */
    public static abstract class Mixin
            extends AbstractModelChangeOperation
            implements NewServiceOperation {

        private IMap                    map;

        private String                  serviceType;

        private String                  pathSpec;


        public Mixin() {
            super( "[undefined]" );
        }


        public void init( IMap _map, String _serviceType ) {
            this.map = _map;
            this.serviceType = _serviceType;
            this.pathSpec = ServicesPlugin.validPathSpec( map.getLabel() );
            setLabel( "Service anlegen" );
        }


        public IStatus doExecute( IProgressMonitor monitor, IAdaptable info )
        throws ExecutionException {
            try {
                ServiceRepository repo = ServiceRepository.instance();
                ProvidedServiceComposite service = repo.newEntity( ProvidedServiceComposite.class, null );
                service.mapId().set( map.id() );
                service.serviceType().set( serviceType );
                service.setPathSpec( pathSpec );
                
                repo.addService( service );
            }
            catch (Throwable e) {
                throw new ExecutionException( e.getMessage(), e );
            }
            return Status.OK_STATUS;
        }

    }

}
