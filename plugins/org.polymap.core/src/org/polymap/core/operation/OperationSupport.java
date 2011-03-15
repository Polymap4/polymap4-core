/* 
 * polymap.org
 * Copyright 2009, Polymap GmbH, and individual contributors as indicated
 * by the @authors tag.
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
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 * $Id$
 */
package org.polymap.core.operation;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.refractions.udig.ui.OffThreadProgressMonitor;
import net.refractions.udig.ui.PlatformJobs;

import org.eclipse.swt.widgets.Display;

import org.eclipse.rwt.SessionSingletonBase;

import org.eclipse.jface.operation.IRunnableWithProgress;

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.DefaultOperationHistory;
import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.IOperationHistoryListener;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.commands.operations.ObjectUndoContext;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ListenerList;

import org.polymap.core.runtime.Polymap;

/**
 * The API and implementation of the operations system.
 * <p>
 * Besides the undo/redo history the <code>OperationSupport</code> also provides
 * the API and SPI to handle Save/Revert/Merge changes to the domain model of
 * the systems.
 * 
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class OperationSupport
        extends SessionSingletonBase {
        //implements IWorkbenchOperationSupport {

    private static Log log = LogFactory.getLog( OperationSupport.class );

//    private IWorkbenchOperationSupport  operationSupport;

    private IUndoContext                context;

    private DefaultOperationHistory     history;

//    private AdvancedValidationUserApprover approver;
    
    private ListenerList                saveListeners = new ListenerList( ListenerList.IDENTITY );
        
    
    /**
     *
     * <p>
     * This method must be called from the UI thread.
     */
    public static OperationSupport instance() {
        return (OperationSupport)getInstance( OperationSupport.class );
    }
    
    
    protected OperationSupport() {
        IWorkbench workbench = PlatformUI.getWorkbench();
        
        // this just returnes one static instance :(
        //operationSupport = workbench.getOperationSupport();

        // this uses a static history 
        //operationSupport = new WorkbenchOperationSupport();
        
        // context
        context = new ObjectUndoContext( this, "Workbench Context" ); //$NON-NLS-1$

        // history
        history = new DefaultOperationHistory();
//        approver = new AdvancedValidationUserApprover( context );
//        history.addOperationApprover( approver );
        history.setLimit( context, 25 );
    }

    
    /**
     * Disposes of anything created by the operation support.
     */
    public void dispose() {
//        history.removeOperationApprover( approver );
        history.dispose( context, true, true, true );
    }


    /**
     * Get the operation that will next be undone in the given undo context.
     * 
     * @return the operation to be undone or null if there is no operation
     *         available. There is no guarantee that the available operation is
     *         valid for the undo.
     */
    public IUndoableOperation getUndoOperation() {
        return history.getUndoOperation( context );
    }
    
    /**
     * Get the operation that will next be redone in the given undo context.
     * 
     * @return the operation to be redone or null if there is no operation
     *         available. There is no guarantee that the available operation is
     *         valid for the undo.
     */
    public IUndoableOperation getRedoOperation() {
        return history.getRedoOperation( context );
    }
    
    public int undoHistorySize() {
        return history.getUndoHistory( context ).length;
    }
    
    public IOperationHistory getOperationHistory() {
        return history;
    }

    public void undo()
    throws ExecutionException {
        final IUndoableOperation op = getUndoOperation();
        assert op != null && op.canUndo();
        
        IRunnableWithProgress runnable = new IRunnableWithProgress(){
            public void run( IProgressMonitor _monitor ) 
            throws InvocationTargetException {
                try {
                    _monitor.beginTask( op.getLabel(), 10 );
                    history.undo( context, _monitor, new OperationInfo() );
                } 
                catch (Exception e) {
                    throw new InvocationTargetException( e );
                }
            }
        };
        execute( runnable, op.getLabel(), true, true );
    }
    
    public void redo()
    throws ExecutionException {
        final IUndoableOperation op = getUndoOperation();
        assert op != null && op.canRedo();
        
        IRunnableWithProgress runnable = new IRunnableWithProgress(){
            public void run( IProgressMonitor _monitor ) 
            throws InvocationTargetException {
                try {
                    _monitor.beginTask( op.getLabel(), 10 );
                    history.redo( context, _monitor, new OperationInfo() );
                } 
                catch (Exception e) {
                    throw new InvocationTargetException( e );
                }
            }
        };
        execute( runnable, op.getLabel(), true, true );
    }
    
    public void addOperationHistoryListener( IOperationHistoryListener l ) {
        history.addOperationHistoryListener( l );
    }
    
    public void removeOperationHistoryListener( IOperationHistoryListener l ) {
        history.removeOperationHistoryListener( l );
    }


    /**
     * Example operation:
     * <pre>
     * try {
     *     OffThreadProgressMonitor monitor = new OffThreadProgressMonitor( _monitor );
     *     JobMonitors.set( monitor );
     * 
     *     monitor.subTask( getLabel() );
     *     ... do it ...
     *     monitor.worked( 1 );
     * }
     * catch (Exception e) {
     *     throw new ExecutionException( &quot;Failure obtaining bounds&quot;, e );
     * }
     * finally {
     *     JobMonitors.remove();
     * }
     * return Status.OK_STATUS;
     * </pre>

     * @param op
     * @param async
     * @param progress Indicates the the operation is executed inside a progress
     *        dialog.
     * @throws ExecutionException
     */
    public void execute( final IUndoableOperation op, boolean async, boolean progress )
            throws ExecutionException {
        op.addContext( context );

        IRunnableWithProgress runnable = new IRunnableWithProgress(){
            public void run( IProgressMonitor _monitor ) 
            throws InvocationTargetException {
                try {
                    _monitor.beginTask( op.getLabel(), 10 );
                    history.execute( op, _monitor, new OperationInfo() );
                } 
                catch (Exception e) {
                    throw new InvocationTargetException( e );
                }
            }
        };
        execute( runnable, op.getLabel(), async, progress );
    }


    protected void execute( IRunnableWithProgress runnable, String label, boolean async, boolean progress )
            throws ExecutionException {
        try {
            // run in new job
            if (Display.getCurrent() != null) {
                if (progress) {
                    PlatformJobs.runInProgressDialog( label, true, runnable, async );
                }
                else if (async) {
                    PlatformJobs.run( runnable, null, label );
                }
                else {
                    PlatformJobs.runSync( runnable, null );
                }
            }
            // non-UI thread (job)
            else {
                OffThreadProgressMonitor monitor = JobMonitors.get();
                // XXX ignoring the async flag assuming that we are in a job
                // already
                runnable.run( monitor );
            }
        }
        catch (InvocationTargetException e) {
            Throwable ee = e.getTargetException();
            if (ee instanceof ExecutionException) {
                throw (ExecutionException)ee;
            }
            else {
                throw new ExecutionException( e.getMessage(), ee );
            }
        }
        catch (InterruptedException e) {
            throw new ExecutionException( "Ausführung der Operation wurde unterbrochen.", e );
        }
    }


    // save / revert **************************************
    
    public void addOperationSaveListener( IOperationSaveListener listener ) {
        saveListeners.add( listener );
    }
    
    public void removeOperationSaveListener( IOperationSaveListener listener ) {
        saveListeners.remove( listener );
    }

    /**
     * Notifies all {@link IOperationSaveListener}s to persistently save
     * changes. If successful the history of operations is disposed.
     * 
     * @throws Exception If the prepare save operation of any of the listeners
     *         failed.
     */
    public void saveChanges()
    throws Exception {
        Object[] listeners = saveListeners.getListeners();
        for (Object listener : listeners) {
            ((IOperationSaveListener)listener).prepareSave( this );
        }        
        for (Object listener : listeners) {
            ((IOperationSaveListener)listener).save( this );
        }
        history.dispose( context, true, true, false );
    }


    /**
     * Notifies all {@link IOperationSaveListener}s to revert changes.
     * Afterwards the history of operations is disposed.
     */
    public void revertChanges() {
        Object[] listeners = saveListeners.getListeners();
        for (Object listener : listeners) {
            ((IOperationSaveListener)listener).revert( this );
        }        
        history.dispose( context, true, true, true );
    }
    
    
    /**
     * Used when execute/undo/redo operation.
     *
     * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
     * @version POLYMAP3 ($Revision$)
     * @since 3.0
     */
    class OperationInfo
            implements IAdaptable {

        Display         display;
        
        
        OperationInfo() {
            super();
            this.display = Polymap.getSessionDisplay();
            assert this.display != null;
        }


        public Object getAdapter( Class adapter ) {
            if (Display.class.isAssignableFrom( adapter)  ) {
                return display;
            }
            else {
                return null;
            }
        }
        
    }

}
