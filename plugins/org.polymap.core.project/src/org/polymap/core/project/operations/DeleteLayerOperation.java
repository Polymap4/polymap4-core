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
package org.polymap.core.project.operations;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.polymap.core.project.ILayer;
import org.polymap.core.project.IMap;
import org.polymap.core.runtime.config.Config2;
import org.polymap.core.runtime.config.ConfigurationFactory;
import org.polymap.core.runtime.config.Immutable;
import org.polymap.core.runtime.config.Mandatory;

import org.polymap.model2.runtime.UnitOfWork;

/**
 *
 * <p/>
 * This might open dialogs and must not be executed with progress dialog.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class DeleteLayerOperation
        extends AbstractOperation
        implements IUndoableOperation {

    private static Log log = LogFactory.getLog( DeleteLayerOperation.class );

    /**
     * The UnitOfWork to work with. This UnitOfWork is committed/rolled back and
     * closed by this operation.
     */
    @Mandatory
    @Immutable
    public Config2<DeleteLayerOperation,UnitOfWork>    uow;
    
    @Immutable
    public Config2<DeleteLayerOperation,ILayer>        layer;


    public DeleteLayerOperation() {
        super( "Delete layer" );
        ConfigurationFactory.inject( this );
    }


    @Override
    public IStatus execute( IProgressMonitor monitor, IAdaptable info ) throws ExecutionException {
        try {
            monitor.beginTask( getLabel(), 2 );
            
            ILayer localLayer = uow.get().entity( layer.get() );
            IMap map = localLayer.parentMap.get();
            if (!map.layers.remove( localLayer )) {
                throw new IllegalStateException( "Unable to remove layer from map." );
            }
            // force commit (https://github.com/Polymap4/polymap4-model/issues/6)
            map.label.set( map.label.get() );
            
            assert !map.layers.contains( localLayer );
            uow.get().removeEntity( localLayer );
            monitor.worked( 1 );
            
            uow.get().commit();
            monitor.done();
            return Status.OK_STATUS;
        }
        catch (Throwable e) {
            uow.get().rollback();
            throw new ExecutionException( e.getMessage(), e );
        }
        finally {
            uow.get().close();
        }
    }


    @Override
    public IStatus redo( IProgressMonitor monitor, IAdaptable info ) throws ExecutionException {
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public IStatus undo( IProgressMonitor monitor, IAdaptable info ) throws ExecutionException {
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public boolean canUndo() {
        return false;
    }
    
    @Override
    public boolean canRedo() {
        return false;
    }

}
