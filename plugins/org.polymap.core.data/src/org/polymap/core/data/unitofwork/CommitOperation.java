/* 
 * polymap.org
 * Copyright (C) 2016, Falko Bräutigam. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.core.data.unitofwork;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.polymap.core.operation.DefaultOperation;
import org.polymap.core.runtime.config.Config2;
import org.polymap.core.runtime.config.ConfigurationFactory;
import org.polymap.core.runtime.config.Mandatory;

/**
 * Commits a given {@link UnitOfWork}.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class CommitOperation
        extends DefaultOperation {

    private static Log log = LogFactory.getLog( CommitOperation.class );

    @Mandatory
    public Config2<CommitOperation,UnitOfWork>  uow;
    
    
    public CommitOperation() {
        super( "Commit" );
        ConfigurationFactory.inject( this );
    }


    @Override
    protected IStatus doExecute( IProgressMonitor monitor, IAdaptable info ) throws Exception {
        uow.get().prepare( monitor );
        uow.get().commit( monitor );
        return Status.OK_STATUS;
    }
    
}
