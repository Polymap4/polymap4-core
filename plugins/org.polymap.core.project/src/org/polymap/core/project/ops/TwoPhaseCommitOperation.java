/* 
 * polymap.org
 * Copyright (C) 2016, Polymap GmbH. All rights reserved.
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

import org.polymap.core.operation.DefaultOperation;
import org.polymap.core.runtime.config.ConfigurationFactory;
import org.polymap.model2.Entity;
import org.polymap.model2.runtime.TwoPhaseCommit;
import org.polymap.model2.runtime.TwoPhaseCommit.CommitType;
import org.polymap.model2.runtime.TwoPhaseCommit.TransactionAware;
import org.polymap.model2.runtime.TwoPhaseCommit.UnitOfWorkAdapter;
import org.polymap.model2.runtime.UnitOfWork;

/**
 * Provides a {@link DefaultOperation} for a typically user interface operations
 * that modifies {@link Entity} or other transaction aware resources and does a
 * {@link TwoPhaseCommit}.
 *
 * @author Falko Bräutigam
 */
public abstract class TwoPhaseCommitOperation
        extends DefaultOperation {

    private static final Log log = LogFactory.getLog( TwoPhaseCommitOperation.class );

    private TwoPhaseCommit          twoPhaseCommit = new TwoPhaseCommit();

    
    /**
     * Creates a new instance with the given label. Calls
     * {@link ConfigurationFactory#inject(Object)}.
     */
    public TwoPhaseCommitOperation( String label ) {
        super( label );
        ConfigurationFactory.inject( this );
    }


    /**
     *
     */
    protected abstract IStatus doWithCommit( IProgressMonitor monitor, IAdaptable info ) throws Exception;

    /**
     * Override in order to perform task after commit.
     */
    protected void onSuccess() {
    }

    /**
     * Override in order to perform task after rollback.
     */
    protected void onError( Throwable e ) {
    }
    
    
    @Override
    public final IStatus doExecute( IProgressMonitor monitor, IAdaptable info ) throws Exception {
        try {
            IStatus result = doWithCommit( monitor, info );
            
            assert !twoPhaseCommit.registered().isEmpty() : "No TransactionAware resource registered.";
            twoPhaseCommit.commit( CommitType.KEEP_OPEN );
            onSuccess();
            return result;
        }
        catch (Throwable e) {
            log.warn( "", e );
            twoPhaseCommit.rollback( CommitType.KEEP_OPEN );
            onError( e );
            throw e;
        }
    }

    
    protected TwoPhaseCommitOperation register( UnitOfWork uow ) {
        return register( new UnitOfWorkAdapter( uow ) );
    }
    

    protected TwoPhaseCommitOperation register( TransactionAware res ) {
        twoPhaseCommit.register( res );
        return this;
    }
    
}
