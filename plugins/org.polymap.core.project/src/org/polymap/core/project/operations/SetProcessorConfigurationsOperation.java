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

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.polymap.core.project.PipelineHolder;
import org.polymap.core.project.PipelineProcessorConfiguration;
import org.polymap.core.qi4j.event.AbstractModelChangeOperation;

/**
 * This operation allows to set the pipeline processor configs
 * (see {@link PipelineHolder}).
 * 
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @since 3.0
 */
public class SetProcessorConfigurationsOperation
        extends AbstractModelChangeOperation {

    private PipelineHolder          holder;

    private PipelineProcessorConfiguration[] procs;


    public SetProcessorConfigurationsOperation() {
        super( "[undefined]" );
    }


    public void init( PipelineHolder _holder, PipelineProcessorConfiguration[] _procs ) {
        this.holder = _holder;
        this.procs = _procs;
    }


    public IStatus doExecute( IProgressMonitor monitor, IAdaptable info )
            throws ExecutionException {
        holder.setProcessorConfigs( procs );
        return Status.OK_STATUS;
    }

}
