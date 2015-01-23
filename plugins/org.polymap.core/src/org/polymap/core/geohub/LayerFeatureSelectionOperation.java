/* 
 * polymap.org
 * Copyright 2011, Polymap GmbH. All rights reserved.
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
package org.polymap.core.geohub;

import java.beans.PropertyChangeListener;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.polymap.core.Messages;
import org.polymap.core.geohub.LayerFeatureSelectionManager.MODE;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class LayerFeatureSelectionOperation
        extends AbstractOperation
        implements IUndoableOperation {

    private static Log log = LogFactory.getLog( LayerFeatureSelectionOperation.class );

    private static int             opCount;
    
    private Filter                 newFilter;

    private MODE                   modeHint;

    private PropertyChangeListener ommit;

    private Object                 layer;
    
    private Filter                 undoFilter;


    public LayerFeatureSelectionOperation() {
        super( Messages.get( "LayerFeatureSelectionOperation_name" ) + " (" + opCount++ + ")" );
    }


    @SuppressWarnings("hiding")
    public void init( Object layer, Filter newFilter, MODE modeHint, PropertyChangeListener ommit ) {
        this.layer = layer;
        this.newFilter = newFilter;
        this.modeHint = modeHint;
        this.ommit = ommit;
    }
    

    public IStatus execute( IProgressMonitor monitor, IAdaptable info )
    throws ExecutionException {
        monitor.beginTask( getLabel(), 100 );
        monitor.worked( 10 );
        LayerFeatureSelectionManager fsm = LayerFeatureSelectionManager.forLayer( layer );
        undoFilter = fsm.getFilter();
        fsm.changeSelection( newFilter, modeHint, ommit );
        monitor.done();
        return Status.OK_STATUS;
    }


    public IStatus undo( IProgressMonitor monitor, IAdaptable info )
    throws ExecutionException {
        LayerFeatureSelectionManager fsm = LayerFeatureSelectionManager.forLayer( layer );
        fsm.changeSelection( undoFilter, MODE.REPLACE, null );
        return Status.OK_STATUS;
    }


    public IStatus redo( IProgressMonitor monitor, IAdaptable info )
    throws ExecutionException {
        LayerFeatureSelectionManager fsm = LayerFeatureSelectionManager.forLayer( layer );
        fsm.changeSelection( newFilter, modeHint, null );
        return Status.OK_STATUS;
    }


}