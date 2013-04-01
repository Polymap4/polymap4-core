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
package org.polymap.core.data.feature.buffer;

import static com.google.common.collect.Iterables.*;

import org.opengis.filter.Filter;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.polymap.core.data.PipelineFeatureSource;
import org.polymap.core.project.ILayer;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class RevertLayerDataOperation
        extends AbstractOperation
        implements IUndoableOperation {

    private ILayer          layer;
    
    
    public RevertLayerDataOperation( String label, ILayer layer ) {
        super( label );
        this.layer = layer;
    }


    @Override
    public IStatus execute( IProgressMonitor monitor, IAdaptable info )
            throws ExecutionException {
        try {
            PipelineFeatureSource fs = PipelineFeatureSource.forLayer( layer, false );
            IFeatureBufferProcessor proc = getOnlyElement( filter( fs.getPipeline(), IFeatureBufferProcessor.class ), null );
            if (proc != null) {
                proc.revert( Filter.INCLUDE, monitor );
            }
            return Status.OK_STATUS;
        }
        catch (Exception e) {
            throw new ExecutionException( e.getLocalizedMessage(), e );
        }
    }
    
    @Override
    public boolean canUndo() {
        return false;
    }

    @Override
    public IStatus redo( IProgressMonitor monitor, IAdaptable info )
            throws ExecutionException {
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public IStatus undo( IProgressMonitor monitor, IAdaptable info )
            throws ExecutionException {
        throw new RuntimeException( "not yet implemented." );
    }
    
}
