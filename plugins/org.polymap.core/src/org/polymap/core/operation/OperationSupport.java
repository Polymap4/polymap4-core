/* 
 * polymap.org
 * Copyright (C) 2009-2013, Polymap GmbH. All rights reserved.
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
package org.polymap.core.operation;

import java.util.function.Consumer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.DefaultOperationHistory;
import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.IOperationHistoryListener;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.commands.operations.ObjectUndoContext;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;

import org.polymap.core.runtime.UIJob;
import org.polymap.core.runtime.session.SessionSingleton;

/**
 * The API and implementation of the operations system.
 * <p/>
 * Besides the undo/redo history the <code>OperationSupport</code> also provides
 * the API and SPI to handle Save/Revert/Merge changes to the domain model of
 * the systems.
 * 
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @since 3.0
 */
public class OperationSupport
        extends SessionSingleton {

    private static Log log = LogFactory.getLog( OperationSupport.class );

//    /**
//     * An error handler that uses the {@link StatusDispatcher} to show an error
//     * message to the user.
//     */
//    public static final Consumer<Throwable> showErrorMsg( String msg ) {
//        return e -> StatusDispatcher.handle( new Status( IStatus.ERROR, CorePlugin.PLUGIN_ID, msg, e ), Style.SHOW );
//    }
        
    /**
     * Scheduling rule that allows just the associated (save) Job to be executed.
     * Used for the the {@link OperationSupport#saveChanges()} job.
     */
    class OneSaver 
            implements ISchedulingRule {
        
        public boolean isConflicting( ISchedulingRule rule ) {
            return rule == this || rule instanceof OneSaver || rule instanceof MultipleOperations;
        }
        public boolean contains( ISchedulingRule rule ) {
            return rule == this;
        }
    }

    /**
     * Scheduling rule that allows multiple worker jobs to be executed but conflicts
     * with {@link OneSaver} jobs.
     */
    class MultipleOperations 
            implements ISchedulingRule {
        @Override
        public boolean isConflicting( ISchedulingRule rule ) {
            return rule == this || rule instanceof OneSaver;
        }
        @Override
        public boolean contains( ISchedulingRule rule ) {
            return rule instanceof MultipleOperations
                    // allow for nested resource opne/update
                    || rule instanceof IResource;
        }
    }

    
    // instance *******************************************
    
    private IUndoContext                context;

    private DefaultOperationHistory     history;

//    private AdvancedValidationUserApprover approver;
    
//    private List<IOperationSaveListener> saveListeners = new LinkedList();
    
    
    /**
     *
     * <p>
     * This method must be called from the UI thread.
     */
    public static OperationSupport instance() {
        return instance( OperationSupport.class );
    }
    
    
    protected OperationSupport() {
        // context
        context = new ObjectUndoContext( this, "Polymap Context" );

        // history
        history = new DefaultOperationHistory();
        history.setLimit( context, 25 );
    }

    
    /**
     * Disposes of anything created by the operation support.
     */
    public void dispose() {
        history.dispose( context, true, true, true );
    }


    public boolean canUndo() {
        return history.canUndo( context );
    }
    
    public boolean canRedo() {
        return history.canRedo( context );
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

    public void addOperationHistoryListener( IOperationHistoryListener l ) {
        history.addOperationHistoryListener( l );
    }
    
    public void removeOperationHistoryListener( IOperationHistoryListener l ) {
        history.removeOperationHistoryListener( l );
    }

    
    public void undo() throws ExecutionException {
        IUndoableOperation op = getUndoOperation();
        assert op != null && op.canUndo();
        
        OperationJob job = new OperationJob( op ) {
            protected void run() throws Exception {
                // try to preset task name without beginTask()
                monitor.setTaskName( op.getLabel() );
                history.undo( context, monitor, null );
            }
        };
        run( job, true, true );
    }
    
    
    public void redo() throws ExecutionException {
        IUndoableOperation op = getRedoOperation();
        assert op != null && op.canRedo();
        
        OperationJob job = new OperationJob( op ) {
            protected void run() throws Exception {
                // try to preset task name without beginTask()
                monitor.setTaskName( op.getLabel() );
                history.redo( context, monitor, null );
            }
        };
        run( job, true, true );
    }
    
    
    public void execute( final IUndoableOperation op, boolean async, boolean progress, Runnable... doneHandlers ) {
        IJobChangeListener[] listeners = new IJobChangeListener[ doneHandlers.length ];
        for (int i=0; i<doneHandlers.length; i++) {
            Runnable handler = doneHandlers[i];
            listeners[i] = new JobChangeAdapter() {
                @Override
                public void done( IJobChangeEvent ev ) {
                    handler.run();
                }
            };
        }
        execute( op, async, progress, listeners );
    }
    
    
    public void execute( final IUndoableOperation op, boolean async, boolean progress, Consumer<IJobChangeEvent>... doneHandlers ) {
        IJobChangeListener[] listeners = new IJobChangeListener[ doneHandlers.length ];
        for (int i=0; i<doneHandlers.length; i++) {
            Consumer<IJobChangeEvent> handler = doneHandlers[i];
            listeners[i] = new JobChangeAdapter() {
                @Override
                public void done( IJobChangeEvent ev ) {
                    handler.accept( ev );
                }
            };
        }
        execute( op, async, progress, listeners );
    }
    
    
    /**
     * Executes the given operation inside a {@link UIJob job}.
     * 
     * @param op
     * @param async Indicates that the calling thread should not block execution and
     *        return imediatelly.
     * @param progress Indicates the the operation is executed inside a progress
     *        dialog. Make sure that the operation does not open dialogs as they might get covered up
     *        by the progress dialog. 
     * @throws ExecutionException
     */
    public void execute( final IUndoableOperation op, boolean async, boolean progress, IJobChangeListener... listeners ) {
        OperationJob job = new OperationJob( op ) {
            protected void run() throws Exception {
                // try to preset task name without beginTask()
                monitor.setTaskName( op.getLabel() );
                
                OperationExecutor executor = OperationExecutor.newInstance( op );
                IUndoableOperation executorOp = executor.getOperation();
                executorOp.addContext( context );
                
                history.execute( executorOp, monitor, executor.getInfo() );
            }
        };
        for (IJobChangeListener l : listeners) {
            job.addJobChangeListenerWithContext( l );
        }

        // check nested
        UIJob parent = UIJob.forThread();
        
        if (parent != null && parent instanceof OperationJob) {
            log.info( "Nested operation: " + op );
            run( job, true, progress );

//            IUndoableOperation parentOp = ((OperationJob)parent).op;
//            
//            SubProgressMonitor subMonitor = new SubProgressMonitor( 
//                    UIJob.monitorForThread(), IProgressMonitor.UNKNOWN, parentOp.getLabel() );
//            
//            OperationExecutor executor = OperationExecutor.newInstance( op );
//            IUndoableOperation executorOp = executor.getOperation();
//            executorOp.addContext( context );
//            
//            history.execute( executorOp, subMonitor, executor.getInfo() );
        }
        
        // start job
        else {
            run( job, async, progress );
        }
    }


    protected void run( OperationJob job, boolean async, boolean progress ) {
        if (progress) {
            job.setShowProgressDialog( null, true );
        }
        
        job.schedule();
        
        if (!async) {
            log.info( "Waiting for operation job to finish..." );
            job.joinAndDispatch( 3 * 60 * 1000 );
        }
    }

    
    /**
     * 
     */
    abstract class OperationJob
            extends UIJob {
    
        protected IUndoableOperation        op;

        protected IProgressMonitor          monitor;
        
        
        public OperationJob( IUndoableOperation op ) {
            super( op.getLabel() );
            this.op = op;
            setPriority( LONG );
            setRule( new MultipleOperations() );
        }

        @SuppressWarnings("hiding")
        protected void runWithException( IProgressMonitor monitor )
        throws Exception {
            this.monitor = monitor;
            run();
        }

        protected abstract void run()
        throws Exception;
        
    }

    
//    // save / revert **************************************
//    
//    public void addOperationSaveListener( IOperationSaveListener listener ) {
//        List<IOperationSaveListener> newList = new LinkedList( saveListeners );
//        newList.add( listener );
//        saveListeners = newList;
//    }
//    
//    public void prependOperationSaveListener( IOperationSaveListener listener ) {
//        List<IOperationSaveListener> newList = new LinkedList( saveListeners );
//        newList.add( 0, listener );
//        saveListeners = newList;
//    }
//    
//    public void removeOperationSaveListener( IOperationSaveListener listener ) {
//        List<IOperationSaveListener> newList = new LinkedList( saveListeners );
//        newList.remove( listener );
//        saveListeners = newList;        
//    }
//
//    /**
//     * Notifies all {@link IOperationSaveListener}s to persistently save
//     * changes. If successful the history of operations is disposed.
//     * 
//     * @throws Exception If the prepare save operation of any of the listeners
//     *         failed.
//     */
//    public void saveChanges()
//    throws Exception {
//        UIJob job = new UIJob( Messages.get( "OperationSupport_saveChanges" ) ) {
//            protected void runWithException( IProgressMonitor monitor ) throws Exception {
//            
//                monitor.beginTask( getName(), saveListeners.size() * 11 );
//                try {
//                    // prepare
//                    for (IOperationSaveListener listener : saveListeners) {
//                        SubProgressMonitor subMon = new SubProgressMonitor( monitor, 10, "Preparing" );
//                        listener.prepareSave( OperationSupport.this, subMon );
//                        if (monitor.isCanceled()) {
//                            throw new OperationCanceledException( "Operation wurde abgebrochen." );
//                        }
//                        subMon.done();
//                    }
//                    // commit
//                    for (IOperationSaveListener listener : saveListeners) {
//                        SubProgressMonitor subMon = new SubProgressMonitor( monitor, 1, "Committing" );
//                        listener.save( OperationSupport.this, subMon );
//                        subMon.done();
//                    }
//                    history.dispose( context, true, true, false );
//                }
//                catch (final Throwable e) {
//                    // rollback
//                    for (IOperationSaveListener listener : saveListeners) {
//                        SubProgressMonitor subMon = new SubProgressMonitor( monitor, 1, "Rolling back" );
//                        listener.rollback( OperationSupport.this, subMon );
//                        subMon.done();
//                    }
//                    Polymap.getSessionDisplay().asyncExec( new Runnable() {
//                        public void run() {
//                            StatusDispatcher.handleError( CorePlugin.PLUGIN_ID, OperationSupport.this, e.getLocalizedMessage(), e );
//                        }
//                    });
//                }
//            }
//        };
//        // don't block the UI thread but use rules to prevent other
//        // operations to change things during save
//        job.setRule( new OneSaver() );
//        job.setShowProgressDialog( null, true );
//        job.schedule();
//    }
//
//
//    /**
//     * Notifies all {@link IOperationSaveListener}s to revert changes.
//     * Afterwards the history of operations is disposed.
//     */
//    public void revertChanges() {
//        UIJob job = new UIJob( Messages.get( "OperationSupport_saveChanges" ) ) {            
//            protected void runWithException( IProgressMonitor monitor ) throws Exception {
//                
//                List<Exception> exceptions = new ArrayList();            
//                monitor.beginTask( getName(), saveListeners.size() * 10 );
//
//                for (IOperationSaveListener listener : saveListeners) {
//                    try {
//                        SubProgressMonitor subMon = new SubProgressMonitor( monitor, 10, "Revert" );
//                        listener.revert( OperationSupport.this, subMon );
//                        subMon.done();
//                    }
//                    catch (final Exception e) {
//                        exceptions.add( e );
//                    }
//                }        
//                history.dispose( context, true, true, true );
//                
//                if (!exceptions.isEmpty()) {
//                    throw exceptions.get( 0 );
//                }
//            }
//        };
//        job.setRule( new OneSaver() );
//        job.setShowProgressDialog( null, true );
//        job.schedule();
//    }

    
    /*
     * 
     */
    class SubProgressMonitor
            extends org.eclipse.core.runtime.SubProgressMonitor {

        private String          taskPrefix;
        
        public SubProgressMonitor( IProgressMonitor monitor, int ticks, String taskPrefix ) {
            super( monitor, ticks, PREPEND_MAIN_LABEL_TO_SUBTASK );
            this.taskPrefix = taskPrefix;
        }

        public void beginTask( String name, int totalWork ) {
            super.beginTask( taskPrefix + " : " + name, totalWork );
        }

    }
    
}
