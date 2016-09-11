/* 
 * polymap.org
 * Copyright 2009-2013, Polymap GmbH. All rights reserved.
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
package org.polymap.core.project.ops;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.polymap.core.project.ILayer;
import org.polymap.core.project.IMap;
import org.polymap.core.runtime.config.Config2;
import org.polymap.core.runtime.config.Immutable;
import org.polymap.core.runtime.config.Mandatory;

import org.polymap.model2.runtime.UnitOfWork;

/**
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class DeleteLayerOperation
        extends TwoPhaseCommitOperation {

    private static Log log = LogFactory.getLog( DeleteLayerOperation.class );

    /** Inbound: The UnitOfWork to work with. */
    @Mandatory
    @Immutable
    public Config2<DeleteLayerOperation,UnitOfWork>    uow;
    
    @Immutable
    public Config2<DeleteLayerOperation,ILayer>        layer;


    public DeleteLayerOperation() {
        super( "Delete layer" );
    }


    @Override
    public IStatus doWithCommit( IProgressMonitor monitor, IAdaptable info ) throws Exception {
        assert layer.get().belongsTo( uow.get() );
        
        monitor.beginTask( getLabel(), 2 );
        register( uow.get() );

        IMap map = layer.get().parentMap.get();
        if (!map.layers.remove( layer.get() )) {
            throw new IllegalStateException( "Unable to remove layer from map." );
        }
        //assert !map.layers.contains( layer.get() );

        // force commit (https://github.com/Polymap4/polymap4-model/issues/6)
        map.label.set( map.label.get() );

        uow.get().removeEntity( layer.get() );
        monitor.worked( 1 );

        uow.get().commit();
        monitor.done();
        return Status.OK_STATUS;
    }

}
