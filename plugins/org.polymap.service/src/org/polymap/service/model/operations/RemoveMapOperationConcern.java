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
package org.polymap.service.model.operations;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

import org.polymap.core.operation.IOperationConcernFactory;
import org.polymap.core.operation.OperationConcernAdapter;
import org.polymap.core.operation.OperationInfo;
import org.polymap.core.project.IMap;
import org.polymap.core.project.operations.RemoveMapOperation;

import org.polymap.service.IProvidedService;
import org.polymap.service.ServiceRepository;

/**
 * Listen to {@link RemoveMapOperation} in order to remove an associated service. 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class RemoveMapOperationConcern
        extends IOperationConcernFactory {

    private static Log log = LogFactory.getLog( RemoveMapOperationConcern.class );

    @Override
    public IUndoableOperation newInstance( final IUndoableOperation op, final OperationInfo info ) {
        if (op instanceof RemoveMapOperation) {

            // concern implementation
            return new OperationConcernAdapter() {
                
                @Override
                public IStatus execute( IProgressMonitor _monitor, IAdaptable _info ) 
                throws ExecutionException {
                    ServiceRepository repo = ServiceRepository.instance();
                    IMap removed = ((RemoveMapOperation)op).getMap();
                    
                    List<IProvidedService> services = new ArrayList( repo.allServices() );
                    for (IProvidedService service : services) {
                        if (service.getMap().equals( removed )) {
                            log.info( "Removing service for map: " + removed.getLabel() );
                            repo.removeService( service );
                        }
                    }
                    
                    // call next
                    return info.next().execute( _monitor, _info );
                }

                @Override
                protected OperationInfo getInfo() {
                    return info;
                }
            };
        }
        return null;
    }
    
}
