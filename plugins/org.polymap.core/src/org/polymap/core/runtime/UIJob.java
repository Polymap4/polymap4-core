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

import java.util.concurrent.atomic.AtomicBoolean;

import net.refractions.udig.ui.OffThreadProgressMonitor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.rwt.lifecycle.UICallBack;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;

import org.eclipse.ui.PlatformUI;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;

import org.polymap.core.CorePlugin;
import org.polymap.core.Messages;

/**
 * Extended Job implementation that provides:
 * <ul>
 * <li>implicite handling of RWT session context</li>
 * <li>support for progress dialog</li>
 * <li>simplified exception handling</li>
 * <li>access to the {@link #forThread() job of the current thread} and its monitor and status</li>
 * <li>{@link #joinAndDispatch(int)}</li>
 * <li>{@link #cancelAndInterrupt()}</li>
 * </ul>
 * <p/>
 * Ideas and code inspired by {@link PlatformGIS}.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class UIJob
        extends Job {

    private static Log log = LogFactory.getLog( UIJob.class );
    
    private static final ThreadLocal<UIJob> threadJob = new ThreadLocal();
    
    
    public static UIJob forThread() {
        return threadJob.get();
    }

    
    // instance *******************************************
    
    private IStatus         resultStatus;
    
    private Display         display;

    private ProgressDialog  progressDialog;
    
    
    public UIJob( String name ) {
        super( name );
        this.display = Polymap.getSessionDisplay();
        assert display != null : "Unable to determine current session/display.";
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
    
    
    protected abstract void runWithException( IProgressMonitor monitor )
    throws Exception;
    

    protected final IStatus run( final IProgressMonitor monitor ) {
        // give the runnable to correct session context
        UICallBack.runNonUIThreadWithFakeContext( display, new Runnable() {
            public void run() {
                if (!PlatformUI.getWorkbench().isClosing()) {
                    try {
                        threadJob.set( UIJob.this );

                        IProgressMonitor mon = progressDialog != null
                                ? progressDialog.getProgressMonitor() : monitor;
                        
                        runWithException( mon );
                        
                        resultStatus = Status.OK_STATUS;
                    }
                    // ThreadDeath is a normal error when the thread is dying.
                    // We must propagate it in order for it to properly terminate.
                    catch (ThreadDeath e) {
                        throw e;
                    }
                    catch (Throwable e) {
                        log.warn( "Job exception: ", e );
                        resultStatus = new Status( IStatus.ERROR, CorePlugin.PLUGIN_ID,
                                Messages.get( "UIJob_errormsg" ), e );
                    }
                    finally {
                        threadJob.set( null );
                    }
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
    public UIJob setShowProgressDialog( String dialogTitle, boolean showRunInBackground ) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog( dialogTitle, showRunInBackground );
        }
        return this;
    }

    
    public boolean isShowProgressDialog() {
        return progressDialog != null;
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
        Display threadDisplay = Display.getCurrent();
        assert threadDisplay != null : "joinWithDispatch() must be called from UIThread.";
        assert threadDisplay == display;

        final AtomicBoolean done = new AtomicBoolean( false );

        addJobChangeListener( new JobChangeAdapter() {
            public void done( IJobChangeEvent event ) {
                synchronized (done) {
                    done.set( true );
                    done.notify();
                }
            }
        });

        final Timer timer = new Timer();
        while (!done.get() 
                && timer.elapsedTime() < timeoutMillis
                && !threadDisplay.isDisposed() ) {

            Thread.yield();
            if (!threadDisplay.readAndDispatch()) {
                // just waiting on done causes the UIThread to hang, so use
                // timerExec and sleep
                threadDisplay.timerExec( 250, new Runnable() {
                    public void run() {
                        // just wakeup thread                                
                    }
                });
                display.sleep();
            }
        }
        return done.get();
    }
    
    
    /*
     * 
     */
    class ProgressDialog
            extends ProgressMonitorDialog {
        
        private String              title;
        
        private boolean             showRunInBackground;
        
        private JobChangeAdapter    jobChangeAdapter;

        
        protected ProgressDialog( String title, boolean showRunInBackground ) {
            super( display.getActiveShell() );
            this.title = title != null
                    ? title : Messages.get( "UIJob_ProgressDialog_title" );
            this.showRunInBackground = showRunInBackground;
            setCancelable( true );
//            setBlockOnOpen( true );
            
            // job listener
            this.jobChangeAdapter = new JobChangeAdapter() {
                
                public void aboutToRun( IJobChangeEvent ev ) {
                    display.asyncExec( new Runnable() {
                        public void run() {
                            log.debug( "ProgressDialog.open(): ..." );
                            ProgressDialog.this.aboutToRun();
                        }
                    });
                }

                public void done( IJobChangeEvent ev ) {
                    display.asyncExec( new Runnable() {
                        public void run() {
                            log.debug( "ProgressDialog.close(): ..." );
                            ProgressDialog.this.finishedRun();
                            removeJobChangeListener( jobChangeAdapter );
                        }
                    });
                }
            };
            addJobChangeListener( jobChangeAdapter );
        }


        public IProgressMonitor getProgressMonitor() {
            return new OffThreadProgressMonitor( super.getProgressMonitor(), display );
        }


        protected void configureShell( Shell shell ) {
            super.configureShell(shell);
            shell.setText( title );
        }

        
        protected void createButtonsForButtonBar( Composite parent ) {
            if (showRunInBackground) {
                createButton(parent, IDialogConstants.BACK_ID,
                        Messages.get( "UIJob_ProgressDialog_runInBackground" ), true);
            }
            super.createButtonsForButtonBar( parent );
        }

        
        protected void buttonPressed( int buttonId ) {
            if (buttonId == IDialogConstants.BACK_ID) {
                getShell().setVisible( false );
            } 
            else {
                super.buttonPressed( buttonId );
            }
        }


        protected void cancelPressed() {
            UIJob.this.cancelAndInterrupt();
            super.cancelPressed();
        }
        
    }

}
