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
package org.polymap.core.runtime;

import static org.polymap.core.runtime.UIThreadExecutor.runtimeException;

import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import org.eclipse.swt.widgets.Display;

import org.eclipse.jface.operation.ModalContext;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;

import org.polymap.core.CorePlugin;
import org.polymap.core.Messages;
import org.polymap.core.runtime.i18n.IMessages;
import org.polymap.core.runtime.session.SessionContext;
import org.polymap.core.ui.StatusDispatcher;
import org.polymap.core.ui.UIUtils;

/**
 * Extended Job implementation that provides:
 * <ul>
 * <li>implicite handling of {@link SessionContext}</li>
 * <li>support for progress dialog</li>
 * <li>wrapping monitor into {@link OffThreadProgressMonitor}</li>
 * <li>simplified exception handling</li>
 * <li>access to the {@link #forThread() job of the current thread} and its monitor and status</li>
 * <li>{@link #joinAndDispatch(int)}</li>
 * <li>{@link #cancelAndInterrupt()}</li>
 * </ul>
 * <p/>
 * Ideas and code inspired by {@link PlatformGIS}.
 *
 * @see ModalContext
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class UIJob
        extends Job {

    private static Log log = LogFactory.getLog( UIJob.class );
    
    /** The default priority for newly created jobs. */
    public static final int                 DEFAULT_PRIORITY = Job.SHORT;
    
    private static final ThreadLocal<UIJob> threadJob = new ThreadLocal();
    
    private static final NullProgressMonitor nullMonitor = new NullProgressMonitor();
    
    private static final IMessages          i18n = Messages.forClass( UIJob.class );
    
    
    public static UIJob forThread() {
        return threadJob.get();
    }

    
    public static void joinJobs( Iterable<UIJob> jobs ) throws InterruptedException {
        for (UIJob job : jobs) {
            job.join();
        }
    }


    /**
     * Returns the progress monitor of the job of the current thread a
     * {@link NullProgressMonitor} if the current thread is not a job.
     */
    public static IProgressMonitor monitorForThread() {
        UIJob job = forThread();
        return job != null ? job.executionMonitor : nullMonitor;
    }
    
    
    // instance *******************************************
    
    private IStatus             resultStatus;
    
    private Display             display;

    private boolean             showProgress;
    
    private IProgressMonitor    executionMonitor;

    private SessionContext      sessionContext;

    private String              uiCallbackHandle;


    /**
     * Construct a new instance with the given name and default priority {@link Job#LONG}
     * and {@link #isShowProgressDialog()} set to false.
     * 
     * @param name The name of the job. Must not be null.
     */
    public UIJob( String name ) {
        this( name, false );
    }


    /**
     * Construct a new instance with the given name and default priority {@link Job#LONG}
     * and {@link #isShowProgressDialog()} set to false.
     * 
     * @param name The name of the job. Must not be null.
     * @param system {@link #setSystem(boolean)}
     */
    public UIJob( String name, boolean system ) {
        super( name );
        this.sessionContext = SessionContext.current();
        this.display = UIUtils.sessionDisplay();

        setSystem( system );
        setPriority( DEFAULT_PRIORITY );
    }


    /**
     * Makes sure that
     * <p/>
     * XXX Not quite sure if setSystem(false) and/or setUser(true) will so the same
     * with a special {@link IProgressMonitor}. However, this is usefull for system
     * threads then.
     *
     * @param delay
     */
    public void scheduleWithUIUpdate( long delay ) {
        uiCallbackHandle = UIJob.this.toString() + "/" + UIJob.this.hashCode();
        UIThreadExecutor.syncFast( () -> UIUtils.activateCallback( uiCallbackHandle ), runtimeException() );
        schedule( delay );
    }


    /**
     * @see #scheduleWithUIUpdate(long)
     */
    public void scheduleWithUIUpdate() {
        scheduleWithUIUpdate( 0L );
    }
    
    
    public Display getDisplay() {
        return display;
    }


    /**
     * This cancels the job and, if this does not succeeded, it interrupts the
     * thread of the job. This gives NIO request the chance to cancel.
     */
    public boolean cancelAndInterrupt() {
        boolean result = cancel();
        if (!result) {
            Thread jobThread = UIJob.this.getThread();
            if (jobThread != null && jobThread.isAlive()) {
                jobThread.interrupt();
            }
        }
        return result;
    }
    
    
    protected abstract void runWithException( IProgressMonitor monitor ) throws Exception;
    

    protected final IStatus run( final IProgressMonitor monitor ) {
        sessionContext.execute( () -> {
            try {
                executionMonitor = monitor;
                threadJob.set( UIJob.this );
                runWithException( executionMonitor );
                resultStatus = executionMonitor.isCanceled() ? Status.CANCEL_STATUS : Status.OK_STATUS;
            }
            // ThreadDeath is a normal error when the thread is dying.
            // We must propagate it in order for it to properly terminate.
            catch (ThreadDeath e) {
                throw e;
            }
            catch (final Throwable e) {
                log.warn( "UIJob exception: ", e );

                if (display != null) {
                    // XXX use UIThreadExecutor and/or error handlers
                    display.syncExec( () ->
                            StatusDispatcher.handleError( CorePlugin.PLUGIN_ID, UIJob.this, i18n.get( "errorMsg", getName() ), e ) );
                }

                // users don't read any further if they see an 'error' sign, so make a warning
                resultStatus = new Status( IStatus.WARNING, CorePlugin.PLUGIN_ID,
                        e.getLocalizedMessage() /*Messages.get( "UIJob_errormsg" )*/, e );
            }
            finally {
                threadJob.set( null );
                executionMonitor = null;
                if (uiCallbackHandle != null) {
                    UIThreadExecutor.async( () -> UIUtils.deactivateCallback( uiCallbackHandle ), runtimeException() );                    
                }
            }
        });
        return resultStatus;
    }


    /**
     * Signals that a progress dialog should be shown when the job starts execution.
     * 
     * @param dialogTitle The title of the dialog or null if the default is to be
     *        used.
     * @param showRunInBackground Signals that the dialog should have a
     *        "showInBackground" button.
     */
    public UIJob setShowProgressDialog( final String dialogTitle, final boolean showRunInBackground ) {
        this.showProgress = true;

        // enable UI only if we have a display
        if (display != null) {
//            if (progressDialog == null) {
//                display.syncExec( new Runnable() {
//                    public void run() {
//                        progressDialog = new ProgressDialog( dialogTitle != null ? dialogTitle : getName(), 
//                                showRunInBackground );
//                    }
//                });
//            }
            setUser( true );
        }
        return this;
    }

    
    public boolean isShowProgressDialog() {
        return showProgress;
    }


    /**
     * Waits until this job is finished. This method will block the calling thread
     * until the job has finished executing, or until this thread has been
     * interrupted, or the timeout exceeds. A job must not be joined from within the
     * scope of its run method.
     * <p/>
     * This method must be called from the UIThread. It calls readAndDispatch() of
     * the current display, so that the UI is responsive during the call is blocked.
     * 
     * @param timeoutMillis
     * @return False if the job did not finish within the given timeout or the
     *         calling thread was interrupted.
     */
    public boolean joinAndDispatch( long timeoutMillis ) {

        final AtomicBoolean done = new AtomicBoolean( 
                getState() == Job.NONE );

        IJobChangeListener jobListener = new JobChangeAdapter() {
            public void done( IJobChangeEvent event ) {
                synchronized (done) {
                    done.set( true );
                    done.notify();
                }
            }
        };
        addJobChangeListener( jobListener );

        // check if job is done already - after the listener has been
        // registered; this avoids race cond. between the two
        if (getState() == Job.NONE) {
            removeJobChangeListener( jobListener );
            return true;
        }

        final Display threadDisplay = Display.getCurrent();
        final Timer timer = new Timer();
        while (!done.get() 
                && timer.elapsedTime() < timeoutMillis
                && (threadDisplay == null || !threadDisplay.isDisposed()) ) {

            Thread.yield();
            if (threadDisplay != null) {
                if (!threadDisplay.readAndDispatch()) {
                    synchronized (done) {
                        try { 
                            done.wait( 300 ); 
                            log.debug( "wake after: " + timer.elapsedTime() + "ms" );
                        } 
                        catch (InterruptedException e) {}
                    }
//                    // just wait on #done blocks hangs;
//                    // display.sleep() might wait forever, so we need this watchdog
//                    Polymap.executorService().execute( new Runnable() {
//                        public void run() {
//                            synchronized (done) {
//                                try { done.wait( 300 ); } catch (InterruptedException e) {}
//                            }
//                            log.info( "wake ..." );
//                            threadDisplay.wake();
//                        }
//                    });
//                    threadDisplay.sleep();
                }
            }
            else {
                synchronized (done) {
                    try { done.wait( 250 );
                    } catch (InterruptedException e) {}
                }
            }
        }
        removeJobChangeListener( jobListener );
        return done.get();
    }

    
    public final void addJobChangeListenerWithContext( final IJobChangeListener listener ) {
        super.addJobChangeListener( new IJobChangeListener() {
            @Override
            public void sleeping( final IJobChangeEvent event ) {
                sessionContext.execute( () -> listener.sleeping( event ) );
            }
            @Override
            public void scheduled( final IJobChangeEvent event ) {
                sessionContext.execute( () -> listener.scheduled( event ) );
            }
            @Override
            public void running( final IJobChangeEvent event ) {
                sessionContext.execute( () -> listener.running( event ) );
            }
            @Override
            public void done( final IJobChangeEvent event ) {
                sessionContext.execute( () -> listener.done( event ) );
            }
            @Override
            public void awake( final IJobChangeEvent event ) {
                sessionContext.execute( () -> listener.awake( event ) );
            }
            @Override
            public void aboutToRun( final IJobChangeEvent event ) {
                sessionContext.execute( () -> listener.aboutToRun( event ) );
            }
        });
    }


//    /**
//     * 
//     * @deprecated Seems that this was written by udig developers just for fun, since
//     *             setUser() provides this already. I found out to late and ported
//     *             the code... well... just for fun :(
//     */
//    class ProgressDialog
//            extends ProgressMonitorDialog {
//        
//        private String              title;
//        
//        private boolean             showRunInBackground;
//        
//        private JobChangeAdapter    jobChangeAdapter;
//
//        
//        protected ProgressDialog( String title, boolean showRunInBackground ) {
//            super( display.getActiveShell() );
//            this.title = title != null
//                    ? title : Messages.get( "UIJob_ProgressDialog_title" );
//            this.showRunInBackground = showRunInBackground;
//            setCancelable( true );
////            setBlockOnOpen( true );
//            
//            // job listener
//            this.jobChangeAdapter = new JobChangeAdapter() {
//                
//                public void aboutToRun( IJobChangeEvent ev ) {
//                    display.asyncExec( new Runnable() {
//                        public void run() {
//                            log.debug( "ProgressDialog.open(): ..." );
//                            ProgressDialog.this.aboutToRun();
//                        }
//                    });
//                }
//
//                public void done( IJobChangeEvent ev ) {
//                    display.asyncExec( new Runnable() {
//                        public void run() {
//                            log.debug( "ProgressDialog.close(): ..." );
//                            ProgressDialog.this.finishedRun();
//                            removeJobChangeListener( jobChangeAdapter );
//                        }
//                    });
//                }
//            };
//            addJobChangeListener( jobChangeAdapter );
//        }
//
//
//        public IProgressMonitor getProgressMonitor() {
//            log.warn( "No OffThreadProgressMonitor after migrating to RAP2.3!" );
//            return /*new OffThreadProgressMonitor(*/ super.getProgressMonitor()/*, display )*/;
//        }
//
//
//        protected void configureShell( Shell shell ) {
//            super.configureShell(shell);
//            shell.setText( title );
//        }
//
//        
//        protected void createButtonsForButtonBar( Composite parent ) {
//            if (showRunInBackground) {
//                createButton(parent, IDialogConstants.BACK_ID,
//                        Messages.get( "UIJob_ProgressDialog_runInBackground" ), true);
//            }
//            super.createButtonsForButtonBar( parent );
//        }
//
//        
//        protected void buttonPressed( int buttonId ) {
//            if (buttonId == IDialogConstants.BACK_ID) {
//                getShell().setVisible( false );
//            } 
//            else {
//                super.buttonPressed( buttonId );
//            }
//        }
//
//
//        protected void cancelPressed() {
//            UIJob.this.cancelAndInterrupt();
//            super.cancelPressed();
//        }
//        
//    }

}
