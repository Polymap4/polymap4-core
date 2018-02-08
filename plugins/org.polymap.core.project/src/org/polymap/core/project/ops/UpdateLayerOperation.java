/* 
 * polymap.org
 * Copyright 2016, Polymap GmbH. All rights reserved.
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

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.polymap.core.project.ILayer;
import org.polymap.core.runtime.config.Config2;
import org.polymap.core.runtime.config.Immutable;
import org.polymap.core.runtime.config.Mandatory;

import org.polymap.model2.runtime.UnitOfWork;

/**
 *
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class UpdateLayerOperation
        extends TwoPhaseCommitOperation {

    @Mandatory
    @Immutable
    public Config2<UpdateLayerOperation,UnitOfWork>    uow;
    
    @Immutable
    public Config2<UpdateLayerOperation,ILayer>        layer;


    public UpdateLayerOperation() {
        super( "Update layer" );
    }


    @Override
    public IStatus doWithCommit( IProgressMonitor monitor, IAdaptable info ) throws Exception {
        monitor.beginTask( getLabel(), 2 );
        assert layer.get().belongsTo( uow.get() );
        
        // just commit/rollback the UnitOfWork
        register( uow.get() );
        
        monitor.done();
        return Status.OK_STATUS;
    }

}
