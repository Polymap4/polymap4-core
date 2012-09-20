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
package org.polymap.core.data.operation;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @since 3.1
 */
public interface IFeatureOperation {

    public enum Status { OK, Error, Cancel };


    /**
     * Init this operation and check if it can run in the given context.
     * <p/>
     * Implementations should avoid accessing the features of the context as this
     * may block the UIThread. Rather delegate to {@link #execute()} and display a
     * message there.
     * 
     * @param context
     * @return True if this operation can work in the given context, false otherwise.
     */
    public boolean init( IFeatureOperationContext context );

    public IFeatureOperationContext getContext();
    
//    /**
//     * Return the label that should be used to show the name of the operation to the
//     * user. This label is typically combined with the command strings shown to the
//     * user in "Undo" and "Redo" user interfaces.
//     * 
//     * @return the String label. Should never be <code>null</code>.
//     */
//    public String getLabel();
//
//    public ImageDescriptor getImageDescriptor();
//    
//    public String getTooltip();

    /**
     * Returns whether the operation can be executed in its current state.
     * <p/>
     * Note: The computation for this method must be fast, as it is called
     * frequently. If necessary, this method can be optimistic in its computation
     * (returning true) and later perform more time-consuming computations during the
     * actual execution of the operation, returning the appropriate status if the
     * operation cannot actually execute at that time.
     * 
     * @return <code>true</code> if the operation can be executed; <code>false</code>
     *         otherwise.
     */
    public boolean canExecute();


    /**
     * Execute the operation. This method is only be called the first time an
     * operation is executed.
     * 
     * @param monitor the progress monitor (or <code>null</code>) to use for
     *        reporting progress to the user.
     * @param info the IAdaptable (or <code>null</code>) provided by the caller in
     *        order to supply UI information for prompting the user if necessary.
     *        When this parameter is not <code>null</code>, it should minimally
     *        contain an adapter for the org.eclipse.swt.widgets.Shell.class.
     * 
     * @return the Status of the execution. The status severity should be set to
     *         <code>OK</code> if the operation was successful, and
     *         <code>ERROR</code> if it was not. Any other status is assumed to
     *         represent an incompletion of the execution.
     * @throws ExecutionException if an exception occurred during execution.
     */
    public Status execute( IProgressMonitor monitor )
    throws Exception;
    
    public boolean canUndo();

    public Status undo( IProgressMonitor monitor )
    throws Exception;
    
    public boolean canRedo();

    public Status redo( IProgressMonitor monitor )
    throws Exception;
    
}
