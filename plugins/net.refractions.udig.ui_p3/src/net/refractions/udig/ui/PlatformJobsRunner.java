package net.refractions.udig.ui;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.mysql.jdbc.Messages;

import net.refractions.udig.core.internal.CorePlugin;
import net.refractions.udig.internal.ui.UiPlugin;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.rwt.internal.lifecycle.RWTLifeCycle;
import org.eclipse.rwt.lifecycle.UICallBack;

import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

/**
 * The runner job. 
 *
 * @version $Revision: $
 */
class PlatformJobsRunner 
        extends Job {

    private static final Log log = LogFactory.getLog( PlatformJobsRunner.class );

    private Object              runnable;
    
    private IProgressMonitor    monitor;
    
    private Display             display = RWTLifeCycle.getSessionDisplay();
    

    public PlatformJobsRunner( String title ) {
        super( title ); //$NON-NLS-1$
        assert display != null : "No display. Job started outside UI thread.";
    }

    
    /**
     * Add a runnable object to be run.
     * 
     * @param runnable {@link ISafeRunnable} or {@link IRunnableWithProgress}.
     * @param monitor The monitor, or null if no monitor is to be given
     *      to the {@link #runnable}.
     */
    public void setRequest( Object runnable, IProgressMonitor monitor ) {
        this.runnable = runnable;
        this.monitor = monitor;
    }

    
    @Override
    protected IStatus run( final IProgressMonitor jobMonitor ) {
        // _p3: give the runnable to correct context
        UICallBack.runNonUIThreadWithFakeContext( display, new Runnable() {
            public void run() {
                if (!PlatformUI.getWorkbench().isClosing()) {
                    if (runnable != null) {
                        if (runnable instanceof ISafeRunnable) {
                            PlatformJobsRunner.this.run( (ISafeRunnable)runnable );
                        }
                        else if (runnable instanceof IRunnableWithProgress) {
                            IRunnableWithProgress r = (IRunnableWithProgress)runnable;
                            IProgressMonitor m = monitor != null ? monitor : jobMonitor;
                            PlatformJobsRunner.this.run( r, m );
                        }
                    }
                }
            }
        } );
        return Status.OK_STATUS;
    }
    
    
    private void run( final ISafeRunnable runnable ) {
        try {
            runnable.run();
        }
        catch (Throwable e) {
            if (e.getMessage() != null) {
                UiPlugin.log( e.getMessage(), e );
            }
            else {
                UiPlugin.log( "", e ); //$NON-NLS-1$
            }
            runnable.handleException( e );
        }
    }

    
    private void run( final IRunnableWithProgress runnable, final IProgressMonitor monitor ) {
        try {
            runnable.run( monitor );
        }
        catch (Throwable e) {
            // the runnable should have catched every special exception;
            // if not, then this is the last chance to get the user informed
            handleError( e.getLocalizedMessage(), e );
            UiPlugin.log( "", e ); //$NON-NLS-1$
        }
    }
    
    
    /**
     * Handle the given error by opening an error dialog and logging the given
     * message to the CorePlugin log.
     * 
     * @param src
     * @param msg The error message. If null, then a standard message is used.
     * @param e The reason of the error, must not be null.
     */
    private void handleError( final String msg, Throwable e) {
        e = e instanceof InvocationTargetException
                ? ((InvocationTargetException)e).getTargetException()
                : e;
                
        log.error( msg, e );

        final Status status = new Status( IStatus.ERROR, "net.refractions.udig.ui", e.getLocalizedMessage(), e );
        CorePlugin.getDefault().getLog().log( status );

        if (display == null) {
            log.error( "No display -> no error message." );
            return;
        }
        
        display.asyncExec( new Runnable() {
            public void run() {
                Shell shell = getShellToParentOn();
                ErrorDialog dialog = new ErrorDialog(
                        shell,
                        "Problem",
                        msg != null ? msg : "Fehler beim Ausführen der Operation.",
                        status,
                        IStatus.OK | IStatus.INFO | IStatus.WARNING | IStatus.ERROR );
//                dialog.setBlockOnOpen( true );
                dialog.open();
            }
        });
    }

    
    /**
     * Return an appropriate shell to parent dialogs on. This will be one of the
     * workbench windows (the active one) should any exist. Otherwise
     * <code>null</code> is returned.
     * 
     * @return the shell to parent on or <code>null</code> if there is no
     *         appropriate shell
     * @since 3.3
     */
    public static Shell getShellToParentOn() {
        IWorkbench workbench = PlatformUI.getWorkbench();
        IWorkbenchWindow activeWindow = workbench.getActiveWorkbenchWindow();
        IWorkbenchWindow windowToParentOn = activeWindow == null ? (workbench
                .getWorkbenchWindowCount() > 0 ? workbench
                .getWorkbenchWindows()[0] : null) : activeWindow;
        return windowToParentOn == null ? null : activeWindow.getShell();
    }

    

}
