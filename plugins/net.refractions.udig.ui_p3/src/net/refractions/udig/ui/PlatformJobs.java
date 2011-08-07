/*
 *    uDig - User Friendly Desktop Internet GIS client
 *    http://udig.refractions.net
 *    (C) 2004, Refractions Research Inc.
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 */
package net.refractions.udig.ui;

import java.util.concurrent.atomic.AtomicBoolean;

import java.lang.reflect.InvocationTargetException;

import net.refractions.udig.internal.ui.UiPlugin;
import net.refractions.udig.ui.internal.Messages;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.rwt.SessionSingletonBase;
import org.eclipse.rwt.lifecycle.UICallBack;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;

/**
 * Re-implementation of the job/thread related methods provided by
 * {@link PlatformGIS}. 
 *
 * @deprecated See {@link org.polymap.core.runtime.UIJob} instead.
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 *         <li>26.10.2009: created</li>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class PlatformJobs
        extends SessionSingletonBase {

    /**
     * Returns the instance that is associated to the current session/thread.
     * This is different from UDig since we have not just one singleton
     * application in the VM but multiple sessions.
     */
    public static final PlatformJobs getInstance() {
        return (PlatformJobs)getInstance( PlatformJobs.class );
    }

    
    // instance *******************************************
    
    protected PlatformJobs() {
    }


    /**
     * This method runs the runnable in a separate thread. It is useful in cases
     * where a thread must wait for a long running and potentially blocking
     * operation (for example an IO operation). If the IO is done in the UI
     * thread then the user interface will lock up. This allows synchronous
     * execution of a long running thread in the UI thread without locking the
     * UI.
     * 
     * @param runnable The runnable(operation) to run
     * @param monitor the progress monitor to update or null if the jobs monitor
     *        should be used.
     * @throws InvocationTargetException
     * @throws InterruptedException
     */
    public static void runSync( final IRunnableWithProgress request,
            IProgressMonitor _monitor ) 
            throws InvocationTargetException, InterruptedException {

        // FIXME this hides the job monitor
//        final IProgressMonitor monitor = (_monitor != null)
//                ? _monitor : new NullProgressMonitor();
        System.out.println( "FIXME _p3: ignoring given progress monitor! **********" );
        final IProgressMonitor monitor = new NullProgressMonitor();

        Display display = Display.getCurrent();
        final AtomicBoolean done = new AtomicBoolean( false );
        
        // job
        PlatformJobsRunner runner = new PlatformJobsRunner( "..." );
        runner.setRequest( request, monitor );
        runner.setSystem( false );
        runner.addJobChangeListener( new JobChangeAdapter() {
            public void done( IJobChangeEvent event ) {
                synchronized (done) {
                    done.set( true );
                    done.notify();
                }
            }
        });
        runner.schedule();
        
        // read and dispatch
        synchronized (done) {
            while (done.get() == false) {
                try {
                    System.out.println( "readAndDispatch()..." );
                    Thread.yield();
                    if (display == null) {
                        done.wait( 300 );
                    } 
                    else {
                        if (!display.readAndDispatch()) {
                            done.wait( 300 );
                        }
                    }
                }
                catch (InterruptedException e) {
                    // check conditions in next cycle
                }
                catch (Exception e) {
                    UiPlugin.log( "Error occurred net.refractions.udig.issues.internal while waiting for an operation to complete", e); //$NON-NLS-1$
                    e.printStackTrace();
                }
            }
        }
//        if (runner..isCanceled()) {
//            runner.cancel();
//            throw new InterruptedException( "" );
//        }
        
        // result exception
        IStatus result = runner.getResult();
        if (result != null && result.getException() != null) {
            throw new InvocationTargetException( result.getException() );
        }
    }

    
    /**
     * Runs the given runnable inside a {@link Job}, providing it a progress monitor. Exceptions
     * thrown by the runnable are logged, and not rethrown.
     */
    public static Job run( IRunnableWithProgress request ) {
        PlatformJobsRunner runner = new PlatformJobsRunner( "..." );
        runner.setRequest( request, null );
        runner.schedule();
        return runner;
    }
    
    /**
     * Runs the given runnable inside a {@link Job}, providing it a progress monitor. Exceptions
     * thrown by the runnable are logged, and not rethrown.
     */
    public static Job run( IRunnableWithProgress request, IProgressMonitor monitorToUse) {
        return run( request, monitorToUse, "..." );
    }


    /**
     * Runs the given runnable inside a {@link Job}, providing it a progress monitor. Exceptions
     * thrown by the runnable are logged, and not rethrown.
     */
    public static Job run( IRunnableWithProgress request, IProgressMonitor monitorToUse, String title) {
        PlatformJobsRunner runner = new PlatformJobsRunner( title );
        runner.setRequest( request, monitorToUse );
        runner.schedule();
        return runner;
    }


    /**
     * Runs the given runnable inside a {@link Job} in a protected mode.
     * Exceptions thrown in the runnable are logged and passed to the runnable's
     * exception handler. Such exceptions are not rethrown by this method.
     */
    public static Job run( ISafeRunnable request ) {
        PlatformJobsRunner runner = new PlatformJobsRunner( "..." );
        runner.setRequest( request, null );
        runner.schedule();
        return runner;
    }

    
    /**
     * Runs a blocking task in a ProgressDialog. It is ran in such a way that even if the task
     * blocks it can be cancelled. This is unlike the normal ProgressDialog.run(...) method which
     * requires that the {@link IProgressMonitor} be checked and the task to "nicely" cancel.
     * 
     * @param dialogTitle The title of the Progress dialog
     * @param showRunInBackground if true a button added to the dialog that will make the job be ran
     *        in the background.
     * @param runnable the task to execute.
     * @param runASync if true the runnable will be ran asynchronously
     */
    public static void runInProgressDialog( final String dialogTitle,
            final boolean showRunInBackground, final IRunnableWithProgress runnable,
            boolean runASync ) {

        Runnable object = new Runnable(){
            public void run() {
                Shell shell = Display.getDefault().getActiveShell();
                ProgressMonitorDialog dialog = new ProgressMonitorDialog(shell){
                    @Override
                    protected void configureShell( Shell shell ) {
                        super.configureShell(shell);
                        shell.setText(dialogTitle);
                    }

                    @Override
                    protected void createButtonsForButtonBar( Composite parent ) {
                        if (showRunInBackground)
                            createBackgroundButton(parent);
                        super.createButtonsForButtonBar(parent);
                    }

                    private void createBackgroundButton( Composite parent ) {
                        createButton(parent, IDialogConstants.BACK_ID,
                                Messages.PlatformGIS_background, true);
                    }

                    @Override
                    protected void buttonPressed( int buttonId ) {
                        if (buttonId == IDialogConstants.BACK_ID) {
                            getShell().setVisible(false);
                        } else
                            super.buttonPressed(buttonId);
                    }
                };
                try {
                    final Display display = Display.getCurrent();
                    dialog.run( true, true, new IRunnableWithProgress(){
                        public void run( final IProgressMonitor monitor ) {
                            try {
                                UICallBack.runNonUIThreadWithFakeContext( display, new Runnable() {
                                    public void run() {
                                        try {
                                            // thread already forked by the dialog
                                            runnable.run( monitor );
                                        }
                                        catch (InvocationTargetException e) {
                                            throw new RuntimeException( "", e.getTargetException() );
                                        }
                                        catch (InterruptedException e) {
                                            throw new RuntimeException( e.getMessage() );
                                        }
                                    }
                                });
                                
//                                runSync(new IRunnableWithProgress(){
//
//                                    public void run( IProgressMonitor monitor )
//                                            throws InvocationTargetException, InterruptedException {
//                                        runnable.run(monitor);
//                                    }
//                                }, monitor);
                            } catch (Exception e) {
                                UiPlugin.log("", e); //$NON-NLS-1$
                            }

                        }
                    });
                } catch (Exception e) {
                    UiPlugin.log("", e); //$NON-NLS-1$
                }
            }
        };

        Display.getCurrent().syncExec( object );
//        if (runASync)
//            Display.getDefault().asyncExec(object);
//        else
//            PlatformGIS.syncInDisplayThread(object);
    }

}
